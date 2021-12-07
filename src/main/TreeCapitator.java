package main;

import java.io.IOException;
import java.util.ArrayList;
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
	private final ChatColor textColor = ChatColor.WHITE;
	private final ChatColor accentColor = ChatColor.GOLD;
	private final ChatColor errorColor = ChatColor.DARK_RED;
	private final String header = mainColor + "[" + desc.getName() + "] " + textColor;

	// (Soft)Dependencies
	private WorldGuardPlugin wg;

	// Options
	private Configuration config;

	private static final String STRG_MAX_BLOCKS = "destroy limit";
	private int maxBlocks = -1;
	private static final String DESC_MAX_BLOCKS = "Sets the maximun number of logs and leaves that can be destroyed at once. -1 to unlimit.";

	private static final String STRG_VIP_MODE = "vip mode";
	private boolean vipMode = false;
	private static final String DESC_VIP_MODE = "Sets vip mode. If enabled, a permission node (cristreecapitator.vip) is required to take down trees at once.";

	private static final String STRG_AXE_NEEDED = "axe needed";
	private boolean axeNeeded = true;
	private static final String DESC_AXE_NEEDED = "Sets if an axe is required to Cut down trees at once.";

	private static final String STRG_DAMAGE_AXE = "damage axe";
	private boolean damageAxe = true;
	private static final String DESC_DAMAGE_AXE = "If \"" + STRG_AXE_NEEDED
			+ "\" is set to true, sets if axes used are damaged or not. If \"" + STRG_AXE_NEEDED
			+ "\" is false, this option is ignored.";

	private static final String STRG_BREAK_AXE = "break axe";
	private boolean breakAxe = false;
	private static final String DESC_BREAK_AXE = "If \"" + STRG_AXE_NEEDED + "\" and \"" + STRG_DAMAGE_AXE
			+ "\" are set to true, sets if the axe should not be broken. Otherwise this option is ignored.";

	private static final String STRG_REPLANT = "replant";
	private boolean replant = true;
	private static final String DESC_REPLANT = "Sets if trees should be replanted automatically.";

	private static final String STRG_INVINCIBLE_REPLANT = "invincible replant";
	private boolean invincibleReplant = false;
	private static final String DESC_INVINCIBLE_REPLANT = "Sets if saplings replanted by this plugin should be unbreakable by regular players (including the block beneath).";

	private static final String STRG_ADMIT_NETHER_TREES = "cut nether \"trees\"";
	private boolean admitNetherTrees = false;
	private static final String DESC_ADMIT_NETHER_TREES = "Sets if the new 1.16 nether trees should be treated as regular trees, and therefore cut down entirely as well.";

	private static final String STRG_START_ACTIVATED = "start activated";
	private boolean startActivated = true;
	private static final String DESC_START_ACTIVATED = "Sets if this plugin starts activated for players when they enter the server. If false, players will need to use /tc toggle to activate it for themselves.";

	private static final String STRG_JOIN_MSG = "initial message";
	private boolean joinMsg = true;
	private static final String DESC_JOIN_MSG = "If true, it sends each player a message about /tc toggle when they join the server. The message changes depending on the value of \""
			+ STRG_START_ACTIVATED + "\".";

	// Messages
	private final String joinMensajeActivated = header + "Remember " + accentColor + "{player}" + textColor
			+ ", you can use " + accentColor + "/tc toggle" + textColor + " to avoid breaking things made of logs.";
	private final String joinMensajeDeactivated = header + "Remember " + accentColor + "{player}" + textColor
			+ ", you can use " + accentColor + "/tc toggle" + textColor + " to cut down trees faster.";

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
			getLogger().info("WorldGuard not found. Maybe WorldGuard or this plugin are not up to date?");
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
		config.setInfo(STRG_MAX_BLOCKS, DESC_MAX_BLOCKS);

		vipMode = config.getBoolean(STRG_VIP_MODE, vipMode);
		config.setInfo(STRG_VIP_MODE, DESC_VIP_MODE);

		axeNeeded = config.getBoolean(STRG_AXE_NEEDED, axeNeeded);
		config.setInfo(STRG_AXE_NEEDED, DESC_AXE_NEEDED);

		damageAxe = config.getBoolean(STRG_DAMAGE_AXE, damageAxe);
		config.setInfo(STRG_DAMAGE_AXE, DESC_DAMAGE_AXE);

		breakAxe = config.getBoolean(STRG_BREAK_AXE, damageAxe);
		config.setInfo(STRG_BREAK_AXE, DESC_BREAK_AXE);

		replant = config.getBoolean(STRG_REPLANT, replant);
		config.setInfo(STRG_REPLANT, DESC_REPLANT);

		invincibleReplant = config.getBoolean(STRG_INVINCIBLE_REPLANT, invincibleReplant);
		config.setInfo(STRG_INVINCIBLE_REPLANT, DESC_INVINCIBLE_REPLANT);

		admitNetherTrees = config.getBoolean(STRG_ADMIT_NETHER_TREES, admitNetherTrees);
		config.setInfo(STRG_ADMIT_NETHER_TREES, DESC_ADMIT_NETHER_TREES);

		startActivated = config.getBoolean(STRG_START_ACTIVATED, startActivated);
		config.setInfo(STRG_START_ACTIVATED, DESC_START_ACTIVATED);

		joinMsg = config.getBoolean(STRG_JOIN_MSG, joinMsg);
		config.setInfo(STRG_JOIN_MSG, DESC_JOIN_MSG);
	}

	private void saveConfiguration() {
		try {
			config.setValue(STRG_MAX_BLOCKS, maxBlocks);
			config.setValue(STRG_VIP_MODE, vipMode);
			config.setValue(STRG_AXE_NEEDED, axeNeeded);
			config.setValue(STRG_DAMAGE_AXE, damageAxe);
			config.setValue(STRG_BREAK_AXE, breakAxe);
			config.setValue(STRG_REPLANT, replant);
			config.setValue(STRG_INVINCIBLE_REPLANT, invincibleReplant);
			config.setValue(STRG_ADMIT_NETHER_TREES, admitNetherTrees);
			config.setValue(STRG_START_ACTIVATED, startActivated);
			config.setValue(STRG_JOIN_MSG, joinMsg);
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
		if (joinMsg) {
			Player p = e.getPlayer();
			boolean enabled = startActivated;
			List<MetadataValue> metas = p.getMetadata(PLAYER_ENABLE_META);
			for (MetadataValue meta : metas) {
				enabled = meta.asBoolean();
			}
			if (enabled)
				p.sendMessage(joinMensajeActivated.replace("{player}", p.getDisplayName()));
			else
				p.sendMessage(joinMensajeDeactivated.replace("{player}", p.getDisplayName()));
		}
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
			boolean enabled = startActivated;
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
			if (damageItem(player, tool, material)) {
				stop = true;
			} else {
				if (lego.breakNaturally()) {
					destroyed++;
				} else {
					return destroyed;
				}
			}

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
			if (isDirt(below)) {
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
					if (damageItem(player, tool, material)) {
						return destroyed;
					} else {
						if (lego.breakNaturally()) {
							destroyed++;
						} else {
							return destroyed;
						}
					}
					break;
				}
				if (replant) {
					lego.setMetadata(STRG_INVINCIBLE_REPLANT, new FixedMetadataValue(this, true));
					below.setMetadata(STRG_INVINCIBLE_REPLANT, new FixedMetadataValue(this, true));
				}
			} else {
				if (damageItem(player, tool, material)) {
					stop = true;
				} else {
					if (lego.breakNaturally()) {
						destroyed++;
					} else {
						return destroyed;
					}
				}
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
									+ "Updates the plugin if there is a new version. Only recomended if your server works on Minecraft 1.13 or superior.",
							accentColor + "/" + label + " reload: " + textColor
									+ "Looks for changes in the configuration file and applies them.",
							accentColor + "/" + label + " toggle <true/false>: " + textColor
									+ "Toggles the plugin to work or not on you.",
							accentColor + "/" + label + " values: " + textColor
									+ "Checks the values set in the configuration.",
							accentColor + "/" + label + " setLimit <number>: " + textColor + DESC_MAX_BLOCKS,
							accentColor + "/" + label + " setVipMode <true/false>: " + textColor + DESC_VIP_MODE,
							accentColor + "/" + label + " setReplant <true/false>: " + textColor + DESC_REPLANT,
							accentColor + "/" + label + " setInvincibleReplanting <true/false>: " + textColor
									+ DESC_INVINCIBLE_REPLANT,
							accentColor + "/" + label + " setAxeNeeded <true/false>: " + textColor + DESC_AXE_NEEDED,
							accentColor + "/" + label + " setDamageAxe <true/false>: " + textColor + DESC_DAMAGE_AXE,
							accentColor + "/" + label + " setBreakAxes <true/false>: " + textColor + DESC_BREAK_AXE,
							accentColor + "/" + label + " setNetherTrees <true/false>: " + textColor
									+ DESC_ADMIT_NETHER_TREES,
							accentColor + "/" + label + " setStartActivated <true/false>: " + textColor
									+ DESC_START_ACTIVATED,
							accentColor + "/" + label + " setJoinMsg <true/false>: " + textColor + DESC_JOIN_MSG });

					break;

				case "version":
					sender.sendMessage(header + getName() + " v" + desc.getVersion());
					break;

				case "values":
					sender.sendMessage(new String[] { header + "Values:",
							accentColor + "Join Message: " + textColor + (joinMsg ? "true" : "false"),
							accentColor + "Starts Activated: " + textColor + (startActivated ? "true" : "false"),
							accentColor + "Limit: " + textColor + (maxBlocks < 0 ? "unbounded" : maxBlocks),
							accentColor + "Vip Mode: " + textColor + (vipMode ? "enabled" : "disabled"),
							accentColor + "Replant: " + textColor + (replant ? "enabled" : "disabled"),
							accentColor + "Invincible replant: " + textColor
									+ (invincibleReplant ? "enabled" : "disabled"),
							accentColor + "Axe Needed: " + textColor + (axeNeeded ? "true" : "false"),
							accentColor + "Axe Damaged: " + textColor + (axeNeeded ? "true" : "false"),
							accentColor + "Damage Axe: " + textColor + (damageAxe ? "enabled" : "disabled"),
							accentColor + "Break Axe: " + textColor + (breakAxe ? "enabled" : "disabled"), });
					break;

				case "toggle":
					if (sender instanceof Player) {
						boolean enabled = startActivated;
						List<MetadataValue> metas = ((Player) sender).getMetadata(PLAYER_ENABLE_META);
						for (MetadataValue meta : metas) {
							enabled = meta.asBoolean();
						}
						enabled = !enabled;
						((Player) sender).setMetadata(PLAYER_ENABLE_META, new FixedMetadataValue(this, enabled));
						sender.sendMessage(header + "Cristichi's Tree Capitator is now "
								+ (enabled ? "enabled" : "disabled") + " for you.");
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

				case "setdamage":
				case "setdamageaxe":
				case "setaxedamage":
				case "damageaxe":
				case "axedamage":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								damageAxe = true;
								break;
							case "false":
							case "no":
								damageAxe = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_DAMAGE_AXE, damageAxe);
							try {
								config.saveConfig();
								sender.sendMessage(header + (damageAxe ? "Axes " + accentColor + "can be damaged"
										: "Axes " + accentColor + "can't be damaged"));
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

				case "setbreak":
				case "setbreakaxe":
				case "setaxebreak":
				case "breakaxe":
				case "axebreak":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								breakAxe = true;
								break;
							case "false":
							case "no":
								breakAxe = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_BREAK_AXE, breakAxe);
							try {
								config.saveConfig();
								sender.sendMessage(header + (breakAxe ? "Axes " + accentColor + "can be broken"
										: "Axes " + accentColor + "can't be broken"));
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

				case "setcutnethertrees":
				case "setcutdownnethertrees":
				case "setnethertrees":
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

				case "setstartactivated":
				case "startactivated":
				case "preactivated":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								startActivated = true;
								break;
							case "false":
							case "no":
								startActivated = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_START_ACTIVATED, startActivated);
							try {
								config.saveConfig();
								sender.sendMessage(
										header + (startActivated ? "Plugin activated by default " + accentColor + "true"
												: "Plugin activated by default " + accentColor + "false"));
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

				case "setjoinmsg":
				case "setjoinmessage":
				case "joinmsg":
				case "joinmessage":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								joinMsg = true;
								break;
							case "false":
							case "no":
								joinMsg = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_JOIN_MSG, joinMsg);
							try {
								config.saveConfig();
								sender.sendMessage(header + (joinMsg
										? "Message reminding /tc toggle on join set to " + accentColor + "true"
										: "Message reminding /tc toggle on join set to " + accentColor + "false"));
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

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		switch (args.length) {
		case 0:
			list.add("help");
			if (sender.hasPermission("cristreecapitator.admin")) {
				list.add("update");
				list.add("reload");
			}
			list.add("toggle");
			if (sender.hasPermission("cristreecapitator.admin")) {
				list.add("values");
				list.add("setLimit");
				list.add("setVipMode");
				list.add("setReplant");
				list.add("setInvincibleReplant");
				list.add("setAxeNeeded");
				list.add("setDamageAxe");
				list.add("setBreakAxes");
				list.add("setNetherTrees");
				list.add("setStartActivated");
				list.add("setJoinMsg");
			}
			break;
		case 1:
			args[0] = args[0].toLowerCase();
			switch (args[0]) {
			case "help":
			case "update":
			case "reload":
			case "toggle":
				break;

			default:
				if ("help".startsWith(args[0]))
					list.add("help");
				if (sender.hasPermission("cristreecapitator.admin")) {
					if ("update".startsWith(args[0]))
						list.add("update");
					if ("reload".startsWith(args[0]))
						list.add("reload");
				}
				if ("toggle".startsWith(args[0]))
					list.add("toggle");
				if (sender.hasPermission("cristreecapitator.admin")) {
					if ("values".startsWith(args[0]))
						list.add("values");
					if ("setlimit".startsWith(args[0]))
						list.add("setLimit");
					if ("setvipmode".startsWith(args[0]))
						list.add("setVipMode");
					if ("setreplant".startsWith(args[0]))
						list.add("setReplant");
					if ("setinvinciblereplant".startsWith(args[0]))
						list.add("setInvincibleReplant");
					if ("setaxeneeded".startsWith(args[0]))
						list.add("setAxeNeeded");
					if ("setdamageaxe".startsWith(args[0]))
						list.add("setDamageAxe");
					if ("setbreakaxe".startsWith(args[0]))
						list.add("setBreakAxes");
					if ("setnethertrees".startsWith(args[0]))
						list.add("setNetherTrees");
					if ("setstartactivated".startsWith(args[0]))
						list.add("setStartActivated");
					if ("setjoinmsg".startsWith(args[0]))
						list.add("setJoinMsg");
				}
				break;
			}
			break;

		case 2:
			args[0] = args[0].toLowerCase();
			switch (args[0]) {
			case "setlimit":
				break;
			case "setvipmode":
			case "setreplant":
			case "setinvinciblereplant":
			case "setaxeneeded":
			case "setdamageaxe":
			case "setbreakaxe":
			case "setnethertrees":
			case "setstartactivated":
			case "setjoinmsg":
				list.add("true");
				list.add("false");
			default:
				break;
			}
			break;
		default:
			break;
		}
		return list;
	}

	/**
	 * Deals 1 damage to an item, if possible
	 * 
	 * @param player
	 * @param tool
	 * @return true if item is destroyed or should not be damaged anymore, false if
	 *         not damageable or damaged but not destroyed
	 */
	private boolean damageItem(Player player, ItemStack tool, Material material) {
		if (axeNeeded && damageAxe && tool != null && isLog(material)) {
			ItemMeta meta = tool.getItemMeta();
			if (meta instanceof Damageable) {
				short maxDmg = tool.getType().getMaxDurability();
				Damageable damageable = (Damageable) meta;
				int dmg = damageable.getDamage();
				damageable.setDamage(++dmg);
				tool.setItemMeta((ItemMeta) damageable);

				if (dmg >= maxDmg) {
					if (breakAxe) {
						player.sendMessage(header + "breakAxe: " + breakAxe);
						tool.setAmount(0);
						player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
					} else {
						damageable.setDamage(maxDmg - 1);
						tool.setItemMeta((ItemMeta) damageable);
					}
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
			return ret || mat.name().equals("NETHER_WART_BLOCK") || mat.name().equals("WARPED_WART_BLOCK")
					|| mat.name().equals("SHROOMLIGHT");
		return ret;
	}

	private boolean isDirt(Block below) {
		return below.getType().equals(Material.DIRT) || below.getType().equals(Material.GRASS_BLOCK)
				|| below.getType().equals(Material.PODZOL);
	}
}
