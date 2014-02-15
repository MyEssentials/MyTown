package mytown.cmd.sub.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import mytown.Constants;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdDumpDB extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "dump";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.dump";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		canUse(sender);
		try {
			String file = Constants.CONFIG_FOLDER + Constants.DEFAULT_DB_DUMP_FILE;
			if (args.length == 1)
				file = Constants.CONFIG_FOLDER + args[0];
			File dstFile = new File(file);
			FileOutputStream outStream = new FileOutputStream(dstFile);
			MyTownDatasource.instance.dumpData(new OutputStreamWriter(outStream, "UTF-8"));
			outStream.close();
			MyTown.sendChatToPlayer(sender, "Database dumped to " + file);
		} catch (Exception e) {
			MyTown.sendChatToPlayer(sender, "Failed to dump database to file. " + e.getMessage());
		}
	}
}