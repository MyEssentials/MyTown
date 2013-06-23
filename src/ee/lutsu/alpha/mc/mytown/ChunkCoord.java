package ee.lutsu.alpha.mc.mytown;

public class ChunkCoord 
{
	public static int getCoord(double pos)
	{
		int i = (int)pos;
		if (i < 0 && pos != i)
			i--;
		
		return i >> 4;
	}
}
