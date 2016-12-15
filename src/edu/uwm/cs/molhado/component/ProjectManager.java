package edu.uwm.cs.molhado.component;

import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;
import edu.cmu.cs.fluid.util.UniqueID;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 *
 * @author chengt
 */

public class ProjectManager extends Observable implements Serializable{

	private transient static final long serialVersionUID = 1l;

	public transient final static ProjectManager instance = new ProjectManager();

	private transient final static Set<Project> projects = new HashSet<Project>();

	private ProjectManager(){

	}

	public ProjectManager getInstance(){
		return instance;
	}
	public Project newProject(File path, String name){
		Project p = new Project(path, name);
		//projects.add(p);
		setChanged();
		notifyObservers(new ProjectCreatedEvent(p));
		return p;
	}

	public Project loadProject(File path) throws IOException{
		Project p = Project.load(path);
		//projects.add(p);
		setChanged();
		notifyObservers(new ProjectLoadedEvent(p));
		return p;
	}

	public Project[] getProjects(){
    return Project.getAllProjects();
	}

	public Project findProject(UniqueID id){
		return Project.findProject(id);
	}

	public void closeProject(Project p){
		p.close();
		//projects.remove(p);
		setChanged();
		notifyObservers(new ProjectClosedEvent(p));
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for (Project project : getProjects()) {
			sb.append(project.getName());
			sb.append(", ");
			sb.append(project.getId());
			sb.append(", ");
			sb.append(project.getPath());
			sb.append("\n");
		}
		return sb.toString();
	}

	public void store(File path) throws IOException{
	  ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));	
	  out.writeObject(this);
	  out.close();
	}

	public void load(File path) throws IOException, ClassNotFoundException{
	//	projects.clear();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
		in.readObject();
	}

	public Object  writeReplace(){
		return new SerializeHelper();
	}

	class SerializeHelper implements Serializable{
		transient ProjectManager m = ProjectManager.instance;

		SerializeHelper(){
		}

		private void writeObject(ObjectOutputStream out) throws IOException{
			Project[] projects = m.getProjects();
			out.writeInt(projects.length);
			for (Project project : projects) {
				//TODO: create directory path if it does not exist
				out.writeObject(project.getPath());
				project.store();
			}
		}

		private void readObject(ObjectInputStream in) throws IOException,
				  ClassNotFoundException{
			this.m = ProjectManager.instance;
			int n = in.readInt();
			for(int i=0; i<n; i++){
				File f = (File) in.readObject();
				try{
					m.loadProject(f);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		public Object readResolve(){
			return m;
		}
	}

}
