package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.sperion.forgeperms.Log;

import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class ArsMagica2 extends ProtBase {
	public static ArsMagica2 instance = new ArsMagica2();
	
	Class<?> clSpellBase, clSpellUtils;
	Field clSpellUtilsInstance;
	
    @Override
    public void load() throws Exception {
    	clSpellBase = Class.forName("am2.items.SpellBase");
    	clSpellUtils = Class.forName("am2.spell.SpellUtils");
    	clSpellUtilsInstance = clSpellUtils.getField("instance");
    }

    public boolean isEntityInstance(Item item) {
    	return clSpellBase.isInstance(item);
    }

    public String update(Resident r, Item tool, ItemStack item) throws Exception {
    	Log.info("SpellBase!");
    	return null;
    }

    @Override
    public boolean loaded() {
    	return clSpellBase != null;
    }
    
	@Override
	public String getMod() {
		return "Ars Magics 2";
	}

	@Override
	public String getComment() {
		return "Protects against Ars Magica 2 Spells";
	}
}