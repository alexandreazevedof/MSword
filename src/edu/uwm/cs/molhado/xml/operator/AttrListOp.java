package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.tree.Operator;

public class AttrListOp extends NodeListOp implements IList {

  public static final AttrListOp prototype = new AttrListOp();
  
	@Override
  public Operator superOperator() {
    return NodeListOp.prototype;
  }
  
  @Override
  public Operator childOperator(int i) {
    return AttrOp.prototype;
  }
  
	@Override
  public Operator variableOperator() {
    return AttrOp.prototype;
  }
  
  protected AttrListOp(){ }
    
}
