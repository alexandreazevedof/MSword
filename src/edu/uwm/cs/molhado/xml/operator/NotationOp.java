package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.tree.Operator;


public class NotationOp extends NodeOp {

  public final static NotationOp prototype = new NotationOp();
  
  protected NotationOp(){ }


	@Override
  public Operator superOperator() {
    return NodeOp.prototype;
  }
	
}
