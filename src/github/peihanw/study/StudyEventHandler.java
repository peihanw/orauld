package github.peihanw.study;

import com.lmax.disruptor.EventHandler;

import github.peihanw.ut.PubMethod;

import static github.peihanw.ut.Stdout.*;

public class StudyEventHandler implements EventHandler<StudyEvent> {
	@Override
	public void onEvent(StudyEvent event, long sequence, boolean endOfBatch) throws Exception {
		PubMethod.Sleep(1000);
		P(DBG, "sequence %d, %s", sequence, event.toString());
		if (sequence > 0 && sequence % 13 == 0) {
			throw new RuntimeException("a runtime exception for " + sequence);
		}
	}
}
