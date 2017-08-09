package github.peihanw.study;

import com.lmax.disruptor.ExceptionHandler;
import static github.peihanw.ut.Stdout.*;

public class StudyExceptionHandler implements ExceptionHandler<Object> {
	@Override
	public void handleEventException(Throwable e, long seq, Object event) {
		P(WRN, e, "seq %d", seq);
	}

	@Override
	public void handleOnShutdownException(Throwable e) {
		P(WRN, e, "shutdown exception");
	}

	@Override
	public void handleOnStartException(Throwable e) {
		P(WRN, e, "start exception");
	}
}
