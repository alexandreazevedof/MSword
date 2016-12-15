/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.fm.dialog;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.component.FileComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author chengt
 */
public class IRWrapperTreeNode extends DefaultMutableTreeNode{
	private IRNode node;
	private FileComponent fc;
	private VersionTracker tracker;
	public IRWrapperTreeNode(FileComponent c, VersionTracker tracker){
		this.node = c.getShadowNode();
		this.fc = c;
		this.tracker = tracker;
	}

	@Override
	public Object getUserObject() {
		return fc;
	}

	public String getPathString(){
		return fc.getPath();
	}
}
