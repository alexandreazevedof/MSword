package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.tree.Operator;

/**
 * No children
 * @author chengt
 *
 */

public class ProcessingInstructionOp extends NodeOp {

  public final static ProcessingInstructionOp prototype = new ProcessingInstructionOp();
  
  protected ProcessingInstructionOp(){ }
  
	@Override
  public Operator superOperator() {
    return NodeOp.prototype;
  }

}
