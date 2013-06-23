package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;

public class NoAccessException extends Exception
{
	public String node;
	public ICommandSender executor;
	
	public NoAccessException(ICommandSender executor, String node)
	{
		this.node = node;
		this.executor = executor;
	}
	
	@Override
	public String toString()
	{
		return Formatter.dollarToColorPrefix(getCustomizedMessage(Term.ErrCannotAccessCommand.toString()));
	}
	
	private String getCustomizedMessage(String def)
	{
		String message;
		String perm = node;
		int index;

		while ((index = perm.lastIndexOf(".")) != -1) {
			perm = perm.substring(0, index);

			message = Permissions.getOption(executor, "permission-denied-" + perm, null);
			if (message == null)
				continue;

			return message;
		}

		message = Permissions.getOption(executor, "permission-denied", null);
		if (message != null)
			return message;
		
		return def;
	}
}
