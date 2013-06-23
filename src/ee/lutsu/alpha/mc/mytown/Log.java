package ee.lutsu.alpha.mc.mytown;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log 
{
	private static final Pattern color_pattern = Pattern.compile("(?i)§([0-9A-FK-OR])");
	public static Logger mclog = Logger.getLogger("Minecraft");
	public static boolean isUnix = isUnix();
	

	public static void info(String msg, Object ... paras)
	{
		log(Level.INFO, msg, paras);
	}
	
	public static void warning(String msg, Object ... paras)
	{
		log(Level.WARNING, msg, paras);
	}
	
	public static void severe(String msg, Object ... paras)
	{
		log(Level.SEVERE, msg, paras);
	}
	
	public static void severe(String msg, Throwable t, Object ... paras)
	{
		log(Level.SEVERE, msg, t, paras);
	}
	
	public static void log(Level l, String msg, Object ... paras)
	{
		mclog.log(l, consoleColors(String.format("§7[§a%s§7]%s", MyTown.MOD_NAME, String.format(msg, paras))));
	}

	public static void log(Level l, String msg, Throwable t, Object ... paras)
	{
		mclog.log(l, consoleColors(String.format("§7[§a%s§7]%s", MyTown.MOD_NAME, String.format(msg, paras))), t);
	}
	
	public static void direct(String msg)
	{
		mclog.log(Level.INFO, consoleColors(msg));
	}
	
	public static String consoleColors(String str)
	{
		if (str == null || str.equals(""))
			return "";

		Matcher m = color_pattern.matcher(str);
		String s = str;
		
		while (m.find())
		{
			String color = m.group(1).toLowerCase();
			s = m.replaceFirst(replaceColor(color.charAt(0)));
			m = m.reset(s);
		}
		
		return s + replaceColor('r');
	}
	
	private static String replaceColor(char color)
	{
		if (!isUnix)
			return "";
		
		if (color == 'r')
			return "\033[0m";
		else if (color < '0' || color > 'f' || (color > '9' && color < 'a'))
			return "";
		
		int c = color - (color >= 'a' ? 'a' - 10 : '0');
		boolean bold = c > 7;
		c = c % 8;
		
		if (c == 1) // blue
			c = 4;
		else if (c == 3) // cyan
			c = 6;
		else if (c == 4) // red
			c = 1;
		else if (c == 6) // yellow
			c = 3;
		
		return String.format("\033[%s;%sm", c + 30, bold ? 1 : 22);
	}
	
	public static boolean isUnix() 
	{ 
		String OS = System.getProperty("os.name").toLowerCase();
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}

}
