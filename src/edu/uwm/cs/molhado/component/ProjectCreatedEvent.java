/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.component;

/**
 *
 * @author chengt
 */
public class ProjectCreatedEvent extends ProjectManagerEvent{
  public ProjectCreatedEvent(Project p){
	  super(p);
  }
}
