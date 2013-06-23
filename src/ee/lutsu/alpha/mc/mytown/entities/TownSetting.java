package ee.lutsu.alpha.mc.mytown.entities;

import java.util.Map;

import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;

public class TownSetting 
{
	private String name;
	public Object value;
	public Object wildValue;
	public Object effectiveValue;
	
	private String valueDesc;
	private Class instanceOf;
	private String seriaKey;
	
	public TownSetting(String name, String key, Object value, Object wildValue, String valueDesc, Class instanceOf)
	{
		this.name = name;
		this.seriaKey = key;
		this.value = value;
		this.valueDesc = valueDesc;
		this.instanceOf = instanceOf;
		this.wildValue = wildValue;
	}
	
	public String getName() { return name; }
	public String getValueDescription() { return valueDesc; }
	public String getSerializationKey() { return seriaKey; }
	public String getValueType() { return instanceOf.getSimpleName(); }
	public Class getValueClass() { return instanceOf; }
	
	public String getVisualValue()
	{
		return (effectiveValue == null ? "null" : effectiveValue.toString()) + (value == null && effectiveValue != null ? " (inherited)" : "");
	}
	
	public void setValue(String from)
	{
		if (from == null || from.equalsIgnoreCase("inherit") || from.equalsIgnoreCase("null"))
			value = null;
		
		else if (instanceOf == String.class)
			value = from;
		else if (instanceOf == int.class)
			value = new Integer(Integer.parseInt(from));
		else if (instanceOf == boolean.class)
			value = new Boolean(from.equalsIgnoreCase("1") || from.equalsIgnoreCase("on") || from.equalsIgnoreCase("active") || from.equalsIgnoreCase("yes") || from.equalsIgnoreCase("true"));
		
		else if (instanceOf == Permissions.class)
			value = Permissions.parse(from);
		
		else
			throw new RuntimeException("Unimplemented TownSetting type");
	}
	
	public String getValue()
	{
		if (value == null)
			return null;
		
		else if (instanceOf == String.class)
			return (String)value;
		else if (instanceOf == int.class)
			return ((Integer)value).toString();
		else if (instanceOf == boolean.class)
			return (Boolean)value ? "1" : "0";
		
		else if (instanceOf == Permissions.class)
			return ((Permissions)value).getShort();
		
		else
			throw new RuntimeException("Unimplemented TownSetting type");
	}
	
	public <T> T effValue()
	{
		return (T)effectiveValue;
	}
	
	public boolean getEffBoolean()
	{
		return (Boolean)effectiveValue;
	}
	
	public int getEffInt()
	{
		return (Integer)effectiveValue;
	}
}
