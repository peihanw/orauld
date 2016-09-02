package github.peihanw.orauld;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import github.peihanw.ut.PubMethod;
import static github.peihanw.ut.Stdout.*;

public class OrauldDmpRunnable implements Runnable {

	public int _dmpCnt;
	private final BlockingQueue<OrauldTuple>[] _upQueues;
	private final BlockingQueue<OrauldTuple>[] _dnQueues;
	private final boolean[] _eofs;
	private PrintWriter _pw;
	private boolean _terminateFlag = false;

	public OrauldDmpRunnable(BlockingQueue<OrauldTuple>[] up_queues, BlockingQueue<OrauldTuple>[] dn_queues) {
		_upQueues = up_queues;
		_dnQueues = dn_queues;
		_eofs = new boolean[_dnQueues.length];
	}

	public void setTerminateFlag() {
		_terminateFlag = true;
		P(INF, "_terminateFlag is set");
	}

	@Override
	public void run() {
		P(INF, "thread started");
		boolean end_ = false;
		int eof_cnt_ = 0;
		try {
			while (!end_) {
				for (int i = 0; i < _dnQueues.length; ++i) {
					if (_eofs[i]) {
						++eof_cnt_;
					} else {
						OrauldTuple tuple_ = _dnQueues[i].poll(10, TimeUnit.MILLISECONDS);
						if (tuple_ == null) {
							if (_terminateFlag) {
								P(INF, "_terminateFlag detected");
								end_ = true;
								break;
							} else {
								continue;
							}
						}
						if (tuple_.isEOF()) {
							P(INF, "EOF tuple detected, i=%d", i);
							_eofs[i] = true;
						} else {
							_dump(tuple_);
						}
					}
				}
				if (eof_cnt_ >= _dnQueues.length) {
					P(INF, "%d of %d dn queues EOF", eof_cnt_, _dnQueues.length);
					break;
				}
				eof_cnt_ = 0;
			}
		} catch (Exception e) {
			P(WRN, e, "encounter exception, _dmpCnt=%d, re-throw as RuntimeException", _dmpCnt);
			throw new RuntimeException(e);
		} finally {
			PubMethod.Close(_pw);
		}
		P(INF, "thread ended, _dmpCnt=%d", _dmpCnt);
	}

	private void _dump(OrauldTuple tuple) throws Exception {
		if (_pw == null) {
			OrauldCmdline cmdline_ = OrauldCmdline.GetInstance();
			FileOutputStream fos_ = new FileOutputStream(cmdline_._bcpFnm);
			OutputStreamWriter osw_;
			osw_ = new OutputStreamWriter(fos_, cmdline_._charset);
			_pw = new PrintWriter(osw_);
			P(INF, "%s opened for writing, charset [%s]", cmdline_._bcpFnm, cmdline_._charset);
		}
		if (tuple._joined == null) {
			_pw.println("");
		} else {
			_pw.println(tuple._joined);
		}
		_dmpCnt++;
		if (_dmpCnt % 1000000 == 0) {
			List<Integer> up_sizes_ = new ArrayList<Integer>();
			List<Integer> dn_sizes_ = new ArrayList<Integer>();
			for (int i = 0; i < _upQueues.length; ++i) {
				up_sizes_.add(_upQueues[i].size());
				dn_sizes_.add(_dnQueues[i].size());
			}
			P(DBG, "%,d records dumped, up (%s), dn (%s)", _dmpCnt, PubMethod.Collection2Str(up_sizes_, ","),
				PubMethod.Collection2Str(up_sizes_, ","));
		}
	}
}
