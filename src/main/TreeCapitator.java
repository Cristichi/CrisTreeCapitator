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

public class TreeCapitator extends JavaPlugin implements Listener {
	private PluginDescriptionFile desc = getDescription();

	private final ChatColor mainColor = ChatColor.BLUE;
	private final ChatColor textColor = ChatColor.AQUA;
	private final ChatColor accentColor = ChatColor.DARK_AQUA;
	private final String header = mainColor + "[" + desc.getName() + "] " + textColor;

	// Ajustes
	private int maxBlocks = 2;
	private boolean vipMode = false;

	// Updater
	private static final int ID = 294976;
	private static Updater updater;
	public static boolean update = false;

	private boolean checkUpdate() {
		updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
		update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;

		return update;

	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		if (checkUpdate()) {
			getServer().getConsoleSender()
					.sendMessage(header + ChatColor.GREEN
							+ "An update is available, use /tc update to update to the lastest version (from v"
							+ desc.getVersion() + " to v" + updater.getRemoteVersion() + ")");
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
				&& (tipo.name().contains("LOG") /* || tipo.name().contains("LEAVES") */)) {

			try {
				int destr = breakRec(primero, tipo, 0);
				e.getPlayer().sendMessage(header + "Destroyed " + destr + ".");
			} catch (StackOverflowError e1) {
			}
		}
	}

	private int breakRec(Block lego, Material type, int destroyed) {
		Material tipo = lego.getBlockData().getMaterial();
		if (tipo.name().contains("LOG") || tipo.name().contains("LEAVES")) {
			if (destroyed > maxBlocks && maxBlocks > 0) {
				return destroyed;
			}
			World mundo = lego.getWorld();
			if (lego.breakNaturally()) {
				// mundo.strikeLightningEffect(lego.getLocation());
				/*
				 * try { Thread.sleep(10); } catch (InterruptedException e) {
				 * e.printStackTrace(); }
				 */
				destroyed++;
			} else
				return destroyed;

			int x = lego.getX(), y = lego.getY(), z = lego.getZ();

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x, y - 1, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x, y + 1, z), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x + 1, y, z + 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x + 1, y, z - 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x - 1, y, z + 1), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x - 1, y, z - 1), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x + 1, y, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x, y, z + 1), type, destroyed);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x - 1, y, z), type, destroyed);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRec(mundo.getBlockAt(x, y, z - 1), type, destroyed);
		}

		return destroyed;
	}

	/*
	 * private int breakRecUp(Block lego, Material type, int destroyed) { Material
	 * tipo = lego.getBlockData().getMaterial(); if (tipo.name().contains("LOG") ||
	 * tipo.name().contains("LEAVES")) { if (destroyed >= maxBlocks/2 && maxBlocks >
	 * 0) { return destroyed; } if (lego.breakNaturally()) destroyed++;
	 * 
	 * World mundo = lego.getWorld(); int x = lego.getX(), y = lego.getY(), z =
	 * lego.getZ();
	 * 
	 * if (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecUp(mundo.getBlockAt(x, y + 1, z), type, destroyed);
	 * 
	 * if (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecUp(mundo.getBlockAt(x + 1, y, z), type, destroyed); if (destroyed <
	 * maxBlocks || maxBlocks < 0) destroyed = breakRecUp(mundo.getBlockAt(x, y, z +
	 * 1), type, destroyed);
	 * 
	 * if (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecUp(mundo.getBlockAt(x + 1, y, z + 1), type, destroyed); if (destroyed
	 * < maxBlocks || maxBlocks < 0) destroyed = breakRecUp(mundo.getBlockAt(x + 1,
	 * y, z - 1), type, destroyed); if (destroyed < maxBlocks || maxBlocks < 0)
	 * destroyed = breakRecUp(mundo.getBlockAt(x - 1, y, z + 1), type, destroyed);
	 * if (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecUp(mundo.getBlockAt(x - 1, y, z - 1), type, destroyed); }
	 * 
	 * return destroyed; } private int breakRecDown(Block lego, Material type, int
	 * destroyed) { Material tipo = lego.getBlockData().getMaterial(); if
	 * (tipo.name().contains("LOG") || tipo.name().contains("LEAVES")) { if
	 * (destroyed >= maxBlocks && maxBlocks > 0) { return destroyed; } if
	 * (lego.breakNaturally()) destroyed++;
	 * 
	 * World mundo = lego.getWorld(); int x = lego.getX(), y = lego.getY(), z =
	 * lego.getZ();
	 * 
	 * 
	 * if (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecDown(mundo.getBlockAt(x, y - 1, z), type, destroyed);
	 * 
	 * if (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecDown(mundo.getBlockAt(x - 1, y, z), type, destroyed); if (destroyed <
	 * maxBlocks || maxBlocks < 0) destroyed = breakRecDown(mundo.getBlockAt(x, y, z
	 * - 1), type, destroyed);
	 * 
	 * if (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecDown(mundo.getBlockAt(x + 1, y, z + 1), type, destroyed); if
	 * (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecDown(mundo.getBlockAt(x + 1, y, z - 1), type, destroyed); if
	 * (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecDown(mundo.getBlockAt(x - 1, y, z + 1), type, destroyed); if
	 * (destroyed < maxBlocks || maxBlocks < 0) destroyed =
	 * breakRecDown(mundo.getBlockAt(x - 1, y, z - 1), type, destroyed); }
	 * 
	 * return destroyed; }
	 */

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		boolean bueno = label.equals(command.getLabel());
		String[] cmds = command.getAliases().toArray(new String[] {});
		for (int i = 0; i < cmds.length && !bueno; i++) {
			if (label.equals(cmds[i])) {
				bueno = true;
			}
		}

		boolean sinPermiso = false;
		if (bueno) {
			if (args.length > 0) {
				switch (args[0]) {

				case "help":
					sender.sendMessage(header + "Commands:\n" + accentColor + "/" + label + " help:" + textColor
							+ " Shows this help message.\n" + accentColor + "/" + label + " update:" + textColor
							+ " Updates the plugin if there is a new version.\n" + accentColor + "/" + label
							+ " setlimit <number>:" + textColor
							+ " Sets the block limit to break each time. Negative number for unlimited.");

					break;

				case "limit":
				case "setlimit":
				case "blocklimit":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <number>" + textColor + ".");
						} else {
							try {
								int nuevoMax = Integer.parseInt(args[1]);
								maxBlocks = nuevoMax < 0 ? -1 : nuevoMax;
								sender.sendMessage(
										header + "Limit set to " + (nuevoMax < 0 ? "unbounded" : nuevoMax) + ".");
							} catch (NumberFormatException e) {
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <number>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid number)");
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "setvipmode":
				case "vipmode":
				case "vipneeded":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								vipMode = true;
								break;
							case "false":
							case "no":
								vipMode = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "update":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (checkUpdate()) {
							sender.sendMessage(header + "Updating CrisTreeCapitator...");
							updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.DEFAULT, true);
							updater.getResult();
							sender.sendMessage(
									header + "Use " + accentColor + "/restart" + textColor + " to apply changes.");
						} else {
							sender.sendMessage(header + "This plugin is already up to date.");
						}
					} else {
						sinPermiso = true;
					}

					break;

				default:
					sender.sendMessage(header + "Command not found, please check \"/" + command + " help\".");
					break;
				}
			} else {
				return false;
			}
		}

		if (sinPermiso) {
			sender.sendMessage(header + ChatColor.RED + "You don't have permission to use this command.");
		}
		return bueno;
	}
}
