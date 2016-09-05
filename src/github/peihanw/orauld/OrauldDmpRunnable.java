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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrauldDmpRunnable implements Runnable {

	public long _dmpCnt;
	public long _splitCnt;
	public int _splitSeq;
	private final BlockingQueue<OrauldTuple>[] _upQueues;
	private final BlockingQueue<OrauldTuple>[] _dnQueues;
	private final boolean[] _eofs;
	private PrintWriter _pw;
	private boolean _terminateFlag = false;
	private OrauldCmdline _cmdline;
	private static Pattern _FnmSfx = Pattern.compile("\\.[0-9A-Za-z]+$");

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
		_cmdline = OrauldCmdline.GetInstance();
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
			_openPw();
		}
		if (tuple._joined == null) {
			_pw.println("");
		} else {
			_pw.println(tuple._joined);
		}
		_dmpCnt++;
		_splitCnt++;
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
		if (_cmdline._splitLines > 0 && _splitCnt >= _cmdline._splitLines) {
			PubMethod.Close(_pw);
			_pw = null;
			_splitCnt = 0;
		}
	}

	private void _openPw() throws Exception {
		String bcp_fnm_ = _cmdline._bcpFnm;
		if (_cmdline._splitLines > 0) {
			String sfx_ = "";
			String pfx_ = bcp_fnm_;
			Matcher m = _FnmSfx.matcher(bcp_fnm_);
			if (m.find()) {
				sfx_ = m.group();
				pfx_ = m.replaceAll("");
			}
			String seq_ = String.format("_%09d", ++_splitSeq);
			bcp_fnm_ = pfx_ + seq_ + sfx_;
		}
		FileOutputStream fos_ = new FileOutputStream(bcp_fnm_);
		OutputStreamWriter osw_;
		osw_ = new OutputStreamWriter(fos_, _cmdline._charset);
		_pw = new PrintWriter(osw_);
		P(INF, "%s opened for writing, charset [%s]", bcp_fnm_, _cmdline._charset);
	}
}
