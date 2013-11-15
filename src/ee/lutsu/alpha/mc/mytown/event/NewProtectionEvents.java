package ee.lutsu.alpha.mc.mytown.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class NewProtectionEvents {
    @ForgeSubscribe(priority = EventPriority.HIGHEST)
    public void entityAttacked(LivingAttackEvent event){
        EntityLivingBase attacked = event.entityLiving;
        DamageSource source = event.source;
        
        if (source.getSourceOfDamage() != null && source.getSourceOfDamage() instanceof EntityPlayer){
            Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)source.getSourceOfDamage());
            if (!res.canAttack(attacked)){
                event.setCanceled(true);
            }
        }
    }
    
    /*
    @ForgeSubscribe(priority = EventPriority.HIGHEST)
    public void blockBroken(BlockEvent.BreakEvent event){
        EntityPlayer player = event.getPlayer();
        
        if (player != null){
            Resident res = MyTownDatasource.instance.getOrMakeResident(player);
            if (!res.canInteract(event.world.provider.dimensionId, event.x, event.y, event.z, Permissions.Build)){
                event.setCanceled(true);
            }
        }
    }
    */
}