/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.delta;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Tree;
import java.util.HashMap;

/**
 *
 * @author chengt
 */
public abstract class Addition extends Edit{

	public Addition(int targetId){
		super(targetId);
	}
}
