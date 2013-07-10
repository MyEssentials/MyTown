package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public abstract class PermissionsBase {
    public boolean loaded = false;
    public String name = "Unknown";
    
    public abstract boolean load();
    
    //public abstract boolean canAccess(EntityPlayer name, String node);
    
    //public abstract boolean canAccess(ICommandSender name, String node);
    
    //public abstract boolean canAccess(Resident name, String node);
    
    public abstract boolean canAccess(String name, String world, String node);
    
    public abstract String getPrefix(String player, String world);
    
    public abstract String getPostfix(String player, String world);
    
    public abstract String getOption(String player, String world, String node, String def);
    
    public abstract String getOption(ICommandSender name, String node, String def);
}