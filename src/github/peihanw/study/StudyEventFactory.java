package github.peihanw.study;

import com.lmax.disruptor.EventFactory;

public class StudyEventFactory implements EventFactory<StudyEvent> {
	@Override
	public StudyEvent newInstance() {
		return new StudyEvent();
	}
}
