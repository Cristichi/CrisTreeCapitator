package objs;

public static class AxeTypeInput {
  public static final int WOODEN = 1;
  public static final int STONE = 2;
  public static final int IRON = 3;
  public static final int GOLDEN = 4;
  public static final int DIAMOND = 5;
  public static final int NETHERITE = 6;

  public boolean isValid(int value) {
    return value >= AxeTypeInput.WOODEN && value <= AxeTypeInput.NETHERITE;
  }
}

public static class AxeType {
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