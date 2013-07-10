package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import ru.tehkode.permissions.IPermissionEntity;
import ru.tehkode.permissions.IPermissions;

public class PEXPermissions extends PermissionsBase{
    int pexOn = 0;
    IPermissions pex = null;
    
    public PEXPermissions(){
        name = "Forge PEX";
    }
    
    public boolean load(){
        try {
            Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
    
    private boolean pexAvailable()
    {
        if (pexOn == 0)
        {
            for (ModContainer cont : Loader.instance().getModList())
            {
                if (cont.getModId().equalsIgnoreCase("PermissionsEx"))
                {
                    if (cont.getMod() instanceof IPermissions)
                        pex = (IPermissions)cont.getMod();
                    
                    break;
                }
            }
            pexOn = pex == null ? 2 : 1;
        }
        
        return pexOn == 1;
    }
    
    public boolean canAccess(EntityPlayer name, String node)
    {
        return canAccess(name.username, String.valueOf(name.dimension), node);
    }
    
    public boolean canAccess(ICommandSender name, String node)
    {
        if (!(name instanceof EntityPlayer))
            return true;
        else
        {
            EntityPlayer pl = (EntityPlayer)name;
            return canAccess(pl.username, String.valueOf(pl.dimension), node);
        }
    }
    
    public boolean canAccess(String name, String world, String node)
    {
        if (!pexAvailable())
            throw new RuntimeException("PEX not found");
        
        return pex.has(name, node, world);
    }
    
    public String getPrefix(String player, String world)
    {
        if (!pexAvailable())
            return "";

        return pex.prefix(player, world);
    }
    
    public String getPostfix(String player, String world)
    {
        if (!pexAvailable())
            return "";

        return pex.suffix(player, world);
    }
    
    public String getOption(String player, String world, String node, String def)
    {
        if (!pexAvailable())
            return def;
        
        IPermissionEntity entity = pex.getUser(player);
        if (entity == null)
            return def;
        
        return entity.getOption(node, world, def);
    }
    
    public String getOption(ICommandSender name, String node, String def)
    {
        if (!(name instanceof EntityPlayer))
            return def;
        else
        {
            EntityPlayer pl = (EntityPlayer)name;
            return getOption(pl.username, String.valueOf(pl.dimension), node, def);
        }
    }
}