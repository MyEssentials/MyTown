package ee.lutsu.alpha.mc.mytown.commands;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

import net.minecraft.command.CommandServerMessage;
import net.minecraft.command.CommandServerSay;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CmdPrivateMsg extends CommandServerMessage
{
	public static Map<EntityPlayer, EntityPlayer> lastMessages = Maps.newHashMap();
	public static Map<EntityPlayer, EntityPlayer> chatLock = Maps.newHashMap();
	public static List<ICommandSender> snoopers = Lists.newArrayList();

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs)
    {
    	return cs instanceof EntityPlayer && Permissions.canAccess(cs, "mytown.ecmd.msg");
    }
    
    public void processCommand(ICommandSender cs, String[] arg)
    {
    	if (arg.length > 0) 
    	{
            EntityPlayerMP target = func_82359_c(cs, arg[0]);

            if (target == null)
                throw new PlayerNotFoundException();

            if (arg.length > 1) // send chat
            {
	            String msg = func_82361_a(cs, arg, 1, !(cs instanceof EntityPlayer));
	            sendChat((EntityPlayer)cs, target, msg);
            }
            else // lock mode
            {
            	lockChatWithNotify((EntityPlayer)cs, target);
            }
        }
    	else if (chatLock.get((EntityPlayer)cs) != null)
    		stopLockChatWithNotify((EntityPlayer)cs);
    	else
    	{
    		cs.sendChatToPlayer("§4Usage /tell [target] [msg]");
    	}
    }
    
    public static void stopLockChatWithNotify(EntityPlayer sender)
    {
    	EntityPlayer pl = chatLock.remove(sender);
    	
    	if (pl != null)
    		sender.sendChatToPlayer("§dStopped chatting with " + Resident.getOrMake(pl).formattedName());
    }
    
    public static void lockChatWithNotify(EntityPlayer sender, EntityPlayer target)
    {
    	chatLock.put(sender, target);
    	sender.sendChatToPlayer("§dNow chatting with " + Resident.getOrMake(target).formattedName());
    }
    
    public static void sendChat(EntityPlayer sender, EntityPlayer target, String msg)
    {
    	if (sender == null || target == null || msg == null)
    		return;
    	
    	lastMessages.put(target, sender);
    	
		if (Permissions.canAccess(sender, "mytown.chat.allowcolors"))
			msg = Formatter.dollarToColorPrefix(msg);
		
    	for (ICommandSender cs : snoopers)
    	{
    		Log.direct(String.format("§7[%s §7-> %s§7] %s",
    				Resident.getOrMake(sender).formattedName(),
    				Resident.getOrMake(target).formattedName(),
    				msg));
    	}
    	
        if (!Formatter.formatChat)
        {
        	sender.sendChatToPlayer("\u00a77\u00a7o" + sender.translateString("commands.message.display.outgoing", new Object[] {target.getCommandSenderName(), msg}));
        	target.sendChatToPlayer("\u00a77\u00a7o" + target.translateString("commands.message.display.incoming", new Object[] {sender.getCommandSenderName(), msg}));
        }
        else
        {
        	sender.sendChatToPlayer(Formatter.formatPrivMsg(Resident.getOrMake(sender), Resident.getOrMake(target), msg, true));
        	target.sendChatToPlayer(Formatter.formatPrivMsg(Resident.getOrMake(sender), Resident.getOrMake(target), msg, false));
        }
    }
}
