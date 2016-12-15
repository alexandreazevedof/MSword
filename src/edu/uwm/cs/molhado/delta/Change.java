/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.delta;

import edu.uwm.cs.molhado.util.AttributeList;
import java.util.ArrayList;

/**
 *
 * @author chengt
 */
public class Change{
	int nodeId;
	String newName;
	ArrayList<Integer> newChildren;
	AttributeList newAttributes;
	public Change(int nodeId){
		this.nodeId = nodeId;
	}

	public void setNewName(String name){
		newName = name;
	}
	public void setNewChildren(ArrayList<Integer> children){
		newChildren = children;
	}

	public void setNewAttributes(AttributeList attrs){
		newAttributes = attrs;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("<molhado:edit id='");
		sb.append(nodeId);
		sb.append("'");
		if (newName!= null){
			sb.append(" newname='");
			sb.append(newName);
			sb.append("'");
		}
		if (newChildren != null){
			sb.append(" newchildren='");
			sb.append(newChildren.toString());
			sb.append("'");
		}
		if (newAttributes != null){
			sb.append(" newattributes='");
			sb.append(newAttributes.toString());
			sb.append("'");
		}
		sb.append(" />");
		return sb.toString();
	}
}