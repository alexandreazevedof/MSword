package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Operator;


public class CommentOp extends CharacterDataOp{

  public static final CommentOp prototype = new CommentOp();
  
	@Override
  public Operator superOperator() {
    return CharacterDataOp.prototype;
  }
  
  protected CommentOp() { }

  //add by Gen
	public IRNode createCommentNode(String data) {
		IRNode node = createNode(tree);
		node.setSlotValue(textValueAttr, data);
		return node;
	}
	
	@Override
	public IRNode cloneNode(boolean deep, IRNode node){
		String data = prototype.getData(node);
		return prototype.createCommentNode(data);	
	}
	
}
