package mytown.commands.subcommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import mytown.Assert;
import mytown.CommandException;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Setting;
import mytown.entities.SettingCollection;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.sperion.forgeperms.ForgePerms;

public class MyTownResident {
    public static List<String> getAutoComplete(ICommandSender cs, String[] args) {
        ArrayList<String> list = new ArrayList<String>();

        if (!(cs instanceof EntityPlayer)) {
            return list;
        }

        Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs);
        if (res.town() == null) {
            return list;
        }

        if (args.length == 1) {
            list.add(Term.TownCmdLeave.toString());
            list.add(Term.TownCmdOnline.toString());
            list.add(Term.TownCmdPerm.toString()); // /t perm town|res|plot [force|(set key [val])]
        } else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString())) {
            list.add(Term.TownCmdPermArgsTown.toString());
            list.add(Term.TownCmdPermArgsResident.toString());
            list.add(Term.TownCmdPermArgsPlot.toString());
        } else if (args.length == 3 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString())) {
            list.add(Term.TownCmdPermArgs2Set.toString());
            list.add(Term.TownCmdPermArgs2Force.toString());
        } else if (args.length == 4 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString())) {
        	Iterator<Setting> it = MyTown.instance.serverSettings.getSettings().values().iterator();
        	while(it.hasNext()){
        		list.add(it.next().getName());
        	}
        } else if (args.length == 5 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString()) && args[2].equalsIgnoreCase(Term.TownCmdPermArgs2Set.toString())) {
            Setting s = MyTown.instance.serverSettings.getSetting(args[3]);
            if (s != null) {
                Class<?> c = s.getType();

                if (c == boolean.class) {
                    list.add("yes");
                    list.add("no");
                }
            }
        }

        return list;
    }

    public static boolean handleCommand(ICommandSender cs, String[] args) throws CommandException, NoAccessException {
        if (!(cs instanceof EntityPlayer)) {
            return false;
        }

        Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs);
        if (res.town() == null) {
            return false;
        }

        boolean handled = false;
        String color = "2";
        if (args.length < 1) {
            Assert.Perm(cs, "mytown.cmd.info");
            handled = true;

            res.town().sendTownInfo(res.onlinePlayer);
        } else {
            if (args.length == 1 && (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString()))) {
                handled = true;
                MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdLeave.toString(), "", Term.TownCmdLeaveDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdOnline.toString(), "", Term.TownCmdOnlineDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
            } else if (args[0].equalsIgnoreCase(Term.TownCmdLeave.toString())) {
                Assert.Perm(cs, "mytown.cmd.leave");
                handled = true;

                if (res.rank() == Rank.Mayor) {
                    throw new CommandException(Term.TownErrMayorsCantLeaveTheTown);
                }

                Town t = res.town();
                t.sendNotification(Level.INFO, Term.TownPlayerLeft.toString(res.name()));
                t.removeResident(res);
            } else if (args[0].equalsIgnoreCase(Term.TownCmdOnline.toString())) {
                Assert.Perm(cs, "mytown.cmd.online");
                handled = true;

                Town t = res.town();

                StringBuilder sb = new StringBuilder();
                for (Resident r : t.residents()) {
                    if (!r.isOnline()) {
                        continue;
                    }

                    if (sb.length() > 0) {
                        sb.append("§2, ");
                    }

                    sb.append(Formatter.formatResidentName(r));
                }

                MyTown.sendChatToPlayer(cs, Term.TownPlayersOnlineStart.toString(sb.toString()));
            } else if (args[0].equalsIgnoreCase(Term.TownCmdPerm.toString())) {  // /t perm [town|res|plot] [core|out|town|nation|friends] [force|(set key [val])]
                handled = true;
                if (args.length < 3) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
                    return true;
                }

                String type = args[1];
                if (!type.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && !type.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString()) && !type.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
                }
                
                if (args.length == 3){
                	showSettingCollection(cs, type, args[2]);
                } else {
                	String action = args[3];
					if (action.equalsIgnoreCase(Term.TownCmdPermArgs2Set.toString()) && args.length > 3) {
						setPermission(cs, type, args[2], args[4], args[5]);
					} else if (action.equalsIgnoreCase(Term.TownCmdPermArgs2Force.toString())) {
						Assert.Perm(cs, "mytown.cmd.perm.show." + type + "." + args[2] + "." + args[4]);
						flushPermissions(cs, type, args[2], args[4], args[5]);
					} else {
						MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
					}
                }
            }
        }

        return handled;
    }
    
    private static SettingCollection getCollection(ICommandSender sender, String type, String collection) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident(sender.getCommandSenderName());
		SettingCollection setCollection = null;
    	if (type.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) || type.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())){
    		Town town = res.town();
    		if (town == null){
              throw new CommandException(Term.ErrPermYouDontHaveTown);
    		}
    		
    		if (type.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString())){
    			setCollection = town.settings.get(collection);
    		} else{
				TownBlock block = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
				if (block == null || block.town() == null) {
					throw new CommandException(Term.ErrPermPlotNotInTown);
				}
				
				if (block.town() != res.town()) {
					throw new CommandException(Term.ErrPermPlotNotInYourTown);
				}
				setCollection = block.settings.get(collection);
    		}
    	} else if(type.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString())){
    		setCollection = res.settings.get(collection);
    	} else{
            throw new CommandException(Term.ErrPermSettingCollectionNotFound, collection);
      	}
    	
    	return setCollection;
    }

    private static void flushPermissions(ICommandSender sender, String type, String collection, String key, String value) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident(sender.getCommandSenderName());
        SettingCollection set = getCollection(sender, type, collection);

        if (set.getChildren().size() < 1) {
            throw new CommandException(Term.ErrPermNoChilds);
        }

        if (type.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && res.rank() == Rank.Resident) {
            throw new CommandException(Term.ErrPermRankNotEnough);
        }

        set.forceChildsToInherit(value);
        MyTown.sendChatToPlayer(sender, Term.PermForced.toString(type, value == null || value.equals("") ? "all" : value));
    }
    
    private static void setPermission(ICommandSender sender, String type, String collection, String key, String value) throws CommandException, NoAccessException {
		Assert.Perm(sender, "mytown.cmd.perm.set." + type + "." + collection + "." + key + "." + value);
		SettingCollection setCollection = getCollection(sender, type, collection);
		setCollection.getSetting(key).setValue(value);
		showSettingCollection(sender, type, collection);
    }
    
    private static void showSettingCollection(ICommandSender sender, String type, String collection) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident(sender.getCommandSenderName());
		SettingCollection setCollection = getCollection(sender, type, collection);
		if (setCollection == null) return;
		String title = "";
    	
    	Iterator<Setting> it = setCollection.getSettings().values().iterator();
    	Setting set;
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("§6-- §e%s permissions for %s§6 --\n", collection, title));
        
        while (it.hasNext()){
        	set = it.next();
            if (ForgePerms.getPermissionManager().canAccess(res.onlinePlayer.getCommandSenderName(), res.onlinePlayer.worldObj.provider.getDimensionName(), "mytown.cmd.perm.show." + type + "." + collection + "." + set.getName())) {
            	builder.append(set.getDisplay());
            }
        }
        builder.append("§6----------------------------");

        MyTown.sendChatToPlayer(sender, builder.toString());
    }
}
