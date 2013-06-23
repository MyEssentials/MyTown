package ee.lutsu.alpha.mc.mytown;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class Formatter 
{
	public static boolean formatChat = true;
	private static final Pattern color_pattern = Pattern.compile("(?i)\\$([0-9A-FK-OR])");
	
	public static String formatLevel(Level lvl)
	{
		if (lvl == Level.SEVERE)
			return "§4" + Term.SevereLevel;
		else if (lvl == Level.SEVERE)
			return "§6" + Term.WarningLevel;
		else
			return "§a" + Term.InfoLevel;
	}
	
	public static String townNotification(Level lvl, String msg)
	{
		return String.format("[%s%s§f][%s§f] %s%s", ChatChannel.Town.color, ChatChannel.Town.abbrevation, formatLevel(lvl), ChatChannel.Town.color, msg);
	}
	
	public static String formatCommand(String cmd, String args, String info, String color)
	{
		if (color == null)
			color = "f";
		
		String arg = args == null || args.length() == 0 ? "" : " §3" + args;

		return String.format("§%s    %s%s §7- %s", color, cmd, arg, info);
	}
	
	public static String formatGroupCommand(String cmd, String args, String info, String color)
	{
		if (color == null)
			color = "f";
		
		String arg = args == null || args.length() == 0 ? "" : " §3" + args;

		return String.format("§%s +  %s%s §7- %s", color, cmd, arg, info);
	}
	
	public static String formatAdminCommand(String cmd, String args, String info, String color)
	{
		if (color == null)
			color = "f";

		return String.format("§%s/%s %s §7%s §%s- %s", color, Term.TownAdmCommand, cmd, args, color, info);
	}
	
	public static String commandError(Level lvl, String msg)
	{
		return String.format("[%s§f] %s", formatLevel(lvl), msg);
	}
	
	public static String formatChat(Resident res, String line, ChatChannel channel, boolean emote)
	{
		if (!formatChat)
			return emote ? String.format("* %s %s", res.name(), line) : String.format("<%s> %s", res.name(), line);

		return (emote ? Term.EmoteFormat : Term.ChatFormat).toString()
				.replace("$color$", channel.color)
				.replace("$channel$", channel.abbrevation)
				.replace("$name$", res.name())
				.replace("$msg$", line)
				.replace("$prefix$", res.prefix())
				.replace("$postfix$", res.postfix());
	}

	public static String formatChatSystem(String line, ChatChannel channel)
	{
		if (!formatChat)
			return "<§4Sys:MyTown§f> " + line;

		return Term.ChatFormat.toString()
				.replace("$color$", channel.color)
				.replace("$channel$", channel.abbrevation)
				.replace("$name$", "§4MyTown")
				.replace("$msg$", line)
				.replace("$prefix$", "§f[§4Sys§f]")
				.replace("$postfix$", "");
	}
	
	public static String formatPrivMsg(Resident sender, Resident receiver, String line, boolean out)
	{
		return (out ? Term.PrivMsgFormatOut : Term.PrivMsgFormatIn).toString()
				.replace("$sname$", sender.name())
				.replace("$sprefix$", sender.prefix())
				.replace("$spostfix$", sender.postfix())
				
				.replace("$name$", receiver.name())
				.replace("$prefix$", receiver.prefix())
				.replace("$postfix$", receiver.postfix())
				
				.replace("$msg$", line);
	}
	
	public static String formatResidentName(Resident r)
	{
		if (r.isOnline())
			return String.format("§f%s§4*", r.formattedName());
		else
			return String.format("§f%s", r.formattedName());
	}
	
	public static String dollarToColorPrefix(String str)
	{
		if (str == null || str.equals(""))
			return "";

		Matcher m = color_pattern.matcher(str);
		String s = str;
		
		while (m.find())
		{
			String color = m.group(1).toLowerCase();
			s = m.replaceFirst("§" + color.charAt(0));
			m = m.reset(s);
		}
		
		return s + "§r";
	}
}
