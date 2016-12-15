package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
//import edu.cmu.cs.fluid.java.operator.Statement;
import edu.cmu.cs.fluid.tree.Operator;

/**
 * Document is the root of the XML tree.  
 * It can have the following children:
 *   Element (maximum of one), 
 *   ProcessingInstruction, 
 *   Comment
 *   DocumentType (maximum of one)
 */

public class DocumentOp extends NodeOp {
	
	public static final DocumentOp prototype = new DocumentOp();
	
	protected DocumentOp() { }
	
	
	public IRNode getDoctype(IRNode doc){
		return null;
	}
	
	//-------- dom level 1 methods ------------------------------//
	
	public IRNode getDocumentElement(IRNode root){
		return DocumentOp.prototype.getChild(root, 0);
	}
	
	public IRNode createElement(String tagName){
		return ElementOp.prototype.createElementNode(tagName);
	}
	
	public IRNode createTextNode(String data){
		return TextOp.prototype.createTextNode(data);
	}
	
	public IRNode createComment(String data){
		return CommentOp.prototype.createCommentNode(data);
	}
	
	public IRNode createCDATASection(String data){
		return CDATASectionOp.prototype.createCDATASection(data);
	}
	
	public IRNode createProcessingInstruction(String target, String data){
		throw new XmlException("createProcessingInstruction is not implemented");
	}
	
	public IRNode createAttribute(String name){
		return AttrOp.prototype.createAttrNode(name, "");
	}
	
	public IRNode createEntityReference(String name){
		throw new XmlException("createProcessingInstruction is not implemented");
	}
	
	public IRNode getEntityReference(String name){
		throw new XmlException("createProcessingInstruction is not implemented");
	}
	
	public IRNode getElementsByTagName(String tagName){
		throw new XmlException("createProcessingInstruction is not implemented");
	}
	
	//----------------------------------------------------------
	

	//TODO: can this be rename??  
	@Override
	public IRNode createNode(){
		IRNode node = super.createNode();
		createChildList(tree, node);
		return node;
	}
	
	@Override
	public Operator superOperator() {
		return NodeOp.prototype;
	}
	
	@Override
	public Operator childOperator(int i) {
		if (i==0) {
			return NodeListOp.prototype;
		}
		return null;
	}

	//add by Gen
	//TODO: probably not correct.  Always has CHILD_LIST but that doesn't
	//mean it has children.  The list may be empty.
	
	public boolean hasChildren(IRNode node) {
		validateOperator(node);
		return tree.hasChild(node, NodeOp.CHILD_LIST);
	}
	
	@Override
	public int numChildren(){
		return 1;
	}
	
	
}
