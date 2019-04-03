package org.dwbzen.text.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * A data store for Lambda functions (Function<Integer, String>) used in patterns and
 * makes them available to clients during the generation process.
 * There is one global instance.
 * 
 * @author DBacon
 *
 */
public final class FunctionManager {

	private static FunctionManager instance = null;
	
	public static synchronized FunctionManager getInstance() {
		if(instance == null) {
			instance = new FunctionManager();
		}
		return instance;
	}
	
	private Map<String, Function<Integer, String>>	functionMap = new TreeMap<>();
	
	private FunctionManager() {
	}
	
	public void addFunction(String name, Function<Integer, String> function) {
		functionMap.put(name, function);
	}
	
	public final Map<String, Function<Integer, String>> getFunctionMap() {
		return functionMap;
	}

	/**
	 * 
	 * @param name The name of the function you want.
	 * @return the Function<Integer, String> associated to the name, or null if no such function exists
	 */
	public Function<Integer, String> getFunction(String name) {
		return functionMap.get(name);
	}
}
