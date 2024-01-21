package lang;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Nullable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public enum LocalizedString {
	//TODO: El resto de mensajes (son un montón)
	JOIN_MSG_ENABLED("join_msg_enabled",
			"Remember, you can use {accentColor}/tc toggle{textColor} to toggle this plugin off!."),
	JOIN_MSG_DISABLED("join_msg_disabled",
			"Remember, you can use {accentColor}/tc toggle{textColor} to toggle this plugin on!.");

	public static String LANG_FOLDER = "plugins/CrisTreeCapitator/lang";
	public static HashMap<String, String> variables;

	public static void addVariable(String variable, String replacement) {
		variables.put(variable, replacement);
	}

	public static void loadLangs() throws IOException, ParseException {
		File langFolder = new File(LANG_FOLDER);
		if (!langFolder.exists()) {
			langFolder.mkdir();
		}
		{
			File langDefault = new File(langFolder, "default.json");
			langDefault.delete();
			langDefault.createNewFile();
			JSONObject content = new JSONObject();
			content.put("name", "English");
			HashMap<String, String> mapMsgs = new HashMap<>(LocalizedString.values().length);
			mapMsgs.put(JOIN_MSG_ENABLED.id, JOIN_MSG_ENABLED.get("english"));
			mapMsgs.put(JOIN_MSG_DISABLED.id, JOIN_MSG_DISABLED.get("english"));
			content.put("messages", mapMsgs);
//			System.out.println(content.toJSONString());
			FileWriter fw = new FileWriter(langDefault);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			System.out.println(gson.toJson(content));
			fw.write(gson.toJson(content));
			fw.close();
		}

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

			JOIN_MSG_ENABLED.put(language, msgsJSONObj.get(JOIN_MSG_ENABLED.getId()).toString());
			JOIN_MSG_DISABLED.put(language, msgsJSONObj.get(JOIN_MSG_ENABLED.getId()).toString());
		}
	}

	private HashMap<String, String> loadedTranslations;

	private String id;

	private LocalizedString(String id, String english) {
		this.id = id;
		loadedTranslations = new HashMap<>(2);
		loadedTranslations.put("english", english);
	}

	public String getId() {
		return id;
	}

	private void put(String lang, String message) {
		loadedTranslations.put(lang.toLowerCase(), message);
	}

	@Nullable
	public String get(String language) {
		String loc = loadedTranslations.get(language.toLowerCase());
		return loc == null ? loadedTranslations.get("english") : loc;
	}

	/*
	 * Example files:
	 * default.json
	 * {
	 * name: "English",
	 * messages: {
	 * "join_msg_enabled": "Remember, you can use {accentColor}/tc toggle{textColor} to toggle this plugin off!",
	 * "join_msg_disabled": "Remember, you can use {accentColor}/tc toggle{textColor} to toggle this plugin on!"
	 * }
	 * }
	 * custom_lang.json
	 * {
	 * name: "Castellano",
	 * messages: {
	 * "join_msg_enabled": "Recuerda, puedes usar {accentColor}/tc toggle{textColor} para desactivar la talla de árboles automática.",
	 * "join_msg_disabled": "Recuerda, puedes usar {accentColor}/tc toggle{textColor} para activar la talla de árboles automática."
	 * }
	 * }
	 */
}
