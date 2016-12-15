package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Operator;


public class TextOp extends CharacterDataOp{

  public final static TextOp prototype = new TextOp();
  
  protected TextOp(){}
  

	@Override
  public Operator superOperator() {
    return CharacterDataOp.prototype;
  }

//add by Gen
	public IRNode createTextNode(String data) {
		IRNode node = createNode(tree);
		node.setSlotValue(textValueAttr, data);
		return node;
	}

	public IRNode cloneNode(boolean deep, IRNode node){
		String data = prototype.getData(node);
		return prototype.createCharacterDataNode(data);	
	}
	
}
