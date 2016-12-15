package edu.uwm.cs.molhado.xml.operator;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Operator;
import edu.cmu.cs.fluid.tree.SyntaxTreeInterface;
import edu.uwm.cs.molhado.xml.XmlParser;

/**
 * # Element -- 
 * Element, 
 * Text, 
 * Comment, 
 * ProcessingInstruction, 
 * CDATASection, 
 * EntityReference
 * 
 * @author chengt
 *
 */

public class ElementOp extends NodeOp {

	private HashSet<SlotInfo> slotInfos;

	private SlotInfo<String> elementNameSlotInfo = XmlParser.elementNameAttr;

	public final static ElementOp prototype = new ElementOp();

	protected ElementOp() {
	}

	@Override
	public IRNode createNode() {
		IRNode node = super.createNode();
		createChildList(tree, node);
		createAttrList(tree, node);
		return node;
	}

	private boolean hasAttributes;

	@Override
	public Operator childOperator(int i) {
		if (i == 0) {
			return NodeListOp.prototype;
		}
		else if (i == 1) {
			return AttrListOp.prototype;
		}
		return null;
	}

	@Override
	public Operator superOperator() {
		return NodeOp.prototype;
	}

	@Override
	public int numChildren() {
		return 2;
	}

	public IRNode createElementNode(String tagName) {
		return createElementNode(tree, tagName);
	}

	public IRNode createElementNode(SyntaxTreeInterface tree, String tagName) {
		IRNode node = createNode();
		node.setSlotValue(elementNameSlotInfo, tagName);
		return node;
	}

	public String getElementName(IRNode node) {
		return this.getValue2(node, elementNameSlotInfo);
	}

	public String getAttributeValue(IRNode node, String attrName) {
		validateOperator(node);
		IRNode attrList = tree.getChild(node, NodeOp.ATTR_LIST);
		validateOperator(AttrListOp.prototype, attrList);
		int count = AttrListOp.prototype.getLength(attrList);
		for (int i = 0; i < count; i++) {
			IRNode attr = tree.getChild(attrList, i);
			AttrOp op = AttrOp.prototype;
			validateOperator(op, attr);
			String name = op.getName(attr);
			if (attrName.equals(name)) {
				return op.getValue(attr);
			}
		}
		return null;
	}

	public void setAttributeValue(IRNode node, String name, String newValue) {
		validateOperator(node);
		// does it exist
		// if not, create a new one
		validateOperator(node);
		IRNode attrList = tree.getChild(node, NodeOp.ATTR_LIST);
		validateOperator(AttrListOp.prototype, attrList);
		int count = AttrListOp.prototype.getLength(attrList);
		for (int i = 0; i < count; i++) {
			IRNode attr = tree.getChild(attrList, i);
			AttrOp op = AttrOp.prototype;
			validateOperator(op, attr);
			String attrName = op.getName(attr);
			if (attrName.equals(name)) {
				op.setValue(attr, newValue);
				return;
			}
		}
		// if we are here, we don't have an attribute with that name
		// so create a new attribute.
		IRNode attrNode = AttrOp.prototype.createAttrNode(name, newValue);
		ElementOp.prototype.addAttribute(node, attrNode);
	}

	public void removeAttribute(IRNode node, String name) {
		validateOperator(node);
		// does it exist
		// if not, create a new one
		validateOperator(node);
		IRNode attrList = tree.getChild(node, NodeOp.ATTR_LIST);
		validateOperator(AttrListOp.prototype, attrList);
		int count = AttrListOp.prototype.getLength(attrList);
		for (int i = 0; i < count; i++) {
			IRNode attr = tree.getChild(attrList, i);
			AttrOp op = AttrOp.prototype;
			validateOperator(op, attr);
			String attrName = op.getName(attr);
			if (attrName.equals(name)) {
				tree.removeChild(attrList, attr);
				return;
			}
		}
	}

	public void addAttribute(IRNode element, IRNode attr) {
		validateOperator(element);
		IRNode attrList = tree.getChild(element, NodeOp.ATTR_LIST);
		AttrListOp.prototype.addItem(attrList, attr);
	}

	public static IRIterator getAttrIterator(IRNode node) {
		return new IRIterator(tree.getChild(node, NodeOp.ATTR_LIST));
	}

	public boolean hasAttributes(IRNode node) {
		validateOperator(node);
		return tree.hasChild(node, NodeOp.ATTR_LIST);
	}

	public boolean hasChildren(IRNode node) {
		validateOperator(node);
		return tree.hasChild(node, NodeOp.CHILD_LIST);
	}

	@Override
	public Set<SlotInfo> getAttributes() {
		if (slotInfos == null) {
			slotInfos = new HashSet<SlotInfo>();
			slotInfos.add(elementNameSlotInfo);
		}
		return slotInfos;
	}
	
	@Override
	public IRNode cloneNode(boolean deep, IRNode toCopy){
		String name = getElementName(toCopy);
		IRNode cloneNode = createElementNode(name);
		IRIterator attrIt = getAttrIterator(toCopy);
		AttrOp attrOp = AttrOp.prototype;
		while(attrIt.hasNext()){
			IRNode attrNode = attrIt.next();
			String attrName = attrOp.getName(attrNode);
			String attrVal = attrOp.getValue(attrNode);
			
			IRNode cloneAttrNode = attrOp.createAttrNode(attrName, attrVal);
			ElementOp.prototype.addAttribute(cloneNode, cloneAttrNode);
		}
		if (deep){
			IRIterator childIt = getChildIterator(toCopy);
			while(childIt.hasNext()){
				IRNode child = childIt.next();
				NodeOp nop = (NodeOp) tree.getOperator(child);
				IRNode x = nop.cloneNode(deep, child);
				ElementOp.prototype.addChild(cloneNode, x);
			}
		}
		return cloneNode;
	}

}
