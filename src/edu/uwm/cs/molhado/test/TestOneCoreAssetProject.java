package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.spl.CoreAssetProject;
import java.io.IOException;
import java.io.File;

public class TestOneCoreAssetProject extends FluidRegistryLoading {

	public static void main(String[] args) throws IOException {

		File path = new File("irdata/TestOneCoreAssetProject");
		Project project = null;

		if (args[0].equals("store")) {

			project = new CoreAssetProject(path, "Test");

			Version.saveVersion(project.getInitialVersion());

			DirectoryComponent c1 = project.createDirectory("child 1");
			DirectoryComponent c2 = project.createDirectory("child 2");
			project.createDirectory(c1, "child 1.1");
			project.createDirectory(c1, "child 1.2");
			project.createDirectory(c2, "child 2.1");
			project.createDirectory(c2, "child 2.2");

			Version v1 = project.commit("v1.2", "v1.2");
			project.importFile(new File("testxmlfiles/test.xml"));
			Version v2 = project.commit("v1.3", "v1.3");
			project.store();

			Version.restoreVersion();

		} else {
			project = CoreAssetProject.load(path);
		}

		project.printVersionTree();
		project.printProjectTree();

	}

}
