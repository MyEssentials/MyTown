package ee.lutsu.alpha.mc.mytown.ext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

import ru.tehkode.permissions.IPermissions;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class Mffs 
{
	private static int checked = 0;
	private static Object mod = null;
	private static Field fAdmin;
	
	public static boolean check()
	{
		if (checked == 0)
		{
			try
			{
				for (ModContainer cont : Loader.instance().getModList())
				{
					if (cont.getModId().equalsIgnoreCase("ModularForceFieldSystem"))
					{
						mod = cont.getMod();
						fAdmin = mod.getClass().getDeclaredField("Admin");
						break;
					}
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				mod = null;
			}
			checked = mod == null ? 2 : 1;
		}
		
		return checked == 1;
	}
	
	public static void grantAdminBypass(String user)
	{
		List<String> list = getAdminBypass();
		
		for (String s : list)
		{
			if (s.equalsIgnoreCase(user))
				return;
		}
		
		list.add(user);
		setAdminBypass(list);
	}
	
	public static void removeAdminBypass(String user)
	{
		List<String> list = getAdminBypass();
		List<String> toRemove = new ArrayList<String>();

		for (String s : list)
		{
			if (s.equalsIgnoreCase(user))
				toRemove.add(s);
		}
		
		list.removeAll(toRemove);
		setAdminBypass(list);
	}

	public static void setAdminBypass(List<String> val)
	{
		if (!check())
			throw new RuntimeException("MFFS not found");
		
		try
		{
			String v = Joiner.on(";").join(val);
			fAdmin.set(null, v);
		}
		catch (Throwable e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	public static List<String> getAdminBypass()
	{
		if (!check())
			throw new RuntimeException("MFFS not found");
		
		try
		{
			String val = (String)fAdmin.get(null);
			if (val != null && !val.equals(""))
			{
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(Arrays.asList(val.split(";")));
				return list;
			}
			else
				return new ArrayList<String>();
		}
		catch (Throwable e) 
		{
			throw new RuntimeException(e);
		}
	}
}
