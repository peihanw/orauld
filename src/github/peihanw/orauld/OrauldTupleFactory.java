package github.peihanw.orauld;

import com.lmax.disruptor.EventFactory;

public class OrauldTupleFactory implements EventFactory<OrauldTuple> {
	private int _columnCnt;

	public OrauldTupleFactory(int column_cnt) {
		_columnCnt = column_cnt;
	}

	@Override
	public OrauldTuple newInstance() {
		return new OrauldTuple(_columnCnt);
	}
}
