package github.peihanw.orauld;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static github.peihanw.ut.Stdout.*;

public class OrauldWrkRunnable implements Runnable {

	public static int[] _ColumnTypes;
	public long _wrkCnt;
	private BlockingQueue<OrauldTuple> _upQueue;
	private BlockingQueue<OrauldTuple> _dnQueue;
	private int _idleCnt;
	private StringBuilder _sb;
	private OrauldCmdline _cmdline;
	private boolean _terminateFlag = false;

	public OrauldWrkRunnable(BlockingQueue<OrauldTuple> up_queue, BlockingQueue<OrauldTuple> dn_queue) {
		_upQueue = up_queue;
		_dnQueue = dn_queue;
		_sb = new StringBuilder();
		_cmdline = OrauldCmdline.GetInstance();
	}

	public void setTerminateFlag() {
		_terminateFlag = true;
		P(INF, "_terminateFlag is set");
	}

	@Override
	public void run() {
		P(INF, "thread started");
		try {
			while (true) {
				OrauldTuple tuple_ = _upQueue.poll(50, TimeUnit.MILLISECONDS);
				if (tuple_ == null) {
					++_idleCnt;
					if (_terminateFlag) {
						P(INF, "_terminateFlag detected");
						break;
					} else {
						if (_idleCnt > 99) {
							P(WRN, "_idleCnt=%d", _idleCnt);
							_idleCnt = 0;
						}
						continue;
					}
				}
				_idleCnt = 0;
				if (tuple_.isEOF()) {
					P(INF, "EOF tuple detected");
					_dnQueue.offer(tuple_, 86400, TimeUnit.SECONDS);
					break;
				}
				_concatenate(tuple_);
				_dnQueue.offer(tuple_, 86400, TimeUnit.SECONDS);
				++_wrkCnt;
			}
		} catch (Exception e) {
			P(WRN, e, "encounter exception, _wrkCnt=%d, re-throw as RuntimeException", _wrkCnt);
			throw new RuntimeException(e);
		}
		P(INF, "thread ended, _wrkCnt=%d", _wrkCnt);
	}

	private void _concatenate(OrauldTuple tuple) throws SQLException {
		_sb.delete(0, _sb.length());
		for (int i = 1; i < tuple._cells.length; ++i) {
			tuple.addCell(_sb, i, _ColumnTypes, _cmdline._delimiter, _cmdline._trimChar);
		}
		tuple._joined = _sb.substring(0);
	}
}
