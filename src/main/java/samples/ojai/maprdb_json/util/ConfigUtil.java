package samples.ojai.maprdb_json.util;

import java.util.Map;
import java.util.Properties;

public class ConfigUtil {

	public static int getInt(Map<String, String> config, String property) {
		return Integer.parseInt(config.get(property));
	}
}
