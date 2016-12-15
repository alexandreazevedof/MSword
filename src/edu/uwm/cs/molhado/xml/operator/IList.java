package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;

public interface IList {
  
  public IRNode getItem(IRNode list, int index);
  
  public void addItem(IRNode list, IRNode item);
  
  public int getLength(IRNode list);
  
}
