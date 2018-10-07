package main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import updater.Updater;
import updater.Updater.ReleaseType;

public class TreeCapitator extends JavaPlugin implements Listener {
	private PluginDescriptionFile desc = getDescription();

	private final String header = ChatColor.BLUE + "[" + desc.getName() + "] " + ChatColor.AQUA;

	// Ajustes
	private int maxBlocks = 1500;
	private boolean vipMode = false;

	// Updater
	private static final int ID = 294976;
	public static boolean update = false;
	public static String name = "";
	public static ReleaseType type = null;
	public static String version = "";
	public static String link = "";

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		Updater updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
		update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
		name = updater.getLatestName();
		version = updater.getLatestGameVersion();
		type = updater.getLatestType();
		link = updater.getLatestFileLink();

		if (update) {
			getLogger().info("An update is available, use /tc update to update to the lastest version");
		}

		getLogger().info("Enabled");
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}

	@EventHandler
	private void onBlockBreak(BlockBreakEvent e) {
		Block primero = e.getBlock();
		Material tipo = primero.getBlockData().getMaterial();

		if ((vipMode && e.getPlayer().hasPermission("cristreecapitator.vip") || !vipMode)
				&& (tipo.name().contains("LOG") || tipo.name().contains("LEAVES"))) {
			int destr = breakRec(primero, tipo, 0);
			e.getPlayer().sendMessage(header + "You destroyed " + destr + " blocks.");
		}
	}

	private int breakRec(Block lego, Material type, int destroyed) {
		Material tipo = lego.getBlockData().getMaterial();
		if (tipo.name().contains("LOG") || tipo.name().contains("LEAVES")) {
			lego.breakNaturally();
			if (++destroyed >= maxBlocks && maxBlocks > 0) {
				return destroyed;
			}

			World mundo = lego.getWorld();
			int x = lego.getX(), y = lego.getY(), z = lego.getZ();

			destroyed = breakRec(mundo.getBlockAt(x + 1, y, z), type, destroyed);
			destroyed = breakRec(mundo.getBlockAt(x, y + 1, z), type, destroyed);
			destroyed = breakRec(mundo.getBlockAt(x, y, z + 1), type, destroyed);

			destroyed = breakRec(mundo.getBlockAt(x - 1, y, z), type, destroyed);
			destroyed = breakRec(mundo.getBlockAt(x, y - 1, z), type, destroyed);
			destroyed = breakRec(mundo.getBlockAt(x, y, z - 1), type, destroyed);
		}
		
		return destroyed;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		boolean bueno = label.equals(command.getLabel());
		String[] cmds = command.getAliases().toArray(new String[] {});
		for (int i = 0; i < cmds.length && !bueno; i++) {
			if (label.equals(cmds[i])) {
				bueno = true;
			}
		}

		boolean sinPermiso = true;
		if (bueno) {
			if (args.length > 0) {
				switch (args[0]) {
				case "update":
					if (sender.hasPermission("cristreecapitator.admin")) {
						sinPermiso = false;
					}
					break;

				default:
					break;
				}
			} else {
				return false;
			}
		}

		if (sinPermiso) {
			sender.sendMessage(header + ChatColor.RED + "You don't have permission to use this.");
		}
		return bueno;
	}
}
