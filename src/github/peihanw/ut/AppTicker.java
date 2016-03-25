package github.peihanw.ut;

public class AppTicker {

	public int _recNum;
	public double _term; // in seconds
	public int _pfm; // performance: records per second
	public long _tsStart;
	public long _tsEnd;

	public AppTicker() {
		_tsStart = System.currentTimeMillis();
		_tsEnd = _tsStart;
	}

	public void tickStart() {
		_tsStart = System.currentTimeMillis();
	}

	public void tickEnd() {
		_tsEnd = System.currentTimeMillis();
	}

	public void tickEnd(int rec_num) {
		_tsEnd = System.currentTimeMillis();
		calcPfm(rec_num);
	}

	public void flip() {
		_tsStart = _tsEnd;
	}

	public void calcPfm(int rec_num) {
		_recNum = rec_num;
		long dur_millis_ = _tsEnd - _tsStart;
		if (dur_millis_ <= 0) {
			_term = 0;
			_pfm = 0;
		} else {
			_term = ((double) dur_millis_) / 1000.0;
			_pfm = (int) (_recNum / _term);
		}
	}
}
