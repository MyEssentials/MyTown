package mytown.entities;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * Used to define a single setting/permission option for the wild, towns, plots, residents, etc
 * @author Joe Goett
 */
public class Setting {
	private String name;
	private String desc;
	private Object defaultValue;
	private Object value = null;
	private Class<?> type;
	
	/**
	 * Takes the name, description, default value and type and sets value to default value.
	 * @param name
	 * @param desc
	 * @param defaultValue
	 * @param type
	 */
	public Setting(String name, String desc, Object defaultValue, Class<?> type){
		this(name, desc, defaultValue, defaultValue, type);
	}
	
	/**
	 * Sets name, description, value, default value, and type.
	 * @param name
	 * @param desc
	 * @param defaultValue
	 * @param value
	 * @param type
	 */
	public Setting(String name, String desc, Object defaultValue, Object value, Class<?> type){
		this.name = name;
		this.desc = desc;
		this.defaultValue = defaultValue;
		this.value = value;
		this.type = type;
	}
	
	/**
	 * Returns the Setting in a human readable format
	 * @return
	 */
	public String getDisplay(){
		return String.format("§a%s §2[%s] : %s%s\n", getDesc(), getName(), value == null ? "§d" : "§c", value == null ? "inherited" : getValue());
	}
	
	/**
	 * Returns the name
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Returns the description
	 * @return
	 */
	public String getDesc(){
		return desc;
	}
	
	/**
	 * Returns the default value
	 * @return
	 */
	public Object getDefaultValue(){
		return defaultValue;
	}
	
	/**
	 * Sets the value to the given value
	 * @param value
	 */
	public void setValue(Object value){
		this.value = value;
	}
	
	/**
	 * Sets the value to the given settings value
	 * @param set
	 */
	public void setValue(Setting set){
		if (set==null){  //If null is passed to setValue it always calls this
			value = null;
			return;
		}
		setValue(set.value);
	}
	
	/**
	 * Sets the value to the default
	 */
	public void resetValue(){
		setValue(defaultValue);
	}
	
	/**
	 * Gets the value as a generic object
	 * @return
	 */
	public Object getValue(){
		return value;
	}
	
	/**
	 * Returns the type class
	 * @return
	 */
	public Class<?> getType(){
		return type;
	}
	
	/**
	 * Returns value as the given type, or null if it is null or can't be cast.
	 * @param type
	 * @return
	 */
	public <T> T getValue(Class<T> type){
		try{
			return type.cast(value);
		} catch(ClassCastException ex){  //Ignore and return null
			return null;
		}
	}
	
	/**
	 * Returns the serialized form of the setting. In the form name:value. If the value is an array, then its a comma-separated string
	 * @return
	 */
	public String serialize(){
		String ret = "";
		if (type.isInstance(String.class)){
			ret = (String) value;
		} else if (type.isInstance(Integer.class)){
			ret = Integer.toString((Integer)value);
		} else if (type.isInstance(Boolean.class)){
			ret = (Boolean)value ? "1" : "0";
		} else if (type.isInstance(List.class)){
			ret = Joiner.on(",").join(List.class.cast(value));
		} else if (type.isInstance(Object[].class)){
			ret = Joiner.on(",").join((String[])value);
		} else {
			ret = value.toString();
		}
		return name + ":" + ret;
	}
	
	/**
	 * Takes a string to be deserialized
	 * @param val
	 */
	public void deserialize(String val){
		if (type.isInstance(String.class)){
			value = val;
		} else if (type.isInstance(Integer.class)){
			value = Integer.parseInt(val);
		} else if (type.isInstance(Boolean.class)){
			value = val == "1" ? true : false;
		}  else if (type.isInstance(List.class)){
			value = Arrays.asList(val.split(","));
		} else if (type.isInstance(String[].class)){
			value = val.split(",");
		} else{
			value = val;
		}
	}
}