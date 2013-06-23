package ee.lutsu.alpha.mc.mytown.event.prot;

import java.util.List;

import ee.lutsu.alpha.mc.mytown.Log;

public class CustomNPCs 
{
	public static boolean debug = false;

	public static void addNPCClasses(List<Class> list) throws Exception
	{
		addSub(list, "org.millenaire.common.MillVillager");
		addSub(list, "noppes.npcs.EntityNPCInterface");
	}
	
	private static void addSub(List<Class> list, String name)
	{
		try
		{
			list.add(Class.forName(name));
		}
		catch (Throwable t)
		{
			if (debug)
				Log.warning(String.format("Cannot load %s for Custom NPCs", name));
		}
	}
}
