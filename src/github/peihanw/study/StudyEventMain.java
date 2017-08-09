package github.peihanw.study;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import github.peihanw.ut.PubMethod;

public class StudyEventMain {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		StudyEventFactory factory_ = new StudyEventFactory();
		Disruptor<StudyEvent> disruptor_ = new Disruptor<StudyEvent>(factory_, 64, new StudyThreadFactory());
		disruptor_.handleEventsWith(new StudyEventHandler());
		disruptor_.setDefaultExceptionHandler(new StudyExceptionHandler());
		disruptor_.start();

		RingBuffer<StudyEvent> ring_buffer_ = disruptor_.getRingBuffer();
		StudyEventProducer producer_ = new StudyEventProducer(ring_buffer_);

		for (long i = 1000000; i < 2000000; ++i) {
			producer_.onData(i);
			PubMethod.Sleep(10);
		}
	}
}
