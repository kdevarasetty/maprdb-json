package samples.ojai.maprdb_json;

import static samples.ojai.maprdb_json.util.TableUtil.addFamilyDescriptor;
import static samples.ojai.maprdb_json.util.TableUtil.deleteTable;
import static samples.ojai.maprdb_json.util.TableUtil.newTd;

import com.mapr.db.Admin;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.db.TableDescriptor;

import samples.ojai.maprdb_json.annotations.AboutTest;
import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;

/**
 * Sample code for ColumnFamily. Adapted from maprdb-ojai-101 project
 * 
 * @author kirand
 *
 */
@UseBean({
		@Property(name = "bean", value = "samples.ojai.maprdb_json.beans.WebAnalytics"),
		@Property(name = "bean.size", value = "10"),
		@Property(name = "bean.cf.name", value = "clicks"),
		@Property(name = "table.name", value = "/apps/colfamily_webanalytics") })
@AboutTest({ @Property(name = "test.name", value = "ColumnFamilyTest"),
		@Property(name = "test.category", value = "column family"),
		@Property(name = "test.desc", value = "simple description"),
		@Property(name = "cleanup.after", value = "false"),
		@Property(name = "setup.before", value = "false"),
		@Property(name = "data.file", value = "columnfamilytest_webanalytics.json") })
public class ColumnFamilyTest extends TestConfig {

	public static void main(String[] args) {

	}

	public static String TABLE_PATH;
	private Table table;

	void init() {
		TABLE_PATH = getProperty("table.name");
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

	Table getTable(String tablePath) {
		// delete table
		if (MapRDB.tableExists(tablePath)) {
			MapRDB.deleteTable(tablePath);
		}

		TableDescriptor td = newTd(tablePath, 512/* MB */,
				/* bulkLoad = */false);
		addFamilyDescriptor(td, /* default = */true, /* inMemory = */true,
				null);
		addFamilyDescriptor(td, /* default = */false, /* inMemory = */false,
				getProperty("bean.cf.name"));

		// Admin Tool
		Admin admin = MapRDB.newAdmin();
		return admin.createTable(td);
	}

}
