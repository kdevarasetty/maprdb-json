package samples.ojai.maprdb_json.util;

import java.util.Properties;

public class ConfigUtil {

	public static int getInt(Properties config, String property) {
		return Integer.parseInt(config.getProperty(property));
	}
}
