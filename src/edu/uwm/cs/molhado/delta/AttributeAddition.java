
package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.tree.Tree;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.HashMap;

/**
 *
 * @author chengt
 */
public class AttributeAddition extends Addition{
	private final String attrName;
	private final String value;

	public AttributeAddition(int targetId, String attrName, String value){
		super(targetId);
		this.attrName = attrName;
		this.value = value;
	}

	@Override
	public void patch(HashMap<Integer, IRNode> map ) {
		IRNode n = map.get(getNodeId());
		IRSequence<Property> seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
		//probably don't need to check.
		for(int i=0; i<seq.size(); i++){
			Property p = seq.elementAt(i);
			if (p.getName().equals(attrName)) return;
		}
		seq.appendElement(new Property(attrName, value));
	}

	public String toString(){
		return indent() + "<molhado:attr-add nodeid= '" + getNodeId() + "' attr='" + attrName + "'" + " value='" + value+"' />";
	}

	@Override
	public void relabelIds(HashMap<Integer, Integer> idMap) {
		Integer newId = idMap.get(getNodeId());
		if (newId == null) return;
		setNodeId(newId);
	}

}
