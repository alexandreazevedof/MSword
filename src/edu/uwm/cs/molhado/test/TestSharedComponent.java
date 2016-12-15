package edu.uwm.cs.molhado.test;

import de.schlichtherle.io.File;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.SharedComponent;
import edu.uwm.cs.molhado.component.SharedDirectoryComponent;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class TestSharedComponent {

	private static File path1 = new File("/tmp/irdata/p1");
	private static File path2 = new File("/tmp/irdata/p2");
	private static Project p1;
	private static Project p2;

	public static void test2(String[] args){
	}


	public static void main(String[] args) throws IOException{
		if (args[0].equals("store")){
			p1 = new Project(path1, "p1");
			p2 = new Project(path2, "p2");

			Version.setVersion(p1.getInitialVersion());
			DirectoryComponent c1 = new DirectoryComponent(p1, p1.getRootComponent(),
              "c1");
      Version v1 = p1.commit("","");

			DirectoryComponent c2 = new DirectoryComponent(p1, c1, "c2");
      Version v2 = p1.commit("", "");

			DirectoryComponent c3 = new DirectoryComponent(p1, c1, "c3");
      p1.importFile(c3, new File("testfiles/Doc.xml"));
      Version v3 = p1.commit("", "");

			Version.setVersion(p2.getInitialVersion());

  	  SharedComponent s1 = new SharedDirectoryComponent(p2, 
              p2.getRootComponent(), c1, v2);
      p2.commit("","");

      p1.printProjectTree();
			p2.printProjectTree();

      p1.store();
      p2.store();
		} else {
			
      p1 = Project.load(path1);
      p2 = Project.load(path2);

      System.out.println("Project 1 ===========");
      p1.printProjectTree();

      System.out.println("Project 2============");
      p2.printProjectTree();
		}
	}

}
