/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.fluid.test;

import java.util.Vector;

/**
 *
 * @author chengt
 */
public final class ProjectManager {

  Vector<Project> projects;

  public Project[] getProjects(){
    return new Project[]{};
  }

  public Project getProject(String path, String name){
    for (Project project : projects) {
      if (project.getPath().equals(path) && project.getName().equals(name)) {
        return project;
      }
    }
    return null;
  }

  public void closeProject(Project project){

  }

  public void openProject(String path){

  }

  public void deleteProject(Project project){

  }

}
