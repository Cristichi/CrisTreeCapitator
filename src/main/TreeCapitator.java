package main;

import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import objs.Configuration;
import updater.Updater;

public class TreeCapitator extends JavaPlugin implements Listener {
	private PluginDescriptionFile desc = getDescription();

	// Colors
	private final ChatColor mainColor = ChatColor.BLUE;
	private final ChatColor textColor = ChatColor.AQUA;
	private final ChatColor accentColor = ChatColor.GOLD;
	private final ChatColor errorColor = ChatColor.DARK_RED;
	private final String header = mainColor + "[" + desc.getName() + "] " + textColor;

	// (Soft)Dependencies
	private WorldGuardPlugin wg;

	// Options
	private Configuration config;
	private static final String STRG_MAX_BLOCKS = "destroy limit";
	private int maxBlocks = -1;
	private static final String STRG_VIP_MODE = "vip mode";
	private boolean vipMode = false;
	private static final String STRG_AXE_NEEDED = "axe needed";
	private boolean axeNeeded = true;
	private static final String STRG_REPLANT = "replant";
	private boolean replant = true;
	private static final String STRG_INVINCIBLE_REPLANT = "invincible replant";
	private boolean invincibleReplant = false;
	private static final String STRG_ADMIT_NETHER_TREES = "cut nether \"trees\"";
	private boolean admitNetherTrees = false;

	// Messages
	private final String joinMensaje = header + "Remember " + accentColor + "{player}" + textColor + ", you can use "
			+ accentColor + "/tc toggle" + textColor + " to avoid breaking things made of logs.";

	// Metadata
	private static final String PLAYER_ENABLE_META = "cristichi_treecap_meta_disable";

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
		wg = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
		if (wg == null)
			getLogger().info("WorldGuard not found.");
		else
			getLogger().info("WorldGuard found, extra protection enabled.");

		getServer().getPluginManager().registerEvents(this, this);

		if (checkUpdate()) {
			getServer().getConsoleSender()
					.sendMessage(header + ChatColor.GREEN
							+ "An update is available, use /tc update to update to the lastest version (from v"
							+ desc.getVersion() + " to v" + updater.getRemoteVersion() + ")");
		}

		config = new Configuration("plugins/CrisTreeCapitator/config.yml", "Cristichi's TreeCapitator");
		loadConfiguration();
		saveConfiguration();
		getLogger().info("Enabled");
	}

	private void loadConfiguration() {
		config.reloadConfig();

		maxBlocks = config.getInt(STRG_MAX_BLOCKS, maxBlocks);
		config.setInfo(STRG_MAX_BLOCKS,
				"Sets the maximun number of logs and leaves that can be destroyed at once. -1 to unlimit.");

		vipMode = config.getBoolean(STRG_VIP_MODE, vipMode);
		config.setInfo(STRG_VIP_MODE,
				"Sets vip mode. If enabled, a permission node (cristreecapitator.vip) is required to take down trees at once.");

		axeNeeded = config.getBoolean(STRG_AXE_NEEDED, axeNeeded);
		config.setInfo(STRG_AXE_NEEDED, "Sets if an axe is required to Cut down trees at once.");

		replant = config.getBoolean(STRG_REPLANT, replant);
		config.setInfo(STRG_REPLANT, "Sets if trees should be replanted automatically.");

		invincibleReplant = config.getBoolean(STRG_INVINCIBLE_REPLANT, invincibleReplant);
		config.setInfo(STRG_INVINCIBLE_REPLANT,
				"Sets if saplings replanted by this plugin whould be unbreakable (the block behind too).");

		admitNetherTrees = config.getBoolean(STRG_ADMIT_NETHER_TREES, admitNetherTrees);
		config.setInfo(STRG_ADMIT_NETHER_TREES,
				"Sets if the new 1.16 trees should be cut down as well (does nothing in prior versions).");
	}

	private void saveConfiguration() {
		try {
			config.setValue(STRG_MAX_BLOCKS, maxBlocks);
			config.setValue(STRG_VIP_MODE, vipMode);
			config.setValue(STRG_AXE_NEEDED, axeNeeded);
			config.setValue(STRG_REPLANT, replant);
			config.setValue(STRG_INVINCIBLE_REPLANT, invincibleReplant);
			config.setValue(STRG_ADMIT_NETHER_TREES, admitNetherTrees);
			config.saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		p.sendMessage(joinMensaje.replace("{player}", p.getDisplayName()));
	}

	@EventHandler
	private void onBlockBreak(BlockBreakEvent e) {
		final Block primero = e.getBlock();
		final Material material = primero.getBlockData().getMaterial();
		final Player player = e.getPlayer();
		ItemStack tool = player.getInventory().getItemInMainHand();
		if (!tool.getType().name().contains("_AXE")) {
			tool = null;
		}

		if (wg != null && !wg.createProtectionQuery().testBlockBreak(player, primero))
			return;

		if (player.getGameMode().equals(GameMode.SURVIVAL)) {
			boolean enabled = true;
			List<MetadataValue> metas = player.getMetadata(PLAYER_ENABLE_META);
			for (MetadataValue meta : metas) {
				enabled = meta.asBoolean();
			}

			if (enabled && !e.isCancelled() && (vipMode && player.hasPermission("cristreecapitator.vip") || !vipMode)
					&& (isLog(material))) {
				try {
					boolean cutDown = true;
					if (axeNeeded) {
						PlayerInventory inv = player.getInventory();
						ItemStack hand = inv.getItemInMainHand();
						if (!hand.getType().name().contains("_AXE")) {
							cutDown = false;
						}
					}
					if (cutDown) {
						if (replant) {
							breakRecReplant(player, tool, primero, material, 0, false);
						} else {
							breakRecNoReplant(player, tool, primero, material, 0, false);
						}
						e.setCancelled(true);
					}
				} catch (StackOverflowError e1) {
				}
			} else if (invincibleReplant) {
				List<MetadataValue> metasReplant = primero.getMetadata(STRG_INVINCIBLE_REPLANT);
				for (MetadataValue metareplant : metasReplant) {
					if (metareplant.asBoolean()) {
						long actual = System.currentTimeMillis();
						if (player.hasPermission("cristreecapitator.admin")) {
							List<MetadataValue> metasMsg = player.getMetadata("msged");
							if (metasMsg.isEmpty() || actual - 5000 > metasMsg.get(0).asLong()) {
								player.sendMessage(header + "You broke a protected block.");
								player.setMetadata("msged", new FixedMetadataValue(TreeCapitator.this, actual));
							}
						} else {
							List<MetadataValue> metasMsg = player.getMetadata("msged");
							if (metasMsg.isEmpty() || actual - 5000 > metasMsg.get(0).asLong()) {
								player.sendMessage(header + "This sapling is protected, please don't try to break it.");
								player.setMetadata("msged", new FixedMetadataValue(TreeCapitator.this, actual));
							}
							e.setCancelled(true);
						}
						break;
					}
				}
			}
		}

	}

	private int breakRecNoReplant(Player player, ItemStack tool, Block lego, Material type, int destroyed,
			boolean stop) {
		if ((wg != null && !wg.createProtectionQuery().testBlockBreak(player, lego)) || stop)
			return destroyed;
		Material material = lego.getBlockData().getMaterial();
		if (isLog(material) || isLeaves(material)) {
			if (destroyed > maxBlocks && maxBlocks > 0) {
				return destroyed;
			}
			World mundo = lego.getWorld();
			if (lego.breakNaturally()) {
				destroyed++;
				if (damageItem(player, tool)) {
					stop = true;
				}
			} else
				return destroyed;

			int x = lego.getX(), y = lego.getY(), z = lego.getZ();

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x, y - 1, z), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x, y + 1, z), type, destroyed, stop);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x + 1, y, z + 1), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x + 1, y, z - 1), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x - 1, y, z + 1), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x - 1, y, z - 1), type, destroyed, stop);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x + 1, y, z), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x, y, z + 1), type, destroyed, stop);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x - 1, y, z), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecNoReplant(player, tool, mundo.getBlockAt(x, y, z - 1), type, destroyed, stop);
		}

		return destroyed;
	}

	private int breakRecReplant(Player player, ItemStack tool, Block lego, Material type, int destroyed, boolean stop) {
		if ((wg != null && !wg.createProtectionQuery().testBlockBreak(player, lego)) || stop)
			return destroyed;
		Material material = lego.getBlockData().getMaterial();
		if (isLog(material) || isLeaves(material)) {
			if (maxBlocks > 0 && destroyed > maxBlocks) {
				return destroyed;
			}
			World mundo = lego.getWorld();
			int x = lego.getX(), y = lego.getY(), z = lego.getZ();
			Block below = mundo.getBlockAt(x, y - 1, z);
			if (below.getType().equals(Material.DIRT) || below.getType().equals(Material.GRASS_BLOCK)) {
				switch (lego.getType()) {
				case ACACIA_LOG:
					lego.setType(Material.ACACIA_SAPLING);
					break;
				case BIRCH_LOG:
					lego.setType(Material.BIRCH_SAPLING);
					break;
				case DARK_OAK_LOG:
					lego.setType(Material.DARK_OAK_SAPLING);
					break;
				case JUNGLE_LOG:
					lego.setType(Material.JUNGLE_SAPLING);
					break;
				case OAK_LOG:
					lego.setType(Material.OAK_SAPLING);
					break;
				case SPRUCE_LOG:
					lego.setType(Material.SPRUCE_SAPLING);
					break;
				default:
					if (lego.breakNaturally()) {
						destroyed++;
						if (damageItem(player, tool)) {
							stop = true;
						}
					} else
						return destroyed;
					break;
				}
				if (replant) {
					lego.setMetadata(STRG_INVINCIBLE_REPLANT, new FixedMetadataValue(this, true));
					below.setMetadata(STRG_INVINCIBLE_REPLANT, new FixedMetadataValue(this, true));
				}
			} else {
				if (lego.breakNaturally()) {
					destroyed++;
					if (damageItem(player, tool)) {
						stop = true;
					}
				} else
					return destroyed;
			}

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x, y - 1, z), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x, y + 1, z), type, destroyed, stop);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x + 1, y, z + 1), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x + 1, y, z - 1), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x - 1, y, z + 1), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x - 1, y, z - 1), type, destroyed, stop);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x + 1, y, z), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x, y, z + 1), type, destroyed, stop);

			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x - 1, y, z), type, destroyed, stop);
			if (destroyed < maxBlocks || maxBlocks < 0)
				destroyed = breakRecReplant(player, tool, mundo.getBlockAt(x, y, z - 1), type, destroyed, stop);
		}

		return destroyed;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		label = label.toLowerCase();
		boolean bueno = label.equals(command.getLabel());
		String[] cmds = command.getAliases().toArray(new String[] {});
		for (int i = 0; i < cmds.length && !bueno; i++) {
			cmds[i] = cmds[i].toLowerCase();
			if (label.equals(cmds[i])) {
				bueno = true;
			}
		}

		boolean sinPermiso = false;
		if (bueno) {
			if (args.length > 0) {
				switch (args[0].toLowerCase()) {

				case "help":
					sender.sendMessage(new String[] { header + "Commands:\n",
							accentColor + "/" + label + " help: " + textColor + "Shows this help message.",
							accentColor + "/" + label + " update: " + textColor
									+ "Updates the plugin if there is a new version.",
							accentColor + "/" + label + " setLimit <number>: " + textColor
									+ "Sets the block limit to break each time. Negative number for unlimited.",
							accentColor + "/" + label + " vipMode <true/false>: " + textColor
									+ "Enables or disables Vip Mode (if cristreecapitator.vip is needed to take down trees at once)",
							accentColor + "/" + label + " setReplant <true/false>: " + textColor
									+ "Enables autoreplanting.",
							accentColor + "/" + label + " setInvincibleReplanting <true/false>: " + textColor
									+ "Replanted saplings are invincible. Ignored if replanting is not enabled.",
							accentColor + "/" + label + " setAxeNeeded <true/false>: " + textColor
									+ "Sets if an axe is needed for the plugin to act (in that case, it's damaged).",
							accentColor + "/" + label + " toggle <true/false>: " + textColor
									+ "Toggles the plugin to work on you.",
							accentColor + "/" + label + " nethertrees <true/false>: " + textColor
									+ "Sets if nether trees can be cut down at once or not." });

					break;

				case "version":
					sender.sendMessage(header + getName() + " v" + desc.getVersion());
					break;

				case "values":
					sender.sendMessage(header + "Values" + accentColor + "\nLimit: " + textColor
							+ (maxBlocks < 0 ? "unbounded" : maxBlocks) + accentColor + "\nReplant: " + textColor
							+ (replant ? "enabled" : "disabled") + accentColor + "\nInvincible replant: " + textColor
							+ (invincibleReplant ? "enabled" : "disabled") + accentColor + "\nVip Mode: " + textColor
							+ (vipMode ? "enabled" : "disabled") + accentColor + "\nAxe Needed: " + textColor
							+ (axeNeeded ? "enabled" : "disabled"));
					break;

				case "toggle":
					if (sender instanceof Player) {
						List<MetadataValue> metas = ((Player) sender).getMetadata(PLAYER_ENABLE_META);
						if (metas.isEmpty()) {
							((Player) sender).setMetadata(PLAYER_ENABLE_META, new FixedMetadataValue(this, false));
							sender.sendMessage(header + "Cristichi's Tree Capitator is disabled for you now. Use /"
									+ label + " toggle again to enable it.");
						}
						for (MetadataValue meta : metas) {
							boolean este = !meta.asBoolean();
							((Player) sender).setMetadata(PLAYER_ENABLE_META, new FixedMetadataValue(this, este));
							sender.sendMessage(header + "Cristichi's Tree Capitator is "
									+ (meta.asBoolean() ? "disabled" : "enabled") + " for you.");
						}
					} else {
						sender.sendMessage(header + "This plugin can only be used for players");
					}
					break;

				case "limit":
				case "setlimit":
				case "blocklimit":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Blocks destroyed at once limit is currently " + accentColor
									+ maxBlocks + textColor + ".");
						} else {
							try {
								int nuevoMax = Integer.parseInt(args[1]);
								maxBlocks = nuevoMax < 0 ? -1 : nuevoMax;
								config.setValue(STRG_MAX_BLOCKS, maxBlocks);
								try {
									config.saveConfig();
									sender.sendMessage(
											header + "Limit set to " + (nuevoMax < 0 ? "unbounded" : nuevoMax) + ".");
								} catch (IOException e) {
									sender.sendMessage(header + errorColor
											+ "Error trying to save the value in the configuration file.");
									e.printStackTrace();
								}
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
							config.setValue(STRG_VIP_MODE, vipMode);
							try {
								config.saveConfig();
								sender.sendMessage(header + "Vip mode " + accentColor
										+ (vipMode ? "enabled" : "disabled") + textColor + ".");
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "setreplant":
				case "replant":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								replant = true;
								break;
							case "false":
							case "no":
								replant = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_REPLANT, replant);
							try {
								config.saveConfig();
								sender.sendMessage(header + "Replanting " + accentColor
										+ (replant ? "enabled" : "disabled") + textColor + ".");
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "setinvinciblereplant":
				case "invinciblereplant":
				case "invinciblereplants":
				case "invinciblereplanting":
				case "invinciblereplantings":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								invincibleReplant = true;
								break;
							case "false":
							case "no":
								invincibleReplant = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_INVINCIBLE_REPLANT, invincibleReplant);
							try {
								config.saveConfig();
								sender.sendMessage(header + "Invincible replanted saplings " + accentColor
										+ (invincibleReplant ? "enabled" : "disabled") + textColor + ".");
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "setaxe":
				case "axeneeded":
				case "setaxeneeded":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								axeNeeded = true;
								break;
							case "false":
							case "no":
								axeNeeded = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_AXE_NEEDED, axeNeeded);
							try {
								config.saveConfig();
								sender.sendMessage(header + (axeNeeded ? "Axe " + accentColor + "needed"
										: "Axe " + accentColor + "not needed"));
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "cutnethertrees":
				case "cutdownnethertrees":
				case "nethertrees":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								admitNetherTrees = true;
								break;
							case "false":
							case "no":
								admitNetherTrees = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_ADMIT_NETHER_TREES, admitNetherTrees);
							try {
								config.saveConfig();
								sender.sendMessage(
										header + (admitNetherTrees ? "Cut down nether trees " + accentColor + "true"
												: "Cut down nether trees " + accentColor + "false"));
							} catch (IOException e) {
								sender.sendMessage(header + errorColor
										+ "Error trying to save the value in the configuration file.");
								e.printStackTrace();
							}
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "reload":
					if (sender.hasPermission("cristreecapitator.admin")) {
						loadConfiguration();
						sender.sendMessage(header + "Configuration loaded from file.");
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
									header + "Use " + accentColor + "/reload" + textColor + " to apply changes.");
						} else {
							sender.sendMessage(header + "This plugin is already up to date.");
						}
					} else {
						sinPermiso = true;
					}

					break;

				default:
					sender.sendMessage(
							header + errorColor + "Command not found, please check \"/" + label + " help\".");
					break;
				}
			} else {
				return false;
			}
		}

		if (sinPermiso) {
			sender.sendMessage(header + errorColor + "You don't have permission to use this command.");
		}
		return bueno;
	}

	/**
	 * Deals 1 damage to an item, if possible
	 * 
	 * @param player
	 * @param tool
	 * @return true if item is destroyed, false if not damageable or damaged but not
	 *         destroyed
	 */
	private boolean damageItem(Player player, ItemStack tool) {
		if (axeNeeded && tool != null) {
			ItemMeta meta = tool.getItemMeta();
			if (meta instanceof Damageable) {
				short maxDmg = tool.getType().getMaxDurability();
				Damageable damageable = (Damageable) meta;
				int dmg = damageable.getDamage();
				damageable.setDamage(++dmg);
				tool.setItemMeta((ItemMeta) damageable);

				if (dmg >= maxDmg) {
					tool.setAmount(0);
					player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
					return true;
				}
			}
		}
		return false;
	}

	private boolean isLog(Material mat) {
		boolean ret = mat.name().contains("LOG");
		if (!ret && admitNetherTrees)
			return ret || mat.name().equals("CRIMSON_STEM") || mat.name().equals("WARPED_STEM");
		return ret;
	}

	private boolean isLeaves(Material mat) {
		boolean ret = mat.name().contains("LEAVES");
		if (!ret && admitNetherTrees)
			return ret || mat.name().equals("NETHER_WART_BLOCK") || mat.name().equals("WARPED_WART_BLOCK")|| mat.name().equals("SHROOMLIGHT");
		return ret;
	}
}
