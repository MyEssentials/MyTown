package ee.lutsu.alpha.mc.mytown.event.tick;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.entities.*;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.event.TickBase;

public class OldResidentRemover extends TickBase
{
	public int daysOld = -1;
	public boolean enabled = false;
	public int timeout = 20 * 60;
	public String[] safeResidents = new String[0];
	
	@Override
	public boolean enabled() { return enabled; }
	
	@Override
	public void run() throws Exception
	{
		Date limit = new Date(System.currentTimeMillis() - (long)daysOld * 24 * 60 * 60 * 1000); 
		List<Resident> players = source().getOldResidents(limit);

		for (Resident r : players)
		{
			boolean exit = false;
			if (safeResidents != null)
				for (String s : safeResidents)
					if (r.name().equalsIgnoreCase(s))
						exit = true;
			
			if (exit)
				continue;
			
			try
			{
				if (r.town().residents().size() <= 1)
					continue; // last player, cannot remove
				
				if (r.rank() == Rank.Mayor)
				{
					boolean gotOtherMayor = false;
					for (Resident r2 : r.town().residents())
					{
						if (r2 != r && r2.rank() == Rank.Mayor)
						{
							gotOtherMayor = true;
							break;
						}
					}
					if (!gotOtherMayor)
					{
						Resident nextMayor = null;
						for (Resident r2 : r.town().residents())
						{
							if (r2 != r && r2.rank() == Rank.Assistant)
							{
								nextMayor = r2;
								break;
							}
						}
						if (nextMayor == null)
						{
							for (Resident r2 : r.town().residents())
							{
								if (r2 != r)
								{
									nextMayor = r2;
									break;
								}
							}
						}
						if (nextMayor == null)
							continue; // cannot assign new mayor, don't handle this player
						
						Log.info("[OldResidentRemover]Assigning new mayor for town %s - %s (%s)", r.town().name(), nextMayor.name(), nextMayor.rank().toString());
						r.town().setResidentRank(nextMayor, Rank.Mayor);
					}
				}
				Town t = r.town();
				r.town().removeResident(r);
				Log.info("[OldResidentRemover]Removed resident %s from town %s", r.name(), t.name());
			}
			catch (Exception e)
			{
				Log.severe("[OldResidentRemover]Error removing resident %s", e, r.name());
			}
		}
	}
	
	@Override
	public void loadConfig() throws Exception
	{
		daysOld = MyTown.instance.config.get("TickHandlers.OldResidentRemover", "DaysAtleastOld", 30, "Remove residents where he hasn't logged in for this amount of days").getInt();
		enabled = MyTown.instance.config.get("TickHandlers.OldResidentRemover", "Enabled", false, "Feature enabled?").getBoolean(false);
		timeout = MyTown.instance.config.get("TickHandlers.OldResidentRemover", "WorkerTimeoutTicks", 20 * 60, "How often should the worker check for old residents? Default 1min - 1200 ticks").getInt();
		safeResidents = MyTown.instance.config.get("TickHandlers.OldResidentRemover", "SafeResidentList", "", "Resident name comma seperated list which are exempt from this feature").getString().split(",");
		
		if (timeout <= 0)
			throw new Exception("WorkerTimeoutTicks cannot be at or below 0");
		if (daysOld <= 0)
			throw new Exception("DaysAtleastOld cannot be at or below 0");
	}

	@Override
	public String name() { return "Old resident remover"; }
	public int getWaitTimeTicks() { return timeout; } // every minute
}
