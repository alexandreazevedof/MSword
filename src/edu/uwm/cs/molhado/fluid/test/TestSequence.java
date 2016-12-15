/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.ir.IRLocation;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.SimpleExplicitSlotFactory;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;

/**
 *
 * @author chengt
 */
public class TestSequence {

  public static void testVersionSquence(){

    IRSequence<String> seq = VersionedSlotFactory.prototype.newSequence(-1);
    seq.appendElement("hello");
    Version v1 = Version.getVersion();
    seq.appendElement("world");
    Version v2 = Version.getVersion();
    Version.saveVersion(v1);
    System.out.println(seq.size());
    Version.restoreVersion();
    Version.saveVersion(v2);
    System.out.println(seq.size());
    Version.restoreVersion();

  }

  public static void main(String[] args){

    testVersionSquence();
    System.out.println("==========");
    IRSequence<String> seq = SimpleExplicitSlotFactory.prototype.newSequence(-1);

    seq.appendElement("hello");
    seq.appendElement("world");
    seq.appendElement("there");
    for(int i=0; i<3; i++){
      System.out.println(seq.elementAt(i));
    }

    System.out.println("=================");
    for(IRLocation l = seq.lastLocation(); seq.hasElements(); l = seq.prevLocation(l)){
      seq.removeElementAt(l);
    }
    for(int i=0; i<seq.size(); i++){
      System.out.println(seq.elementAt(i));
    }
    System.out.println("=================");
    seq.appendElement("hello");
    seq.appendElement("world");
    seq.appendElement("there");
    for(int i=0; i<3; i++){
      System.out.println(seq.elementAt(i));
    }
  }

}
