package ee.lutsu.alpha.mc.mytown;

import ee.lutsu.alpha.mc.mytown.entities.Resident;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class Assert 
{
	public static void Perm(ICommandSender cs, String node) throws NoAccessException
	{
		//if (!Permissions.canAccess(cs, node))
	    EntityPlayer p = (EntityPlayer)cs;
	    if (!MyTown.instance.perms.canAccess(p.username, p.worldObj.provider.getDimensionName(), node))
			throw new NoAccessException(cs, node);
	}
}
