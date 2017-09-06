package samples.ojai.maprdb_json.util;

import static java.lang.System.out;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.json.Json;

import com.mapr.db.MapRDB;
import com.mapr.db.Table;

public class TableUtil {
	
	public static void insertOneBean(Table table, Object bean) {
		Document doc = Json.newDocument(bean);
		table.insertOrReplace(doc);
		table.flush();
	}
	
	public static void insertMultipleBeans(Table table, List<?> beans) {
		for (Object bean : beans) {
			table.insertOrReplace(Json.newDocument(bean));
		}
		table.flush();
	}
	
	public static void printAll(Table table) {
		out.println("All Records");
		try (DocumentStream docs = table.find()) {
			for (Document doc : docs) {
				out.println(doc);
				out.println("name = " + doc.getString("name"));
			}
		}
	}
	
	public static <T> T findBeanById(Table table, String _id, Class<T> clazz) {
		return table.findById(_id).toJavaBean(clazz);
	}

	public static Document findById(Table table, String _id) {
		return table.findById(_id);
	}

	public static Table getTable(String tablePath) {
		if (MapRDB.tableExists(tablePath))
			return MapRDB.getTable(tablePath);
		else
			return MapRDB.createTable(tablePath);
	}

	public static void deleteTable(String tablePath) {
		if (MapRDB.tableExists(tablePath))
			MapRDB.deleteTable(tablePath);
	}

	public static void printTableInformation(String tableName) throws IOException {
		Table table = MapRDB.getTable(tableName);
		out.println("\n=============== TABLE INFO ===============");
		out.println(" Table Name : " + table.getName());
		out.println(" Table Path : " + table.getPath());
		out.println(" Table Infos : " + Arrays.toString(table.getTabletInfos()));
		out.println("==========================================\n");
	}
	
	public static void deleteDocumentById(Table table, String _id) {
		table.delete(_id);
	}

	public static void deleteAllDocuments(Table table) {
		out.println("Unsupported operation");
	}

}
