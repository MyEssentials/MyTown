package ee.lutsu.alpha.mc.mytown;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;

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

    @Override
    public boolean canBuild(EntityPlayer player, int dimension, int x, int y, int z) {
        Resident res = MyTownDatasource.instance.getOrMakeResident(player);
        Log.info("%s attempted to build in %s at (%s, %s, %s)", player.username, dimension, x, y, z);
        return res.canInteract(dimension, x, y, z, TownSettingCollection.Permissions.Build);
    }
}