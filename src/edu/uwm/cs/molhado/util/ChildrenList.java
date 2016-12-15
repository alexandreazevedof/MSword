/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

import edu.cmu.cs.fluid.ir.IRNode;
import java.util.ArrayList;

/**
 *
 * @author chengt
 */
public class ChildrenList {

  private ArrayList<IRNode> list;

  public ChildrenList(){
    list = new ArrayList<IRNode>();
  }

  public ChildrenList(int s){
    list = new ArrayList<IRNode>(s);
  }


}
