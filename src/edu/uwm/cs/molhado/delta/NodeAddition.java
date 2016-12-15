/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.util.AttributeList;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.HashMap;

/**
 *
 * @author chengt
 */
public class NodeAddition extends Addition {
	private final String name;

	public NodeAddition(int nodeId, String name ){
		super(nodeId);
		this.name = name;
	}

	@Override
	public void patch(HashMap<Integer, IRNode> map) {
		PlainIRNode n = new PlainIRNode();
		map.put(getNodeId(), n);
		initNode(n, name);
	}

	private void initNode(IRNode node, String tagName) {
		node.setSlotValue(SimpleXmlParser3.tagNameAttr, tagName);
		IRSequence<Property> sq = VersionedSlotFactory.prototype.newSequence(-1);
		node.setSlotValue(SimpleXmlParser3.mouidAttr, getNodeId());
		node.setSlotValue(SimpleXmlParser3.nodeTypeAttr, "element");
		node.setSlotValue(SimpleXmlParser3.attrsSeqAttr, sq);
		SimpleXmlParser3.tree.initNode(node);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(indent());
		sb.append("<molhado:node-add ");
		sb.append("nodeid='").append(getNodeId()).append("' ");
		sb.append(" name='").append(name).append("' />");
		return sb.toString();
	}

		@Override
	public void relabelIds(HashMap<Integer, Integer> idMap) {
		Integer newId = idMap.get(getNodeId());
		if (newId == null) return;
		setNodeId(newId);
	}


}
