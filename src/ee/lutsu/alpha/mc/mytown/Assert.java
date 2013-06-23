package ee.lutsu.alpha.mc.mytown;

import ee.lutsu.alpha.mc.mytown.entities.Resident;
import net.minecraft.command.ICommandSender;

public class Assert 
{
	public static void Perm(ICommandSender cs, String node) throws NoAccessException
	{
		if (!Permissions.canAccess(cs, node))
			throw new NoAccessException(cs, node);
	}
}
