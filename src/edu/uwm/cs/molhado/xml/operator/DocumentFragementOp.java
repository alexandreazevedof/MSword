package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Operator;

/*
 * A fragement of an XML document
 * Can only have the following children:
 *  Element, 
 *  ProcessingInstruction, 
 *  Comment, 
 *  Text, 
 *  CDATASection, 
 *  EntityReference
 */

public class DocumentFragementOp extends NodeOp{

  public final static DocumentFragementOp prototype = new DocumentFragementOp();
  
  protected DocumentFragementOp() { }

	@Override
  public Operator superOperator() {
    return NodeOp.prototype;
  }
  
	//add by Gen
	public boolean hasChildren(IRNode node) {
		validateOperator(node);
		return tree.hasChild(node, NodeOp.CHILD_LIST);
	}

  
}
