package lang;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import main.TreeCapitator;

public class Localization {

	public static String LANG_FOLDER = "lang";
	public static HashMap<String, String> variables = new HashMap<>(2);

	public static void addVariable(String variable, String replacement) {
		variables.put(variable, replacement);
	}

	private static void saveDefaultLang(File langFolder) throws IOException {
		File langDefault = new File(langFolder, "default.json");
		langDefault.delete();
		langDefault.createNewFile();
		JSONObject content = new JSONObject();
		content.put("name", "English");
		LinkedHashMap<String, String> defaultLangMap = new LinkedHashMap<>();
		for (LocalizedString locStr : LocalizedString.values()) {
			defaultLangMap.put(locStr.getId(), locStr.getRaw("english"));
		}
		content.put("messages", defaultLangMap);

		Gson gson = new GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
		FileWriter fw = new FileWriter(langDefault);
		fw.write(gson.toJson(content));
		fw.close();
	}

	private static void saveLang(File langFolder, File file, String language) throws IOException {
		file.delete();
		file.createNewFile();
		JSONObject content = new JSONObject();
		content.put("name", language);
		LinkedHashMap<String, String> defaultLangMap = new LinkedHashMap<>();
		for (LocalizedString locStr : LocalizedString.values()) {
			defaultLangMap.put(locStr.getId(), locStr.getRaw(language));
		}
		content.put("messages", defaultLangMap);

		Gson gson = new GsonBuilder().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
		FileWriter fw = new FileWriter(file);
		fw.write(gson.toJson(content));
		fw.close();
	}

	public static void loadLangs(TreeCapitator plugin) throws IOException, ParseException {
		File langFolder = new File(plugin.pluginFolder, LANG_FOLDER);
		if (!langFolder.exists()) {
			langFolder.mkdir();
		}
		saveDefaultLang(langFolder);

		for (File file : langFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".json") && !pathname.getName().equals("default.json");
			}
		})) {
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject) parser.parse(new FileReader(file));
			String language = ((String) jsonObj.get("name")).toLowerCase();
			JSONObject msgsJSONObj = (JSONObject) jsonObj.get("messages");

			String missingEntriesLog = plugin.header + "The language \"" + language + "\" specified in \""
					+ file.getName() + "\" is incomplete. It lacks the following entries:";
			LinkedList<String> missingEntries = new LinkedList<>();
			for (LocalizedString locStr : LocalizedString.values()) {
				Object msg = msgsJSONObj.get(locStr.getId());
				if (msg != null) {
					locStr.put(language, msg.toString());
				} else {
					locStr.put(language, locStr.get(null));
					missingEntries.add(locStr.getId());
					missingEntriesLog += "\n  -" + locStr.getId();
				}
			}
			if (missingEntries.size() > 0) {
				saveLang(langFolder, file, language);
				Bukkit.getLogger().log(Level.WARNING, missingEntriesLog);
				Bukkit.getLogger().log(Level.WARNING, plugin.header
						+ "Please check your default.json for the usage of those entries and edit them in \""
						+ file.getName() + "\", then reload. Until then, those entries will use the English version.");
			}
		}
	}
}
