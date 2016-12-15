package edu.uwm.cs.molhado.svg;

import de.schlichtherle.io.File;
import edu.uwm.cs.molhado.component.Project;
import java.io.IOException;

public class SvgMolhado {
  public static String defaultRepo = "/tmp/repo";

  public static void main(String[] args) throws IOException{
    if (args[0].equals("import")){
      Project project = new Project(new File(defaultRepo), "Test");
      project.importFile(new File(args[1]));
      project.commit("1.2", "Initial version");
      project.store();
    } else if (args[0].equals("ci")){
      Project project = Project.load(new File(defaultRepo));
    //  project.checkIn(new File(args[1]));
      project.commit("--", "--");
      project.store();
    } else if (args[0].equals("co")){
      Project project = Project.load(new File(defaultRepo));
    //  project.checkOut(args[1]);
    }
  }

}
