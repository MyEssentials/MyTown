package mytown.event.tick;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.entities.Town;
import mytown.event.TickBase;

public class OldTownRemover extends TickBase {
	public int daysOld = -1;
	public boolean enabled = false;
	public int timeout = 20 * 60;
	public String[] safeTowns = new String[0];
	public double plotDaysAddition = 0.5;

	@Override
	public boolean enabled() {
		return enabled;
	}

	@Override
	public void run() throws Exception {
		List<Town> towns = source().getOldTowns(System.currentTimeMillis() - (long) daysOld * 24 * 60 * 60 * 1000, plotDaysAddition);

		for (Town t : towns) {
			boolean exit = false;
			if (safeTowns != null) {
				for (String s : safeTowns) {
					if (t.name().equalsIgnoreCase(s)) {
						exit = true;
					}
				}
			}

			if (exit) {
				continue;
			}

			try {
				if (t.nation() != null) {
					if (t.nation().capital() == t) {
						if (t.nation().towns().size() > 1) {
							List<Town> nationTowns = new ArrayList<Town>();
							nationTowns.addAll(t.nation().towns().values());
							Town newCapitol = nationTowns.get(nationTowns.indexOf(t) == 0 ? 1 : 0);
							MyTown.instance.coreLog.info(String.format("[OldTownRemover]Moving capitol of %s to %s", t.nation().name(), newCapitol.name()));
							t.nation().setCapital(newCapitol);
							t.nation().removeTown(t);
						} else {
							MyTown.instance.coreLog.info(String.format("[OldTownRemover]Deleting nation %s", t.nation().name()));
							t.nation().delete();
						}
					} else {
						MyTown.instance.coreLog.info(String.format("[OldTownRemover]Removing town %s from nation %s", t.name(), t.nation().name()));
						t.nation().removeTown(t);
					}
				}

				t.deleteTown();

				MyTown.instance.coreLog.info("[OldTownRemover]Deleted town " + t.name());
			} catch (Exception e) {
				MyTown.instance.coreLog.severe("[OldTownRemover]Error deleting town " + t.name(), e);
			}
		}
	}

	@Override
	public void loadConfig() throws Exception {
		daysOld = MyTown.instance.config.get("TickHandlers.OldTownRemover", "DaysAtleastOld", 7, "Delete towns where members haven't logged in for this amount of days").getInt();
		plotDaysAddition = MyTown.instance.config.get("TickHandlers.OldTownRemover", "PlotDaysAddition", plotDaysAddition, "Each plot of the town adds extra safe time for the town").getDouble(plotDaysAddition);
		enabled = MyTown.instance.config.get("TickHandlers.OldTownRemover", "Enabled", false, "Feature enabled?").getBoolean(false);
		timeout = MyTown.instance.config.get("TickHandlers.OldTownRemover", "WorkerTimeoutTicks", 20 * 60, "How often should the worker check for old towns? Default 1min - 1200 ticks").getInt();
		safeTowns = MyTown.instance.config.get("TickHandlers.OldTownRemover", "SafeTownList", "Spawn,Server", "Town name comma seperated list which are exempt from this feature").getString().split(",");

		if (timeout <= 0) {
			throw new Exception("WorkerTimeoutTicks cannot be at or below 0");
		}
		if (daysOld <= 0) {
			throw new Exception("DaysAtleastOld cannot be at or below 0");
		}
	}

	@Override
	public String name() {
		return "Old town remover";
	}

	@Override
	public int getWaitTimeTicks() {
		return timeout;
	} // every minute
}
