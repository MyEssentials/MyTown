package ee.lutsu.alpha.mc.mytown;

public class CommandException extends Exception 
{
	public Term errorCode;
	public Object[] args;
	
	public CommandException(Term err, Object ... pArgs)
	{
		super(err.toString());
		errorCode = err;
		args = pArgs;
	}
}
