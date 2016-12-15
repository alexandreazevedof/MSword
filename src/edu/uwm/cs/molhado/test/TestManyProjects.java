package edu.uwm.cs.molhado.test;

import edu.uwm.cs.molhado.component.*;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import java.io.IOException;
import java.io.File;
import org.openide.util.Exceptions;

public class TestManyProjects extends FluidRegistryLoading {

	private static int NPROJ = 5;
	private static String STORE = "/tmp/irdata/proj";
	private static Project[] projects = new Project[NPROJ];
	private static File[] paths = new File[NPROJ];

	static {
		for (int i = 0; i < NPROJ; i++) {
			paths[i] = new File(STORE + i);
		}
	}

	public static void createProjects() {
		for (int i = 0; i < NPROJ; i++) {
			projects[i] = new Project(paths[i], "Project " + i);
			createProjectContent(projects[i]);
			projects[i].commit("v1", "v1");
		}
	}

	public static void createProjectContent(Project p) {
		DirectoryComponent root = p.getRootComponent();
		try {
			Component c = p.importFile(root, new File("test.xml"));
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	public static void store() throws IOException {
		createProjects();
		for (Project p : projects) {
			p.store();
		}
	}

	public static void load() throws IOException {
		for (int i = 0; i < NPROJ; i++) {
			projects[i] = Project.load(paths[i]);
		}
	}

	public static void print() {
		for (Project p : projects) {
			System.out.println(p.getName());
		}
	}

	public static void main(String[] args) throws IOException {
		//if (args[0].equals("store")) {
			store();
		//} else {
		//	load();
		//}
		print();
	}
}
