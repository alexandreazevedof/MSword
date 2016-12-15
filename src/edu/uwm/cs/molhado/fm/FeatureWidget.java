package edu.uwm.cs.molhado.fm;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.VersionTracker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Direction;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.GeomUtil;
import org.openide.util.Utilities;

/**
 *
 * @author chengt
 */
public class FeatureWidget extends LabelWidget implements FMWidget{
	public static final Color COLOR_DEFAULT = Color.BLACK;
	public static final Color COLOR_CONFLICT = Color.RED;
	public static final Color COLOR60_SELECT = new Color(0xFF8500);
	public static final Color COLOR60_HOVER = new Color(0x5B67B0);
	public static final Color COLOR60_HOVER_BACKGROUND = new Color(0xB0C3E1);
	    private static org.netbeans.api.visual.border.Border BORDER_SHADOW_NORMAL = BorderFactory.createImageBorder (new Insets (4, 1, 1, 4), Utilities.loadImage ("edu/uwm/cs/molhado/fm/resources/shadow_normal.png")); // NOI18N
	public final static Border BORDER_DEFAULT = BorderFactory.createCompositeBorder(
			  BorderFactory.createBevelBorder(true, Color.DARK_GRAY), 
			  BorderFactory.createLineBorder(3, 8, 3, 8, Color.BLACK));
			  //BorderFactory.createLineBorder(0, 1, 0, 1, Color.BLACK)); 
			//  BorderFactory.createLineBorder(2, 7, 2, 7, Color.BLACK));
	public final static Border BORDER_HOVER = BorderFactory.createCompositeBorder(
			  BorderFactory.createBevelBorder(true, Color.BLUE), 
			  BorderFactory.createLineBorder(3, 8, 3, 8, Color.BLUE));
	public final static Border BORDER_DEAD_FEATURE = BorderFactory.createCompositeBorder(
			  BorderFactory.createBevelBorder(true, Color.YELLOW), 
			  BorderFactory.createLineBorder(3, 8, 3, 8, Color.YELLOW));
	public final static Border BORDER_COMMON_FEATURE = BorderFactory.createCompositeBorder(
			  BorderFactory.createBevelBorder(true, Color.CYAN), 
			  BorderFactory.createLineBorder(3, 8, 3, 8, Color.CYAN));
		//	  BorderFactory.createLineBorder(2, 7, 2, 7, Color.BLUE));
	public final static Border BORDER_SELECTED = BorderFactory.createCompositeBorder(
			  BorderFactory.createBevelBorder(true, Color.GREEN), 
			  BorderFactory.createLineBorder(3, 8, 3, 8,Color.GREEN)); // BorderFactory.createLineBorder(2, 7, 2, 7, Color.GREEN));
	public final static Border BORDER_CONFLICT = BorderFactory.createCompositeBorder(
			  BorderFactory.createBevelBorder(true, Color.RED), 
			  BorderFactory.createLineBorder(3, 8, 3, 8, Color.RED));
			  //BorderFactory.createLineBorder(2, 7, 2, 7, Color.RED));
	private final static WidgetAction editorAction = 
			  ActionFactory.createInplaceEditorAction(new LabelTextFieldEditor());
	private Anchor topAnchor;
	private AnchorShape topAnchorShape;
	private Anchor bottomAnchor;
	private AnchorShape bottomAnchorShape;
	private IRNode node;
	private VersionTracker tracker;
	public FeatureWidget(FMGraphScene scene, IRNode node){ 
		super(scene, node.getSlotValue(FMComponent.nodeNameAttr));
		tracker = scene.getTracker();
		this.node = node;
		getActions().addAction(editorAction);
		setBorder(BORDER_DEFAULT);
		//setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
		setOpaque(true);
		int nodeType = node.getIntSlotValue(FMComponent.nodeTypeAttr);
		//setBackground(Color.LIGHT_GRAY);
		if (nodeType == FMComponent.NODE_ALT_GROUP){
		//	setBackground(COLOR60_HOVER_BACKGROUND);
		} else if (nodeType == FMComponent.NODE_OR_GROUP){
		//	setBackground(COLOR60_HOVER);
		}
	}

	@Override
	public void setLabel(final String string) {
		if (tracker != null)
		tracker.executeIn(new Runnable(){
			public void run() {
				node.setSlotValue(FMComponent.nodeNameAttr, string);
			}
		});
		super.setLabel(string);
	}

	
	public String getName(){
		return getLabel();
	}

	public IRNode getIRNode(){
		return node;
	}
	

}

class LabelTextFieldEditor implements TextFieldInplaceEditor {
	public boolean isEnabled(Widget widget) { return true; }
	public String getText(Widget widget) { return ((LabelWidget) widget).getLabel(); }
	public void setText(Widget widget, String text) { ((LabelWidget) widget).setLabel(text); }
}
class SingleSidedAnchor extends Anchor{
	private Direction d;
	private int gap;
	public SingleSidedAnchor(Widget widget, Direction d, int gap){
		super(widget); this.d = d; this.gap = gap;
	}
	@Override
	public Result compute(Entry entry) {
		Point relatedLocation = getRelatedSceneLocation();
        Point oppositeLocation = getOppositeSceneLocation (entry);
        Widget widget = getRelatedWidget ();
        Rectangle bounds = widget.convertLocalToScene (widget.getBounds ());
		  Point center = GeomUtil.center (bounds);
		  if (d == Direction.TOP){
			  return new Anchor.Result (new Point (center.x, bounds.y-gap), Direction.TOP);
		  } else if (d == Direction.BOTTOM){
			  return new Anchor.Result (new Point (center.x, bounds.y + bounds.height + gap), Direction.BOTTOM);
		  } else if (d == Direction.LEFT){
			   return new Anchor.Result (new Point (bounds.x - gap, center.y), Direction.LEFT);
		  } else if (d == Direction.RIGHT){
			  return new Anchor.Result (new Point (bounds.x + bounds.width + gap, center.y), Direction.RIGHT);
		  }
		  return null;
	}
}

class MyCircularAnchor extends Anchor {

    private int radius;

    public MyCircularAnchor (Widget widget, int radius) {
        super (widget);
//        assert widget != null;
        this.radius = radius;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        double angle = Math.atan2 (oppositeLocation.y - relatedLocation.y, oppositeLocation.x - relatedLocation.x);

        Point location = new Point (relatedLocation.x+radius + (int) (radius * Math.cos (angle)), radius+relatedLocation.y + (int) (radius * Math.sin (angle)));
        return new Anchor.Result (location, Anchor.DIRECTION_ANY); // TODO - resolve direction
    }

}

class CircleAnchorShape implements AnchorShape{
	private boolean filled;
	private int radius;
	public CircleAnchorShape(int radius, boolean filled){
		this.radius = radius;
		this.filled = filled;
	}
	public int getRadius() { return radius; }
	public boolean isLineOriented() { return false; }
	public double getCutDistance() { return radius; }
	public void paint(Graphics2D gd, boolean bln) {
		Ellipse2D.Double elli = new Ellipse2D.Double(-1*radius, -1*radius, 2*radius, 2*radius);
		if (filled) { gd.fill(elli);
		} else { gd.draw(elli); }
	}
}
class ArcAnchorShape implements AnchorShape{
	private int radius;
	private boolean filled;
	public ArcAnchorShape(int radius, boolean filled){
		this.radius = radius;
		this.filled = filled;
	}
	public boolean isLineOriented() { return false; }
	public int getRadius() { return radius; }
	//public double getCutDistance() { return radius/2; }
	public double getCutDistance() { return radius-3; }
	public void paint(Graphics2D gd, boolean bln) {
		//x,y,w,h,start, extend, type
		//Arc2D d = new Arc2D.Double(-(radius/2.0)-3, -0.6*radius, 1.5*radius, 1.2*radius, 0, -180, Arc2D.OPEN);
		Arc2D d = new Arc2D.Double(-(radius/2.0)-3, -0.8*radius, 1.5*radius, 1.5*radius, 0, -180, Arc2D.OPEN);
		//Arc2D d = new Arc2D.Double(-(radius/2.0)-3, -0.8*radius, 1.5*radius, 1.2*radius, -33, -150, Arc2D.OPEN);
		if (filled) {
			gd.draw(d);
			gd.fill(d);
		} else {
			gd.draw(d);
		}
	}
	
}