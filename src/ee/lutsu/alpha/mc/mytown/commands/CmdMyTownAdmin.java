package ee.lutsu.alpha.mc.mytown.commands;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;
import ee.lutsu.alpha.mc.mytown.event.tick.WorldBorder;

public class CmdMyTownAdmin extends CommandBase {
    @Override
    public String getCommandName() {
        return Term.TownAdmCommand.toString();
    }

    @Override
    public List getCommandAliases() {
        return Arrays.asList(Term.TownAdmCommandAliases.toString().split(" "));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd");
        }
        return false;
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/" + getCommandName();
    }

    @Override
    public void processCommand(ICommandSender cs, String[] var2) {
        EntityPlayerMP p = null;
        if (cs instanceof EntityPlayerMP) {
            p = (EntityPlayerMP) cs;
        } else {
            return;
        }
        try {
            MyTownDatasource src = MyTownDatasource.instance;
            String color = "9";
            if (var2.length == 0 || var2[0].equals("?") || var2[0].equalsIgnoreCase(Term.CommandHelp.toString())) {
                MyTown.sendChatToPlayer(cs, Term.LineSeperator.toString());

                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdNew.toString(), Term.TownadmCmdNewArgs.toString(), Term.TownadmCmdNewDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdDelete.toString(), Term.TownadmCmdDeleteArgs.toString(), Term.TownadmCmdDeleteDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdSet.toString(), Term.TownadmCmdSetArgs.toString(), Term.TownadmCmdSetDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdRem.toString(), Term.TownadmCmdRemArgs.toString(), Term.TownadmCmdRemDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdExtra.toString(), Term.TownadmCmdExtraArgs.toString(), Term.TownadmCmdExtraDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdExtraRes.toString(), Term.TownadmCmdExtraResArgs.toString(), Term.TownadmCmdExtraResDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdReload.toString(), "", Term.TownadmCmdReloadDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdPerm.toString(), Term.TownadmCmdPermArgs.toString(), Term.TownadmCmdPermDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdClaim.toString(), Term.TownadmCmdClaimArgs.toString(), Term.TownadmCmdClaimDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdWipeDim.toString(), Term.TownadmCmdWipeDimArgs.toString(), Term.TownadmCmdWipeDimDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdResetFocusedChannels.toString(), "", Term.TownadmCmdResetFocusedChannelsDesc.toString(), color));
                MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdSnoopPrivateChat.toString(), "", Term.TownadmCmdSnoopPrivateChatDesc.toString(), color));
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdReload.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.reload")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                MyTown.instance.reload();
                MyTown.sendChatToPlayer(cs, Term.TownadmModReloaded.toString());
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdResetFocusedChannels.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.reschannels")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                int i = 0;
                for (Resident r : MyTownDatasource.instance.residents.values()) {
                    if (r.activeChannel != ChatChannel.defaultChannel) {
                        i++;
                        r.setActiveChannel(ChatChannel.defaultChannel);
                    }
                }
                MyTown.sendChatToPlayer(cs, String.format("§2Done. Resetted %s player channels to %s", i, ChatChannel.defaultChannel.name));
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdWipeDim.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.wipedim")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length < 2) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdWipeDim.toString(), Term.TownadmCmdWipeDimArgs.toString(), Term.TownadmCmdWipeDimDesc.toString(), color));
                } else if (var2.length != 3 || !var2[2].equalsIgnoreCase("ok")) {
                    MyTown.sendChatToPlayer(cs, "Add ' ok' to the end of the command if you are absolutely sure. §4There is no going back.");
                } else {
                    int dim = Integer.parseInt(var2[1]);
                    int i = MyTownDatasource.instance.deleteAllTownBlocksInDimension(dim);
                    MyTown.sendChatToPlayer(cs, String.format("§2Done. Deleted %s town blocks", i));
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdNew.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.new")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length != 3) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdNew.toString(), Term.TownadmCmdNewArgs.toString(), Term.TownadmCmdNewDesc.toString(), color));
                } else {
                    Resident r = src.getOrMakeResident(var2[2]);
                    Town t = new Town(var2[1], r, null);
                    MyTown.sendChatToPlayer(cs, Term.TownadmCreatedNewTown.toString(t.name(), r.name()));
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdDelete.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.delete")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length != 2) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdDelete.toString(), Term.TownadmCmdDeleteArgs.toString(), Term.TownadmCmdDeleteDesc.toString(), color));
                } else {
                    Town t = src.getTown(var2[1]);

                    if (t == null) {
                        throw new CommandException(Term.TownErrNotFound, var2[1]);
                    }

                    t.deleteTown();
                    MyTown.sendChatToPlayer(cs, Term.TownadmDeletedTown.toString(t.name()));
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdSet.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.set")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length < 4) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdSet.toString(), Term.TownadmCmdSetArgs.toString(), Term.TownadmCmdSetDesc.toString(), color));
                } else {
                    Town t = src.getTown(var2[1]);
                    if (t == null) {
                        throw new CommandException(Term.TownErrNotFound, var2[1]);
                    }

                    Rank rank = Rank.parse(var2[2]);

                    for (int i = 3; i < var2.length; i++) {
                        Resident r = src.getOrMakeResident(var2[i]);
                        if (r.town() != null) {
                            if (r.town() != t) {
                                r.town().removeResident(r); // unloads the
                                                            // resident
                                r = src.getOrMakeResident(var2[i]);
                            }
                        }

                        t.addResident(r);
                        t.setResidentRank(r, rank);
                    }
                    MyTown.sendChatToPlayer(cs, Term.TownadmResidentsSet.toString());
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdRem.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.rem")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length < 3) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdRem.toString(), Term.TownadmCmdRemArgs.toString(), Term.TownadmCmdRemDesc.toString(), color));
                } else {
                    Town t = src.getTown(var2[1]);
                    if (t == null) {
                        throw new CommandException(Term.TownErrNotFound, var2[1]);
                    }

                    for (int i = 2; i < var2.length; i++) {
                        Resident r = src.getOrMakeResident(var2[i]);
                        if (r.town() != null && r.town() == t) {
                            t.removeResident(r); // unloads the resident
                        }
                    }
                    MyTown.sendChatToPlayer(cs, Term.TownadmResidentsSet.toString());
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdExtra.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.extra")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length < 3) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdExtra.toString(), Term.TownadmCmdExtraArgs.toString(), Term.TownadmCmdExtraDesc.toString(), color));
                } else {
                    Town t = src.getTown(var2[1]);
                    int cnt = Integer.parseInt(var2[2]);
                    if (t == null) {
                        throw new CommandException(Term.TownErrNotFound, var2[1]);
                    }

                    t.setExtraBlocks(cnt);
                    MyTown.sendChatToPlayer(cs, Term.TownadmExtraSet.toString());
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdSnoopPrivateChat.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.snoop")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                boolean done = CmdPrivateMsg.snoopers.remove(MinecraftServer.getServer());
                if (!done) {
                    CmdPrivateMsg.snoopers.add(MinecraftServer.getServer());
                }

                MyTown.sendChatToPlayer(cs, "§aSnooping is now " + (done ? "§4off" : "§2on"));
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdExtraRes.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.extrares")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length != 4) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdExtraRes.toString(), Term.TownadmCmdExtraResArgs.toString(), Term.TownadmCmdExtraResDesc.toString(), color));
                } else {
                    Resident t = src.getResident(var2[1]);
                    String cmd = var2[2];
                    int cnt = Integer.parseInt(var2[3]);

                    if (t == null) {
                        throw new CommandException(Term.TownErrPlayerNotFound, var2[1]);
                    }

                    if (cmd.equalsIgnoreCase("add")) {
                        cnt = t.extraBlocks + cnt;
                    } else if (cmd.equalsIgnoreCase("sub")) {
                        cnt = t.extraBlocks - cnt;
                    }

                    t.setExtraBlocks(cnt);
                    MyTown.sendChatToPlayer(cs, Term.TownadmResExtraSet.toString());
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdPerm.toString())) {
                if (var2.length < 2) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdPerm.toString(), Term.TownadmCmdPermArgs.toString(), Term.TownadmCmdPermDesc.toString(), color));
                    return;
                }

                String node = var2[1];
                if (!node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && !node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString()) && !node.equalsIgnoreCase(Term.TownadmCmdPermArgsServer.toString()) && !node.equalsIgnoreCase(Term.TownadmCmdPermArgsWild.toString())
                        && !node.toLowerCase().startsWith(Term.TownadmCmdPermArgsWild2.toString().toLowerCase())) {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdPerm.toString(), Term.TownadmCmdPermArgs.toString(), Term.TownadmCmdPermDesc.toString(), color));
                    return;
                }

                if (var2.length < 3) // show
                {
                    if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.perm.show." + node)) {
                        MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                        return;
                    }
                    showPermissions(cs, node);
                } else {
                    String action = var2[2];
                    if (action.equalsIgnoreCase(Term.TownadmCmdPermArgs2Set.toString()) && var2.length > 3) {
                        if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.perm.set." + node + "." + var2[3])) {
                            MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                            return;
                        }
                        setPermissions(cs, node, var2[3], var2.length > 4 ? var2[4] : null);
                    } else if (action.equalsIgnoreCase(Term.TownadmCmdPermArgs2Force.toString())) {
                        if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.perm.force." + node)) {
                            MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                            return;
                        }

                        flushPermissions(cs, node, var2.length > 3 ? var2[3] : null);
                    } else {
                        MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdPerm.toString(), Term.TownadmCmdPermArgs.toString(), Term.TownadmCmdPermDesc.toString(), color));
                    }
                }
            } else if (var2[0].equalsIgnoreCase(Term.TownadmCmdClaim.toString())) {
                if (!ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.claim")) {
                    MyTown.sendChatToPlayer(cs, Term.ErrCannotAccessCommand.toString());
                    return;
                }

                if (var2.length < 2) // /ta claim townname [playername]
                                     // [x.y:x.y]
                {
                    MyTown.sendChatToPlayer(cs, Formatter.formatAdminCommand(Term.TownadmCmdClaim.toString(), Term.TownadmCmdClaimArgs.toString(), Term.TownadmCmdClaimDesc.toString(), color));
                    return;
                }

                Town t = null;

                if (var2[1].equals("none") || var2[1].equals("null")) {
                    t = null;
                } else {
                    t = MyTownDatasource.instance.getTown(var2[1]);
                    if (t == null) {
                        throw new CommandException(Term.TownErrNotFound, var2[1]);
                    }
                }

                Resident target_res = null;
                if (var2.length > 2) {
                    if (var2[2].equals("none") || var2[2].equals("null")) {
                        target_res = null;
                    } else {
                        target_res = MyTownDatasource.instance.getResident(var2[2]);
                        if (target_res == null) {
                            throw new CommandException(Term.TownErrPlayerNotFound);
                        }
                    }
                }

                int ax, az, bx, bz, dim;
                if (var2.length > 3) {
                    String[] sp = var2[3].split(":");
                    String[] sp2 = sp[0].split("\\.");

                    ax = bx = Integer.parseInt(sp2[0]);
                    az = bz = Integer.parseInt(sp2[1]);

                    if (sp.length > 1) {
                        sp2 = sp[1].split("\\.");

                        bx = Integer.parseInt(sp2[0]);
                        bz = Integer.parseInt(sp2[1]);
                    }
                    if (sp.length > 2) {
                        dim = Integer.parseInt(sp[2]);
                    } else {
                        Resident res = cs instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs) : null;
                        if (res == null) {
                            throw new CommandException(Term.ErrNotUsableByConsole); // console
                                                                                    // needs
                                                                                    // up
                                                                                    // to
                                                                                    // this
                        }

                        dim = res.onlinePlayer.dimension;
                    }
                } else {
                    Resident res = cs instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs) : null;
                    if (res == null) {
                        throw new CommandException(Term.ErrNotUsableByConsole);
                    }

                    ax = bx = res.onlinePlayer.chunkCoordX;
                    az = bz = res.onlinePlayer.chunkCoordZ;
                    dim = res.onlinePlayer.dimension;
                }

                StringBuilder sb = new StringBuilder();
                int nr = 0;

                for (int z = az; z <= bz; z++) {
                    for (int x = ax; x <= bx; x++) {
                        TownBlock b = MyTownDatasource.instance.getOrMakeBlock(dim, x, z);
                        if (b.town() == t && b.owner() == target_res) {
                            continue;
                        }

                        if (b.town() != null && b.town() != t) {
                            b.town().removeBlock(b);
                        }

                        if (t != null) {
                            b.sqlSetOwner(target_res);
                            t.addBlock(b, true);
                        }

                        nr++;
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(String.format("(%s,%s)", x, z));
                    }
                }

                MyTown.sendChatToPlayer(cs, Term.TownBlocksClaimed.toString(nr, sb.toString()));
            } else if (var2[0].equalsIgnoreCase("togglegen")) {
                WorldBorder.instance.genenabled = !WorldBorder.instance.genenabled;
                MyTown.instance.config.get("worldborder", "chunk-generator-enabled", WorldBorder.instance.genenabled, "Generate blocks?").set(WorldBorder.instance.genenabled);
                MyTown.instance.config.save();
                MyTown.sendChatToPlayer(cs, String.format("§aWorld gen is now %s", WorldBorder.instance.genenabled ? "§2ENABLED" : "§4DISABLED"));
            }
        } catch (CommandException ex) {
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
        } catch (Throwable ex) {
            Log.log(Level.WARNING, String.format("Admin command execution error by %s", cs), ex);
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
        }

    }

    private TownSettingCollection getPermNode(ICommandSender cs, String node) throws CommandException {
        Resident res = cs instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs) : null;
        TownSettingCollection set = null;
        if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString())) {
            if (res == null) {
                throw new CommandException(Term.ErrNotUsableByConsole);
            }

            TownBlock block = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
            if (block == null || block.town() == null) {
                throw new CommandException(Term.ErrPermPlotNotInTown);
            }

            set = block.town().settings;
        } else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
            if (res == null) {
                throw new CommandException(Term.ErrNotUsableByConsole);
            }

            TownBlock block = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
            if (block == null || block.town() == null) {
                throw new CommandException(Term.ErrPermPlotNotInTown);
            }

            set = block.settings;
        } else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsServer.toString())) {
            set = MyTown.instance.serverSettings;
        } else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsWild.toString())) {
            set = MyTown.instance.serverWildSettings;
        } else if (node.toLowerCase().startsWith(Term.TownadmCmdPermArgsWild2.toString().toLowerCase())) {
            int dim = Integer.parseInt(node.substring(Term.TownadmCmdPermArgsWild2.toString().length()));
            set = MyTown.instance.getWorldWildSettings(dim);
        } else {
            throw new CommandException(Term.ErrPermSettingCollectionNotFound, node);
        }

        return set;
    }

    private void showPermissions(ICommandSender sender, String node) throws CommandException {
        TownSettingCollection set = getPermNode(sender, node);
        String title = "";
        if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString())) {
            title = "town '" + ((Town) set.tag).name() + "' (default for residents)";
        } else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
            TownBlock block = (TownBlock) set.tag;
            title = String.format("the plot @ dim %s, %s,%s owned by '%s'", block.worldDimension(), block.x(), block.z(), block.ownerDisplay());
        } else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsServer.toString())) {
            title = "the server (default for towns)";
        } else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsWild.toString())) {
            title = "the wild (default for world wilds)";
        } else if (node.toLowerCase().startsWith(Term.TownadmCmdPermArgsWild2.toString().toLowerCase())) {
            String dim = node.substring(Term.TownadmCmdPermArgsWild2.toString().length());
            title = "the wild in dimension " + dim;
        }

        set.show(sender, title, node, true);
    }

    private void flushPermissions(ICommandSender sender, String node, String perm) throws CommandException {
        TownSettingCollection set = getPermNode(sender, node);
        if (set.childs.size() < 1) {
            throw new CommandException(Term.ErrPermNoChilds);
        }

        set.forceChildsToInherit(perm);
        MyTown.sendChatToPlayer(sender, Term.PermForced.toString(node, perm == null || perm.equals("") ? "all" : perm));
        // sender.sendChatToPlayer(Term.PermForced.toString(node, perm == null
        // || perm.equals("") ? "all" : perm));
    }

    private void setPermissions(ICommandSender sender, String node, String key, String val) throws CommandException {
        TownSettingCollection set = getPermNode(sender, node);

        set.setValue(key, val);

        showPermissions(sender, node);
        MyTown.sendChatToPlayer(sender, Term.PermSetDone.toString(key, node));
        // sender.sendChatToPlayer(Term.PermSetDone.toString(key, node));
    }
}
