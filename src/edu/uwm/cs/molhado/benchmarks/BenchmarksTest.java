package edu.uwm.cs.molhado.benchmarks;

/**
 *
 * @author chengt
 */
public class BenchmarksTest {

	/**
	 * Test import XML file to fluid IR in memory
	 * Performance will be influenced by parse time.
	 *
	 * @param filename
	 * @throws Exception
	 */
	public static void benchmarkImport(String filename) throws Exception{

	}

	/**
	 * Loading of fluid IR for a project
	 * @param location
	 */
	public static void benchmarkIRLoding(String location){

	}

	public static void benchmarkComponentLoading(){

	}

	public static void createProject(String location, String name){

	}

	public static void main(String[] args){

		if (args[0].equals("-create")){
			createProject("testproj", "testproj");
		} else {
			//loadProject()
		}
	}

}
