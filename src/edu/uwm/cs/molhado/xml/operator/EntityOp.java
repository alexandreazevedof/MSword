package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Operator;


public class EntityOp extends NodeOp {

  public final static EntityOp prototype = new EntityOp();
  
  protected EntityOp() { }

	@Override
  public Operator superOperator() {
    return NodeOp.prototype;
  }
  
	@Override
  public int numChildren(){
    return 1;
  }
	//add by Gen
	public boolean hasChildren(IRNode node) {
		validateOperator(node);
		return tree.hasChild(node, NodeOp.CHILD_LIST);
	}

}
