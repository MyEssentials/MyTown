package ee.lutsu.alpha.mc.mytown;

import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class APIHandler extends API {
    @Override
    public boolean canAttack(EntityPlayer player, Entity e) {
        Resident res = MyTownDatasource.instance.getOrMakeResident(player);
        return res.canAttack(e);
    }

    @Override
    public boolean canEnter(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z) {
        Resident res = MyTownDatasource.instance.getOrMakeResident(player);
        return res.canInteract(dimension, x, yFrom, yTo, z, TownSettingCollection.Permissions.Enter);
    }

    @Override
    public boolean canLoot(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z) {
        Resident res = MyTownDatasource.instance.getOrMakeResident(player);
        return res.canInteract(dimension, x, yFrom, yTo, z, TownSettingCollection.Permissions.Loot);
    }

    @Override
    public boolean canAccess(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z) {
        Resident res = MyTownDatasource.instance.getOrMakeResident(player);
        return res.canInteract(dimension, x, yFrom, yTo, z, TownSettingCollection.Permissions.Access);
    }

    @Override
    public boolean canBuild(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z) {
        Resident res = MyTownDatasource.instance.getOrMakeResident(player);
        return res.canInteract(dimension, x, yFrom, yTo, z, TownSettingCollection.Permissions.Build);
    }

}
