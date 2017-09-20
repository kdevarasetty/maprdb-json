package samples.ojai.maprdb_json.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import samples.ojai.maprdb_json.annotations.AboutTest;
import samples.ojai.maprdb_json.annotations.OverrideConf;
import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;

public class Maps {

	private static Map<String, List<String>> categories = new HashMap<>();
	private static Map<String, List<String>> keywords = new HashMap<>();
	private static Map<String, List<String>> useBeans = new HashMap<>();

	private static final String pkg = "samples.ojai.maprdb_json";
	// temp variable
	private static final String cwd = "/dockerdisk/github/maprdb-json/target/classes";

	public static void main(String[] args) {
		new Maps().build();
	}

	private List<Path> allPaths = new ArrayList<>();

	/**
	 * load from properties file(s)
	 */
	public void load() {

	}

	/**
	 * store in properties file(s)
	 */
	public void store() {
		build();
		persist();
	}

	/**
	 * 1. get the class file list for each package(recursive) 2. get annotations for
	 * each class(class-level and method-level) 3. store each relevant property in
	 * the maps
	 */
	private void build() {
		String rootPkg = cwd + pkg.replaceAll(".", "/");
		Path rootPath = Paths.get(rootPkg);
		getAllPackages(rootPath);// all packages/paths

		Map<String, List<Annotation>> classLevelConfs = new HashMap<>();
		Map<String, Map<String, OverrideConf>> methodLevelConfs = new HashMap<>();

		// seems inefficient not to use walkTree approach
		// with double listing of a path's files
		for (Path path : allPaths) {
			List<String> classes = getClassesInPackage(path);

			for (String clazz : classes) {
				Class class1 = null;
				try {
					class1 = Class.forName(clazz);
					Map<String, OverrideConf> methodAnns = new HashMap<>();
					List<Annotation> classAnns = new ArrayList<>();
					classLevelConfs.put(class1.getName(), classAnns);
					methodLevelConfs.put(class1.getName(), methodAnns);
					getClassAndMethodConfs(class1, classAnns, methodAnns);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		sanitizeMaps(classLevelConfs, methodLevelConfs);
		populateMaps(classLevelConfs, methodLevelConfs);
	}

	private void sanitizeMaps(Map<String, List<Annotation>> classLevelConfs,
			Map<String, Map<String, OverrideConf>> methodLevelConfs) {
		if (classLevelConfs == null || methodLevelConfs == null)
			return;

		Iterator<Entry<String, List<Annotation>>> iterator = classLevelConfs.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, List<Annotation>> entry = iterator.next();
			if (remove(entry.getValue())) {
				iterator.remove();
			}
		}

		Iterator<Entry<String, Map<String, OverrideConf>>> iterator1 = methodLevelConfs.entrySet().iterator();
		while (iterator1.hasNext()) {
			Entry<String, Map<String, OverrideConf>> entry = iterator1.next();
			Iterator<Entry<String, OverrideConf>> iterator2 = entry.getValue().entrySet().iterator();
			while (iterator2.hasNext()) {
				if (remove(iterator2.next().getValue())) {
					iterator2.remove();
				}
			}

			if (entry.getValue().isEmpty())
				iterator1.remove();
		}
	}

	private boolean remove(OverrideConf overrideConf) {
		if (overrideConf == null)
			return true;
		return false;
	}

	private boolean remove(List<Annotation> anns) {
		if (anns.get(0) == null)
			return true;
		return false;
	}

	private void populateMaps(Map<String, List<Annotation>> classLevelConfs,
			Map<String, Map<String, OverrideConf>> methodLevelConfs) {
		populateCategories(classLevelConfs, methodLevelConfs);
		populateKeywords(classLevelConfs, methodLevelConfs);
	}

	private void populateKeywords(Map<String, List<Annotation>> classLevelConfs,
			Map<String, Map<String, OverrideConf>> methodLevelConfs) {
		populateIndexes(classLevelConfs, "test.keywords", keywords);
		populateOverrideIndexes(methodLevelConfs, "test.keywords", keywords);
	}

	private void populateCategories(Map<String, List<Annotation>> classLevelConfs,
			Map<String, Map<String, OverrideConf>> methodLevelConfs) {
		populateIndexes(classLevelConfs, "test.category", categories);
		populateOverrideIndexes(methodLevelConfs, "test.category", categories);
	}
	
	private void populateOverrideIndexes(Map<String, Map<String, OverrideConf>> methodLevelConfs, String key,
			Map<String, List<String>> index) {
		for (Entry<String, Map<String, OverrideConf>> conf : methodLevelConfs.entrySet()) {
			String className = conf.getKey();
			Map<String, OverrideConf> overrideMap = conf.getValue();
			overrideMap.entrySet().forEach(entry -> {
				OverrideConf overrideConf = entry.getValue();//null check already done
				List<Property> properties = Arrays.asList(overrideConf.value());
				List<Property> list = properties.stream().filter(p -> p.name().equals(key))
						.collect(Collectors.toList());
				
				list.forEach(p -> {
					List<String> words = Arrays.asList(p.value().split(","));
					words.forEach(word -> {
						List<String> strings = index.get(word.trim());
						if (strings == null) {
							strings = new ArrayList<>();
							index.put(word.trim(), strings);
						}
						strings.add(conf.getKey() + "." + entry.getKey());
					});
				});
			});
		}		
	}

	private void populateIndexes(Map<String, List<Annotation>> classLevelConfs, String key,
			Map<String, List<String>> index) {
		for (Entry<String, List<Annotation>> conf : classLevelConfs.entrySet()) {
			AboutTest aboutTest = (AboutTest) conf.getValue().get(1);
			if (aboutTest != null) {
				List<Property> properties = Arrays.asList(aboutTest.value());
				List<Property> list = properties.stream().filter(p -> p.name().equals(key))
						.collect(Collectors.toList());
				list.forEach(p -> {
					List<String> words = Arrays.asList(p.value().split(","));
					words.forEach(word -> {
						List<String> strings = index.get(word.trim());
						if (strings == null) {
							strings = new ArrayList<>();
							index.put(word.trim(), strings);
						}
						strings.add(conf.getKey());
					});
				});
			}
		}
	}

	private void getClassAndMethodConfs(Class clazz, List<Annotation> classAnns, Map<String, OverrideConf> methodAnns) {
		classAnns.addAll(getClassConfs(clazz));

		Method[] methods = getMethods(clazz);
		List<Method> methodList = Arrays.asList(methods);
		methodList.stream().forEach(m -> methodAnns.put(m.getName(), getOverrideConf(m)));
	}

	private List<String> getClassesInPackage(Path pkg) {
		if (!Files.isDirectory(pkg))
			return null;

		// list files in pkg
		// filter necessary classes
		try {
			List<Path> absClasses = Files.list(pkg)
					.filter(path -> (!Files.isDirectory(path) && path.toString().endsWith(".class")))
					.collect(Collectors.toList());
			return absClasses.stream()
					.map(c -> c.toString().substring(cwd.length() + 1).replaceAll("/", ".").replaceAll(".class", ""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void getAllPackages(Path rootPkg) {
		List<Path> list = getPackages(rootPkg);
		if (list.isEmpty())
			return;
		allPaths.addAll(list);
		list.forEach(pkg -> getAllPackages(pkg));
	}

	private List<Path> getPackages(Path rootPkg) {
		if (!Files.isDirectory(rootPkg))
			return null;

		// list files in given parent path
		try {
			return Files.list(rootPkg).filter(path -> Files.isDirectory(path)).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Annotation> getClassConfs(Class clazz) {
		List<Annotation> anns = new ArrayList<>();
		Annotation useBean = clazz.getAnnotation(UseBean.class);
		Annotation aboutTest = clazz.getAnnotation(AboutTest.class);
		anns.add(useBean);
		anns.add(aboutTest);
		return anns;
	}

	private OverrideConf getOverrideConf(Method m) {
		return m.getDeclaredAnnotation(OverrideConf.class);
	}

	private Method[] getMethods(Class clazz) {
		return clazz.getDeclaredMethods();
	}

	/**
	 * persist all maps as properties files in resources dir of maven project store
	 * a map type under a directory
	 */
	private void persist() {

	}

	public void listCategories() {

	}

	public void listKeywords() {

	}
}
