package samples.ojai.maprdb_json;

import static java.lang.System.out;
import static samples.ojai.maprdb_json.util.TableUtil.*;

import com.mapr.db.Table;

import samples.ojai.maprdb_json.annotations.*;
import samples.ojai.maprdb_json.beans.TypesBean;
import static samples.ojai.maprdb_json.util.TableUtil.*;

@UseBean({
		@Property(name = "bean", value = "samples.ojai.maprdb_json.TypesBean"),
		@Property(name = "table.name", value = "/apps/types_table") })
@AboutTest({ @Property(name = "test.name", value = "CRUD"),
		@Property(name = "test.category", value = "crud,create,read,update,delete,select,insert"),
		@Property(name = "test.desc", value = "simple crud operations"),
		@Property(name = "cleanup.after", value = "true"),
		@Property(name = "setup.before", value = "true"),
		@Property(name = "cleanup.before", value = "true"),
})
public class OJAITypesTest extends TestConfig {

	public static String TABLE_PATH = null;

	private Table table;
	private TypesBean bean;

	void init() {
		TABLE_PATH = getProperty("table.name");
		bean = new TypesBean();
		bean.init();
	}

	void setup() {
		init();
		table = getTable(TABLE_PATH);
	}

	void cleanup() {
		init();
		overrideConf.clear();
		deleteTable(TABLE_PATH);
	}

	void insertBean() {
		insertOneBean(table, bean);
		printAll(table);
	}

	void queryAll() {
		printAll(table);
	}
}
