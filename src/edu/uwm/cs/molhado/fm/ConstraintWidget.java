/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.fm;

import edu.cmu.cs.fluid.ir.IRNode;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author chengt
 */
public class ConstraintWidget extends Widget implements FMWidget{

	private IRNode node;
	 private double radius;
	 private boolean fill;

    public ConstraintWidget(Scene scene, IRNode node, double radius, boolean fill) {
        super (scene);
		  this.node = node;
        this.radius = radius;
		  this.fill = fill;
    }
	 
	@Override
    protected Rectangle calculateClientArea () {
        int r = (int) Math.ceil (radius);
        return new Rectangle (r, r,  r,  r);
    }

	@Override
    protected void paintWidget () {
        int r = (int) Math.ceil (radius);
        Graphics2D g = getGraphics ();
        g.setColor (getForeground ());
		//  g.rotate(Math.PI/4.0, r/2,r/2);
		  if (fill) {
			  g.fillRect(r, r, r, r);
			  g.drawRect(r,r,r,r);
		  }
		  else g.drawRect(r, r, r, r);
    }

	public IRNode getIRNode() {
		return node;
	}
	
}

class OrConstraintWidget extends ConstraintWidget{

	public OrConstraintWidget(Scene scene, IRNode n) {
		super(scene, n,8, false);
	}
	
}

class AndConstraintWidget extends ConstraintWidget{
	public AndConstraintWidget(Scene scene, IRNode n){
		super(scene, n, 8, true);
	}
}