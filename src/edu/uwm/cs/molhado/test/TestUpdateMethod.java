/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionCursor;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.XmlComponent;
import edu.uwm.cs.molhado.version.VersionGraphViewForm;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class TestUpdateMethod {

	public static void main(String[] args) throws IOException, Exception {
		File path = new File("irdata/TestOneProject");
		final Project project = new Project(path, "Test");


		Version.setVersion(project.getInitialVersion());
		final XmlComponent xc1 = new XmlComponent(project, project.getRootComponent(), "simple.xml");
		xc1.importFile(new File("simple.xml"));
		Version v = VersionSupport.commit("initial import", "initial import");
		xc1.dumpContent();
		VersionTracker tracker = new VersionMarker(v);


		tracker.executeIn(new Runnable() {

			public void run() {
				try {
					XmlComponent xc2 = new XmlComponent(project, project.getRootComponent(), "simple.xml");
					xc2.importFile(new File("test.xml"));
					xc1.updateContent("<a molhado:id='0'><b/></a>");
				} catch (Exception ex) {
					Exceptions.printStackTrace(ex);
				}
			}
		});

//		tracker.getVersion().destroy();
		Version.setVersion(tracker.getVersion());
		//	Version.setVersion(v);
		//xc1.dumpContent();
		//xc1.updateContent("<a molhado:id='0'><b molhado:id='1'><c/></b></a>");
		//Version.bumpVersion();
		Version v2 = VersionSupport.commit("final", "final");

		Version.setVersion(v2);

		xc1.dumpContent();

		project.printProjectTree();

		VersionGraphViewForm f = new VersionGraphViewForm(project.getInitialVersion());
		f.setVisible(true);

	}
}
