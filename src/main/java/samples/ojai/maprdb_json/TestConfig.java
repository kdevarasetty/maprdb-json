package samples.ojai.maprdb_json;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import samples.ojai.maprdb_json.annotations.AboutTest;
import samples.ojai.maprdb_json.annotations.OverrideConf;
import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;

public abstract class TestConfig {

	protected final Map<String, String> config = new HashMap<>();
	protected final Map<String, String> overrideConf = new HashMap<>();
	protected final Map<String, String> aboutTest = new HashMap<>();

	protected final Map<String, Object> context = new HashMap<>();

	static final String resPath = "/dockerdisk/github/maprdb-json/src/main/resources/";

	public TestConfig() {
		System.out.println("this.class.name = " + this.getClass().getName());
		UseBean useBean = this.getClass().getAnnotation(UseBean.class);
		if (useBean != null) {
			setProperties(useBean.value(), config);
		}

		AboutTest aboutTestAnnotation = this.getClass()
				.getAnnotation(AboutTest.class);
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
			// System.out.println(prop.name() + " = " + prop.value());
			conf.put(prop.name(), prop.value());
		}
	}

	public int getInt(String property) {
		return Integer.parseInt(getProperty(property, "1"));
	}

	private String getProperty(String property, String def) {
		String val = getProperty(property);
		if (val == null)
			val = def;
		return val;
	}

	public String getProperty(String property) {
		String val = overrideConf.get(property);

		// order shouldn't matter
		if (val == null)
			val = config.get(property);
		if (val == null)
			val = aboutTest.get(property);

		// not my problem if its still null
		return val;
	}

	public boolean getBoolean(String property) {
		return Boolean.parseBoolean(getProperty(property));
	}

	void saveDataToFile(List<? extends Object> beans) {
		String fileName = getProperty("data.file");
		if (fileName == null)
			out.println("data.file property not set. Ignoring save.");

		File file = new File(resPath + fileName);
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(file, beans);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void loadData(String fileName, String beanClass) {
		File file = new File(resPath + fileName);
		ObjectMapper mapper = new ObjectMapper();
		out.println("Loading data from " + file.getName());
		try {
			List<Object> beans = mapper.readValue(file, List.class);
			context.put("beans.list", beans);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
