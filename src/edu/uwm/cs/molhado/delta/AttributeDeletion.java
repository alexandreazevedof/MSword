package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRLocation;
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
public class AttributeDeletion extends Deletion{
	private String attrName;
	private String value;
	public AttributeDeletion(int nodeId, String attrName, String value){
		super(nodeId);
		this.attrName = attrName;
		this.value = value;
	}

	/**
	 * @return the attrName
	 */
	public String getAttrName() {
		return attrName;
	}

	/**
	 * @param attrName the attrName to set
	 */
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	@Override
	public void patch(HashMap<Integer, IRNode> map) {
		IRNode n = map.get(getNodeId());
		IRSequence<Property> seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
		seq.hasElements();
		seq.firstLocation();

		for(IRLocation l = seq.firstLocation(); l!= seq.lastLocation(); l=seq.nextLocation(l)){
			Property p = seq.elementAt(l);
			if (p.getName().equals(getAttrName())){
				seq.removeElementAt(l);
				break;
			}
		}
	}

	public String toString(){
		return indent() + "<molhado:attr-del nodeid='"+getNodeId() + "'" + " attr='" + attrName +
						"' value='" + value + "' />";
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

		@Override
	public void relabelIds(HashMap<Integer, Integer> idMap) {
		Integer newId = idMap.get(getNodeId());
		if (newId == null) return;
		setNodeId(newId);
	}


}
