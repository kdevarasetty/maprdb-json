package samples.ojai.maprdb_json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.System.*;

public class Driver {

	public static void main(String[] args) {
		Driver.run();
	}

	private static void run() {
		String className = getProperty("test.name");
		String methodName = getProperty("test.method");

		validateCommand(className, methodName);

		//hard-code these. Going the annotation route for this will be
		// an unnecessary complication.
		String setup = "setup";
		String cleanup = "cleanup";
		
		try {
			TestConfig instance = (TestConfig) Class.forName(className).newInstance();
			Class<? extends TestConfig> clazz = instance.getClass();
			
			//get the Method for methodName first for use in the next step.
			Method testM = clazz.getDeclaredMethod(methodName);

			//load the override configuration
			instance.loadOverrideConfig(testM);

			//get below properties from TestConfig
			boolean doCleanupBefore = instance.getBoolean("cleanup.before");
			boolean doSetupBefore = instance.getBoolean("setup.before");
			boolean doCleanupAfter = instance.getBoolean("cleanup.after");
			String depends = instance.getProperty("depends");
			out.println("depends = " + depends);

			/*
			 * get the Method objects for setup, cleanup, depends
			 * invoke each in necessary sequence
			 */
			Method setupM = clazz.getDeclaredMethod(setup);
			Method cleanupM = clazz.getDeclaredMethod(cleanup);
			Method dependsM = null;
			if (depends != null)
				dependsM = clazz.getDeclaredMethod(depends);
			
			//Process begins. Invoke the methods
			if (doCleanupBefore) {
				out.println("Calling cleanup...");
				cleanupM.invoke(instance);
				//reload the override configuration
				instance.loadOverrideConfig(testM);
			}
			if (doSetupBefore) {
				out.println("Calling setup...");
				setupM.invoke(instance);
			}
			if (dependsM != null) {
				out.println("Calling dependency " + dependsM.getName() + "...");
				dependsM.invoke(instance);
			}
			out.println("Calling test method " + testM.getName() + "...");
			testM.invoke(instance);
			if (doCleanupAfter) {
				out.println("Calling cleanup...");
				cleanupM.invoke(instance);
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
				| SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private static void validateCommand(String className, String methodName) {
		if (className == null || className.trim().isEmpty()
				|| methodName == null || methodName.isEmpty()) {
			StringBuilder usage = new StringBuilder();

			usage.append("Usage examples:\n").append(
					"mvn exec:java -Dexec.mainClass=\"samples.ojai.maprdb_json.Driver")
					.append(" -Dtest.name=samples.ojai.maprdb_json.AnotherTest -Dtest.method=queryAll")
					.append("\n\n");

			out.println(usage.toString());
			System.exit(1);
		}
	}

}
