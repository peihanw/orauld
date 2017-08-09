package github.peihanw.study;

import com.lmax.disruptor.RingBuffer;

public class StudyEventProducer {
	private final RingBuffer<StudyEvent> _ringBuffer;

	public StudyEventProducer(RingBuffer<StudyEvent> ring_buffer) {
		_ringBuffer = ring_buffer;
	}

	public void onData(long id) {
		long sequence_ = _ringBuffer.next();
		try {
			StudyEvent event_ = _ringBuffer.get(sequence_);
			event_._id = id;
			event_._name = Long.toString(id);
		} finally {
			_ringBuffer.publish(sequence_);
		}
	}
}
