package mytown;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Translates an unlocalized name into the localized version
 * @author Joe Goett
 */
public class Translation {
	private static Map<String, String> translations = new HashMap<String, String>();
	
	/**
	 * Loads a translation file from a BufferedReader
	 * @param translationFile
	 */
	public static void load(BufferedReader translationFile){
		try {
			String line;
			while((line = translationFile.readLine()) != null){
				if (line.trim().startsWith("#")) continue;  // Skip comments
				String[] l = line.split("=");
				translations.put(l[0], l[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets a translation from the loaded localization file. Returns the unlocalizedName if no localized form exists
	 * @param unlocalizedName
	 * @return
	 */
	public static String getTranslation(String unlocalizedName){
		String term =translations.get(unlocalizedName);
		if (term == null) term = unlocalizedName;
		return term;
	}
}