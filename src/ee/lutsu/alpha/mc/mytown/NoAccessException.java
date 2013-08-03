package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;

import com.sperion.forgeperms.ForgePerms;

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
        return Formatter.dollarToColorPrefix(Term.ErrCannotAccessCommand.toString());
    }

    private String getCustomizedMessage(String def) {
        String message;
        String perm = node;
        int index;

        while ((index = perm.lastIndexOf(".")) != -1) {
            perm = perm.substring(0, index);

            message = ForgePerms.getPermissionsHandler().getOption(executor,
                    "permission-denied-" + perm, null);
            // TODO Permissions.getOption(executor, "permission-denied-" + perm,
            // null);
            if (message == null) {
                continue;
            }

            return message;
        }

        // TODO message = Permissions.getOption(executor, "permission-denied",
        // null);
        message = ForgePerms.getPermissionsHandler().getOption(executor,
                "permission-denied", null);
        if (message != null) {
            return message;
        }

        return def;
    }
}
