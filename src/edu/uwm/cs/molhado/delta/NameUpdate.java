
package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Tree;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.HashMap;

/**
 *
 * @author chengt
 */
public final class NameUpdate extends Update{
	public NameUpdate(int nodeId, String oldValue, String newValue){
		super(nodeId, oldValue, newValue);
	}

	@Override
	public void patch(HashMap<Integer, IRNode> map) {
		IRNode n = map.get(getNodeId());
		n.setSlotValue(SimpleXmlParser3.tagNameAttr, getNewValue());
	}

	public String toString(){
		return indent() + "<molhado:name-update nodeid='"+getNodeId()+"' oldvalue='" + getOldValue()+
						"' newvalue='" + getNewValue() + "' />";
	}

		@Override
	public void relabelIds(HashMap<Integer, Integer> idMap) {
		Integer newId = idMap.get(getNodeId());
		if (newId == null) return;
		setNodeId(newId);
	}

}
