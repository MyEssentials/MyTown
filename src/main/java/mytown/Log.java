package mytown;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cpw.mods.fml.common.FMLLog;

/**
 * Handles logging to files
 * 
 * @author Joe Goett
 */
public class Log {
	public Logger logger;
	private FileHandler fileHandler;
	private ConsoleHandler consoleHandler;

	public Log(String name, Logger parent) {
		logger = Logger.getLogger(name);
		logger.setParent(parent);
		consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new MyTownConsoleFormatter());
		logger.addHandler(consoleHandler);
		logger.setUseParentHandlers(false);
	}

	public Log(String name) {
		this(name, FMLLog.getLogger());
	}

	public Log(String name, Logger parent, File file) {
		this(name, parent);
		logToFile(file);
	}

	public Log(String name, Logger parent, String file) {
		this(name, parent, new File(file));
	}

	public Log(String name, String file) {
		this(name, FMLLog.getLogger(), file);
	}

	public Log(String name, File file) {
		this(name, FMLLog.getLogger(), file);
	}

	public Log(String name, Log parent) {
		this(name, parent.logger);
	}

	public Log(String name, Log parent, File file) {
		this(name, parent.logger, file);
	}

	public Log(String name, Log parent, String file) {
		this(name, parent.logger, file);
	}

	public void log(Level level, String msg, Throwable t, Object... args) {
		logger.log(level, String.format(msg, args), t);
	}

	public void log(Level level, String msg, Object... args) {
		log(level, msg, null, args);
	}

	public void fine(String msg, Object... args) {
		log(Level.FINE, msg, args);
	}

	public void finer(String msg, Object... args) {
		log(Level.FINER, msg, args);
	}

	public void finest(String msg, Object... args) {
		log(Level.FINEST, msg, args);
	}

	public void info(String msg, Object... args) {
		log(Level.INFO, msg, args);
	}

	public void severe(String msg, Object... args) {
		log(Level.SEVERE, msg, args);
	}

	public void severe(String msg, Throwable t, Object... args) {
		log(Level.SEVERE, msg, t, args);
	}

	public void warning(String msg, Object... args) {
		log(Level.WARNING, msg, args);
	}

	public void warning(String msg, Throwable t, Object... args) {
		log(Level.WARNING, msg, t, args);
	}

	public void debug(String msg, Object... args) {
		log(Level.ALL, msg, args);
	}

	public void debug(String msg, Throwable t, Object... args) {
		log(Level.ALL, msg, t, args);
	}

	public void logToFile(File file) {
		try {
			file.getParentFile().mkdirs();
			fileHandler = new FileHandler(file.getPath());
			fileHandler.setFormatter(new MyTownFileFormatter());
			logger.addHandler(fileHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void logToFile(String path) {
		File file = new File(path);
		logToFile(file);
	}

	public void setFormatter(java.util.logging.Formatter formatter) {
		if (fileHandler == null)
			return;
		fileHandler.setFormatter(formatter);
	}

	/**
	 * Static stuff!
	 */
	private static final Pattern color_pattern = Pattern.compile("(?i)§([0-9A-FK-OR])");
	public static boolean isUnix = isUnix();

	public static String consoleColors(String str) {
		if (str == null || str.equals("")) {
			return "";
		}

		Matcher m = color_pattern.matcher(str);
		String s = str;

		while (m.find()) {
			String color = m.group(1).toLowerCase();
			s = m.replaceFirst(replaceColor(color.charAt(0)));
			m = m.reset(s);
		}

		return s + replaceColor('r');
	}

	public static String removeColors(String str) {
		if (str == null || str.equals("")) {
			return "";
		}

		Matcher m = color_pattern.matcher(str);
		String s = str;

		while (m.find()) {
			s = m.replaceFirst("");
			m = m.reset(s);
		}

		return s + "";
	}

	private static String replaceColor(char color) {
		if (!isUnix) {
			return "";
		}

		if (color == 'r') {
			return "\033[0m";
		} else if (color < '0' || color > 'f' || color > '9' && color < 'a') {
			return "";
		}

		int c = color - (color >= 'a' ? 'a' - 10 : '0');
		boolean bold = c > 7;
		c = c % 8;

		if (c == 1) {
			c = 4;
		} else if (c == 3) {
			c = 6;
		} else if (c == 4) {
			c = 1;
		} else if (c == 6) {
			c = 3;
		}

		return String.format("\033[%s;%sm", c + 30, bold ? 1 : 22);
	}

	public static boolean isUnix() {
		String OS = System.getProperty("os.name").toLowerCase();
		return OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0;
	}

	static final class MyTownFileFormatter extends java.util.logging.Formatter {
		static final String LINE_SEPARATOR = System.getProperty("line.separator");
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public String format(LogRecord record) {
			StringBuilder msg = new StringBuilder();
			msg.append(this.dateFormat.format(Long.valueOf(record.getMillis())));
			Level lvl = record.getLevel();

			String name = lvl.getLocalizedName();
			if (name == null) {
				name = lvl.getName();
			}

			if ((name != null) && (name.length() > 0)) {
				msg.append(" [" + name + "] ");
			} else {
				msg.append(" ");
			}

			if (record.getLoggerName() != null) {
				msg.append("[" + record.getLoggerName() + "] ");
			} else {
				msg.append("[] ");
			}
			msg.append(removeColors(formatMessage(record)));
			msg.append(LINE_SEPARATOR);
			Throwable thr = record.getThrown();

			if (thr != null) {
				StringWriter thrDump = new StringWriter();
				thr.printStackTrace(new PrintWriter(thrDump));
				msg.append(thrDump.toString());
			}

			return msg.toString();
		}
	}

	static final class MyTownConsoleFormatter extends java.util.logging.Formatter {
		public String format(LogRecord record) {
			StringBuilder msg = new StringBuilder();
			if (record.getLoggerName() != null) {
				msg.append("[" + record.getLoggerName() + "] ");
			} else {
				msg.append("[] ");
			}

			msg.append(consoleColors(formatMessage(record)));
			Throwable thr = record.getThrown();

			if (thr != null) {
				StringWriter thrDump = new StringWriter();
				thr.printStackTrace(new PrintWriter(thrDump));
				msg.append(thrDump.toString());
			}

			return msg.toString();
		}
	}
}