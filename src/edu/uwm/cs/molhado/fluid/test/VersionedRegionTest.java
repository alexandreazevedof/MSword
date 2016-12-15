/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.version.VersionedRegion;

/**
 *
 * @author chengt
 */
public class VersionedRegionTest {

	public static void main(String[] args){
     VersionedRegion r1 = new VersionedRegion();
	  VersionedRegion r2 = new VersionedRegion();

	  IRNode n1 = new PlainIRNode(r1);
	  IRNode n2 = new PlainIRNode(r2);
	  
	  VersionedRegion rx1 = VersionedRegion.getVersionedRegion(n1);
	  VersionedRegion rx2 = VersionedRegion.getVersionedRegion(n2);

	  if (r1 == rx1) { 
		  System.out.println("r1==rx1");
	  }

	  if (r2 == rx2){
		  System.out.println("r2==rx2");
	  }
		
	}
}
