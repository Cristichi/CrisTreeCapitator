package main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class TreeCapitator extends JavaPlugin implements Listener {
	private PluginDescriptionFile desc = getDescription();

	private final String header = ChatColor.BLUE + "[" + desc.getName() + "] " + ChatColor.AQUA;

	// Ajustes
	private int maxBlocks = 1500;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
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

		int destr = breakRec(primero, tipo, 0);
		
		e.getPlayer().sendMessage(header+"You destroyed "+destr+" blocks of "+tipo.name()+".");
	}

	private int breakRec(Block lego, Material type, int destroyed) {
		Material tipo = lego.getBlockData().getMaterial();
		if (tipo != type) {
			return destroyed;
		}

		lego.breakNaturally();
		if (++destroyed >= maxBlocks && maxBlocks>0) {
			return destroyed;
		}

		World mundo = lego.getWorld();
		int x = lego.getX(), y = lego.getY(), z = lego.getZ();

		destroyed = breakRec(mundo.getBlockAt(x+1, y, z), type, destroyed);
		destroyed = breakRec(mundo.getBlockAt(x, y+1, z), type, destroyed);
		destroyed = breakRec(mundo.getBlockAt(x, y, z+1), type, destroyed);

		destroyed = breakRec(mundo.getBlockAt(x-1, y, z), type, destroyed);
		destroyed = breakRec(mundo.getBlockAt(x, y-1, z), type, destroyed);
		destroyed = breakRec(mundo.getBlockAt(x, y, z-1), type, destroyed);
		
		return destroyed;
	}
}
