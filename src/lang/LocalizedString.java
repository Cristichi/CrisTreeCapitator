package lang;

import java.util.HashMap;

import javax.annotation.Nullable;

public enum LocalizedString {
	// TODO: Add all other messages (they are a lot)
	HELP_CMD_COMMANDS("help_cmd_commands", "Commands:"), HELP_CMD_HELP("help_cmd_help", "Shows this help message."),
	HELP_CMD_VERSION("help_cmd_version", "Shows the current version of this plugin you are running."),
	HELP_CMD_UPDATE("help_cmd_update",
			"Updates the plugin if there is a new version. Please, do not use if your server works on Minecraft 1.12.* or an older version."),
	HELP_CMD_RELOAD("help_cmd_reload", "Looks for changes in the configuration file and applies them."),
	HELP_CMD_TOGGLE("help_cmd_toggle", "Toggles the plugin to work or not on you."),
	HELP_CMD_SETTINGS("help_cmd_settings", "Shows all the current settings."),
	HELP_CMD_SET_LIMIT("help_cmd_set_limit",
			"Sets the maximum number of logs and leaves that can be destroyed at once. Use \"-1\" to unlimit."),
	HELP_CMD_SET_VIP_MODE("help_cmd_set_vip_mode",
			"Sets vip mode. If enabled, a permission node (cristreecapitator.vip) is required to take down trees at once."),
	HELP_CMD_SET_REPLANT("help_cmd_set_replant", "Sets if trees should be replanted automatically."),
	HELP_CMD_SET_INV_REPL("help_cmd_set_invincible_replant",
			"Sets if saplings replanted by this plugin should be unbreakable by regular players (including the block beneath)."),
	HELP_CMD_SET_AXE_NEEDED("help_cmd_set_axe_needed", "Sets if an axe is required to Cut down trees at once."),
	HELP_CMD_SET_DMG_AXE("help_cmd_set_damage_axe",
			"Sets whether axes used with this plugin are damaged or not. This only takes place if axes are marked as needed for this plugin."),
	HELP_CMD_SET_BREAK_AXE("help_cmd_set_break_axe",
			"If axes are needed and damage by this plugin, this sets whether it should stop breaking logs to avoid breaking the axe."),
	HELP_CMD_SET_NETHER_TREES("help_cmd_set_nether_trees",
			"Sets if the new 1.16 nether trees should be treated as regular trees, and therefore cut down entirely as well."),
	HELP_CMD_SET_START_ENABLED("help_cmd_set_start_activated",
			"Sets if this plugin starts activated for players when they enter the server. If false, players will need to use /tc toggle to activate it for themselves."),
	HELP_CMD_SET_SEND_JOIN_MSG("help_cmd_set_join_message",
			"If true, it sends each player a message about /tc toggle when they join the server."),
	HELP_CMD_SET_IGNORE_LEAVES("help_cmd_set_ignore_leaves",
			"If true, leaves will not be destroyed and will not connect logs. In vanilla terrain forests this will prevent several trees to be cut down at once, but it will leave most big oak trees floating."),
	HELP_CMD_SET_CROUCH_PREVENTION("help_cmd_set_crouch_prevention",
			"If true, crouching players won't trigger this plugin or only crouching players will. If \"inverted\", players will have to crouch to destroy trees instantly. False by default."),
	HELP_CMD_SET_LANG("help_cmd_set_language",
			"Name of the language to use, as specified in the .json files inside plugins/CrisTreeCapitator/lang. To make your own language, copy default.json and edit the copy. Use the value you specify inside \"name\" here in order to select it."),
	VERSION_CMD("version_cmd", "{accentColor}{pluginName} {textColor}v{accentColor}{pluginVersion}{textColor}."),
	SETTINGS_CMD("settings_cmd", "Settings:"), SETTINGS_YES("settings_yes", "yes"), SETTINGS_NO("settings_no", "no"),
	SETTINGS_INVERTED("settings_inverted", "inverted"), SETTINGS_UNLIMITED("settings_unlimited", "unlimited"),
	SETTINGS_ENABLED("settings_enabled", "enabled"), SETTINGS_DISABLED("settings_disabled", "disabled"),
	JOIN_MSG_ENABLED("join_msg_enabled",
			"Remember, you can use {accentColor}/tc toggle{textColor} to toggle this plugin off!."),
	JOIN_MSG_DISABLED("join_msg_disabled",
			"Remember, you can use {accentColor}/tc toggle{textColor} to toggle this plugin on!."),
	ATTEMPT_BREAK_PROTECTED_REPLANT("attempt_break_protected_sapling",
			"This sapling is protected, please don't try to break it."),
	BROKE_PROTECTED_REPLANT("broken_protected_sapling", "You broke a protected sapling.");

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

	void put(String lang, String message) {
		loadedTranslations.put(lang.toLowerCase(), message);
	}

	/**
	 * @param  language
	 * @return          Message without replacing the variables.
	 */
	String getRaw(@Nullable String language) {
		String loc = loadedTranslations.get(language.toLowerCase());
		return loc == null ? loadedTranslations.get("english") : loc;
	}

	/**
	 * @param  language
	 * @return          Localized String replacing all replaceable variables.
	 */
	public String get(@Nullable String language) {
		String loc = loadedTranslations.get(language == null ? "english" : language.toLowerCase());
		if (loc == null) {
			loc = loadedTranslations.get("english");
		}
		for (String replaced : Localization.variables.keySet()) {
			loc = loc.replace(replaced, Localization.variables.get(replaced));
		}
		return loc;
	}
}
