package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class FallbackPermissions extends PermissionsBase {
    public FallbackPermissions(){
        name = "Fallback";
    }
    
    public boolean load(){
        return true;
    }
    
    public boolean canAccess(EntityPlayer name, String node){
        return false;
    }
    
    public boolean canAccess(ICommandSender name, String node){
        return false;
    }
    
    public boolean canAccess(Resident name, String node){
        return false;
    }
    
    public boolean canAccess(String name, String world, String node){
        return false;
    }
    
    public String getPrefix(String player, String world){
        return "";
    }
    
    public String getPostfix(String player, String world){
        return "";
    }
    
    public String getOption(String player, String world, String node, String def){
        return "";
    }
    
    public String getOption(ICommandSender name, String node, String def){
        return "";
    }
}