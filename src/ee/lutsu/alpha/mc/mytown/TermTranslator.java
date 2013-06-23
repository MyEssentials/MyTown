package ee.lutsu.alpha.mc.mytown;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.minecraftforge.common.Configuration.UnicodeInputStreamReader;

public class TermTranslator 
{
	public static String defaultEncoding = "UTF-8";
	
	public static void load(File file, String language, boolean activate) throws IOException
	{
		UnicodeInputStreamReader input = new UnicodeInputStreamReader(new FileInputStream(file), defaultEncoding);
        
		BufferedReader r = new BufferedReader(input);
		try
		{
			String line;
			
			while((line = r.readLine()) != null)
			{
				line = line.trim();
				int p = line.indexOf("=");
				if (p > 0)
				{
					String sTerm = line.substring(0, p);
					String val = line.substring(p + 1);
					
					Term term = null;
					for(Term t : Term.values())
					{
						if (t.fname().equalsIgnoreCase(sTerm))
						{
							term = t;
							break;
						}
					}
					if (term != null)
						Term.translate(language, term, val);
				}
			}
		
			if (activate)
				switchLanguage(language);
		}
		finally
		{
			r.close();
		}
	}
	
	public static void switchLanguage(String lang)
	{
		Term.language = lang;
	}
}
