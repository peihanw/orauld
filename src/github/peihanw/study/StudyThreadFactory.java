package github.peihanw.study;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class StudyThreadFactory implements ThreadFactory {
	private static AtomicInteger _ThreadCnt = null;
	static {
		_ThreadCnt = new AtomicInteger();
	}

	@Override
	public Thread newThread(Runnable r) {
		String thread_name_ = String.format("T%03d", _ThreadCnt.getAndIncrement());
		return new Thread(r, thread_name_);
	}
}
