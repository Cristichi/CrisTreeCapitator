package objs;

public class AxeTypeInput {
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