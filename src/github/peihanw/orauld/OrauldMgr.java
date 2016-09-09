package github.peihanw.orauld;

import github.peihanw.ut.PubMethod;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static github.peihanw.ut.Stdout.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class OrauldMgr {

	public long _sqlCnt;
	private final BlockingQueue<OrauldTuple>[] _upQueues;
	private final OrauldCmdline _cmdline;
	private Connection _conn;
	private ResultSetMetaData _meta;
	private ResultSet _rs;
	private Statement _stmt;
	private int[] _columnTypes;

	public OrauldMgr(BlockingQueue<OrauldTuple>[] up_queues) {
		_upQueues = up_queues;
		_cmdline = OrauldCmdline.GetInstance();
	}

	public int run() {
		boolean connect_ok_ = false;
		try {
			if (!_initConn()) {
				P(WRN, "call _initConn() error");
				return 1;
			} else {
				connect_ok_ = true;
			}
		} catch (Throwable e) { // for 'java.lang.NoClassDefFoundError'
			P(WRN, e, "call _initConn error");
			return 1;
		} finally {
			if (!connect_ok_) {
				try {
					_emitEOF();
				} catch (Exception e) {
					P(ERO, e, "_emitEOF exception");
				}
			}
		}

		try {
			_exec();
		} catch (Exception e) {
			P(WRN, e, "call _exec() exception");
			return 1;
		}

		return 0;
	}

	public void closeResource() {
		_closeResource(_rs, _stmt, _conn);
		_rs = null;
		_stmt = null;
		_conn = null;
	}

	private boolean _initConn() {
		String jdbc_url_ = String.format("jdbc:oracle:thin:@%s:%d:%s", _cmdline._loginRec._host, _cmdline._loginRec._port, _cmdline._loginRec._sid);
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			_conn = DriverManager.getConnection(jdbc_url_, _cmdline._loginRec._usr, _cmdline._loginRec._password);
		} catch (Exception e) {
			P(WRN, e, "init db connection failed");
			return false;
		}
		P(INF, "init db connection [%s] db user [%s] ok", jdbc_url_, _cmdline._loginRec._usr);
		return true;
	}

	private void _exec() throws Exception {
		_stmt = _conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		_stmt.setFetchSize(1000);
		_rs = _stmt.executeQuery(_cmdline._querySql);
		P(INF, "query executed, fetchSize=%d, maxRows=%d", _stmt.getFetchSize(), _stmt.getMaxRows());
		_meta = _rs.getMetaData();
		_printMeta();
		if (!PubMethod.IsEmpty(_cmdline._bcpFnm)) {
			int column_cnt_ = _columnTypes.length - 1;
			OrauldTuple tuple_;
			while (_rs.next()) {
				tuple_ = new OrauldTuple(column_cnt_);
				for (int i = 1; i <= column_cnt_; i++) {
					_fillTuple(tuple_, _rs, i);
				}
				int idx_ = (int) (_sqlCnt % _upQueues.length);
				_upQueues[idx_].offer(tuple_, _cmdline._queueOfferTimeout, TimeUnit.SECONDS);
				_sqlCnt++;
			}
		}
		_emitEOF();
		P(INF, "%d EOF tuple emitted, _sqlCnt=%d", _upQueues.length, _sqlCnt);
	}

	private void _emitEOF() throws Exception {
		OrauldTuple tuple_ = null;
		for (int i = 0; i < _upQueues.length; ++i) {
			tuple_ = new OrauldTuple(1);
			tuple_._idx = -1;
			_upQueues[i].offer(tuple_, _cmdline._queueOfferTimeout, TimeUnit.SECONDS);
		}
	}

	private void _printMeta() throws Exception {
		StringBuilder sb_ = new StringBuilder();
		int column_cnt_ = _meta.getColumnCount();
		_columnTypes = new int[column_cnt_ + 1];
		for (int i = 1; i <= column_cnt_; i++) {
			_columnTypes[i] = _meta.getColumnType(i);
			P(DBG, "column %3d, [%s] [%s:%d:%d] [%d] [%s]", i, _meta.getColumnName(i), _meta.getColumnTypeName(i),
				_meta.getPrecision(i), _meta.getScale(i), _meta.getColumnType(i), _meta.getColumnClassName(i));
			if (i > 1) {
				sb_.append(_cmdline._delimiter);
			}
			sb_.append(_meta.getColumnName(i));
		}
		if (!PubMethod.IsEmpty(_cmdline._eorStr)) {
			sb_.append(_cmdline._eorStr);
		}
		_cmdline._headerLine = sb_.substring(0);
		OrauldWrkRunnable._ColumnTypes = _columnTypes;
		_printCtl();
	}

	private void _printCtl() throws Exception {
		if (PubMethod.IsEmpty(_cmdline._ctlFnm)) {
			return;
		}
		int column_cnt_ = _meta.getColumnCount();
		String table_name_ = _cmdline._ctlFnm.replaceAll("\\.[0-9A-Za-z]+$", "");
		FileOutputStream fos_ = new FileOutputStream(_cmdline._ctlFnm);
		OutputStreamWriter osw_ = new OutputStreamWriter(fos_, _cmdline._charset);
		PrintWriter pw_ = new PrintWriter(osw_);
		P(INF, "%s opened for writing, charset [%s], table_name [%s]", _cmdline._ctlFnm, _cmdline._charset, table_name_);
		if (_cmdline._header) {
			pw_.printf("OPTIONS (SKIP=1)%n", table_name_);
		}
		pw_.printf("LOAD DATA INFILE *", table_name_);
		if (PubMethod.IsEmpty(_cmdline._eorStr)) {
			pw_.printf("%n");
		} else {
			pw_.printf(" \"STR '%s\\n'\"%n", _cmdline._eorStr);
		}
		pw_.printf("INTO TABLE %s%n", table_name_);
		pw_.printf("APPEND FIELDS TERMINATED BY \"%s\"%n", _cmdline._delimiter);
		pw_.printf("TRAILING NULLCOLS%n(%n");
		for (int i = 1; i <= column_cnt_; i++) {
			pw_.printf(" %s", _meta.getColumnName(i));
			if (_columnTypes[i] == 91) {
				pw_.printf(" DATE 'YYYY-MM-DD HH24:MI:SS'");
			} else if (_columnTypes[i] == 93) {
				pw_.printf(" TIMESTAMP 'YYYY-MM-DD HH24:MI:SS.FF3'");
			} else if (_columnTypes[i] == 12 && _meta.getPrecision(i) > 255) {
				pw_.printf(" CHAR(%d)", _meta.getPrecision(i));
			}
			if (i < column_cnt_) {
				pw_.printf(",%n");
			} else {
				pw_.printf("%n");
			}
		}
		pw_.printf(")%n");
		pw_.printf("-- $ORACLE_HOME/bin/sqlldr userid=\"user/password@oraclesid\"\\%n");
		pw_.printf("--  silent=header,feedback\\%n");
		pw_.printf("--  control=%s\\%n", _cmdline._ctlFnm);
		pw_.printf("--  data=%s.bcp\\%n", table_name_);
		pw_.printf("--  bad=%s.bad\\%n", table_name_);
		pw_.printf("--  log=%s.log\\%n", table_name_);
		pw_.printf("--  bindsize=10485760\\%n");
		pw_.printf("--  readsize=10485760\\%n");
		pw_.printf("--  errors=1000000000\\%n");
		pw_.printf("--  skip_unusable_indexes=true\\%n");
		pw_.printf("--  commit_discontinued=true%n");
		pw_.flush();
		pw_.close();
		P(INF, "%s closed, table_name [%s], column cnt %d", _cmdline._ctlFnm, table_name_, column_cnt_);
	}

	private void _fillTuple(OrauldTuple tuple, ResultSet rs, int idx) throws SQLException {
		switch (_columnTypes[idx]) {
			case 2: // NUMBER, java.math.BigDecimal, (tuple._cells[idx] = rs.getBigDecimal(idx);)
				tuple._bytes[idx] = rs.getBytes(idx);
				break;
			case 12: // VARCHAR/VARCHAR2
			case 1: // CHAR
			case 91: // DATE
			case 93: // TIMESTAMP
			case -8: // ROWID
				tuple._bytes[idx] = rs.getBytes(idx);
				break;
			case 2005: // CLOB
				tuple._cells[idx] = rs.getClob(idx);
				break;
			case 2004: // BLOB, do nothing, always regard as NULL
				break;
			default:
				byte[] bytes_ = rs.getBytes(idx);
				if (bytes_ != null) {
					StringBuilder sb_ = new StringBuilder();
					sb_.append("[");
					int i = 0;
					for (byte b : bytes_) {
						if (i == 0) {
							sb_.append(String.format("%02x", b));
						} else {
							sb_.append(String.format(" %02x", b));
						}
						++i;
					}
					sb_.append("]");
					P(WRN, "_columnTypes[%d] is %d, sample guts: %s", idx, _columnTypes[idx], sb_.substring(0));
					throw new SQLException("unsupported column type " + _columnTypes[idx]);
				}
				break;
		}
	}

	private void _closeResource(ResultSet rs, Statement stmt, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				P(WRN, e, "close ResultSet exception, pls ignore");
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
				P(WRN, e, "close Statement exception, pls ignore");
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				P(WRN, e, "close Connection exception, pls ignore");
			}
		}
	}
}
