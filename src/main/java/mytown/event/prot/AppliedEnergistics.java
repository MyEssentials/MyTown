package mytown.event.prot;

import mytown.entities.Resident;
import mytown.entities.TownSettingCollection;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.lang.reflect.Field;
import java.util.List;

public class AppliedEnergistics extends ProtBase {
	public static AppliedEnergistics instance = new AppliedEnergistics();

	Class<?> clTinyTNT;

	@Override
	public void load() throws Exception {
		clTinyTNT = Class.forName("appeng.compatabilty.tnt.EntityTinyTNTPrimed");
	}

	@Override
	public boolean loaded() {
		return clTinyTNT != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return e instanceof EntityTNTPrimed;
	}

	@Override
	public String update(Entity e) throws Exception {

		return null;
	}

	@Override
	public String getMod() {
		return "Applied Energistics";
	}

	@Override
	public String getComment() {
		return "Add protection against TinyTNT";
	}
}
