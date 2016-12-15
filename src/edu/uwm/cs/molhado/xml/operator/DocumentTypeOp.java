package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.tree.Operator;

/*
 * Has no children.
 */

public class DocumentTypeOp extends NodeOp {

  public static final DocumentTypeOp prototype = new DocumentTypeOp();
  
  protected DocumentTypeOp() {}

	@Override
  public Operator superOperator() {
    return NodeOp.prototype;
  }

}
