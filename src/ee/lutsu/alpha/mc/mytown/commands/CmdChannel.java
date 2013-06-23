package ee.lutsu.alpha.mc.mytown.commands;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class CmdChannel extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return Term.ChannelCommand.toString();
	}
	
	@Override
    public List getCommandAliases()
    {
        return Arrays.asList(Term.ChannelCommandAliases.toString().split(" "));
    }
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
	{
		return true;
	}
	
	@Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
		return "/" + getCommandName() + " channel";
    }

	@Override
	public void processCommand(ICommandSender var1, String[] var2) 
	{
		if (!(var1 instanceof EntityPlayer))
			return;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)var1);
		if (var2.length != 1)
		{
			var1.sendChatToPlayer(Term.ChatListStart.toString());
			for(ChatChannel c : ChatChannel.values())
			{
				if (c.enabled)
					var1.sendChatToPlayer(Term.ChatListEntry.toString(c.color, c.name, c.color, c.abbrevation));
			}
		}
		else
		{
			ChatChannel ch = ChatChannel.parse(var2[0]);
			if (!ch.enabled)
				ch = ChatChannel.defaultChannel;

			if (!Permissions.canAccess(res, "mytown.chat.focus." + ch.name.toLowerCase()))
			{
				var1.sendChatToPlayer("§4You cannot focus to " + ch.name + " channel");
				return;
			}
			
			CmdPrivateMsg.stopLockChatWithNotify(res.onlinePlayer);
			
			if (ch != res.activeChannel)
			{
				res.setActiveChannel(ch);
				var1.sendChatToPlayer(Term.ChatSwitch.toString(ch.color, ch.abbrevation, ch.color, ch.name));
			}
			else
				var1.sendChatToPlayer(Term.ChatSwitchAlreadyIn.toString(ch.color, ch.abbrevation, ch.color, ch.name));
		}
	}

}
