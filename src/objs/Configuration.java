package objs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import exc.ConfigValueNotFound;
import exc.ConfigValueNotParsed;

public class Configuration extends File implements Cloneable {
	private static final long serialVersionUID = 115L;

	private String header;
	private HashMap<String, String> hm;
	private HashMap<String, String> info;

	public Configuration(String file) {
		super(file);
		hm = new HashMap<>();
		info = new HashMap<>();
	}

	public Configuration(String file, String header) {
		super(file);
		hm = new HashMap<>();
		info = new HashMap<>();
		this.header = header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	public void setValue(String key, Object value) {
		hm.put(key, value.toString());
	}

	public void setValue(String key, Object value, String info) {
		hm.put(key, value.toString());
		this.info.put(key, info);
	}

	public String getString(String key, String defaultValue) {
		return hm.getOrDefault(key, defaultValue);
	}

	public String getString(String key) throws ConfigValueNotFound {
		String sol = hm.get(key);
		if (sol == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" was never set in the config file.");
		return sol;
	}

	public int getInt(String key, int defaultValue) {
		String str = hm.getOrDefault(key, defaultValue + "");
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			System.err.println("Error trying to get integer value from config file");
			System.err.println("(Value \"" + str + "\" could not be parsed to integer)");
			return defaultValue;
		}
	}

	public int getInt(String key) throws ConfigValueNotFound, ConfigValueNotParsed {
		String str = hm.get(key);
		if (str == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" was never set in the config file.");
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			System.err.println("");
			System.err.println("");
			throw new ConfigValueNotParsed("Error trying to get integer value from config file (Value \"" + str
					+ "\" could not be parsed to integer)");
		}
	}

	public double getDouble(String key, double defaultValue) {
		String str = hm.getOrDefault(key, defaultValue + "");
		try {
			return Double.parseDouble(str.replace(",", "."));
		} catch (NumberFormatException e) {
			System.err.println("Error trying to get double value from config file");
			System.err.println("(Value \"" + str + "\" could not be parsed to double)");
			return defaultValue;
		}
	}

	public double getDouble(String key) throws ConfigValueNotParsed, ConfigValueNotFound {
		String str = hm.get(key);
		if (str == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" was never set in the config file.");
		try {
			return Double.parseDouble(str.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new ConfigValueNotParsed("Error trying to get double value from config file (Value \"" + str
					+ "\" could not be parsed to double)");
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String str = hm.getOrDefault(key, defaultValue + "");
		switch (str) {
		case "true":
		case "yes":
			return true;
		case "false":
		case "no":
			return false;

		default:
			System.err.println("Error trying to get boolean value from config file");
			System.err.println("(Value \"" + str + "\" could not be parsed to boolean)");
			return defaultValue;
		}
	}

	public boolean getBoolean(String key) throws ConfigValueNotFound, ConfigValueNotParsed {
		String str = hm.get(key);
		if (str == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" was never set in the config file.");
		switch (str) {
		case "true":
		case "yes":
			return true;
		case "false":
		case "no":
			return false;

		default:
			throw new ConfigValueNotParsed("Error trying to get boolean value from config file (Value \"" + str
					+ "\" could not be parsed to boolean)");
		}
	}
	
	public void setInfo(String key, String info) {
		this.info.put(key, info);
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void saveConfig() throws IOException {
		String configTxt = header == null ? "" : "#\t" + header + "\n\n";
		Set<String> keys = hm.keySet();
		for (String key : keys) {
			String value = hm.get(key);
			String info = this.info.get(key);
			if (info != null) {
				configTxt += "#" + info + "\n";
			}
			configTxt += key + ": " + value + "\n";
		}

		if (exists()) {
			delete();
		}
		try {
			getParentFile().mkdirs();
		} catch (NullPointerException e) {
		}
		createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(this));
		writer.write(configTxt);
		writer.close();
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void reloadConfig() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this));
			String line;
			int cont = 0;
			while ((line = reader.readLine()) != null) {
				cont++;
				line = line.trim();
				if (!line.startsWith("#") && !line.isBlank()) {
					StringTokenizer st = new StringTokenizer(line, ":");
					if (st.countTokens() != 2) {
						reader.close();
						throw new IOException("Looks like the file content is not correct. Broken line " + cont + " ("
								+ st.countTokens() + " tokens, should be 2)");
					}
					String key = st.nextToken().trim();
					String value = st.nextToken().trim();
					setValue(key, value);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("Configuration file not created yet. Skipping load.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
