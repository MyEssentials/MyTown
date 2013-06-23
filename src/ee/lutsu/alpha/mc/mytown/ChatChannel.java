package ee.lutsu.alpha.mc.mytown;

import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;

public enum ChatChannel
{
	Global("Global", "G", "§f"),
	Town("Town", "TC", "§a"),
	Nation("Nation", "NC", "§2"),
	Local("Local", "L", "§e"),
	Trade("Trade", "TR", "§7"),
	Help("Help", "H", "§b"),
	;
	
	public String name;
	public String abbrevation;
	public String color;
	public boolean enabled = true;
	public String inLineSwitch;
	
	public static int localChatDistance = 160;
	public static ChatChannel defaultChannel = Global;
	
	ChatChannel(String name, String abbrevation, String color)
	{
		this.name = name;
		this.abbrevation = abbrevation;
		this.color = color;
	}
	
	public static ChatChannel parse(String ch)
	{
        for (ChatChannel type : values()) {
            if (type.toString().equalsIgnoreCase(ch) || type.name.equalsIgnoreCase(ch) || type.abbrevation.equalsIgnoreCase(ch)) {
                return type;
            }
        }
        return defaultChannel;
	}
	
	public void load(String val)
	{
		if (val == null || val.trim().length() < 1)
			return;
		
		String[] parts = val.split(";");
		
		if (parts.length > 0)
			enabled = parts[0].equalsIgnoreCase("1");
		if (parts.length > 1)
			name = parts[1];
		if (parts.length > 2)
			abbrevation = parts[2];
		if (parts.length > 3)
			color = parts[3];
		if (parts.length > 4)
			inLineSwitch = parts[4];
	}
}