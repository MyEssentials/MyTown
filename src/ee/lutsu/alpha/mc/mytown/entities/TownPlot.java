package ee.lutsu.alpha.mc.mytown.entities;

public class TownPlot {
    private int world_dimension;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;
    private Town town;
    private Resident owner;
    public String owner_name; // only for sql loading. Don't use.
}
