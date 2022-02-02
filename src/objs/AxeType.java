package objs;

public class AxeType {
	public static final String WOODEN = "WOODEN_AXE";
	public static final String STONE = "STONE_AXE";
	public static final String IRON = "IRON_AXE";
	public static final String GOLDEN = "GOLDEN_AXE";
	public static final String DIAMOND = "DIAMOND_AXE";
	public static final String NETHERITE = "NETHERITE_AXE";

	public String getAxeName(int value) {
		switch (value) {
			case AxeTypeInput.WOODEN: return AxeType.WOODEN;
			case AxeTypeInput.STONE: return AxeType.STONE;
			case AxeTypeInput.IRON: return AxeType.IRON;
			case AxeTypeInput.GOLDEN: return AxeType.GOLDEN;
			case AxeTypeInput.DIAMOND: return AxeType.DIAMOND;
			case AxeTypeInput.NETHERITE: return AxeType.NETHERITE;
			default: return null;
		}
	}
}
