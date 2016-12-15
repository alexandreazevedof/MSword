package edu.uwm.cs.molhado.xml.operator;

import java.util.*;


import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Operator;
import edu.uwm.cs.molhado.xml.XmlParser;

/**
 * Attr of an XML element. An attribute is part of and XML element rather than
 * part of XML tree.
 * 
 * @author chengt
 * 
 */

public class AttrOp extends NodeOp {

	private static HashSet<SlotInfo> slotInfos;

	public static final AttrOp prototype = new AttrOp();

	@Override
	public Operator superOperator() {
		return NodeOp.prototype;
	}

	protected AttrOp() {
	}
    
	public final static SlotInfo<String> nameAttr = XmlParser.attrNameAttr;
	public final static SlotInfo<String> valueAttr = XmlParser.attrValueAttr;

	public String getName(IRNode node) {
		return getValue2(node, nameAttr);
	}

	public void setName(IRNode node, String newName) {
		setValue2(node, nameAttr, newName);
	}

	public String getValue(IRNode node) {
		return getValue2(node, valueAttr);
	}

	public void setValue(IRNode node, String newName) {
		setValue2(node, valueAttr, newName);
	}

	public IRNode getOwnerElement(IRNode node) {
		validateOperator(node);
		IRNode attrList = tree.getParent(node);
		validateOperator(AttrListOp.prototype, attrList);
		IRNode parent = tree.getParent(attrList);	//node was used here
		validateOperator(ElementOp.prototype, parent);
		return parent;
	}

	
	public IRNode createAttrNode(String attrName, String attrValue) {
		IRNode node = createNode();
		setName(node, attrName);
		setValue(node, attrValue);
		return node;
	}

	//add by Gen
	public boolean hasChildren(IRNode node) {
		validateOperator(node);
		return tree.hasChild(node, NodeOp.CHILD_LIST);
	}
	
	@Override
	public Set<SlotInfo> getAttributes() {
		if (slotInfos == null) {
			slotInfos = new HashSet<SlotInfo>();
			slotInfos.add(nameAttr);
			slotInfos.add(valueAttr);
		}
		return slotInfos;
	}

}
