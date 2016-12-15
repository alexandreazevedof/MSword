package edu.uwm.cs.molhado.xml.operator;

import java.util.Iterator;

import edu.cmu.cs.fluid.ir.IRNode;

public class IRIterator implements Iterator<IRNode>, Iterable<IRNode>{

  private IRNode node;
  private int loc = 0;
  
  public IRIterator(IRNode node){
    this.node = node;
  }
  
  public boolean hasNext() {
    return (loc < NodeOp.tree.numChildren(node));
  }

  public IRNode next() {
    return NodeOp.tree.getChild(node, loc++);
  }

  public void remove() {
    //do nothing.
  }

  public Iterator<IRNode> iterator() {
    return this;
  }

}
