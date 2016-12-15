/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 *
 * @author chengt
 */
public class ProjectRepositoryManager implements Serializable {

  private Vector<ProjectRepository> repositories = new Vector<ProjectRepository>();

  private ProjectRepositoryManager() {
  }
  public static final ProjectRepositoryManager instance = new ProjectRepositoryManager();

  public void addRepository(ProjectRepository repo) {
    repositories.add(repo);
  }

  public void removeRepository(ProjectRepository repo) {
    repositories.remove(repo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (ProjectRepository projectRepository : repositories) {
      sb.append(projectRepository);
      sb.append("\n");
    }
    return sb.toString();
  }

  public static void store(String fn) throws IOException {
    ProjectRepositoryManager manager = ProjectRepositoryManager.instance;

    manager.addRepository(new ProjectRepository(new File("/tmp/ir-data"), "test1"));
    manager.addRepository(new ProjectRepository(new File("/tmp/ir-data"), "test2"));

    System.out.println(manager);

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fn)));
    oos.writeObject(manager);
    oos.flush();
    oos.close();
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {

  }
}
