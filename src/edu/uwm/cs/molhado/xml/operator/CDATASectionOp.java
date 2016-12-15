package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Operator;


public class CDATASectionOp extends CharacterDataOp{

  public static final CDATASectionOp prototype = new CDATASectionOp();

  protected CDATASectionOp() {}

	@Override
  public Operator superOperator() {
    return CharacterDataOp.prototype;
  }

  public IRNode createCDATASection(String data){
	  return prototype.createCharacterDataNode(data);
  }
  
	@Override
  public IRNode cloneNode(boolean deep, IRNode node){
	  return super.cloneNode(deep, node);
  }
  
}
