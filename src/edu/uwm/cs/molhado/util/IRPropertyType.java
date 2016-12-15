/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

import edu.cmu.cs.fluid.ir.IRInput;
import edu.cmu.cs.fluid.ir.IROutput;
import edu.cmu.cs.fluid.ir.IRPersistent;
import edu.cmu.cs.fluid.ir.IRType;
import java.io.IOException;
import java.util.Comparator;

/**
 *
 * @author chengt
 */
public class IRPropertyType implements IRType<Property>{

  public static final IRPropertyType prototype = new IRPropertyType();

  static {
    IRPersistent.registerIRType(prototype,'Y');
  }

  public boolean isValid(Object value) {
    return value instanceof Property;
  }

  public Comparator getComparator() {
    return null;
  }

  public void writeValue(Property p, IROutput out) throws IOException {
    out.writeUTF(p.getName());
    out.writeUTF(p.getValue());
  }

  public Property readValue(IRInput in) throws IOException {
    return new Property(in.readUTF(), in.readUTF());
  }

  public void writeType(IROutput out) throws IOException {
    out.writeByte('Y');
  }

  public IRType readType(IRInput in) throws IOException {
    return this;
  }

  public Property fromString(String s) {
    String [] a = s.split(":");
    return new Property(a[0], a[1]);
  }

  public String toString(Property o) {
    return o.getName() + ":" + o.getValue();
  }

}
