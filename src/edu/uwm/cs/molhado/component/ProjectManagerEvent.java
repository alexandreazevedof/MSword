/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.component;

/**
 *
 * @author chengt
 */

public abstract class ProjectManagerEvent {
	private Project p;
	public ProjectManagerEvent(Project p){
		this.p = p;
	}
	public Project getProject(){
		return p;
	}
}
