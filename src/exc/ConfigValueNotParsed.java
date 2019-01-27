package exc;

public class ConfigValueNotParsed extends ConfigurationException  {
	private static final long serialVersionUID = 1L;

	public ConfigValueNotParsed() {
		super();
	}

	public ConfigValueNotParsed(String message) {
		super(message);
	}
}
