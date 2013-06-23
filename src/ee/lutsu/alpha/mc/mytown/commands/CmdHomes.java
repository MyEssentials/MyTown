package ee.lutsu.alpha.mc.mytown.commands;

import java.util.List;
import java.util.logging.Level;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.SavedHome;
import ee.lutsu.alpha.mc.mytown.entities.SavedHomeList;

public class CmdHomes extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "homes";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs)
	{
		return cs instanceof EntityPlayerMP && Permissions.canAccess(cs, "mytown.ecmd.homes");
	}
	
	@Override
	public String getCommandUsage(ICommandSender cs) 
	{
		return getCommandName() + " [loc] - Shows the player homes [with location]";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) 
	{
		EntityPlayerMP pl = (EntityPlayerMP)cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);
		
		try 
		{
			if (!res.home.hasHomes())
				cs.sendChatToPlayer(Term.HomeCmdNoHomes.toString());
			else
			{
				if (args.length == 1 && args[0].equalsIgnoreCase("loc"))
				{
					cs.sendChatToPlayer(Term.HomeCmdHomesTitle.toString(""));
					if (SavedHomeList.defaultIsBed && pl.getBedLocation() != null)
					{
						SavedHome s = SavedHome.fromBed(pl);
						cs.sendChatToPlayer(Term.HomeCmdHomesUnaccessibleItem2.toString("default", s.dim, (int)s.x, (int)s.y, (int)s.z));
					}
					
					for (SavedHome h : res.home)
						cs.sendChatToPlayer(Term.HomeCmdHomesItem2.toString(h.name, h.dim, (int)h.x, (int)h.y, (int)h.z));
				}
				else
				{
					List<String> items = Lists.newArrayList();
					if (SavedHomeList.defaultIsBed && pl.getBedLocation() != null)
						items.add(Term.HomeCmdHomesUnaccessibleItem.toString("default"));
					
					for (SavedHome h : res.home)
						items.add(Term.HomeCmdHomesItem.toString(h.name));
					
					cs.sendChatToPlayer(Term.HomeCmdHomesTitle.toString(Joiner.on(", ").join(items)));
				}
			}
		} 
		catch (Throwable ex)
		{
			Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
			cs.sendChatToPlayer(Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}
}
