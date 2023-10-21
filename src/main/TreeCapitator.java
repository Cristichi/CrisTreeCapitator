package main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import objs.Configuration;
import objs.Updater;

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

	// Files
	private Configuration config;
	private File fExtraLogs;
	private File fExtraLeaves;

	// Options
	private static final String STRG_MAX_BLOCKS = "destroy limit";
	private int maxBlocks = -1;
	private static final String DESC_MAX_BLOCKS = "Sets the maximum number of logs and leaves that can be destroyed at once. -1 to unlimit.";

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
	private static final String META_INV_REPL = "inv_repl";

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

	private static final String STRG_IGNORE_LEAVES = "ignore leaves";
	private boolean ignoreLeaves = false;
	private static final String DESC_IGNORE_LEAVES = "If true, leaves will not be destroyed and will not connect logs. In vanilla terrain forests this will prevent several trees to be cut down at once, but it will leave most big oak trees floating.";

	private static final String STRG_SNEAKING_PREVENTION = "crouch for prevention";
	private String sneakingPrevention = "false";
	private static final String DESC_SNEAKING_PREVENTION = "If true, crouching players won't trigger this plugin or only crouching players will. If \"inverted\", players will have to crouch to destroy trees instantly. False by default so updating from previous versions won't change this behaviour without notice.";

	// TODO Extra logs/leaves
	private JSONParser parser = new JSONParser();
	private Material[] extraLogs = new Material[0], extraLeaves = new Material[0];

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
		try {
			updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
		} catch (ParseException e) {
			e.printStackTrace();
		}

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

		loadExtraJSONs();

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

		ignoreLeaves = config.getBoolean(STRG_IGNORE_LEAVES, ignoreLeaves);
		config.setInfo(STRG_IGNORE_LEAVES, DESC_IGNORE_LEAVES);

		String defaultSP = new String(sneakingPrevention);
		sneakingPrevention = config.getString(STRG_SNEAKING_PREVENTION, defaultSP).toLowerCase();
		config.setInfo(STRG_SNEAKING_PREVENTION, DESC_SNEAKING_PREVENTION);

		if (!sneakingPrevention.equalsIgnoreCase("true") && !sneakingPrevention.equals("inverted")
				&& !sneakingPrevention.equals("false")) {
			sneakingPrevention = defaultSP;
		}
	}

	private void loadExtraJSONs() {
		fExtraLogs = new File("plugins/CrisTreeCapitator/extra_logs.json");
		if (fExtraLogs.exists()) {
			try (FileReader reader = new FileReader(fExtraLogs)) {
				JSONObject jsonObject = (JSONObject) parser.parse(reader);
				JSONArray JArrayLogs = (JSONArray) jsonObject.get("logs");
				Object[] strExtraLogs = JArrayLogs.toArray();
				extraLogs = new Material[strExtraLogs.length];
				for (int i = 0; i < strExtraLogs.length; i++) {
					extraLogs[i] = Material.getMaterial(strExtraLogs[i].toString());
					if (extraLogs[i] == null) {
						getLogger().warning("Material \"" + strExtraLogs[i]
								+ "\" in extra_logs.json could not be recognized as any in-game Material.");
					}
				}
				getLogger().log(Level.INFO, "Logs from JSON: " + Arrays.toString(extraLogs));
			} catch (IOException e) {
				getLogger().warning(
						"extra_logs.json could not be read. Only the default logs (+ nether) will be detected.");
				getLogger().throwing(this.getClass().getCanonicalName(), "onEnable", e);
				extraLogs = new Material[0];
			} catch (ParseException e) {
				getLogger().warning(
						"extra_logs.json is an invalid JSON. Please make sure the contents of the file are a valid JSON format. Only the default logs (+ nether) will be detected.");
				getLogger().throwing(this.getClass().getCanonicalName(), "onEnable", e);
				extraLogs = new Material[0];
			}
		} else {
			try {
				fExtraLogs.createNewFile();
				JSONObject jsonData = new JSONObject();
				JSONArray jsonArrayLogs = new JSONArray();
				jsonArrayLogs.add("OAK_LOG");
				jsonArrayLogs.add("OAK_LOG");
				jsonArrayLogs.add("OAK_LOG");
				jsonArrayLogs.add("OAK_LOG");
				jsonData.put("logs", jsonArrayLogs);
				FileWriter fw = new FileWriter(fExtraLogs);
				fw.write(jsonData.toJSONString());
				fw.close();
			} catch (IOException e) {
				getLogger().warning(
						"extra_logs.json could not be created. Only the default logs (+ nether) will be detected.");
				getLogger().throwing(this.getClass().getCanonicalName(), "onEnable", e);
				extraLogs = new Material[0];
			}
		}

		fExtraLeaves = new File("plugins/CrisTreeCapitator/extra_leaves.json");
		if (fExtraLeaves.exists()) {
			try (FileReader reader = new FileReader(fExtraLeaves)) {
				JSONObject jsonObject = (JSONObject) parser.parse(reader);
				JSONArray JArrayLeaves = (JSONArray) jsonObject.get("leaves");
				Object[] strExtraLeaves = JArrayLeaves.toArray();
				extraLeaves = new Material[strExtraLeaves.length];
				for (int i = 0; i < strExtraLeaves.length; i++) {
					extraLeaves[i] = Material.getMaterial(strExtraLeaves[i].toString());
					if (extraLeaves[i] == null) {
						getLogger().warning("Material \"" + strExtraLeaves[i]
								+ "\" in extra_leaves.json could not be recognized as any in-game Material.");
					}
				}
				getLogger().log(Level.INFO, "Leaves from JSON: " + Arrays.toString(extraLeaves));
			} catch (IOException e) {
				getLogger().warning(
						"extra_leaves.json could not be read. Only the default leaves (+ nether) will be detected.");
				getLogger().throwing(this.getClass().getCanonicalName(), "onEnable", e);
				extraLeaves = new Material[0];
			} catch (ParseException e) {
				getLogger().warning(
						"extra_leaves.json is an invalid JSON. Please make sure the contents of the file are a valid JSON format. Only the default leaves (+ nether) will be detected.");
				getLogger().throwing(this.getClass().getCanonicalName(), "onEnable", e);
				extraLeaves = new Material[0];
			}
		} else {
			try {
				fExtraLeaves.createNewFile();
				JSONObject jsonData = new JSONObject();
				JSONArray jsonArrayLeaves = new JSONArray();
				jsonArrayLeaves.add("OAK_LEAVES");
				jsonArrayLeaves.add("OAK_LEAVES");
				jsonArrayLeaves.add("OAK_LEAVES");
				jsonArrayLeaves.add("OAK_LEAVES");
				jsonData.put("leaves", jsonArrayLeaves);
				FileWriter fw = new FileWriter(fExtraLeaves);
				fw.write(jsonData.toJSONString());
				fw.close();
			} catch (IOException e) {
				getLogger().warning(
						"extra_leaves.json could not be created. Only the default logs (+ nether) will be detected.");
				getLogger().throwing(this.getClass().getCanonicalName(), "onEnable", e);
				// Bukkit.getPluginManager().disablePlugin(this);
				extraLeaves = new Material[0];
			}
		}
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
			config.setValue(STRG_IGNORE_LEAVES, ignoreLeaves);
			config.setValue(STRG_SNEAKING_PREVENTION, sneakingPrevention);
			config.saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled");
		saveConfig();
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
	private void onBlockBreak(BlockBreakEvent event) {
		final Block firstBrokenB = event.getBlock();
		final Material material = firstBrokenB.getBlockData().getMaterial();
		final Player player = event.getPlayer();
		ItemStack tool = player.getInventory().getItemInMainHand();

		if (invincibleReplant && !(canPlant(
				firstBrokenB.getWorld().getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() - 1, firstBrokenB.getZ()),
				material)
				|| canPlant(firstBrokenB, firstBrokenB.getWorld()
						.getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() + 1, firstBrokenB.getZ()).getType()))) {
			List<MetadataValue> fbbReplantMetas = firstBrokenB.getMetadata(META_INV_REPL);
			for (MetadataValue replantMeta : fbbReplantMetas) {
				if (replantMeta.asBoolean()) {
					long currentTime = System.currentTimeMillis();
					if (!isSappling(material)){
						player.setMetadata("msged", new FixedMetadataValue(this, currentTime));
						firstBrokenB.removeMetadata(META_INV_REPL, this);
						firstBrokenB.getWorld()
								.getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() - 1, firstBrokenB.getZ())
								.removeMetadata(META_INV_REPL, this);
						firstBrokenB.getWorld()
								.getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() + 1, firstBrokenB.getZ())
								.removeMetadata(META_INV_REPL, this);
					} else if (player.hasPermission("cristreecapitator.admin")) {
						player.sendMessage(header + "You broke a protected sapling.");
						player.setMetadata("msged", new FixedMetadataValue(this, currentTime));
						firstBrokenB.removeMetadata(META_INV_REPL, this);
						firstBrokenB.getWorld()
								.getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() - 1, firstBrokenB.getZ())
								.removeMetadata(META_INV_REPL, this);
						firstBrokenB.getWorld()
								.getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() + 1, firstBrokenB.getZ())
								.removeMetadata(META_INV_REPL, this);
					} else {
						List<MetadataValue> metasMsg = player.getMetadata("msged");
						if (metasMsg.isEmpty() || currentTime - 5000 > metasMsg.get(0).asLong()) {
							player.sendMessage(header + "This sapling is protected, please don't try to break it.");
							player.setMetadata("msged", new FixedMetadataValue(this, currentTime));
						}
						event.setCancelled(true);
					}
					return;
				}
			}
		}
		firstBrokenB.removeMetadata(META_INV_REPL, this);
		firstBrokenB.getWorld().getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() - 1, firstBrokenB.getZ())
				.removeMetadata(META_INV_REPL, this);
		firstBrokenB.getWorld().getBlockAt(firstBrokenB.getX(), firstBrokenB.getY() + 1, firstBrokenB.getZ())
				.removeMetadata(META_INV_REPL, this);

		if ((wg != null && !wg.createProtectionQuery().testBlockBreak(player, firstBrokenB))
				|| (sneakingPrevention.equals("true") && player.getPose().equals(Pose.SNEAKING))
				|| (sneakingPrevention.equals("inverted") && !player.getPose().equals(Pose.SNEAKING))
				|| (!player.getGameMode().equals(GameMode.SURVIVAL))) {
			return;
		}

		boolean enabled = startActivated;
		List<MetadataValue> metas = player.getMetadata(PLAYER_ENABLE_META);
		for (MetadataValue meta : metas) {
			enabled = meta.asBoolean();
		}

		if (enabled && !event.isCancelled() && isLog(material) && player.hasPermission("cristreecapitator.user")
				&& (vipMode && player.hasPermission("cristreecapitator.vip") || !vipMode)) {
			try {
				// Yes it could use some tuning
				if (!tool.getType().name().contains("_AXE")) {
					tool = null;
				}

				boolean cutDown = true;
				if (axeNeeded && (tool == null || !tool.getType().name().endsWith("_AXE"))) {
					cutDown = false;
				}
				if (cutDown && axeNeeded && !breakAxe && (tool.hasItemMeta() && tool.getItemMeta() instanceof Damageable
						&& ((Damageable) tool.getItemMeta()).getDamage() >= tool.getType().getMaxDurability())) {
					cutDown = false;
				}
				if (cutDown) {
					if (replant) {
						breakRecReplant(player, tool, firstBrokenB, material, 0, false);
					} else {
						breakRecNoReplant(player, tool, firstBrokenB, material, 0, false);
					}
					event.setCancelled(true);
				}
			} catch (StackOverflowError e) {
				Bukkit.getLogger().throwing("TreeCapitator.java", "onBlockBreak(Event)", e);
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
		if ((wg != null && !wg.createProtectionQuery().testBlockBreak(player, lego)) || stop
				|| (maxBlocks > 0 && destroyed > maxBlocks))
			return destroyed;
		Material material = lego.getBlockData().getMaterial();
		if (isLog(material) || isLeaves(material)) {
			World mundo = lego.getWorld();
			int x = lego.getX(), y = lego.getY(), z = lego.getZ();
			Block below = mundo.getBlockAt(x, y - 1, z);

			if (canPlant(below, lego.getType())) {
				Material saplingType = null;
				switch (lego.getType()) {
				case ACACIA_LOG:
					saplingType = Material.ACACIA_SAPLING;
					break;
				case BIRCH_LOG:
					saplingType = Material.BIRCH_SAPLING;
					break;
				case DARK_OAK_LOG:
					saplingType = Material.DARK_OAK_SAPLING;
					break;
				case JUNGLE_LOG:
					saplingType = Material.JUNGLE_SAPLING;
					break;
				case OAK_LOG:
					saplingType = Material.OAK_SAPLING;
					break;
				case SPRUCE_LOG:
					saplingType = Material.SPRUCE_SAPLING;
					break;
				case MANGROVE_LOG:
					saplingType = Material.MANGROVE_PROPAGULE;
					break;
				case CRIMSON_STEM:
					saplingType = Material.CRIMSON_FUNGUS;
					break;
				case WARPED_STEM:
					saplingType = Material.WARPED_FUNGUS;
					break;
				case CHERRY_LOG:
					saplingType = Material.CHERRY_SAPLING;
					break;
				default:
					break;
				}

				if (damageItem(player, tool, material)) {
					return destroyed;
				} else {
					if (lego.breakNaturally()) {
						if (saplingType != null) {
							lego.setType(saplingType);
							lego.setMetadata(META_INV_REPL, new FixedMetadataValue(this, true));
							below.setMetadata(META_INV_REPL, new FixedMetadataValue(this, true));
						}
						destroyed++;
					} else {
						return destroyed;
					}
				}

			} else {
				if (damageItem(player, tool, material)) {
					return destroyed;
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
							accentColor + "/" + label + " settings: " + textColor
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
							accentColor + "/" + label + " setJoinMsg <true/false>: " + textColor + DESC_JOIN_MSG,
							accentColor + "/" + label + " setIgnoreLeaves <true/false>: " + textColor
									+ DESC_IGNORE_LEAVES,
							accentColor + "/" + label + " setCrouchPrevention <true/false>: " + textColor
									+ DESC_SNEAKING_PREVENTION, });

					break;

				case "version":
					sender.sendMessage(header + getName() + " v" + desc.getVersion());
					break;

				case "config":
				case "values":
				case "settings":
					sender.sendMessage(new String[] { header + "Values:",
							accentColor + "Join Message: " + textColor + (joinMsg ? "show" : "not show"),
							accentColor + "Starts Activated: " + textColor + (startActivated ? "yes" : "no"),
							accentColor + "Limit: " + textColor + (maxBlocks < 0 ? "unbounded" : maxBlocks),
							accentColor + "Vip Mode: " + textColor + (vipMode ? "enabled" : "disabled"),
							accentColor + "Replant: " + textColor + (replant ? "enabled" : "disabled"),
							accentColor + "Invincible replant: " + textColor
									+ (invincibleReplant ? "enabled" : "disabled"),
							accentColor + "Axe Needed: " + textColor + (axeNeeded ? "yes" : "no"),
							accentColor + "Axe Damaged: " + textColor + (axeNeeded ? "yes" : "no"),
							accentColor + "Damage Axe: " + textColor + (damageAxe ? "yes" : "no"),
							accentColor + "Break Axe: " + textColor + (breakAxe ? "yes" : "no"),
							accentColor + "Ignore Leaves: " + textColor + (ignoreLeaves ? "yes" : "no"),
							accentColor + "Crouch Prevention: " + textColor + (sneakingPrevention.equals("true") ? "yes"
									: (sneakingPrevention.equals("false") ? "no" : "inverted")), });
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
						sender.sendMessage(
								header + " You " + (enabled ? "enabled" : "disabled") + " quick log destroy.");
					} else {
						sender.sendMessage(header + "This command can only be used by players");
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

				case "setignoreleaves":
				case "ignoreleaves":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								ignoreLeaves = true;
								break;
							case "false":
							case "no":
								ignoreLeaves = false;
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no>" + textColor + ". (" + accentColor + args[1] + textColor
										+ " is not a valid argument)");
								break;
							}
							config.setValue(STRG_IGNORE_LEAVES, ignoreLeaves);
							try {
								config.saveConfig();
								sender.sendMessage(
										header + (ignoreLeaves ? "Leaves will be " + accentColor + "left untouched"
												: "Leaves will be " + accentColor + "removed"));
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

				case "setcrouchprevention":
				case "setsneakingprevention":
				case "setcrouch":
				case "setsneaking":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (args.length != 2) {
							sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
									+ " <true/false/yes/no/inv/inverted>" + textColor + ".");
						} else {
							switch (args[1]) {
							case "true":
							case "yes":
								sneakingPrevention = "true";
								break;
							case "false":
							case "no":
								sneakingPrevention = "false";
								break;
							case "inv":
							case "inverted":
							case "reverse":
							case "reversed":
								sneakingPrevention = "inverted";
								break;

							default:
								sender.sendMessage(header + "Use: " + accentColor + "/" + label + " " + args[0]
										+ " <true/false/yes/no/inv/inverted>" + textColor + ". (" + accentColor
										+ args[1] + textColor + " is not a valid argument)");
								break;
							}
							config.setValue(STRG_SNEAKING_PREVENTION, sneakingPrevention);
							try {
								config.saveConfig();
								sender.sendMessage(header + (sneakingPrevention.equals("true")
										? accentColor + "Crouching" + textColor
												+ " players will break only 1 log at a time."
										: (sneakingPrevention.equals("false")
												? accentColor + "Crouching won't affect" + textColor
														+ " how players break logs."
												: accentColor + "Only crouching" + textColor
														+ " players will break 1 log at a time.")));
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
						loadExtraJSONs();
						sender.sendMessage(header + "Configuration loaded from file.");
					} else {
						sinPermiso = true;
					}

					break;

				case "update":
					if (sender.hasPermission("cristreecapitator.admin")) {
						if (checkUpdate()) {
							sender.sendMessage(header + "Updating CrisTreeCapitator...");
							try {
								updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.DEFAULT, true);
								updater.getResult();
								sender.sendMessage(
										header + "Use " + accentColor + "/reload" + textColor + " to apply changes.");
							} catch (ParseException e) {
								sender.sendMessage(header + errorColor
										+ "An internal error ocurred while trying to update: " + e.getMessage());
								e.printStackTrace();
							}
						} else {
							sender.sendMessage(header + "This plugin is already up to date.");
						}
					} else {
						sinPermiso = true;
					}

					break;

				case "forceupdate115935":
					if (sender.hasPermission("cristreecapitator.admin")) {
						try {
							updater = new Updater(this, ID, this.getFile(), Updater.UpdateType.DEFAULT, true);
							updater.getResult();
							sender.sendMessage(
									header + "Use " + accentColor + "/reload" + textColor + " to apply changes.");
						} catch (ParseException e) {
							sender.sendMessage(header + errorColor
									+ "An internal error ocurred while trying to update: " + e.getMessage());
							e.printStackTrace();
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
				list.add("settings");
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
				list.add("setIgnoreLeaves");
				list.add("setCrouchPrevention");
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
				if ("help".contains(args[0]))
					list.add("help");
				if (sender.hasPermission("cristreecapitator.admin")) {
					if ("update".contains(args[0]))
						list.add("update");
					if ("reload".contains(args[0]))
						list.add("reload");
				}
				if ("toggle".contains(args[0]))
					list.add("toggle");
				if (sender.hasPermission("cristreecapitator.admin")) {
					if ("settings".contains(args[0]))
						list.add("settings");
					if ("setlimit".contains(args[0]))
						list.add("setLimit");
					if ("setvipmode".contains(args[0]))
						list.add("setVipMode");
					if ("setreplant".contains(args[0]))
						list.add("setReplant");
					if ("setinvinciblereplant".contains(args[0]))
						list.add("setInvincibleReplant");
					if ("setaxeneeded".contains(args[0]))
						list.add("setAxeNeeded");
					if ("setdamageaxe".contains(args[0]))
						list.add("setDamageAxe");
					if ("setbreakaxe".contains(args[0]))
						list.add("setBreakAxe");
					if ("setnethertrees".contains(args[0]))
						list.add("setNetherTrees");
					if ("setstartactivated".contains(args[0]))
						list.add("setStartActivated");
					if ("setjoinmsg".contains(args[0]))
						list.add("setJoinMsg");
					if ("setignoreleaves".contains(args[0]))
						list.add("setIgnoreLeaves");
					if ("setcrouchprevention".contains(args[0]))
						list.add("setCrouchPrevention");
					if ("setsneakingprevention".contains(args[0]))
						list.add("setSneakingPrevention");
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
			case "setignoreleaves":
			case "setsneaking":
			case "setcrouch":
				list.add("true");
				list.add("false");
				break;
			case "setsneakingprevention":
			case "setcrouchprevention":
				list.add("true");
				list.add("inverted");
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

				// damageable.setDamage(++dmg);
				// Substituted for the following code by exwundee (https://github.com/exwundee)
				// This adds support for any level of the Durability enchantment
				{
					Random rand = new Random();

					int unbLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);

					if (rand.nextInt(unbLevel + 1) == 0) {
						damageable.setDamage(++dmg);
					}
				}
				tool.setItemMeta((ItemMeta) damageable);

				if (dmg >= maxDmg) {
					if (breakAxe) {
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
		for (int i = 0; i < extraLogs.length; i++) {
			if (extraLogs[i].equals(mat)) {
				return true;
			}
		}
		boolean ret = !mat.name().contains("STRIPPED_") && mat.name().contains("_LOG");
		if (!ret && admitNetherTrees)
			return mat.name().equals("CRIMSON_STEM") || mat.name().equals("WARPED_STEM");
		return ret;
	}

	private boolean isLeaves(Material mat) {
		if (ignoreLeaves)
			return false;
		for (int i = 0; i < extraLeaves.length; i++) {
			if (extraLeaves[i].equals(mat)) {
				return true;
			}
		}
		boolean ret = mat.name().contains("LEAVES");
		if (!ret && admitNetherTrees)
			return ret || mat.name().equals("NETHER_WART_BLOCK") || mat.name().equals("WARPED_WART_BLOCK")
					|| mat.name().equals("SHROOMLIGHT");
		return ret;
	}

	private boolean isSappling(Material mat) {
		boolean ret = mat.name().contains("_SAPLING");
		return ret;
	}

	/**
	 * <Block below, Log material>
	 */
	private HashMap<Material, List<Material>> treeMap;

	private boolean canPlant(Block below, Material woodType) {
		if (treeMap == null) {
			treeMap = new HashMap<>(10);

			// Elegance is my passion /s
			ArrayList<Material> woods = new ArrayList<>(9);
			try {
				woods.add(Material.OAK_LOG);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			try {
				woods.add(Material.DARK_OAK_LOG);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			try {
				woods.add(Material.SPRUCE_LOG);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			try {
				woods.add(Material.ACACIA_LOG);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			try {
				woods.add(Material.AZALEA);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			try {
				woods.add(Material.BIRCH_LOG);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			try {
				woods.add(Material.JUNGLE_LOG);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			try {
				woods.add(Material.MANGROVE_LOG);
			} catch (Exception | NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
			for (Material wood : woods) {
				List<Material> plantSurfaces = Arrays.asList(Material.DIRT, Material.GRASS_BLOCK, Material.MYCELIUM,
						Material.FARMLAND);
				try {
					plantSurfaces.add(Material.PODZOL);
				} catch (Exception | NoSuchFieldError e) {
					// Material doesn't exist in this version
				}
				try {
					plantSurfaces.add(Material.MOSS_BLOCK);
				} catch (Exception | NoSuchFieldError e) {
					// Material doesn't exist in this version
				}
				try {
					plantSurfaces.add(Material.ROOTED_DIRT);
				} catch (Exception | NoSuchFieldError e) {
					// Material doesn't exist in this version
				}
				try {
					plantSurfaces.add(Material.COARSE_DIRT);
				} catch (Exception | NoSuchFieldError e) {
					// Material doesn't exist in this version
				}
				try {
					plantSurfaces.add(Material.MUD);
				} catch (Exception | NoSuchFieldError e) {
					// Material doesn't exist in this version
				}
				treeMap.put(wood, new ArrayList<>(plantSurfaces));
			}

			try {
				treeMap.get(Material.MANGROVE_LOG).add(Material.CLAY);
			} catch (NoSuchFieldError e) {
				// Material doesn't exist in this version
			}
		}

		try {
			if (admitNetherTrees && !treeMap.containsKey(Material.WARPED_STEM)) {
				treeMap.put(Material.WARPED_STEM, Arrays.asList(Material.WARPED_NYLIUM));
				treeMap.put(Material.CRIMSON_STEM, Arrays.asList(Material.CRIMSON_NYLIUM));
			} else if (!admitNetherTrees && treeMap.containsKey(Material.WARPED_STEM)) {
				treeMap.remove(Material.WARPED_STEM);
				treeMap.remove(Material.CRIMSON_STEM);
			}
		} catch (NoSuchFieldError e) {
			// Material doesn't exist in this version
		}

		return treeMap.getOrDefault(woodType, new ArrayList<>(0)).contains(below.getType());
	}
}
