package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;

@SuppressWarnings("serial")
public class NoAccessException extends Exception {
    public String node;
    public ICommandSender executor;

    public NoAccessException(ICommandSender executor, String node) {
        this.node = node;
        this.executor = executor;
    }

    @Override
    public String toString() {
        return Formatter.applyColorCodes(Term.ErrCannotAccessCommand.toString());
    }
}
