package ee.lutsu.alpha.mc.mytown.event;

import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.event.tick.*;

public class TickHandler implements ITickHandler
{
	public static TickHandler instance = new TickHandler();
	public long tick = 0;
	
	public TickBase[] handlers = new TickBase[]
	{
		new OldTownRemover(),
		new OldResidentRemover(),
		WorldBorder.instance
	};
	
	public void loadConfigs()
	{
		for (TickBase t : handlers)
		{
			try
			{
				t.loadConfig();
			}
			catch (Exception e)
			{
				throw new RuntimeException("Tick handler '" + t.name() + "' config loading failed. Reason: " + e.getMessage(),e);
			}
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		if (tick == Long.MAX_VALUE)
			tick = 0;
		
		tick++;
		
		for (TickBase t : handlers)
		{
			if (!t.enabled())
				continue;
			
			if (tick % t.getWaitTimeTicks() != 0)
				continue;
			
			try
			{
				t.run();
			}
			catch (Throwable e)
			{
				Log.severe("Tick handler '" + t.name() + "' tick failed.", e);
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel() { return "MyTown general work tick handler."; }
}
