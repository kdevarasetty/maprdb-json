package samples.ojai.maprdb_json.util;

import static java.lang.System.out;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.json.Json;
import org.ojai.store.DocumentMutation;
import org.ojai.store.QueryCondition;

import com.mapr.db.Admin;
import com.mapr.db.FamilyDescriptor;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.db.TableDescriptor;

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

	public static DocumentStream findByCondition(Table table,
			QueryCondition condition) {
		return table.find(condition);
	}

	public static void executeQueryAndPrintResults(Table table,
			QueryCondition condition) {
		try (DocumentStream documentStream = findByCondition(table,
				condition)) {
			for (Document doc : documentStream) {
				System.out.println("\t" + doc);
			}
		}
	}

	public static void printResults(DocumentStream results) {
		try (DocumentStream documentStream = results) {// does this work?
			for (Document doc : documentStream) {
				System.out.println("\t" + doc);
			}
		}
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

	public static void update(Table table, String _id,
			DocumentMutation mutation, boolean flush) {
		table.update(_id, mutation);
		if (flush)
			table.flush();
	}

	public static DocumentMutation newMutation() {
		return MapRDB.newMutation();
	}

	public static <T> T findBeanById(Table table, String _id, Class<T> clazz) {
		return table.findById(_id).toJavaBean(clazz);
	}

	public static Document findById(Table table, String _id) {
		return table.findById(_id);
	}

	public static Document findById(Table table, String _id, String... cols) {
		return table.findById(_id, cols);
	}

	public static Table getTable(String tablePath) {
		if (MapRDB.tableExists(tablePath))
			return MapRDB.getTable(tablePath);
		else
			return MapRDB.createTable(tablePath);
	}

	public static Table createTable(TableDescriptor td) {
		// Admin Tool
		Admin admin = MapRDB.newAdmin();
		return admin.createTable(td);
	}

	public static TableDescriptor newTd(String tablePath, int splitSize,
			boolean bulkLoad) {
		// Create a table descriptor
		TableDescriptor tableDescriptor = MapRDB.newTableDescriptor()
				.setPath(tablePath) // set the Path of the table in MapR-FS
				.setSplitSize(splitSize) // Size in mebibyte (Mega Binary Bytes)
				.setBulkLoad(bulkLoad); // Created with Bulk mode by default
		return tableDescriptor;
	}

	public static void addFamilyDescriptor(TableDescriptor td,
			boolean isDefault, boolean isInMemory, String cfName) {
		FamilyDescriptor familyDesc;

		if (isDefault) {/* Default Column Family */
			familyDesc = MapRDB.newDefaultFamilyDescriptor()
					.setCompression(FamilyDescriptor.Compression.None)
					.setInMemory(true); // keep in RAM as much as possible
		} else {
			// Create a new column family to store specific JSON attributes
			familyDesc = MapRDB.newFamilyDescriptor().setName(cfName)
					.setJsonFieldPath(cfName).setInMemory(false)
					.setCompression(FamilyDescriptor.Compression.ZLIB); // CF
																		// compression
		}
		td.addFamily(familyDesc);
	}

	public static Table createTable(String tablePath) throws IOException {
		// delete table
		if (MapRDB.tableExists(tablePath)) {
			MapRDB.deleteTable(tablePath);
		}

		// Admin Tool
		Admin admin = MapRDB.newAdmin();

		// Create a table descriptor
		TableDescriptor tableDescriptor = MapRDB.newTableDescriptor()
				.setPath(tablePath) // set the Path of the table in MapR-FS
				.setSplitSize(512) // Size in mebibyte (Mega Binary Bytes)
				.setBulkLoad(false); // Created with Bulk mode by default

		// Configuration of the default Column Family, used to store JSON
		// element by default
		FamilyDescriptor familyDesc = MapRDB.newDefaultFamilyDescriptor()
				.setCompression(FamilyDescriptor.Compression.None)
				.setInMemory(true); // To tell the DB to keep these value in RAM
									// as much as possible
		tableDescriptor.addFamily(familyDesc);

		// Create a new colmn family to store specific JSON attributes
		familyDesc = MapRDB.newFamilyDescriptor().setName("clicks")
				.setJsonFieldPath("clicks")
				.setCompression(FamilyDescriptor.Compression.ZLIB) // compression
																	// for this
																	// CF
				.setInMemory(false);

		tableDescriptor.addFamily(familyDesc);

		return admin.createTable(tableDescriptor);
	}

	public static void deleteTable(String tablePath) {
		if (MapRDB.tableExists(tablePath))
			MapRDB.deleteTable(tablePath);
	}

	public static void printTableInformation(String tableName)
			throws IOException {
		Table table = MapRDB.getTable(tableName);
		out.println("\n=============== TABLE INFO ===============");
		out.println(" Table Name : " + table.getName());
		out.println(" Table Path : " + table.getPath());
		out.println(
				" Table Infos : " + Arrays.toString(table.getTabletInfos()));
		out.println("==========================================\n");
	}

	public static void deleteDocumentById(Table table, String _id) {
		table.delete(_id);
	}

	public static void deleteAllDocuments(Table table) {
		DocumentStream rs = table.find();
		Iterator<Document> itrs = rs.iterator();
		Document readRecord;
		while (itrs.hasNext()) {
			readRecord = itrs.next();
			table.delete(readRecord.getId());
		}
		rs.close();
		table.flush();
	}

}
