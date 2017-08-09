package github.peihanw.orauld;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class OrauldThreadFactory implements ThreadFactory {
	private static AtomicInteger _ThreadCnt = null;
	private String _thrPfx;
	static {
		_ThreadCnt = new AtomicInteger();
	}

	public OrauldThreadFactory(String thr_pfx) {
		_thrPfx = thr_pfx;
	}

	@Override
	public Thread newThread(Runnable r) {
		String thr_nm_ = _thrPfx;
		if (_thrPfx.length() == 1) {
			thr_nm_ = String.format("%s%03d", _ThreadCnt.getAndIncrement());
		}
		return new Thread(r, thr_nm_);
	}
}
