package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.HashMap;

/**
 *
 * @author chengt
 */
public final class AttributeUpdate extends Update{
	private String attrName;

	public AttributeUpdate(int nodeId, String attrName, String oldValue, String newValue){
		super(nodeId, oldValue, newValue);
		this.attrName = attrName;
	}

	/**
	 * @return the attrName
	 */
	public final String getAttrName() {
		return attrName;
	}

	/**
	 * @param attrName the attrName to set
	 */
	public final void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	@Override
	public void patch(HashMap<Integer, IRNode> map) {
		IRNode n = map.get(getNodeId());
		IRSequence<Property> seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
		for(int i=0; i<seq.size(); i++){
			Property p = seq.elementAt(i);
			if (p.getName().equals(getAttrName())){
				p.setValue(getNewValue());
				break;
			}
		}
	}

	@Override
	public String toString(){
		return indent() + "<molhado:attr-update nodeid='" + getNodeId() + "' attr='" + attrName
						+ "' oldvalue='" + getOldValue() + "' newvalue='" + getNewValue() + "' />";
	}

		@Override
	public void relabelIds(HashMap<Integer, Integer> idMap) {
		Integer newId = idMap.get(getNodeId());
		if (newId == null) return;
		setNodeId(newId);
	}


}
