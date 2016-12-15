package edu.uwm.cs.molhado.fm;

import edu.cmu.cs.fluid.ir.IRNode;
import java.awt.BasicStroke;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;


/**
 *
 * @author chengt
 */
public abstract class FMEdgeWidget extends ConnectionWidget implements FMWidget{
	public final static AnchorShape NONE = AnchorShape.NONE;
	public final static AnchorShape ARCH_FILLED = new ArcAnchorShape(13, true);
	public final static AnchorShape ARCH_HOLLOW = new ArcAnchorShape(13, false);
	public final static AnchorShape CIRCLE_FILLED = new CircleAnchorShape(5, true);
	public final static AnchorShape CIRCLE_HALLOW = new CircleAnchorShape(5, false);
	
	public final static float[] DASH = {3f, 3f, 3f, 3f};
	public final static float[] DASH_LARGE = {4f, 4f, 4f, 4f};
	public final static BasicStroke STROKE_DASH = new BasicStroke(1f, 1, 1, 1, DASH, 2);
	public final static BasicStroke STROKE_DASH_LARGE = new BasicStroke(3f, 1, 1, 1, DASH_LARGE, 2);
	public final static BasicStroke STROKE_SOLID = new BasicStroke(.6f);
	public final static BasicStroke STROKE_SOLID_LARGE = new BasicStroke(2f);

	private IRNode edge;

	public FMEdgeWidget(Scene scene, IRNode edge, boolean solid){
		super(scene);
		this.edge = edge;
		if (solid) setStroke(STROKE_SOLID);
		else setStroke(STROKE_DASH);
	}

	public IRNode getIRNode(){ return edge; }
}

class MandatoryEdgeWidget extends FMTreeEdgeWidget{
	public MandatoryEdgeWidget(Scene scene, IRNode edge){ super(scene, edge); }
}

class OptionalEdgeWidget extends FMTreeEdgeWidget{
	public OptionalEdgeWidget(Scene scene, IRNode edge){ super(scene, edge); }
}
class AlternativeEdgeWidget extends FMTreeEdgeWidget{
	public AlternativeEdgeWidget(Scene scene, IRNode edge){ super(scene, edge); }
}

class OrEdgeWidget extends FMTreeEdgeWidget{
	public OrEdgeWidget(Scene scene, IRNode edge){ super(scene, edge); }
}

class FMTreeEdgeWidget extends FMEdgeWidget{
	public FMTreeEdgeWidget(Scene scene, IRNode edge){ super(scene, edge, true); }
}
class FMConstraintEdgeWidget extends FMEdgeWidget{
	public FMConstraintEdgeWidget(Scene scene, IRNode edge){ super(scene, edge, false); }
}
