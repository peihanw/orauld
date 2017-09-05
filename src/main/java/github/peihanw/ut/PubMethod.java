package github.peihanw.ut;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static github.peihanw.ut.Stdout.*;

public class PubMethod {

	public static class TsSeq {

		public long _ts;
		public int _seq;
	}

	public enum StripMode {
		StripLeading, StripTrailing, StripBoth
	}

	public enum TimeStrFmt {
		Fmt23, // yyyy-MM-dd HH:mm:ss.SSS
		Fmt19, // yyyy-MM-dd HH:mm:ss
		Fmt18, // yyyyMMddHHmmss.SSS
		Fmt17, // yyyyMMddHHmmssSSS
		Fmt16, // yyMMddHHmmss.SSS
		Fmt14, // yyyyMMddHHmmss
		Fmt12, // yyMMddHHmmss
		Fmt10, // yyyy-MM-dd
		Fmt8, // yyyyMMdd
		Fmt6 // yyMMdd
	}

	public static final String _TMFMT23 = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String _TMFMT19 = "yyyy-MM-dd HH:mm:ss";
	public static final String _TMFMT18 = "yyyyMMddHHmmss.SSS";
	public static final String _TMFMT17 = "yyyyMMddHHmmssSSS";
	public static final String _TMFMT16 = "yyMMddHHmmss.SSS";
	public static final String _TMFMT14 = "yyyyMMddHHmmss";
	public static final String _TMFMT12 = "yyMMddHHmmss";
	public static final String _DTFMT10 = "yyyy-MM-dd";
	public static final String _DTFMT8 = "yyyyMMdd";
	public static final String _DTFMT6 = "yyMMdd";

	public static final Pattern _ExpTMFMT23 = Pattern.compile("^[0-9]{4}[^0-9]([0-9]{2}[^0-9]){5}[0-9]{3}");
	public static final Pattern _ExpTMFMT19 = Pattern.compile("^[0-9]{4}[^0-9]([0-9]{2}[^0-9]){4}[0-9]{2}");
	public static final Pattern _ExpTMFMT18 = Pattern.compile("^[0-9]{14}[^0-9][0-9]{3}");
	public static final Pattern _ExpTMFMT17 = Pattern.compile("^[0-9]{17}");
	public static final Pattern _ExpTMFMT16 = Pattern.compile("^[0-9]{12}[^0-9][0-9]{3}");
	public static final Pattern _ExpTMFMT14 = Pattern.compile("^[0-9]{14}");
	public static final Pattern _ExpTMFMT12 = Pattern.compile("^[0-9]{12}");
	public static final Pattern _ExpTMFMT10 = Pattern.compile("^[0-9]{4}[^0-9][0-9]{2}[^0-9][0-9]{2}");
	public static final Pattern _ExpTMFMT8 = Pattern.compile("^[0-9]{8}");
	public static final Pattern _ExpTMFMT6 = Pattern.compile("^[0-9]{6}");

	public static final DateTimeFormatter _TMDF23 = DateTimeFormatter.ofPattern(_TMFMT23);
	public static final DateTimeFormatter _TMDF19 = DateTimeFormatter.ofPattern(_TMFMT19);
	public static final DateTimeFormatter _TMDF18 = DateTimeFormatter.ofPattern(_TMFMT18);
	public static final DateTimeFormatter _TMDF17 = DateTimeFormatter.ofPattern(_TMFMT17);
	public static final DateTimeFormatter _TMDF16 = DateTimeFormatter.ofPattern(_TMFMT16);
	public static final DateTimeFormatter _TMDF14 = DateTimeFormatter.ofPattern(_TMFMT14);
	public static final DateTimeFormatter _TMDF12 = DateTimeFormatter.ofPattern(_TMFMT12);
	public static final DateTimeFormatter _DTDF10 = DateTimeFormatter.ofPattern(_DTFMT10);
	public static final DateTimeFormatter _DTDF8 = DateTimeFormatter.ofPattern(_DTFMT8);
	public static final DateTimeFormatter _DTDF6 = DateTimeFormatter.ofPattern(_DTFMT6);

	public static final ZoneId _LocTZ = ZoneId.systemDefault();
	public static final long _UNIX_EPOCH_MILLIS = 0;
	public static final LocalDateTime _UNIX_EPOCH_DATE = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

	public static final Pattern _ExpIPv4 = Pattern
			.compile("^(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}" + "(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))$");
	public static final Pattern _ExpEnv = Pattern.compile("(\\$\\(\\w+\\)|\\$\\w+)");
	private static final Pattern _ExpLong = Pattern.compile("^(\\+|-)?\\d+");

	public static final List<Integer> _PrimesAnd1;
	private static final int[] _PrimesAndOne = new int[] { 1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61,
			67, 71, 73, 79, 83, 89, 97 };

	private static final char _Alphabet[] = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	private static final int _Crctab[] = { 0x00000000, 0x04c11db7, 0x09823b6e, 0x0d4326d9, 0x130476dc, 0x17c56b6b, 0x1a864db2,
			0x1e475005, 0x2608edb8, 0x22c9f00f, 0x2f8ad6d6, 0x2b4bcb61, 0x350c9b64, 0x31cd86d3, 0x3c8ea00a, 0x384fbdbd, 0x4c11db70,
			0x48d0c6c7, 0x4593e01e, 0x4152fda9, 0x5f15adac, 0x5bd4b01b, 0x569796c2, 0x52568b75, 0x6a1936c8, 0x6ed82b7f, 0x639b0da6,
			0x675a1011, 0x791d4014, 0x7ddc5da3, 0x709f7b7a, 0x745e66cd, 0x9823b6e0, 0x9ce2ab57, 0x91a18d8e, 0x95609039, 0x8b27c03c,
			0x8fe6dd8b, 0x82a5fb52, 0x8664e6e5, 0xbe2b5b58, 0xbaea46ef, 0xb7a96036, 0xb3687d81, 0xad2f2d84, 0xa9ee3033, 0xa4ad16ea,
			0xa06c0b5d, 0xd4326d90, 0xd0f37027, 0xddb056fe, 0xd9714b49, 0xc7361b4c, 0xc3f706fb, 0xceb42022, 0xca753d95, 0xf23a8028,
			0xf6fb9d9f, 0xfbb8bb46, 0xff79a6f1, 0xe13ef6f4, 0xe5ffeb43, 0xe8bccd9a, 0xec7dd02d, 0x34867077, 0x30476dc0, 0x3d044b19,
			0x39c556ae, 0x278206ab, 0x23431b1c, 0x2e003dc5, 0x2ac12072, 0x128e9dcf, 0x164f8078, 0x1b0ca6a1, 0x1fcdbb16, 0x018aeb13,
			0x054bf6a4, 0x0808d07d, 0x0cc9cdca, 0x7897ab07, 0x7c56b6b0, 0x71159069, 0x75d48dde, 0x6b93dddb, 0x6f52c06c, 0x6211e6b5,
			0x66d0fb02, 0x5e9f46bf, 0x5a5e5b08, 0x571d7dd1, 0x53dc6066, 0x4d9b3063, 0x495a2dd4, 0x44190b0d, 0x40d816ba, 0xaca5c697,
			0xa864db20, 0xa527fdf9, 0xa1e6e04e, 0xbfa1b04b, 0xbb60adfc, 0xb6238b25, 0xb2e29692, 0x8aad2b2f, 0x8e6c3698, 0x832f1041,
			0x87ee0df6, 0x99a95df3, 0x9d684044, 0x902b669d, 0x94ea7b2a, 0xe0b41de7, 0xe4750050, 0xe9362689, 0xedf73b3e, 0xf3b06b3b,
			0xf771768c, 0xfa325055, 0xfef34de2, 0xc6bcf05f, 0xc27dede8, 0xcf3ecb31, 0xcbffd686, 0xd5b88683, 0xd1799b34, 0xdc3abded,
			0xd8fba05a, 0x690ce0ee, 0x6dcdfd59, 0x608edb80, 0x644fc637, 0x7a089632, 0x7ec98b85, 0x738aad5c, 0x774bb0eb, 0x4f040d56,
			0x4bc510e1, 0x46863638, 0x42472b8f, 0x5c007b8a, 0x58c1663d, 0x558240e4, 0x51435d53, 0x251d3b9e, 0x21dc2629, 0x2c9f00f0,
			0x285e1d47, 0x36194d42, 0x32d850f5, 0x3f9b762c, 0x3b5a6b9b, 0x0315d626, 0x07d4cb91, 0x0a97ed48, 0x0e56f0ff, 0x1011a0fa,
			0x14d0bd4d, 0x19939b94, 0x1d528623, 0xf12f560e, 0xf5ee4bb9, 0xf8ad6d60, 0xfc6c70d7, 0xe22b20d2, 0xe6ea3d65, 0xeba91bbc,
			0xef68060b, 0xd727bbb6, 0xd3e6a601, 0xdea580d8, 0xda649d6f, 0xc423cd6a, 0xc0e2d0dd, 0xcda1f604, 0xc960ebb3, 0xbd3e8d7e,
			0xb9ff90c9, 0xb4bcb610, 0xb07daba7, 0xae3afba2, 0xaafbe615, 0xa7b8c0cc, 0xa379dd7b, 0x9b3660c6, 0x9ff77d71, 0x92b45ba8,
			0x9675461f, 0x8832161a, 0x8cf30bad, 0x81b02d74, 0x857130c3, 0x5d8a9099, 0x594b8d2e, 0x5408abf7, 0x50c9b640, 0x4e8ee645,
			0x4a4ffbf2, 0x470cdd2b, 0x43cdc09c, 0x7b827d21, 0x7f436096, 0x7200464f, 0x76c15bf8, 0x68860bfd, 0x6c47164a, 0x61043093,
			0x65c52d24, 0x119b4be9, 0x155a565e, 0x18197087, 0x1cd86d30, 0x029f3d35, 0x065e2082, 0x0b1d065b, 0x0fdc1bec, 0x3793a651,
			0x3352bbe6, 0x3e119d3f, 0x3ad08088, 0x2497d08d, 0x2056cd3a, 0x2d15ebe3, 0x29d4f654, 0xc5a92679, 0xc1683bce, 0xcc2b1d17,
			0xc8ea00a0, 0xd6ad50a5, 0xd26c4d12, 0xdf2f6bcb, 0xdbee767c, 0xe3a1cbc1, 0xe760d676, 0xea23f0af, 0xeee2ed18, 0xf0a5bd1d,
			0xf464a0aa, 0xf9278673, 0xfde69bc4, 0x89b8fd09, 0x8d79e0be, 0x803ac667, 0x84fbdbd0, 0x9abc8bd5, 0x9e7d9662, 0x933eb0bb,
			0x97ffad0c, 0xafb010b1, 0xab710d06, 0xa6322bdf, 0xa2f33668, 0xbcb4666d, 0xb8757bda, 0xb5365d03, 0xb1f740b4 };

	// for NextSeq
	private static AtomicLong _GlobalSeq = new AtomicLong();
	private static ConcurrentHashMap<String, AtomicLong> _SeqHashMap = new ConcurrentHashMap<String, AtomicLong>();

	// for NextTsSeq
	private static TsSeq _GlobalTsSeq = new TsSeq();

	static {
		_PrimesAnd1 = new ArrayList<Integer>();
		for (int p : _PrimesAndOne) {
			_PrimesAnd1.add(p);
		}
	}

	public static boolean IsEmpty(String str) {
		return str == null ? true : str.length() == 0 ? true : false;
	}

	public static boolean IsBlank(String str) {
		return str == null ? true : str.length() == 0 ? true : str.trim().length() == 0 ? true : false;
	}

	public static long Str2Long(String time_str, TimeStrFmt fmt) {
		return Str2Time(time_str, fmt).atZone(_LocTZ).toInstant().toEpochMilli();
	}

	public static LocalDateTime Str2Time(String time_str, TimeStrFmt fmt) {
		LocalDateTime rv_ = _UNIX_EPOCH_DATE;
		DateTimeFormatter dtf_ = null;
		String time_str_ = time_str;
		switch (fmt) {
		case Fmt23:
			dtf_ = _TMDF23;
			if (time_str.length() > 23) {
				time_str_ = time_str.substring(0, 23);
			}
			if (time_str_.length() > 10 && time_str_.charAt(10) != ' ') {
				time_str_ = time_str_.substring(0, 9) + " " + time_str_.substring(11);
			}
			break;
		case Fmt19:
			dtf_ = _TMDF19;
			if (time_str.length() > 19) {
				time_str_ = time_str.substring(0, 19);
			}
			break;
		case Fmt18:
			dtf_ = _TMDF18;
			if (time_str.length() > 18) {
				time_str_ = time_str.substring(0, 18);
			}
			break;
		case Fmt17:
			dtf_ = _TMDF17;
			if (time_str.length() > 17) {
				time_str_ = time_str.substring(0, 17);
			}
			break;
		case Fmt16:
			dtf_ = _TMDF16;
			if (time_str.length() > 16) {
				time_str_ = time_str.substring(0, 16);
			}
			break;
		case Fmt14:
			dtf_ = _TMDF14;
			if (time_str.length() > 14) {
				time_str_ = time_str.substring(0, 14);
			}
			break;
		case Fmt12:
			dtf_ = _TMDF12;
			if (time_str.length() > 12) {
				time_str_ = time_str.substring(0, 12);
			}
			break;
		case Fmt10:
			dtf_ = _DTDF10;
			if (time_str.length() > 10) {
				time_str_ = time_str.substring(0, 10);
			}
			break;
		case Fmt8:
			dtf_ = _DTDF8;
			if (time_str.length() > 8) {
				time_str_ = time_str.substring(0, 8);
			}
			break;
		case Fmt6:
			dtf_ = _DTDF6;
			if (time_str.length() > 6) {
				time_str_ = time_str.substring(0, 6);
			}
			break;
		default:
			P(WRN, "ukn fmt [%s], regard as Fmt14", fmt);
			dtf_ = _TMDF14;
			if (time_str.length() > 14) {
				time_str_ = time_str.substring(0, 14);
			}
			break;
		}
		try {
			LocalDateTime dt_ = LocalDateTime.parse(time_str_, dtf_);
			rv_ = dt_;
		} catch (DateTimeParseException e) {
			P(ERO, e, "parse [%s,%s] exception, regard as _UNIX_EPOCH", time_str, fmt);
		}
		return rv_;
	}

	public static String Long2Str(long tm, TimeStrFmt fmt) {
		return Time2Str(LocalDateTime.ofInstant(Instant.ofEpochMilli(tm), _LocTZ), fmt);
	}

	public static String Time2Str(LocalDateTime tm, TimeStrFmt fmt) {
		DateTimeFormatter dtf_ = null;
		switch (fmt) {
		case Fmt23:
			dtf_ = _TMDF23;
			break;
		case Fmt19:
			dtf_ = _TMDF19;
			break;
		case Fmt18:
			dtf_ = _TMDF18;
			break;
		case Fmt17:
			dtf_ = _TMDF17;
			break;
		case Fmt14:
			dtf_ = _TMDF14;
			break;
		case Fmt12:
			dtf_ = _TMDF12;
			break;
		case Fmt10:
			dtf_ = _DTDF10;
			break;
		case Fmt8:
			dtf_ = _DTDF8;
			break;
		case Fmt6:
			dtf_ = _DTDF6;
			break;
		default:
			P(WRN, "ukn fmt [%s], regard as Fmt14", fmt);
			dtf_ = _TMDF14;
			break;
		}
		return dtf_.format(tm);
	}

	public static String RandomStr(int str_len) {
		StringBuilder sb_ = new StringBuilder();
		for (int i = 0; i < str_len; ++i) {
			int idx_ = ThreadLocalRandom.current().nextInt(_Alphabet.length);
			sb_.append(_Alphabet[idx_]);
		}
		return sb_.substring(0);
	}

	public static long BuffCksum(byte[] buff) {
		/*
		 * * migrated from GNU text-utilities.* high performance cksum calculation.** origin author:* Q. Frank Xia,
		 * qx@math.columbia.edu.* David MacKenzie, djm@gnu.ai.mit.edu.
		 */
		int cksum_ = 0;
		int len_ = buff.length;
		int i = 0;
		while (len_-- > 0) {
			cksum_ = (cksum_ << 8) ^ _Crctab[((cksum_ >> 24) ^ buff[i++]) & 0xFF];
		}
		for (len_ = buff.length; len_ != 0; len_ >>= 8) {
			cksum_ = (cksum_ << 8) ^ _Crctab[((cksum_ >> 24) ^ buff.length) & 0xFF];
		}

		cksum_ = ~cksum_ & 0xFFFFFFFF;
		long rv_ = cksum_;
		if (rv_ < 0) {
			rv_ += 4294967296L;
		}
		return rv_;
	}

	public static long FileCksum(String file_nm) {
		/*
		 * migrated from GNU text-utilities. high performance cksum calculation. origin author: Q. Frank Xia, qx@math.columbia.edu.
		 * David MacKenzie, djm@gnu.ai.mit.edu.
		 */
		byte[] buf_ = new byte[8192];
		int length_ = 0;
		int bytes_ = 0;
		int cksum_ = 0;

		try {
			BufferedInputStream bis_ = new BufferedInputStream(new FileInputStream(file_nm));
			while ((bytes_ = bis_.read(buf_)) > 0) {
				length_ += bytes_;
				int i = 0;
				while (bytes_-- > 0) {
					cksum_ = (cksum_ << 8) ^ _Crctab[((cksum_ >> 24) ^ buf_[i++]) & 0xFF];
				}
			}
			bis_.close();
		} catch (IOException e) {
			P(WRN, e, "read [%s] exception, reset cksum", file_nm);
			cksum_ = 0;
		}
		for (; length_ > 0; length_ >>= 8) {
			cksum_ = (cksum_ << 8) ^ _Crctab[((cksum_ >> 24) ^ length_) & 0xFF];
		}
		cksum_ = ~cksum_ & 0xFFFFFFFF;
		long rv_ = cksum_;
		if (rv_ < 0) {
			rv_ += 4294967296L;
		}
		return rv_;
	}

	public static String Md5(String str) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(str.getBytes());
			byte[] digest_ = m.digest();
			BigInteger big_int_ = new BigInteger(1, digest_);
			String hash_ = big_int_.toString(16);
			while (hash_.length() < 32) {
				hash_ = "0" + hash_;
			}
			return hash_;
		} catch (java.security.NoSuchAlgorithmException e) {
			P(WRN, e, "get MD5 MessageDigest exception");
			return "00000000000000000000000000000000";
		}
	}

	public static String SubStrByEnv(String str) {
		StringBuilder sb_ = new StringBuilder();
		int p_end_ = 0; // previous end position
		int m_start_ = 0; // matcher start position
		int m_end_ = 0; // matcher end position
		Matcher m = _ExpEnv.matcher(str);
		while (m.find()) {
			m_start_ = m.start();
			m_end_ = m.end();
			sb_.append(str.substring(p_end_, m_start_));
			String env_ = str.substring(m_start_ + 1, m_end_);
			if (env_.charAt(0) == '(') { // strip bracket
				env_ = env_.substring(1, env_.length() - 1);
			}
			String val_ = System.getenv(env_);
			if (val_ == null) {
				P(WRN, "env [%s] not set, replace failed", env_);
				return null;
			}
			sb_.append(val_);
			p_end_ = m_end_;
		}
		sb_.append(str.substring(m_end_));
		return sb_.substring(0);
	}

	private static long _Str2Long(String num_str) {
		String str_trim_ = num_str.trim();
		Matcher m = _ExpLong.matcher(str_trim_);
		long rv_ = 0;
		if (!m.find()) {
			return rv_;
		} else {
			String str_num_ = str_trim_.substring(m.start(), m.end());
			if ((str_num_.length() > 0) && (str_num_.charAt(0) == '+')) {
				str_num_ = str_num_.substring(1, str_num_.length());
			}
			P(DBG, "str_num_=[%s]", str_num_);
			try {
				rv_ = Long.parseLong(str_num_);
			} catch (NumberFormatException e) {
				P(WRN, e, "parse(%s) exception", str_num_);
			}
		}
		return rv_;
	}

	public static long A2L(String num_str) {
		if (PubMethod.IsBlank(num_str)) {
			return 0;
		}
		long rv_ = 0;
		try {
			rv_ = Long.parseLong(num_str);
		} catch (NumberFormatException e) {
			P(WRN, e, "parse(%s) exception", num_str);
			return _Str2Long(num_str);
		}
		return rv_;
	}

	public static boolean CopyAFile(String src, String dst) {
		boolean rc_ = true;
		File dst_file_ = new File(dst);
		String dst_tmp_ = dst_file_.getParent() + "/." + dst_file_.getName();
		try {
			FileInputStream fis_ = new FileInputStream(src);
			FileOutputStream fos_ = new FileOutputStream(dst_tmp_);
			FileChannel ic_ = fis_.getChannel();
			FileChannel oc_ = fos_.getChannel();
			ByteBuffer buf_ = ByteBuffer.allocateDirect(2048);
			int r = 0;
			while (true) {
				buf_.clear();
				r = ic_.read(buf_);
				if (r == -1) {
					break;
				}
				buf_.flip();
				oc_.write(buf_);
			}
			fis_.close();
			fos_.close();
			File hide_ = new File(dst_tmp_);
			if (dst_file_.exists()) {
				dst_file_.delete();
			}
			rc_ = hide_.renameTo(dst_file_);
			if (rc_ == false) {
				P(ERO, "rename(%s,%s) failed", hide_.getPath(), dst_file_.getPath());
			}
		} catch (Exception e) {
			P(ERO, e, "cp [%s,%s] exception", src, dst);
			rc_ = false;
		}
		return rc_;
	}

	public static boolean MoveAFile(String src, String dst) {
		boolean rc_ = true;
		File src_file_ = new File(src);
		File dst_file_ = new File(dst);
		try {
			if (src_file_.renameTo(dst_file_)) {
				return true;
			}
			P(INF, "rename(%s,%s) false, try copy then delete", src, dst);
			if (!CopyAFile(src, dst)) {
				P(WRN, "call CopyAFile(%s,%s) error", src, dst);
				return false;
			}
			if (src_file_.delete()) {
				return true;
			} else {
				P(WRN, "delete (%s) error after copy to (%s)", src, dst);
				return false;
			}
		} catch (Exception e) {
			P(ERO, e, "MoveAFile(%s,%s) exception", src, dst);
			rc_ = false;
		}
		return rc_;
	}

	public static boolean WaitAFile(String filenm, int timeout) {
		File file_obj_ = new File(filenm);
		if (file_obj_.isFile()) {
			return true;
		}

		long now_ = System.currentTimeMillis();
		long begin_ = now_;
		long end_ = now_ + timeout * 1000;
		long diff_millis_ = 0;
		long diff_secs_ = 0;

		while (now_ < end_) {
			PubMethod.Sleep(1000);
			if (file_obj_.isFile()) {
				return true;
			}
			now_ = System.currentTimeMillis();
			diff_millis_ = now_ - begin_;
			diff_secs_ = diff_millis_ / 1000;
			if ((diff_secs_ > 2) && (diff_secs_ % 5 == 0)) {
				P(DBG, "wait [%s] for %d seconds, %d seconds left", filenm, diff_secs_, (end_ - now_) / 1000);
			}
		}
		P(WRN, "wait [%s] for %d seconds, timeout=%d", filenm, diff_secs_, timeout);
		return false;
	}

	public static void Sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			P(WRN, e, "sleep exception, millis=%d, ignore and break out", millis);
		}
	}

	public static void Close(Closeable closeable_obj) {
		try {
			if (closeable_obj != null) {
				closeable_obj.close();
			}
		} catch (IOException e) {
			P(WRN, e, "close [%s] exception, ignore", closeable_obj.toString());
		}
	}

	public static long Ip2Long(String ip_addr) {
		if (ip_addr == null) {
			P(WRN, "ip_addr is null, return -1");
			return -1;
		}
		if (!_ExpIPv4.matcher(ip_addr).find()) {
			P(WRN, "ip_addr [%s] not match pattern [%s], return -1", ip_addr, _ExpIPv4.pattern());
			return -1;
		}
		String[] s_array_ = ip_addr.split("\\.");
		long[] l_array_ = new long[4];
		for (int i = 0; i < 4; ++i) {
			l_array_[i] = Long.parseLong(s_array_[i]);
		}
		return l_array_[0] * 16777216 + l_array_[1] * 65536 + l_array_[2] * 256 + l_array_[3];
	}

	public static long NextSeq() {
		return _GlobalSeq.addAndGet(1);
	}

	public static long NextSeq(String seq_nm) {
		if (_SeqHashMap.get(seq_nm) == null) {
			_SeqHashMap.put(seq_nm, new AtomicLong(0));
		}
		return _SeqHashMap.get(seq_nm).addAndGet(1);
	}

	public static String NextTsSeq() {
		int seq_ = 0;
		long now_ = 0;
		synchronized (_GlobalTsSeq) {
			now_ = System.currentTimeMillis();
			if (now_ == _GlobalTsSeq._ts) {
				++_GlobalTsSeq._seq;
			} else {
				_GlobalTsSeq._ts = now_;
				_GlobalTsSeq._seq = 0;
			}
			seq_ = _GlobalTsSeq._seq;
		}
		String ts_seq_ = String.format("%s000%02d", Long2Str(now_, TimeStrFmt.Fmt17), seq_ % 100);
		return ts_seq_;
	}

	public static String DumpBytes(byte[] raw) {
		if (raw == null || raw.length <= 0) {
			return "";
		}
		StringBuilder sb_fin_ = new StringBuilder();
		StringBuilder sb_tmp_ = new StringBuilder();
		int chunks_ = raw.length / 16;
		if (raw.length % 16 != 0) {
			++chunks_;
		}
		for (int i = 0; i < chunks_; ++i) {
			_DumpBytes(sb_tmp_, raw, i * 16, (i + 1) * 16 > raw.length ? raw.length : (i + 1) * 16);
			sb_fin_.append(sb_tmp_.substring(0));
			if (i < chunks_ - 1) {
				sb_fin_.append(String.format("%n"));
			}
		}
		return sb_fin_.substring(0);
	}

	private static void _DumpBytes(StringBuilder sb, byte[] raw, int start, int end) {
		sb.delete(0, sb.length());
		int delta_ = end - start;
		if (delta_ <= 0) {
			return;
		}
		int i = 0;
		for (i = 0; i < 16; ++i) {
			sb.append(' ');
			if (i < delta_) {
				sb.append(String.format("%02x", raw[start + i]));
			} else {
				sb.append("  ");
			}
		}

		sb.append(" - ");

		for (i = 0; i < 16; ++i) {
			if (i < delta_) {
				int val_ = 0xFF & raw[start + i];
				if (val_ >= 0x20 && val_ <= 0x7e) {
					sb.append(String.format("%c", raw[start + i]));
				} else {
					sb.append('.');
				}
			} else {
				sb.append(' ');
			}
		}
	}

	public static String Collection2Str(Collection<? extends Object> collection, String delimiter) {
		if (collection == null || collection.size() == 0) {
			return "";
		}
		int i = 0;
		StringBuilder sb_ = new StringBuilder();
		for (Object o : collection) {
			if (i > 0) {
				sb_.append(delimiter);
			}
			sb_.append(o.toString());
			i++;
		}
		return sb_.substring(0);
	}

	public static String Strip(String str, String char_set, StripMode strip_mode) {
		if (str == null || str.length() == 0) {
			return str;
		}
		if (char_set == null || char_set.length() == 0) {
			return str;
		}
		int len_ = str.length();
		if (strip_mode == StripMode.StripTrailing || strip_mode == StripMode.StripBoth) {
			while (len_ > 0) {
				if (_ContainsChar(char_set, str.charAt(len_ - 1))) {
					--len_;
				} else if (len_ == str.length()) {
					break;
				} else {
					str = str.substring(0, len_);
					break;
				}
			}
			if (len_ == 0) {
				str = "";
				return str;
			}
		}

		if (strip_mode == StripMode.StripLeading || strip_mode == StripMode.StripBoth) {
			len_ = str.length();
			int pos_ = 0;
			while (pos_ < len_) {
				if (_ContainsChar(char_set, str.charAt(pos_))) {
					++pos_;
				} else if (pos_ == 0) {
					break;
				} else {
					str = str.substring(pos_, len_);
					break;
				}
			}
			if (pos_ >= len_) {
				str = "";
			}
		}
		return str;
	}

	private static boolean _ContainsChar(String char_set, char c) {
		for (int j = 0; j < char_set.length(); ++j) {
			if (char_set.charAt(j) == c) {
				return true;
			}
		}
		return false;
	}
}
