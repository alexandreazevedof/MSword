package edu.uwm.cs.molhado.test;

import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.ProjectClosedEvent;
import edu.uwm.cs.molhado.component.ProjectCreatedEvent;
import edu.uwm.cs.molhado.component.ProjectLoadedEvent;
import edu.uwm.cs.molhado.component.ProjectManager;
import edu.uwm.cs.molhado.component.ProjectManagerEvent;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author chengt
 */
public class TestProjectManager implements Observer{

	public static ProjectManager manager = ProjectManager.instance;
	public static String[] paths = { "/tmp/irdata/p1", "/tmp/irdata/p2", "/tmp/irdata/p3" };
	public static String[] names = { "p1", "p2", "p3" };
	public static String managerPath = "/tmp/irdata/manager";

	public static void main(String[] args) throws IOException, ClassNotFoundException{
		manager.addObserver(new TestProjectManager());
		if (args[0].equals("store")){
			for(int i=0; i<paths.length; i++){
				manager.newProject(new File(paths[i]), names[i]);
			}
			manager.store(new File(managerPath));
		} else {
			manager.load(new File(managerPath));
			manager.newProject(new File("/tmp/irdata/p4"), "p4");
			Project p = manager.loadProject(new File("/tmp/irdata/p1"));
			Project p1 = manager.loadProject(new File("/tmp/irdata/p1"));
			manager.closeProject(p1);
		}
	}

	public void update(Observable o, Object arg) {
		ProjectManagerEvent e = (ProjectManagerEvent) arg;
		if (arg instanceof ProjectCreatedEvent){
			System.out.print("Project created: ");
		} else if (arg instanceof ProjectLoadedEvent){
			System.out.print("Project loaded: ");
		} else if (arg instanceof ProjectClosedEvent){
			System.out.print("Project closed: ");
		}
		Project p = e.getProject();
		System.out.println(p.getName() + ", " + p.getId() + ", " + p.getPath());
		System.out.println("===state===");
		System.out.println(manager);
	}
}
