package github.peihanw.orauld;

import static github.peihanw.ut.Stdout.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import github.peihanw.ut.AppTicker;

public class OrauldMain {

	private static BlockingQueue<OrauldTuple>[] _UpQueues;
	private static BlockingQueue<OrauldTuple>[] _DnQueues;
	private static OrauldWrkRunnable[] _WrkRunnables;
	private static Thread[] _WrkThreads;
	private static OrauldDmpRunnable _DmpRunnable;
	private static Thread _DmpThread;

	public static void main(String[] args) throws Exception {
		try {
			_main(args);
		} catch (Exception e) {
			P(ERO, e, "encounter exception");
			System.exit(3);
		}
	}

	@SuppressWarnings("unchecked")
	private static void _main(String[] args) throws Exception {
		OrauldCmdline cmdline_ = OrauldCmdline.GetInstance();
		cmdline_.init(args);
		AppTicker ticker_ = new AppTicker();
		P(INF, "cmdline parsed and started");
		cmdline_.print();
		_UpQueues = (BlockingQueue<OrauldTuple>[]) new BlockingQueue<?>[cmdline_._wrkNum];
		_DnQueues = (BlockingQueue<OrauldTuple>[]) new BlockingQueue<?>[cmdline_._wrkNum];
		_WrkRunnables = new OrauldWrkRunnable[cmdline_._wrkNum];
		_WrkThreads = new Thread[cmdline_._wrkNum];
		for (int i = 0; i < cmdline_._wrkNum; ++i) {
			_UpQueues[i] = new LinkedBlockingQueue<OrauldTuple>(2000);
			_DnQueues[i] = new LinkedBlockingQueue<OrauldTuple>(2000);
		}

		_DmpRunnable = new OrauldDmpRunnable(_UpQueues, _DnQueues);
		_DmpThread = new Thread(_DmpRunnable, "DUMP");
		_DmpThread.start();

		for (int i = 0; i < cmdline_._wrkNum; ++i) {
			_WrkRunnables[i] = new OrauldWrkRunnable(_UpQueues[i], _DnQueues[i]);
			_WrkThreads[i] = new Thread(_WrkRunnables[i], "WRK" + i);
			_WrkThreads[i].start();
		}

		OrauldMgr mgr_ = new OrauldMgr(_UpQueues);
		int rc_ = mgr_.run();
		P(INF, "total %,d records fetched", mgr_._sqlCnt);

		for (int i = 0; i < cmdline_._wrkNum; ++i) {
			P(DBG, "try join thread %s", _WrkThreads[i].getName());
			_WrkThreads[i].join();
			P(DBG, "thread %s joined", _WrkThreads[i].getName());
		}

		P(DBG, "try join thread %s", _DmpThread.getName());
		_DmpThread.join();
		P(DBG, "thread %s joined", _DmpThread.getName());

		if (mgr_._sqlCnt == _DmpRunnable._dmpCnt) {
			P(INF, "balance ok, fetched eq dumped, %d %d", mgr_._sqlCnt, _DmpRunnable._dmpCnt);
		} else {
			P(ERO, "balance FAILED, fetched ne dumped, %d %d", mgr_._sqlCnt, _DmpRunnable._dmpCnt);
			if (rc_ == 0) {
				rc_ = 2;
			}
		}

		mgr_.closeResource();
		ticker_.tickEnd(mgr_._sqlCnt);
		P(INF, "%s cnt term pfm %d %.3f %d", cmdline_._outputFnm, ticker_._recNum, ticker_._term, ticker_._pfm);
		System.exit(rc_);
	}
}
