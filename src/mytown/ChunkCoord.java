package mytown;

public class ChunkCoord {
    public int x;
    public int z;

    /**
     * Returns chunk coord given block coord
     * 
     * @param pos
     *            block coord (x or z)
     * @return
     */
    public static int getCoord(double pos) {
        int i = (int) pos;
        if (i < 0 && pos != i) {
            i--;
        }

        return i >> 4;
    }

    /**
     * Returns ChunkCoord given block coords
     * 
     * @param x
     *            block x coord
     * @param z
     *            block z coord
     * @return
     */
    public static ChunkCoord getCoord(int x, int z) {
        ChunkCoord chunk = new ChunkCoord();
        chunk.x = getCoord(x);
        chunk.z = getCoord(z);
        return chunk;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ChunkCoord) {
            ChunkCoord chunk = (ChunkCoord) obj;
            if (chunk.x == x && chunk.z == z) {
                return true;
            }
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return x << 8 + z;
    }
}
