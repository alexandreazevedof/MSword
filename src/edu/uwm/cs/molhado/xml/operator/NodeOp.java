package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Operator;
import edu.cmu.cs.fluid.tree.SyntaxTreeInterface;
import edu.uwm.cs.molhado.xml.XmlParser;

public class NodeOp extends Operator {

	public static NodeOp prototype = new NodeOp();

	protected static final Bundle bundle = XmlParser.bundle;

	@Override
	public Operator superOperator() {
		return Operator.prototype;
	}

	public static final SyntaxTreeInterface tree = XmlParser.tree;

	public static final int CHILD_LIST = 0;

	public static final int ATTR_LIST = 1;

	@Override
	public String name() {
		String complete = getClass().getName();
		return complete.substring(complete.lastIndexOf('.') + 1);
	}

	@Override
	public SyntaxTreeInterface tree() {
		return tree;
	}

	@Override
	public IRNode createNode() {
		return createNode(tree);
	}

	@Override
	public IRNode createNode(SyntaxTreeInterface tree) {
		IRNode nd = new PlainIRNode();
		tree.initNode(nd, this);
		return nd;
	}

	protected void createChildList(SyntaxTreeInterface tree, IRNode nd) {
		IRNode n = new PlainIRNode();
		tree.initNode(n, NodeListOp.prototype);
		tree.setChild(nd, NodeOp.CHILD_LIST, n);
		
	}

	protected void createAttrList(SyntaxTreeInterface tree, IRNode nd) {
		IRNode n = new PlainIRNode();
		tree.initNode(n, AttrListOp.prototype);
		tree.setChild(nd, NodeOp.ATTR_LIST, n);
	}

	public boolean hasChildNodes() {
		return false;
	}

	public IRNode cloneNode(boolean deep, IRNode node){
		//TODO: cloneNode
		return null;
	}
	
	// ====================attribute operation ==============================

	protected final String getValue2(IRNode node, SlotInfo<String> si) {
		validateOperator(node);
		return node.getSlotValue(si);
	}

	protected final void setValue2(IRNode node, SlotInfo<String> si,
			String newValue) {
		validateOperator(node);
		node.setSlotValue(si, newValue);
	}

	// ====================tree operations ================================

	public final IRNode getChild(IRNode node, int index) {
		if (node == null) {
			return null;
		}

		validateOperator(node);
		IRNode childList = tree.getChild(node, NodeOp.CHILD_LIST);
		if (childList == null) {
			return null;
		}
		validateOperator(NodeListOp.prototype, childList);
		return tree.getChild(childList, index);
	}

	public final IRNode getChildList(IRNode node) {
		if (node == null) {
			return null;
		}
		validateOperator(node);
		return tree.getChild(node, CHILD_LIST);
	}

	public static IRIterator getChildIterator(IRNode node) {
		return new IRIterator(tree.getChild(node, CHILD_LIST));
	}

	public final int getNumChildren(IRNode node) {
		validateOperator(node);
		return tree.numChildren(node);
	}

	public final void addChild(IRNode parent, IRNode child) {
		validateOperator(parent);
		IRNode childList = tree.getChild(parent, NodeOp.CHILD_LIST);
		validateOperator(NodeListOp.prototype, childList);
		tree.addChild(childList, child);
	}

	//add by Gen	
	public final void insertChildBefore(IRNode parent, IRNode newChild, IRNode refChild){
		validateOperator(parent);
		IRNode childList = tree.getChild(parent, NodeOp.CHILD_LIST);
		validateOperator(NodeListOp.prototype, childList);
		tree.insertChildBefore(childList,newChild,refChild);
	}
	
	public final void removeChild(IRNode parent, IRNode child) {
		validateOperator(parent);
		IRNode childList = tree.getChild(parent, NodeOp.CHILD_LIST);
		validateOperator(NodeListOp.prototype, childList);
		tree.removeChild(childList, child);
	}
	

	/**
	 * Throws exception if node does not have the operator of the same type as
	 * operator.
	 * 
	 * @param node
	 */

	protected final void validateOperator(IRNode node) {
		Operator op = tree.getOperator(node);
		if (!(op.getClass() == this.getClass())) {
			throw new IllegalArgumentException("Node not " + getClass() + ":"
					+ op);
		}
	}

	protected static void validateOperator(Operator op, IRNode node) {
		((NodeOp) op).validateOperator(node);
	}


}
