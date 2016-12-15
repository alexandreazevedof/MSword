/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.delta;

/**
 *
 * @author chengt
 */
public abstract class Update extends Edit{

	private String oldValue;
	private String newValue;

	public Update(int nodeId, String oldValue, String newValue){
		super(nodeId);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * @return the oldValue
	 */
	public final String getOldValue() {
		return oldValue;
	}

	/**
	 * @param oldValue the oldValue to set
	 */
	public final void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	/**
	 * @return the newValue
	 */
	public final String getNewValue() {
		return newValue;
	}

	/**
	 * @param newValue the newValue to set
	 */
	public final void setNewValue(String newValue) {
		this.newValue = newValue;
	}
}
