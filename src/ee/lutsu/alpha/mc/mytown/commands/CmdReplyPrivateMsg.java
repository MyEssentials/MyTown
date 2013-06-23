package ee.lutsu.alpha.mc.mytown.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandServerMessage;
import net.minecraft.command.CommandServerSay;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CmdReplyPrivateMsg extends CommandBase
{
	@Override
    public List getCommandAliases()
    {
        return Arrays.asList(new String[] {"r"});
    }

    @Override
    public String getCommandName()
    {
        return "reply";
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs)
    {
    	return cs instanceof EntityPlayer && Permissions.canAccess(cs, "mytown.ecmd.reply");
    }
    
    @Override
    public void processCommand(ICommandSender cs, String[] arg)
    {
        EntityPlayer pl = CmdPrivateMsg.lastMessages.get((EntityPlayer)cs);
        
        if (pl == null)
        	cs.sendChatToPlayer("§4Noone to reply to");
        else
        {
	    	if (arg.length > 0)
	    	{
	            CmdPrivateMsg.sendChat((EntityPlayer)cs, pl, func_82360_a(cs, arg, 0));
	        }
	    	else
			{
	    		CmdPrivateMsg.lockChatWithNotify((EntityPlayer)cs, pl);
			}
        }
    }
    
    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        return getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
    }
}
