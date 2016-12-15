/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.SharedComponent;
import edu.uwm.cs.molhado.component.SharedDirectoryComponent;
import edu.uwm.cs.molhado.component.XmlComponent;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class TestJavaShared {

	private static File pathX = new File("/tmp/irdata/pX");
	private static File pathA = new File("/tmp/irdata/pA");
	private static File pathB = new File("/tmp/irdata/pB");
	private static File pathC = new File("/tmp/irdata/pC");
	static Project X, A, B, C;

	public static void main(String[] args) throws IOException, Exception {
		if (args[0].equals("store")) {
			X = new Project(pathX, "pX");
			A = new Project(pathA, "pA");
			B = new Project(pathB, "pB");
			C = new Project(pathC, "pC");


			Version.setVersion(X.getInitialVersion());

			DirectoryComponent a = new DirectoryComponent(X, X.getRootComponent(), "COMP_a");
			Version Xv1 = X.commit("", "");

			DirectoryComponent b = new DirectoryComponent(X, a, "COMP_b");
			Version Xv2 = X.commit("", "");

			XmlComponent comp = new XmlComponent(X, a, "test.java");
			comp.updateContent("class a {}");
			Version Xv3 = X.commit("", "");
			//X.printProjectTree();

			DirectoryComponent c = new DirectoryComponent(X, a, "COMP_c");
			comp.updateContent("class a {\n");
			Version Xv4 = X.commit("", "");

			Version.setVersion(A.getInitialVersion());
			SharedComponent a_sc = new SharedDirectoryComponent(A, A.getRootComponent(), a, Xv2);
			Version Av1 = A.commit("", "");
			//a_sc.updateToLatest();
			a_sc.forwardChange();

			A.commit("", "");

			Version.setVersion(B.getInitialVersion());
			XmlComponent comp_copy = new XmlComponent(B, B.getRootComponent(), "COMP_c");
			comp.copyTo(Xv4, comp_copy);
			B.commit("", "");

			Version.setVersion(Xv4);
			//	X.removeComponent((Component)component);
			X.commit("", "");

			//	X.printProjectTree();

			A.printProjectTree();

			X.store();
			A.store();
			B.store();
			C.store();

			//	B.printProjectTree();

		} else {
			X = Project.load(pathX);
			A = Project.load(pathA);
			B = Project.load(pathB);
			C = Project.load(pathC);
		}

	}
}
