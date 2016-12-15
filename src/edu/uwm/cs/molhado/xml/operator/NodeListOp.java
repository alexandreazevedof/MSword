package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
//import edu.cmu.cs.fluid.java.operator.Statement;
import edu.cmu.cs.fluid.tree.Operator;

public class NodeListOp extends NodeOp implements IList {

  public final static NodeListOp prototype = new NodeListOp();
  
  protected NodeListOp(){ }
  
	@Override
  public int numChildren(){
    return -1;
  }
  
	@Override
  public Operator superOperator() {
    return NodeOp.prototype;
  }
  
  @Override
  public Operator childOperator(int i) {
    return NodeOp.prototype;
  }
  
	@Override
  public Operator variableOperator() {
    return NodeOp.prototype;
  }
  
  public int getLength(IRNode node) {
    validateOperator(node);
    return tree.numChildren(node);
  }

  public IRNode getItem(IRNode node, int index) {
    validateOperator(node);
    return tree.getChild(node, index);
  }
  
  public void addItem(IRNode node, IRNode item ){
    validateOperator(node);
    tree.addChild(node, item);
  }
  

}
