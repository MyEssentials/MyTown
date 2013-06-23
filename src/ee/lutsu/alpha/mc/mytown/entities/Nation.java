package ee.lutsu.alpha.mc.mytown.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.base.Joiner;

import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;

public class Nation 
{
	public static int nationAddsBlocks = 0;
	public static int nationAddsBlocksPerResident = 0;
	
	private List<Town> towns = new ArrayList<Town>();
	private int id = 0;
	private String name;
	private Town capital;
	private int extraBlocks = 0;
	
	public String name() { return name; }
	public Town capital(){ return capital; }
	
	public int extraBlocks() { return extraBlocks; }
	public void setExtraBlocks(int val) { extraBlocks = val; save(); }
	
	public List<Town> towns(){ return towns; }
	
	public int id() { return id; }
	public void setId(int val) { id = val; }
	
	protected Nation() { }
	public Nation(String pName, Town pCapital) throws CommandException
	{
		checkName(pName);
		
		if (pCapital.nation() != null)
			throw new CommandException(Term.TownErrAlreadyInNation);
		
		name = pName;
		capital = pCapital;
		
		addTown(capital); // calls save

		MyTownDatasource.instance.addNation(this);
	}
	
	public void checkName(String name) throws CommandException
	{
		if (name == null || name.equals(""))
			throw new CommandException(Term.TownErrNationNameCannotBeEmpty);
		
		for (Nation n : MyTownDatasource.instance.nations)
		{
			if (n != this && n.name.equalsIgnoreCase(name))
				throw new CommandException(Term.TownErrNationNameInUse);
		}
	}
	
	public void save()
	{
		MyTownDatasource.instance.saveNation(this);
	}
	
	public void delete()
	{
		for (Town t : towns)
			t.setNation(null);
		
		MyTownDatasource.instance.deleteNation(this);
		MyTownDatasource.instance.unloadNation(this);
	}
	
	public void addTown(Town t) throws CommandException
	{
		if (t.nation() != null)
			throw new CommandException(Term.TownErrAlreadyInNation);
		
		t.setNation(this);
		towns.add(t);
		t.pendingNationInvitation = null;
		
		save();
	}
	
	public void removeTown(Town t) throws CommandException
	{
		if (t.nation() != this || !towns.contains(t))
			throw new CommandException(Term.TownErrNationNotPartOfNation);
		if (t == capital)
			throw new CommandException(Term.TownErrNationCantRemoveCapital);
		
		t.setNation(null);
		towns.remove(t);
		save();
	}
	
	public void setCapital(Town t) throws CommandException
	{
		if (t.nation() != this || !towns.contains(t))
			throw new CommandException(Term.TownErrNationNotPartOfNation);
		
		capital = t;
		save();
	}
	
	public void setName(String v) throws CommandException
	{ 
		checkName(v);
		name = v; 
		save(); 
	}
	
	public int getTotalExtraBlocks(Town forTown)
	{
		if (forTown.nation() != this)
			return 0;
		
		return nationAddsBlocksPerResident * forTown.residents().size() + extraBlocks + nationAddsBlocks;
	}
	
	public static Nation sqlLoad(int id, String name, int capital, String pTowns, String extra)
	{
		Nation n = new Nation();
		n.id = id;
		n.name = name;
		
		if (pTowns != null && pTowns.trim().length() > 0)
		{
			for (String town : pTowns.trim().split(";"))
			{
				Town t = MyTownDatasource.instance.getTown(Integer.parseInt(town));
				
				if (t != null)
				{
					t.setNation(n);
					n.towns.add(t);
				}
			}
		}
		
		if (capital > 0)
		{
			Town t = MyTownDatasource.instance.getTown(capital);
			
			if (t != null)
				n.capital = t;
		}
		
		// handle extra
		if (extra != null && extra.trim().length() > 0)
		{
			String[] exSp = extra.split(";");
			for (String ex : exSp)
			{
				String[] sp = ex.split(":");
				
				if (sp[0].equals("eb"))
					n.extraBlocks = Integer.parseInt(sp[1]);
			}
		}
		
		return n;
	}
	
	public String serializeExtra()
	{
		List<String> ex = new ArrayList<String>();
		
		if (extraBlocks != 0)
			ex.add("eb:" + String.valueOf(extraBlocks));
		
		return Joiner.on(";").join(ex);
	}
	
	public void sendNotification(Level lvl, String msg)
	{
		String formatted = Formatter.townNotification(lvl, msg);
		for (Town t : towns)
		{
			for(Resident r : t.residents())
			{
				if (!r.isOnline())
					continue;
	
				r.onlinePlayer.sendChatToPlayer(formatted);
			}
		}
	}
	
	public void sendNationInfo(ICommandSender pl)
	{
		Nation n = this;
		
		List<String> names = new ArrayList<String>();
		int b1 = 0, b2 = 0, m1 = 0;
		for (Town t : n.towns())
		{
			b1 += t.blocks().size();
			b2 += t.totalBlocks();
			m1 += t.residents().size();
			names.add(String.format("§b%s[%s]", t.name(), t.residents().size()));
		}
		String tNames = Joiner.on("§2, ").join(names);
		
		String nationColor = "§2";
		if (pl instanceof EntityPlayer)
		{
			Resident target = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)pl);
			if (target.town() == null || target.town().nation() != n)
				nationColor = "§4"; 
		}
		
		pl.sendChatToPlayer(Term.NationStatusName.toString(nationColor, n.name()));
		
		pl.sendChatToPlayer(Term.NationStatusGeneral.toString(b1, b2, m1));
		pl.sendChatToPlayer(Term.NationStatusCapital.toString(n.capital() != null ? n.capital().name() : "?"));
		pl.sendChatToPlayer(Term.NationStatusTowns.toString(tNames));
	}
}
