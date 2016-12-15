package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Operator;

/*
 * Element, 
 * ProcessingInstruction, 
 * Comment, 
 * Text, 
 * CDATASection, 
 * EntityReference
 */

public class EntityReferenceOp extends NodeOp{

  public final static EntityReferenceOp prototype = new EntityReferenceOp();
  
  protected EntityReferenceOp() { }



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
