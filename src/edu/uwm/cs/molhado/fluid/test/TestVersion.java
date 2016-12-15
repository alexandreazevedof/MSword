/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;

/**
 *
 * @author chengt
 */
public class TestVersion extends FluidRegistryLoading{

	public static void main(String[] args){
  	Version.printVersionTree();
		VersionedRegion b = new VersionedRegion();
		Version.printVersionTree();
	}


}
