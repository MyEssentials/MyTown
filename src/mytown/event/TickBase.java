package mytown.event;

import mytown.MyTownDatasource;

public abstract class TickBase {
	public int getWaitTimeTicks() {
		return 20;
	}

	public abstract void run() throws Exception;

	public abstract String name();

	protected MyTownDatasource source() {
		return MyTownDatasource.instance;
	}

	public void loadConfig() throws Exception {
	}

	public boolean enabled() {
		return true;
	}
}
