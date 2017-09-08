package samples.ojai.maprdb_json;

import java.util.Properties;

import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;

public abstract class TestConfig {

	protected final Properties config = new Properties();

	public TestConfig() {
		System.out.println("this.class.name = " + this.getClass().getName());
		UseBean useBean = this.getClass().getAnnotation(UseBean.class);
		if (useBean instanceof UseBean) {
			System.out.println(useBean.value());
			for (Property prop : useBean.value()) {
				System.out.println(prop.name() + " = " + prop.value());
				config.setProperty(prop.name(), prop.value());
			}
		}
	}
}
