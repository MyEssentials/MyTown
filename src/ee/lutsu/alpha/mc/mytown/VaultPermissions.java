package ee.lutsu.alpha.mc.mytown;

import java.lang.reflect.Method;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class VaultPermissions extends PermissionsBase{
    Class bukkit, server, servicesManager, registeredServiceProvider, permission, chat;
    Method getServer, getServicesManager, getRegistration, getProvider, getPrefix, getSuffix, has;
    Object serverObj, servicesManagerObj, registeredServiceProviderObj, registeredServiceProviderObjChat, permProvider, chatProvider;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean load(){
        Class[] hasParams = {String.class, String.class, String.class};
        Class[] getPrefixSuffixPerms = {String.class, String.class};
        Object[] hasParams2 = {String.class, String.class, String.class};
        Object[] getPrefixSuffixPerms2 = {String.class, String.class};
        
        try {
            bukkit = Class.forName("org.bukkit.Bukkit");
            server = Class.forName("org.bukkit.Server");
            servicesManager = Class.forName("org.bukkit.plugin.ServicesManager");
            registeredServiceProvider = Class.forName("org.bukkit.plugin.RegisteredServiceProvider");
            permission = Class.forName("net.milkbowl.vault.permission.Permission");     //TODO: Null pointer here Find a way to get the class... More research needed!
            chat = Class.forName("net.milkbowl.vault.chat.Chat");                       //TODO: Null pointer here Find a way to get the class... More research needed!
    
            getServer = bukkit.getDeclaredMethod("getServer");
            getServicesManager = server.getDeclaredMethod("getServicesManager");
            getRegistration = servicesManager.getDeclaredMethod("getRegistration", Class.class);
            getProvider = permission.getDeclaredMethod("getProvider");
            has = permission.getDeclaredMethod("has", hasParams);
            getPrefix = chat.getDeclaredMethod("getPlayerPrefix");
            getSuffix = chat.getDeclaredMethod("getPlayerSuffix");
            
            serverObj = getServer.invoke(null);
            servicesManagerObj = getServicesManager.invoke(serverObj);
            registeredServiceProviderObj = getRegistration.invoke(servicesManagerObj, permission);
            registeredServiceProviderObjChat = getRegistration.invoke(servicesManagerObj, chat);
            permProvider = getProvider.invoke(registeredServiceProviderObj, getPrefixSuffixPerms2);
            chatProvider = getProvider.invoke(registeredServiceProviderObjChat, getPrefixSuffixPerms2);
        } catch (Exception e) {
            return false;
        }
        
        return true;
        /*
        Code typically used in a bukkit plugin to hook into Vault API
        
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            _permission = permissionProvider.getProvider();
        }
        */
    }
    
    public boolean canAccess(EntityPlayer player, String node){
        return canAccess(player.getEntityName(), player.worldObj.provider.getDimensionName(), node);
    }
    
    public boolean canAccess(ICommandSender sender, String node){
        String worldName = "";
        EntityPlayer ep;
        
        for (WorldServer worldServer : MinecraftServer.getServer().worldServers){
            if ((ep = worldServer.getPlayerEntityByName(sender.getCommandSenderName()))==null){
                worldName = ep.worldObj.provider.getDimensionName();
            }
        }
        return canAccess(sender.getCommandSenderName(), worldName, node);
    }
    
    public boolean canAccess(Resident resident, String node){
        return canAccess(resident.name(), resident.onlinePlayer.worldObj.provider.getDimensionName(), node);
    }
    
    public boolean canAccess(String name, String world, String node){
        //String world, String player, String permission
        String[] perms = {
                world,
                name,
                node
        };
        try {
            Object o = has.invoke(permProvider, perms);
            Log.info("permProvider Returned: %s", o.toString());
            //return (boolean)o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String getPrefix(String player, String world){
        String[] args = {
                world,
                player
        };
        
        try {
            getPrefix.invoke(chat, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public String getPostfix(String player, String world){
        String[] args = {
                world,
                player
        };
        
        try {
            getSuffix.invoke(chat, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public String getOption(String player, String world, String node, String def){
        return "";
    }
    
    public String getOption(ICommandSender name, String node, String def){
        return "";
    }
}
