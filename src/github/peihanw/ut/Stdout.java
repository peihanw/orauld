package github.peihanw.ut;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Stdout {

	public static final int ERO = 0;
	public static final int WRN = 1;
	public static final int INF = 2;
	public static final int DBG = 3;
	public static final int TRC = 4;
	public static int _DftLevel = DBG;
	public static String _JvmPid = null;
	public static final String[] _LevelNames = {"ERO", "WRN", "INF", "DBG", "TRC"};

	static {
		_DftLevel = DBG;
		_JvmPid = ManagementFactory.getRuntimeMXBean().getName();
		if (_JvmPid.indexOf('@') > 0) {
			_JvmPid = _JvmPid.substring(0, _JvmPid.indexOf('@'));
		}
	}

	public static void P(int log_level, String fmt, Object... args) {
		int level_idx_ = log_level;
		if (level_idx_ < ERO || level_idx_ > TRC) {
			level_idx_ = TRC;
		}
		if (level_idx_ > _DftLevel) {
			return;
		}
		Thread current_thr_ = Thread.currentThread();
		StackTraceElement stack_elem_ = current_thr_.getStackTrace()[2];
		SimpleDateFormat sdf_ = new SimpleDateFormat("yyMMddHHmmss.SSS");
		String fnm_ = stack_elem_.getFileName();
		if (fnm_.endsWith(".java")) {
			fnm_ = fnm_.substring(0, fnm_.length() - 5);
		}
		System.out.printf("%s|%s|%s|%s|%s|%s|%s|%s%n", sdf_.format(new Date()), _LevelNames[level_idx_], _JvmPid,
			current_thr_.getName(), fnm_, stack_elem_.getLineNumber(), stack_elem_.getMethodName(), FmtArgs(fmt, args));
	}

	public static void P(int log_level, Throwable e, String fmt, Object... args) {
		int level_idx_ = log_level;
		if (level_idx_ < ERO || level_idx_ > TRC) {
			level_idx_ = TRC;
		}
		Thread current_thr_ = Thread.currentThread();
		StackTraceElement stack_elem_ = current_thr_.getStackTrace()[2];
		SimpleDateFormat sdf_ = new SimpleDateFormat("yyMMddHHmmss,SSS");
		String fnm_ = stack_elem_.getFileName();
		if (fnm_.endsWith(".java")) {
			fnm_ = fnm_.substring(0, fnm_.length() - 5);
		}
		System.out.printf("%s|%s|%s|%s|%s|%s|%s|%s%n", sdf_.format(new Date()), _LevelNames[level_idx_], _JvmPid,
			current_thr_.getName(), fnm_, stack_elem_.getLineNumber(), stack_elem_.getMethodName(), FmtArgs(fmt, args));
		if (e != null) {
			e.printStackTrace();
		}
	}

	public static String FmtArgs(String fmt, Object... args) {
		String info_ = null;
		try {
			info_ = String.format(fmt, args);
		} catch (Exception e) {
			StringBuilder sb_ = new StringBuilder();
			sb_.append(fmt);
			sb_.append(" [fmt error:");
			sb_.append(e.getMessage());
			sb_.append("] [");
			for (int i = 0; i < args.length; ++i) {
				if (args[i] == null) {
					sb_.append("null");
				} else {
					sb_.append(args[i].toString());
				}
				if (i < args.length - 1) {
					sb_.append(";");
				}
			}
			sb_.append("]");
			info_ = sb_.substring(0);
		}
		return info_;
	}
}
