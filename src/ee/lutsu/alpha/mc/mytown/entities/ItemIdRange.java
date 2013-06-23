package ee.lutsu.alpha.mc.mytown.entities;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;

public class ItemIdRange 
{
	public int fromId = -1;
	public int fromIdSub = -1;
	public int toId = -1;
	public int toIdSub = -1;
	public boolean squareTest;
	
	public ItemIdRange() { }
	public ItemIdRange(String line) { load(line); }
	public ItemIdRange(String line, String sep, boolean square) { squareTest = square; load(line, sep); }
	
	public boolean equals(ItemStack item)
	{
		return equals(item.itemID, item.isStackable() ? item.getItemDamage() : 0);
	}
	
	public boolean equals(int id, int subId)
	{
		if (toId < 0) // single check
		{
			if (id != fromId)
				return false;
			
			if (fromIdSub >= 0 && subId != fromIdSub)
				return false;
			
			return true;
		}
		else
		{
			if (squareTest)
			{
				if (id < fromId || id > toId) // X
					return false;
				
				if (subId < fromIdSub || subId > toIdSub) // Y
					return false;
				
				return true;
			}
			
			if (id < fromId || id > toId)
				return false;
			
			if (id == fromId && fromIdSub >= 0 && subId < fromIdSub)
				return false;
			
			if (id == toId && toIdSub >= 0 && subId > toIdSub)
				return false;
			
			return true;
		}
	}
	
	public void load(String line)
	{
		load(line, "-");
	}
	
	public void load(String line, String separator)
	{
		if (line == null || line == "")
			return;
		
		String l1 = "", l2 = "";
		if (line.contains(separator)) // multi
		{
			l1 = line.substring(0, line.indexOf(separator)).trim();
			l2 = line.substring(line.indexOf(separator) + 1).trim();
		}
		else // single
			l1 = line.trim();

		splitAndSet(l1, false);
		splitAndSet(l2, true);
	}
	
	private void splitAndSet(String line, boolean second)
	{
		if (line.equals(""))
			return;
		
		String main = "", sub = "";
		if (line.contains(":"))
		{
			main = line.substring(0, line.indexOf(":")).trim();
			sub = line.substring(line.indexOf(":") + 1).trim();
		}
		else
			main = line.trim();
		
		if (!second)
		{
			fromId = Integer.valueOf(main).intValue();
			fromIdSub = sub.equals("") ? -1 : Integer.valueOf(sub).intValue();
		}
		else
		{
			toId = Integer.valueOf(main).intValue();
			toIdSub = sub.equals("") ? -1 : Integer.valueOf(sub).intValue();
		}
	}
	
	public static LinkedList<ItemIdRange> parseList(List<String> lines)
	{
		return parseList(lines, "-", false);
	}

	public static LinkedList<ItemIdRange> parseList(List<String> lines, String separator, boolean square)
	{
		LinkedList<ItemIdRange> ret = new LinkedList<ItemIdRange>();

		if (lines != null && lines.size() > 0)
		{
			for(String line : lines)
			{
				if (line != "")
					ret.add(new ItemIdRange(line, separator, square));
			}
		}

		return ret;
	}
	
	public static boolean contains(List<ItemIdRange> list, int id, int subId)
	{
		if (list == null)
			return false;
		
		for(ItemIdRange i : list)
		{
			if (i.equals(id, subId))
				return true;
		}
		
		return false;
	}
	
	public static boolean contains(List<ItemIdRange> list, ItemStack item)
	{
		if (list == null || item == null)
			return false;
		
		for(ItemIdRange i : list)
		{
			if (i.equals(item))
				return true;
		}
		
		return false;
	}
}
