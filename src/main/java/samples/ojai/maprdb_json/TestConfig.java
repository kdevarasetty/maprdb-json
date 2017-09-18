package samples.ojai.maprdb_json;

import static java.lang.System.out;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import samples.ojai.maprdb_json.annotations.AboutTest;
import samples.ojai.maprdb_json.annotations.OverrideConf;
import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;
import samples.ojai.maprdb_json.internal.Maps;

public abstract class TestConfig {
	
	static {
		Maps.load();
	}

	protected final Map<String, String> config = new HashMap<>();
	protected final Map<String, String> overrideConf = new HashMap<>();
	protected final Map<String, String> aboutTest = new HashMap<>();

	public TestConfig() {
		System.out.println("this.class.name = " + this.getClass().getName());
		UseBean useBean = this.getClass().getAnnotation(UseBean.class);
		if (useBean != null) {
			setProperties(useBean.value(), config);
		}
		
		AboutTest aboutTestAnnotation = this.getClass().getAnnotation(AboutTest.class);
		if (aboutTestAnnotation != null) {
			setProperties(aboutTestAnnotation.value(), aboutTest);
		}
	}
	
	public void loadOverrideConfig(Method m) {
		OverrideConf override = m.getAnnotation(OverrideConf.class);
		if (override != null) {
			setProperties(override.value(), overrideConf);
		}
	}
	
	private void setProperties(Property[] props, Map<String, String> conf) {
		for (Property prop : props) {
			System.out.println(prop.name() + " = " + prop.value());
			conf.put(prop.name(), prop.value());
		}
	}
	
	public int getInt(String property) {
		return Integer.parseInt(getProperty(property));
	}

	public String getProperty(String property) {
		String val = overrideConf.get(property);
		
		//order shouldn't matter
		if (val == null)
			val = config.get(property);
		if (val == null)
			val = aboutTest.get(property);
		
		//not my problem if its still null
		return val;
	}

	public boolean getBoolean(String property) {
		return Boolean.parseBoolean(getProperty(property));
	}
}
