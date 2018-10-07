package main;

import org.bukkit.plugin.java.JavaPlugin;

public class TreeCapitator extends JavaPlugin {

	@Override
	public void onEnable() {
		getLogger().info("Enabled");
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}
}
