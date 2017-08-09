package github.peihanw.orauld;

import static github.peihanw.ut.Stdout.*;
import com.lmax.disruptor.ExceptionHandler;

public class OrauldExceptionHandler implements ExceptionHandler<Object> {
	@Override
	public void handleEventException(Throwable e, long seq, Object event) {
		P(WRN, e, "seq %d, event %s", seq, event.toString());
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
