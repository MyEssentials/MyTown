package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Town;

public class CmdOnline extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return Term.OnlineCommand.toString();
	}
	
	@Override
    public List getCommandAliases()
    {
        return Arrays.asList(Term.OnlineCommandAliases.toString().split(" "));
    }
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
	{
		return Permissions.canAccess(par1ICommandSender, "mytown.ecmd.online");
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) 
	{
		ArrayList<Resident> sorted = new ArrayList<Resident>(MyTownDatasource.instance.getOnlineResidents());
		
		Collections.sort(sorted, new Comparator<Resident>()
		{
			@Override
			public int compare(Resident arg0, Resident arg1)
			{
				return arg0.name().compareToIgnoreCase(arg1.name());
			}
		});

		StringBuilder sb = new StringBuilder();
		sb.append(Term.OnlineCmdListStart.toString(sorted.size(), ""));
		int i = 0;
		
		for (Resident e : sorted)
		{
			String n = e.formattedName();
			if (i > 0)
				sb.append("§f, ");

			i++;
			sb.append(n);
		}
		
		cs.sendChatToPlayer(sb.toString());
	}
}
