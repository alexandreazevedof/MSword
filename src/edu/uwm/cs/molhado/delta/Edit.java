/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRNode;
import java.util.HashMap;

/**
 *
 * @author chengt
 */
public abstract class Edit {
	private int nodeId;
	private int indentSpaces = 6;

	public Edit(int nodeId){
		this.nodeId = nodeId;
	}

	/**
	 * @return the nodeId
	 */
	public final int getNodeId() {
		return nodeId;
	}

	public abstract void relabelIds(HashMap<Integer, Integer> idMap);
	
	/**
	 * @param nodeId the nodeId to set
	 */
	public final void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	protected String indent(){
		return indent(indentSpaces);
	}

	protected String indent(int spaces){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<spaces; i++){ sb.append(" "); }
		return sb.toString();
	}

	public abstract void patch(HashMap<Integer, IRNode> map);
}
