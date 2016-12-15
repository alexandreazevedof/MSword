/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser2;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 *
 * @author chengt
 */
public class XmlTreeChangeIterator implements Iterator<IRNode>, Iterable<IRNode>{

  public static XmlTreeChangeIterator getChangeIterator(VersionedChangeRecord change, IRNode root,
          Version v1, Version v2){
    return new XmlTreeChangeIterator(change, root, v1, v2);

  }
  private VersionedChangeRecord change;
  private IRNode root;
  private Version v1;
  private Version v2;

  private XmlTreeChangeIterator(VersionedChangeRecord change, IRNode root, Version v1, Version v2) {
    this.change = change;
    this.root = root;
    this.v1 = v1;
    this.v2 = v2;
  }
 /**
   * Stack of enumerations of children
   */
  private Stack<Iterator<IRNode>> toVisit = new Stack<Iterator<IRNode>>();

  private void setNext(IRNode node) {
    next = node;
    Version.saveVersion(v2);
    try {
      toVisit.push(SimpleXmlParser2.getChildren(node).iterator());
    } finally {
      Version.restoreVersion();
    }
  }

  private IRNode next;

  private void findNext() {
    while (!toVisit.isEmpty()) {
      Iterator<IRNode> enm = toVisit.peek();
      while (enm.hasNext()) {
        IRNode n = enm.next();
        if (change.changed(n, v1, v2)) {
          setNext(n);
          return;
        }
      }
      toVisit.pop();
    }
    next = null;
  }

  public boolean hasNext() {
    return next != null;
  }

  public IRNode next() {
    if (next == null) throw new NoSuchElementException("no more changed nodes");
    IRNode n = next;
    findNext();
    return n;
  }
  public Iterator<IRNode> iterator() {
    return this;
  }

  public void remove() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
