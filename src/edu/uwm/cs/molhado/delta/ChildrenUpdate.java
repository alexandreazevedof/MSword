/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author chengt
 */
public class ChildrenUpdate extends Update{
  private ArrayList<Integer> oldChildrenIds;
	private ArrayList<Integer> newChildrenIds;

	public ChildrenUpdate(int nodeId, ArrayList<Integer> oldChildrenIds, 
			  ArrayList<Integer> newChildrenIds){
		super(nodeId, null, null);
		this.oldChildrenIds = oldChildrenIds;
		this.newChildrenIds = newChildrenIds;
	}

	@Override
	public void patch(HashMap<Integer, IRNode> map) {
		IRNode n = map.get(getNodeId());
		Tree tree = SimpleXmlParser3.tree;
		Iteratable<IRNode> children = tree.children(n);
		while(children.hasNext()){
			IRNode c = children.next();
			tree.removeChild(n, c);
		}
		for(Integer i:newChildrenIds){
			IRNode c = map.get(i);
			SimpleXmlParser3.tree.appendChild(n, c);
		}
	}

	public String toString(){
		return indent() + "<molhado:children-update nodeid='" + 
				  getNodeId() + "' oldchildren='" + oldChildrenIds + 
				  "' newchildren='" + newChildrenIds + "' />";
	}

		@Override
	public void relabelIds(HashMap<Integer, Integer> idMap) {
		Integer newId = idMap.get(getNodeId());
		if (newId == null) return;
		setNodeId(newId);

		for(int i=0; i<oldChildrenIds.size(); i++){
			newId = idMap.get(oldChildrenIds.get(i));
			if (newId!=null){
				oldChildrenIds.set(i, newId);
			}
		}
		for(int i=0; i<newChildrenIds.size(); i++){
			newId = idMap.get(newChildrenIds.get(i));
			if (newId!=null){
				newChildrenIds.set(i, newId);
			}
		}
	}

}
