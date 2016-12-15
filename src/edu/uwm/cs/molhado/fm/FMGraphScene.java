package edu.uwm.cs.molhado.fm;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRRegion;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.SymmetricEdgeDigraph;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.cmu.cs.fluid.version.VersionTrackerEvent;
import edu.cmu.cs.fluid.version.VersionTrackerListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.ReconnectProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Direction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.action.AlignWithMoveStrategyProvider;
import org.netbeans.modules.visual.action.MoveAction;
import org.netbeans.modules.visual.action.SingleLayerAlignWithWidgetCollector;
import org.openide.util.Exceptions;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.xplain.QuickXplainStrategy;
import org.sat4j.tools.xplain.Xplain;

/**
 *
 * @author chengt
 */
public class FMGraphScene extends GraphScene<IRNode, IRNode>{

	private ArrayList<FMValidationListener> listeners = new ArrayList<FMValidationListener>();

	public void addValidationListener(FMValidationListener l){
		listeners.add(l);
	}

	public void removeValidationListener(FMValidationListener l){
		listeners.remove(l);
	}
	
	static interface FMValidationListener{
		public void validation(boolean valid, ArrayList<RelationConflict> conflicts);
	}
	
	private void createTestStructure(){
		tracker.addVersionTrackerListener(new TrackerListerner());
		root = createNode("A", FMComponent.NODE_SINGULAR, 300, 100);
		final IRNode b = createNode("B", FMComponent.NODE_SINGULAR, 200, 200);
		final IRNode c = createNode("C", FMComponent.NODE_SINGULAR, 300, 200);
		final IRNode d = createNode("D", FMComponent.NODE_SINGULAR, 400, 200);
		final IRNode e = createNode("E", FMComponent.NODE_SINGULAR, 200, 300);
		final IRNode f = createNode("F", FMComponent.NODE_SINGULAR, 300, 300);
		tracker.executeIn(new Runnable() { public void run() {
				connectNodes(root, b, FMComponent.EDGE_MANDATORY);
			}
		});
		tracker.executeIn(new Runnable() { public void run() {
				connectNodes(root, c, FMComponent.EDGE_MANDATORY);
			}
		});
		tracker.executeIn(new Runnable(){ public void run() {
				connectNodes(root, d, FMComponent.EDGE_MANDATORY);
			}
		});
		tracker.executeIn(new Runnable(){ public void run() {
				connectNodes(b, e, FMComponent.EDGE_MANDATORY);
			}
		});
		tracker.executeIn(new Runnable(){ public void run() {
				connectNodes(c, f, FMComponent.EDGE_MANDATORY);
			}
		});
	//	Version v = tracker.getVersion();
	//	System.out.println(v);
		Version.printVersionTree();
	}
	
	class TrackerListerner implements VersionTrackerListener{

		public void versionChanged(VersionTrackerEvent e) {
			System.out.println("Version changed");
		}
		
	}
	
	public static void main(String[] args){
		FMGraphScene scene = new FMGraphScene(new VersionMarker(Version.getInitialVersion()));
		scene.createTestStructure();
		scene.renderModel();
		SceneSupport.show(scene.createView());
	}


	private class MyMoveProvider implements MoveProvider{
		public void movementStarted(Widget widget) { }
		public void movementFinished(Widget widget) { }
		public Point getOriginalLocation(Widget widget) { return widget.getPreferredLocation(); }
		public void setNewLocation(Widget widget, final Point point) {
            widget.setPreferredLocation (point);
				final IRNode n = ((FeatureWidget)widget).getIRNode();
				tracker.executeIn(new Runnable(){
				public void run() {
					n.setSlotValue(FMComponent.xAttr, point.x);
					n.setSlotValue(FMComponent.yAttr, point.y);
					//System.out.println(point.x + ":" + point.y);
				}
			});
		}
	}
	
    private static final BasicStroke STROKE = new BasicStroke (1.0f, BasicStroke.JOIN_BEVEL, 
				BasicStroke.CAP_BUTT, 5.0f, new float[] { 6.0f, 3.0f }, 0.0f);
	 

    private final AlignWithMoveDecorator ALIGN_WITH_MOVE_DECORATOR_DEFAULT = new AlignWithMoveDecorator() {
        public ConnectionWidget createLineWidget (Scene scene) {
            ConnectionWidget widget = new ConnectionWidget (scene);
            widget.setStroke (STROKE);
            widget.setForeground (Color.BLUE);
            return widget;
        }
    };

	  private static final MoveStrategy MOVE_STRATEGY_FREE = new MoveStrategy () {
        public Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation) {
            return suggestedLocation;
        }
    };
	 
	private MoveAction createAlighMoveAction() {
		SingleLayerAlignWithWidgetCollector wc = new SingleLayerAlignWithWidgetCollector(mainLayer, true);
		AlignWithMoveStrategyProvider sp = new AlignWithMoveStrategyProvider(wc,
				  interactionLayer, ALIGN_WITH_MOVE_DECORATOR_DEFAULT, true);
		MoveAction moveAction = new MoveAction(MOVE_STRATEGY_FREE, new MyMoveProvider());
		return moveAction;
	}

	
	private HashMap<IRNode, Integer> node2id        = new HashMap<IRNode, Integer>();
	private HashMap<IRNode, Widget>  node2widget    = new HashMap<IRNode, Widget>();
	private HashMap<Integer, Widget> id2widget      = new HashMap<Integer,Widget>();
	private HashMap<Integer, IRNode> id2node        = new HashMap<Integer,IRNode>();
	private IRRegion region;
	private ArrayList<IRNode> edges                 = new ArrayList<IRNode>();
	private ArrayList<IRNode> nodes                 = new ArrayList<IRNode>();
	private ArrayList<ConnectionWidget> edgeWidgets = new ArrayList<ConnectionWidget>();
	private ArrayList<FeatureWidget>    nodeWidgets = new ArrayList<FeatureWidget>();
	
	private Xplain<ISolver> solver;
	private HashMap<IConstr, RelationConflict> conflicts = new HashMap<IConstr, RelationConflict>();
	//private Collection<IConstr> constraints;
	
	private IRNode root;
	private VersionTracker tracker;

	//for temporary background interaction widgets - rectangular selection
	private LayerWidget backgroundLayer      = new LayerWidget(this);

	//for main widgets
	private LayerWidget mainLayer            = new LayerWidget(this);

	//for connections widgets
	private LayerWidget connectionLayer      = new LayerWidget(this);

	//temporary foreground interaction widgets
	private LayerWidget interactionLayer     = new LayerWidget(this);

	private WidgetAction connectAction       = 
			  ActionFactory.createExtendedConnectAction(interactionLayer,
			  new FMConnectProvider());
	private WidgetAction hoverAction = ActionFactory.createHoverAction(new FMHoverProvider());
	private WidgetAction selectAction = ActionFactory.createSelectAction(new FMSelectProvider());



	JLabel label = new JLabel ("");
	
	public static FMGraphScene self;
	
	public FMGraphScene(VersionTracker tracker){
		this.tracker = tracker;
		addChild(mainLayer);
		addChild(connectionLayer);
		addChild(interactionLayer);
		getActions().addAction(scenePopupAction);
		getActions().addAction(hoverAction);
		setBackground(Color.WHITE);
		//createNodeAndWidget("Model", FMComponent.NODE_SINGULAR, new Point(this.getLocation().x/2, 10));
      //ComponentWidget widget = new ComponentWidget (this, label);
     // addChild (widget);
		self = this;
	}

	public FMGraphScene(VersionTracker tracker, IRNode root){
		this.tracker = tracker;
		this.root = root;
		addChild(mainLayer);
		addChild(connectionLayer);
		addChild(interactionLayer);
		getActions().addAction(scenePopupAction);
		getActions().addAction(hoverAction);
		getActions().addAction(selectAction);
		setBackground(Color.WHITE);
    //  ComponentWidget widget = new ComponentWidget (this, label);
     // addChild (widget);
		self = this;
	}

	public void renderModel(){

		if (root == null) return;
		ArrayList<IRNode> nodes = FMComponent.getNodes(root, tracker); 
		ArrayList<IRNode> edges = FMComponent.getEdges(root, tracker);

		for(IRNode n:nodes) createNodeWidget(n);
		for(IRNode e:edges) createEdgeWidget(e);
		
	//	validateModel();
		//System.out.println("currentId=" + FMComponent.curId);
	}
	
	public IRNode getRoot(){ return root; }
	
	public void setRegion(IRRegion region){
		this.region = region;
	}
	
	private IRNode createGraphEdge(int type) {
		IRNode n = new PlainIRNode(region);
		n.setSlotValue(FMComponent.edgeTypeAttr, type);
		edges.add(n);
		FMComponent.graph.initEdge(n);
		return n;
	}

	private IRNode connectNodes(IRNode parent, IRNode child, int type){
		//should check src to see if the type is appropriate.
		IRNode edgeNode = createGraphEdge(type);
		FMComponent.graph.connect(edgeNode, parent, child);
		if (type != FMComponent.EDGE_IMPLIES && type != FMComponent.EDGE_EXCLUDES) FMComponent.tree.appendChild(parent, child);
		return edgeNode;
	}

	//private ArrayList<IRNode> constraintNodes = new ArrayList<IRNode>();
//
//	private void createConstraintNodeAndWidget(final int type, final Point loc){
//			tracker.executeIn(new Runnable(){
//			public void run() {
//
//				IRNode n = new PlainIRNode(region);
//				if (root == null) root = n;
//				n.setSlotValue(FMComponent.nodeTypeAttr, type);
//				//don't know if we need ID.  May be used in mapping
//				int id = FMComponent.curId++;
//				n.setSlotValue(FMComponent.nodeUidAttr, id);
//				nodes.add(n);
//				id2node.put(id, n) ;
//
//				//FMComponent.tree.initNode(n);
//				FMComponent.graph.initNode(n);
////				constraintNodes.add(n);
//				Widget w = addNode(n);
//				id2widget.put(id,w);
//				if (loc != null) {
//					w.setPreferredLocation(loc);
//				}
//	//			validate();
//			}
//		});
//		validateModel();
//	}
//
	private void createNodeWidget(final IRNode n) {
		if (node2widget.get(n) != null) {
			return;
		}
//		tracker.executeIn(new Runnable() {

//			public void run() {
		Version.saveVersion(tracker.getVersion());
				String name = n.getSlotValue(FMComponent.nodeNameAttr);
				int id = n.getIntSlotValue(FMComponent.nodeUidAttr);
				int x = n.getSlotValue(FMComponent.xAttr);
				int y = n.getSlotValue(FMComponent.yAttr);

				if (id >= FMComponent.curId) {
					FMComponent.curId = id;
					FMComponent.curId++;
				}

				//System.out.println(id);

				id2node.put(id, n);
				Widget w = addNode(n);
				w.setPreferredLocation(new Point(x,y));
				id2widget.put(id, w);
				node2widget.put(n, w);
				validate();
				Version.restoreVersion();
//			}
//		});


		
	}

	private void createEdgeWidget(final IRNode e){


//		tracker.executeIn(new Runnable(){

//			public void run() {
		Version.saveVersion(tracker.getVersion());
		ConnectionWidget e1w = (ConnectionWidget) addEdge(e);
		IRNode source = FMComponent.graph.getSource(e);
		IRNode target = FMComponent.graph.getSink(e);
		setEdgeSource(e, source);
		setEdgeTarget(e, target);
		Version.restoreVersion();
			//}
//		});
//		validate();
	}
	
	private IRNode createNode(final String name, final int type, final int x, final int y){
		final IRNode n = new PlainIRNode(region);
		tracker.executeIn(new Runnable(){
			public void run() {

				n.setSlotValue(FMComponent.nodeNameAttr, name);
				n.setSlotValue(FMComponent.nodeTypeAttr, type);
				n.setSlotValue(FMComponent.xAttr, x);
				n.setSlotValue(FMComponent.yAttr, y);
				int id = FMComponent.curId++;
				n.setSlotValue(FMComponent.nodeUidAttr, id);
				id2node.put(id, n);

				FMComponent.tree.initNode(n);
				FMComponent.graph.initNode(n);

				if (root == null) {
					root = n;
				}
			}
		});
		return n;

	}
	
	private void createNodeAndWidget(final String name, final int type, final Point loc){
		final IRNode n = new PlainIRNode(region);
		tracker.executeIn(new Runnable(){
			public void run() {

				n.setSlotValue(FMComponent.nodeNameAttr, name);
				n.setSlotValue(FMComponent.nodeTypeAttr, type);
				n.setSlotValue(FMComponent.xAttr, loc.x);
				n.setSlotValue(FMComponent.yAttr, loc.y);
				int id = FMComponent.curId++;
				n.setSlotValue(FMComponent.nodeUidAttr, id);
				id2node.put(id, n);

				FMComponent.tree.initNode(n);
				FMComponent.graph.initNode(n);

				if (root == null) {
					root = n;
				}
				Widget w = addNode(n);
				id2widget.put(id,w);
				node2widget.put(n, w);
				if (loc != null) {
					w.setPreferredLocation(loc);
				}
			}
		});
		validate();
		//validateModel();
	}

	public void removeNode(final Widget widget){
		final Object obj = findObject(widget);
		if (!isNode(obj)) {
			return;
		}

		//removing root is unsupported.
		if (obj == root) return;

		tracker.executeIn(new Runnable() {

			public void run() {
				IRNode node = (IRNode) findObject(widget);
				
				int nParents = FMComponent.graph.numParents(node);
				for(int i=0; i<nParents; i++){
					IRNode e = FMComponent.graph.getParentEdge(node, i);
					Widget ew = findWidget(e);
					if (ew!=null) ew.removeFromParent();
					//FMComponent.graph.removeParentEdge(node, e);

				}
				int nChildren = FMComponent.graph.numChildren(node);
				for(int i=0; i<nChildren; i++){
					IRNode e = FMComponent.graph.getChildEdge(node, i);
					Widget ew = findWidget(e);
					if (ew!=null) ew.removeFromParent();
					//FMComponent.graph.removeChildEdge(node, e);
				}
				FMComponent.graph.removeNode(node);
				IRNode p = FMComponent.tree.getParentOrNull(node);
				if (p!=null) FMComponent.tree.removeChild(p, node);
				widget.removeFromParent();
				
				nodes.remove(node);
			}
		});
		validate();
		validateModel();
	}

	public void removeEdge(final Widget widget) {
		final Object obj = findObject(widget);
		if (!isEdge(obj)) {
			return;
		}
		final FMEdgeWidget con = (FMEdgeWidget) widget;

		tracker.executeIn(new Runnable() {

			public void run() {
				IRNode edge = (IRNode) findObject(widget);
				IRNode parent = FMComponent.graph.getSource(edge);
				IRNode child = FMComponent.graph.getSink(edge);
				FMComponent.graph.removeChildEdge(parent, edge);
				FMComponent.graph.removeParentEdge(child, edge);
				int edgeType = con.getIRNode().getIntSlotValue(FMComponent.edgeTypeAttr); 
				if (edgeType != FMComponent.EDGE_IMPLIES && edgeType != FMComponent.EDGE_EXCLUDES) {
					IRNode tParent = FMComponent.tree.getParent(child);
					FMComponent.tree.removeChild(tParent, child);
				}
				widget.removeFromParent();
				edges.remove(edge);
			}
		});

		validate();
		validateModel();
	}


	@Override
	protected Widget attachNodeWidget(IRNode n) {
		int type = n.getIntSlotValue(FMComponent.nodeTypeAttr);
		Widget w = null;
		if (type == FMComponent.NODE_CONST_AND){
			w = new AndConstraintWidget(this, n);
		} else if (type == FMComponent.NODE_CONST_OR){
			w = new OrConstraintWidget(this, n);
		} else {
			w = new FeatureWidget(this, n);
		}
		w.getActions().addAction(connectAction);
		//w.getActions().addAction(ActionFactory.createMoveAction());
		//WidgetAction moveAction = ActionFactory.createAlignWithMoveAction(mainLayer, interactionLayer, null, false);
		WidgetAction moveAction = createAlighMoveAction();
		w.getActions().addAction(moveAction);
		w.getActions().addAction(widgetPopupAction);
		//w.getActions().addAction(selectAction);
		w.getActions().addAction(hoverAction);
		mainLayer.addChild(w);
		return w;
	}

	@Override
	protected Widget attachEdgeWidget(IRNode e) {
		int edgeType = e.getIntSlotValue(FMComponent.edgeTypeAttr);
		ConnectionWidget cw = null;
		if (edgeType == FMComponent.EDGE_IMPLIES || edgeType == FMComponent.EDGE_EXCLUDES){
			//cw = new FMConnectionWidget(this, e, false);
			cw = new FMConstraintEdgeWidget(this, e);
			cw.setRouter(RouterFactory.createOrthogonalSearchRouter(mainLayer));
		} else {	
			IRNode srcNode = FMComponent.graph.getSource(e);
			int type = srcNode.getIntSlotValue(FMComponent.nodeTypeAttr);
			if (type == FMComponent.NODE_CONST_AND || type== FMComponent.NODE_CONST_OR){
				cw = new FMConstraintEdgeWidget(this, e);
				//cw.setRouter(RouterFactory.createOrthogonalSearchRouter(mainLayer));
			} else
		   cw = new FMTreeEdgeWidget(this, e);
		}
		cw.getActions().addAction(edgePopupAction);
		cw.getActions().addAction(hoverAction);
		connectionLayer.addChild(cw);
		return cw;
	}

	@Override
	protected void attachEdgeSourceAnchor(IRNode e, IRNode oldSource, IRNode newSource) {
		int edgeType = e.getIntSlotValue(FMComponent.edgeTypeAttr);
		ConnectionWidget con = (ConnectionWidget) findWidget(e);
		Widget src = findWidget(newSource);
		Anchor anchor = null;
		AnchorShape anchorShape = null;
		if (edgeType == FMComponent.EDGE_IMPLIES) {
			anchor = AnchorFactory.createRectangularAnchor(src);
			anchorShape = AnchorShape.NONE;
		} else if (edgeType == FMComponent.EDGE_EXCLUDES) {
			anchor = AnchorFactory.createRectangularAnchor(src);
			anchorShape = AnchorShape.TRIANGLE_HOLLOW;
		} else {
		   anchor = new SingleSidedAnchor(src, Direction.BOTTOM, 5);
			int nodeType = newSource.getIntSlotValue(FMComponent.nodeTypeAttr);
			switch (nodeType) {
				case FMComponent.NODE_SINGULAR: {
					anchor = new SingleSidedAnchor(src, Direction.BOTTOM, 0);
					anchorShape = FMEdgeWidget.NONE;
					break;
				} case FMComponent.NODE_OR_GROUP: {
					anchor = new SingleSidedAnchor(src, Direction.BOTTOM, 0);
					anchorShape = FMEdgeWidget.ARCH_FILLED;
					break;
				}
				case FMComponent.NODE_ALT_GROUP: {
					anchor = new SingleSidedAnchor(src, Direction.BOTTOM, 0);
					anchorShape = FMEdgeWidget.ARCH_HOLLOW;
					break;
				}case FMComponent.NODE_CONST_AND:{
					anchor = AnchorFactory.createRectangularAnchor(src);
					anchorShape = FMEdgeWidget.NONE;
					break;
				}case FMComponent.NODE_CONST_OR:{
					anchor = AnchorFactory.createRectangularAnchor(src);
					anchorShape = FMEdgeWidget.NONE;
					break;
				}
			}
		}
		con.setSourceAnchor(anchor);
		con.setSourceAnchorShape(anchorShape);
	}

	@Override
	protected void attachEdgeTargetAnchor(IRNode e, IRNode oldTarget, IRNode newTarget) {
		ConnectionWidget con = (ConnectionWidget) findWidget(e);
		Widget src  = findWidget(newTarget);
		Anchor a = null;//new SingleSidedAnchor(src, Direction.TOP, 5);
		AnchorShape as = null;
		int edgeType = e.getIntSlotValue(FMComponent.edgeTypeAttr);
		if (edgeType == FMComponent.EDGE_MANDATORY) {
			a = new SingleSidedAnchor(src, Direction.TOP, 5);
			as = FMEdgeWidget.CIRCLE_FILLED;
		} else if (edgeType == FMComponent.EDGE_OPTIONAL) {
			a = new SingleSidedAnchor(src, Direction.TOP, 5);
			as = FMEdgeWidget.CIRCLE_HALLOW;
		} else if (edgeType == FMComponent.EDGE_IMPLIES) {
			a = AnchorFactory.createRectangularAnchor(src);
			as = AnchorShape.TRIANGLE_FILLED;
		} else if (edgeType == FMComponent.EDGE_EXCLUDES) {
			a = AnchorFactory.createRectangularAnchor(src);
			as = AnchorShape.TRIANGLE_HOLLOW;
		} else {
			a = AnchorFactory.createRectangularAnchor(src);
			as = AnchorShape.NONE;
		}
		con.setTargetAnchor(a);
		con.setTargetAnchorShape(as);
	}
	
	private WidgetAction edgePopupAction = ActionFactory.createPopupMenuAction(
			  new PopupMenuProvider() {

				  public JPopupMenu getPopupMenu(final Widget widget, Point localLocation) {
					  JPopupMenu popup = new JPopupMenu();

					  JMenuItem removeMenuItem = new JMenuItem("Remove");
					  removeMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  removeEdge(widget);
//							  if(activeValidate)  validateRelationships();
						  }
					  });
					  popup.add(removeMenuItem);


					  JMenuItem toOptionalMenuItem = new JMenuItem("Change to Optional");
					  toOptionalMenuItem.addActionListener(new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							  Widget target = ((ConnectionWidget)widget).getTargetAnchor().getRelatedWidget();
							  Widget source = ((ConnectionWidget)widget).getSourceAnchor().getRelatedWidget();
							  final IRNode p = (IRNode)findObject(source);
							  final IRNode c = (IRNode)findObject(target);
							  removeEdge(widget);
							  tracker.executeIn(new Runnable(){
								  public void run() {
									  IRNode e = connectNodes(p, c, FMComponent.EDGE_OPTIONAL);
									  ConnectionWidget e1w = (ConnectionWidget) addEdge(e);
									  setEdgeSource(e, p);
									  setEdgeTarget(e, c);
									  validate();
								  }
							  }); 							  
							  validateModel();
						  }
					  });
					  popup.add(toOptionalMenuItem);

					  JMenuItem toMandatoryMenuItem = new JMenuItem("Change to Mandatory");
					  toMandatoryMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  Widget target = ((ConnectionWidget) widget).getTargetAnchor().getRelatedWidget();
							  Widget source = ((ConnectionWidget) widget).getSourceAnchor().getRelatedWidget();
							  final IRNode p = (IRNode) findObject(source);
							  final IRNode c = (IRNode) findObject(target);
							  removeEdge(widget);
							  tracker.executeIn(new Runnable() {

								  public void run() {
									  IRNode e = connectNodes(p, c, FMComponent.EDGE_MANDATORY);
									  ConnectionWidget e1w = (ConnectionWidget) addEdge(e);
									  setEdgeSource(e, p);
									  setEdgeTarget(e, c);
								  }
							  });
							  validate();
							  validateModel();
						  }
					  });
					  popup.add(toMandatoryMenuItem);


					  JMenuItem toImpliesMenuItem = new JMenuItem("Change to Implies");
					  toImpliesMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  Widget target = ((ConnectionWidget) widget).getTargetAnchor().getRelatedWidget();
							  Widget source = ((ConnectionWidget) widget).getSourceAnchor().getRelatedWidget();
							  final IRNode p = (IRNode) findObject(source);
							  final IRNode c = (IRNode) findObject(target);
							  removeEdge(widget);
							  tracker.executeIn(new Runnable() {

								  public void run() {
									  IRNode e = connectNodes(p, c, FMComponent.EDGE_IMPLIES);
									  ConnectionWidget e1w = (ConnectionWidget) addEdge(e);
									  setEdgeSource(e, p);
									  setEdgeTarget(e, c);
								  }
							  });
							  validate();
							  validateModel();
//							  removeEdge(widget);
//							  if(activeValidate)  validateRelationships();
						  }
					  });
					  popup.add(toImpliesMenuItem);

					  JMenuItem toExcludesMenuItem = new JMenuItem("Change to Excludes");
					  toImpliesMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  Widget target = ((ConnectionWidget) widget).getTargetAnchor().getRelatedWidget();
							  Widget source = ((ConnectionWidget) widget).getSourceAnchor().getRelatedWidget();
							  final IRNode p = (IRNode) findObject(source);
							  final IRNode c = (IRNode) findObject(target);
							  removeEdge(widget);
							  tracker.executeIn(new Runnable() {

								  public void run() {
									  IRNode e = connectNodes(p, c, FMComponent.EDGE_EXCLUDES);
									  ConnectionWidget e1w = (ConnectionWidget) addEdge(e);
									  setEdgeSource(e, p);
									  setEdgeTarget(e, c);
									  validate();
								  }
							  });
							  validateModel();
//							  removeEdge(widget);
//							  if(activeValidate)  validateRelationships();
						  }
					  });
					  popup.add(toExcludesMenuItem);
					  return popup;
				  }
			  });

	private void attachFiles(FeatureWidget widget){

	}
	
	private JMenuItem createMenuItem(String name, ActionListener l) {
		JMenuItem item = new JMenuItem(name);
		item.addActionListener(l);
		return item;
	}
		
	private WidgetAction widgetPopupAction = ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

		public JPopupMenu getPopupMenu(final Widget widget, Point point) {
			if (!(widget instanceof FeatureWidget)) return null;
			JPopupMenu popup = new JPopupMenu();
			popup.add(createMenuItem("Remove", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeNode(widget);
				}
			}));

			popup.add(createMenuItem("Attach files", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					attachFiles((FeatureWidget)widget);
				}
			}));

			//final IRNode n = (IRNode) findObject(widget);
//			tracker.executeIn(new Runnable() {
//				public void run() {
//					JMenuItem requireMenuItem = null;
//					int derive = n.getIntSlotValue(TestTreeGraph.deriveAttr);
//					if (derive == TestTreeGraph.COMP_DEFAULT || derive == TestTreeGraph.COMP_EXCLUDE) {
//						requireMenuItem = new JMenuItem("Make Required");
//						requireMenuItem .addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) { 
//								setDeriveAttribute(widget, "Required", TestTreeGraph.COMP_REQUIRE); }
//						});
//					} else { 
//						requireMenuItem = new JMenuItem("Make Unrequired");
//						requireMenuItem.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) {
//								setDeriveAttribute(widget, null, TestTreeGraph.COMP_DEFAULT);
//							}
//						});
//					}
//					popup.add(requireMenuItem);
//
//					JMenuItem excludeMenuItem = null;
//					if (derive == TestTreeGraph.COMP_DEFAULT || derive == TestTreeGraph.COMP_REQUIRE) {
//						excludeMenuItem = new JMenuItem("Make Excluded");
//						excludeMenuItem.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) {
//								setDeriveAttribute(widget, "Excluded", TestTreeGraph.COMP_EXCLUDE);
//							}
//						});
//					} else { 
//						excludeMenuItem = new JMenuItem("Make Unexcluded");
//						excludeMenuItem.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) {
//								setDeriveAttribute(widget, null, TestTreeGraph.COMP_DEFAULT);
//							}
//						});
//					}
//					popup.add(excludeMenuItem);
//				}
//			});
//
			return popup;
		}
	});

	
	private WidgetAction scenePopupAction = ActionFactory.createPopupMenuAction(
			  new PopupMenuProvider() {
				  String name = "Untitled";
				  public JPopupMenu getPopupMenu(final Widget widget, final Point loc) {
					  JPopupMenu popup = new JPopupMenu();

					  popup.add(createMenuItem("Version Tree", new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							  Version.printVersionTree();
						  }
					  }));

					  popup.add(createMenuItem("New Feature", new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							  createNodeAndWidget(name, FMComponent.NODE_SINGULAR, loc);
						  }
					  }));

					  popup.add(createMenuItem("New Or Group", new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							  createNodeAndWidget(name, FMComponent.NODE_OR_GROUP, loc);
						  }
					  }));
					  popup.add(createMenuItem("New Alternative Group", new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							  createNodeAndWidget(name, FMComponent.NODE_ALT_GROUP, loc);
						  }
					  }));

					  popup.add(createMenuItem("OR Constraint Node", new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							  createNodeAndWidget(name, FMComponent.NODE_CONST_OR, loc);
						  }
					  }));

					  popup.add(createMenuItem("AND Constraint Node", new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							  createNodeAndWidget(name, FMComponent.NODE_CONST_AND, loc);
						  }
					  }));

					  popup.add(createMenuItem("Validate Model", new ActionListener() {
						  public void actionPerformed(ActionEvent e) {
							 validateModel();
						  }
					  }));
					  return popup;
				  }
			  });

	public VersionTracker getTracker() {
		return tracker;
	}

	private class FMHoverProvider implements TwoStateHoverProvider {
		private Widget savedWidget;
		private Border savedBorder;
		private Color  savedForegroundColor;
		private Color  savedBackgroundColor;
		private Stroke savedStroke;
		private final Color  hoverColor = Color.BLUE;

		@Override
		public void unsetHovering(Widget widget) {
			if (widget == null || widget == selectedWidget) return;
			savedWidget.setBackground(savedBackgroundColor);
			savedWidget.setForeground(savedForegroundColor);
		//	savedWidget.setBorder(savedBorder);
			if (this.savedWidget instanceof ConnectionWidget) {
				ConnectionWidget con = (ConnectionWidget) widget;
				con.setStroke(savedStroke);
			} 
			savedWidget = null;
		}

		@Override
		public void setHovering(Widget widget) {
			if (widget == null || widget == selectedWidget || widget instanceof Scene){return;}

			savedWidget = widget;
			savedBackgroundColor = (Color) widget.getBackground();
			savedForegroundColor = widget.getForeground();
			savedBorder = widget.getBorder();

			widget.setBackground(Color.ORANGE);
			widget.setForeground(Color.BLACK);

			if (widget instanceof FMEdgeWidget) {
				ConnectionWidget con = (ConnectionWidget) widget;
				savedStroke = con.getStroke();
			   widget.setForeground(hoverColor);
				if (widget instanceof FMTreeEdgeWidget) {
					con.setStroke(FMEdgeWidget.STROKE_SOLID_LARGE);
				} else if (widget instanceof FMConstraintEdgeWidget) {
					con.setStroke(FMEdgeWidget.STROKE_DASH_LARGE);
				}
			} else if (widget instanceof FeatureWidget) {
		//		widget.setBorder(FeatureWidget.BORDER_HOVER);
			}
		}
	};


	private Widget selectedWidget;
	private class FMSelectProvider implements SelectProvider {
		private Color savedBackgroundColor;
		private Color savedForegroundColor;
		private final Color selectedBackgroundColor = Color.BLUE;
		private final Color selectedForegroundColor = Color.WHITE;

		@Override
		public boolean isAimingAllowed(Widget arg0, Point arg1, boolean arg2) { return false; }

		@Override
		public boolean isSelectionAllowed(Widget arg0, Point arg1, boolean arg2) { return true; }

		@Override
		public void select(Widget arg0, Point arg1, boolean arg2) {
			System.out.println("calling select method");
			if (selectedWidget != null) {
				//selectedWidget.setBackground(savedBackgroundColor);
				selectedWidget.setForeground(savedForegroundColor);
			}
			if (arg0 instanceof Scene) return;
			savedBackgroundColor = (Color) arg0.getBackground();
			savedForegroundColor =  arg0.getForeground();
			selectedWidget = arg0;
			//selectedWidget.setBackground(selectedBackgroundColor);
			selectedWidget.setForeground(selectedForegroundColor);
//			Version version = ((VersionWidget) arg0).getVersion();
//			content.set(Collections.singleton(version), null);
		}
	}
	
	private class FMConnectProvider implements ConnectProvider {

		private IRNode source = null;
		private IRNode target = null;

		@Override
		public boolean isSourceWidget(Widget sourceWidget) {
			Object object = findObject(sourceWidget);
			source = isNode(object) ? (IRNode) object : null;
			return source != null;
		}

		@Override
		public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
			Object object = findObject(targetWidget);
			target = isNode(object) ? (IRNode) object : null;
			if (target != null) {
				// prevent creating a circle
				if (findObject(sourceWidget).equals(target)) {
					return ConnectorState.REJECT_AND_STOP;
				}
				return !source.equals(target) ? ConnectorState.ACCEPT : ConnectorState.REJECT_AND_STOP;
			}
			return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
		}

		@Override
		public boolean hasCustomTargetWidgetResolver(Scene scene) {
			return false;
		}

		@Override
		public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
			return null;
		}
		int edgeCounter;

		private final String[] featureOptItems = new String[]{ "Mandatory", "Optional", "Implies", "Excludes"};
		private final String[] groupOptItems =   new String[]{ "Parent-Child", "Implies", "Excludes"};
		private final String[] boolOptItems = new String[]{"Parent-Child", "Implies", "Excludes"};
		private final String[] constraintItems = new String[]{"Implies", "Excludes"};

		@Override
		public void createConnection(final Widget sourceWidget, final Widget targetWidget) {
			tracker.executeIn(new Runnable() {
				public void run() {
					final FMWidget srcW = (FMWidget) sourceWidget;
					final FMWidget targetW = (FMWidget) targetWidget;
					int srcNodeType = srcW.getIRNode().getIntSlotValue(FMComponent.nodeTypeAttr);
					int targetNodeType = targetW.getIRNode().getIntSlotValue(FMComponent.nodeTypeAttr);

					int nodeType = srcW.getIRNode().getIntSlotValue(FMComponent.nodeTypeAttr);
					Window window = SwingUtilities.getWindowAncestor(self.getView());
					String result = null;
					if (nodeType == 0) {
						if (FMComponent.tree.getParentOrNull(target) == null){
						result = (String) JOptionPane.showInputDialog(window, "Specify Relation", "Specify Relation",
								  JOptionPane.QUESTION_MESSAGE, null, featureOptItems, null);
						}else {
							result = (String) JOptionPane.showInputDialog(window, "Specify Relation", "Specify Relation",
									  JOptionPane.QUESTION_MESSAGE, null, constraintItems, null);
						}
					} else if (nodeType == FMComponent.NODE_OR_GROUP || nodeType == FMComponent.NODE_ALT_GROUP) {

						if (FMComponent.tree.getParentOrNull(target) == null){
							result = "Parent-Child";
						} else {
							result = (String) JOptionPane.showInputDialog(window, "Specify Relation", "Specify Relation",
									  JOptionPane.QUESTION_MESSAGE, null, constraintItems, null);
						}
						
					} else if (nodeType == FMComponent.NODE_CONST_AND || nodeType == FMComponent.NODE_CONST_OR) {
						result = (String) JOptionPane.showInputDialog(window, "Specify Relation", "Specify Relation",
								  JOptionPane.QUESTION_MESSAGE, null, groupOptItems, null);
					}
					if (result == null) {
						return;
					}
					IRNode e = null;
					if (result.equals("Implies")) {
						e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_IMPLIES);
					} else if (result.equals("Excludes")) {
						e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_EXCLUDES);
					} else if (result.equals("Mandatory")) {
						e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_MANDATORY);
					} else if (result.equals("Optional")) {
						e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_OPTIONAL);
					} else if (result.equals("Parent-Child")) {
						if (nodeType == FMComponent.NODE_OR_GROUP) {
							e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_OR);
						} else if (nodeType == FMComponent.NODE_ALT_GROUP) {
							e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_ALTERNATIVE);
						} else if (nodeType == FMComponent.NODE_CONST_AND) {
							e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_CONST_AND);
						} else if (nodeType == FMComponent.NODE_CONST_OR) {
							e = connectNodes(srcW.getIRNode(), targetW.getIRNode(), FMComponent.EDGE_CONST_OR);
						}
					}
					ConnectionWidget e1w = (ConnectionWidget) addEdge(e);
					setEdgeSource(e, srcW.getIRNode());
					setEdgeTarget(e, targetW.getIRNode());
				}
			});
			validate();
			//validateModel();
		};
	}

    private class FMReconnectProvider implements ReconnectProvider {

        IRNode edge;
        IRNode originalNode;
        IRNode replacementNode;

        public void reconnectingStarted (ConnectionWidget connectionWidget, boolean reconnectingSource) {
        }

        public void reconnectingFinished (ConnectionWidget connectionWidget, boolean reconnectingSource) {
        }

        public boolean isSourceReconnectable (ConnectionWidget connectionWidget) {
            Object object = findObject (connectionWidget);
            edge = isEdge (object) ? (IRNode) object : null;
            originalNode = edge != null ? getEdgeSource (edge) : null;
            return originalNode != null;
        }

        public boolean isTargetReconnectable (ConnectionWidget connectionWidget) {
            Object object = findObject (connectionWidget);
            edge = isEdge (object) ? (IRNode) object : null;
            originalNode = edge != null ? getEdgeTarget (edge) : null;
            return originalNode != null;
        }

        public ConnectorState isReplacementWidget (ConnectionWidget connectionWidget, 
					 Widget replacementWidget, boolean reconnectingSource) {
            Object object = findObject (replacementWidget);
            replacementNode = isNode (object) ? (IRNode) object : null;
            if (replacementNode != null)
                return ConnectorState.ACCEPT;
            return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
        }

        public boolean hasCustomReplacementWidgetResolver (Scene scene) {
            return false;
        }

        public Widget resolveReplacementWidget (Scene scene, Point sceneLocation) {
            return null;
        }
        
        public void reconnect (ConnectionWidget connectionWidget, final Widget replacementWidget, 
					 final boolean reconnectingSource) {
			  tracker.executeIn(new Runnable(){
				  public void run() {
					  if (replacementWidget == null) {
					//	  removeEdge(edge);
					  } else if (reconnectingSource) {
					//	  setEdgeSource(edge, replacementNode);
					  } else {
					//	  setEdgeTarget(edge, replacementNode);
					  }
				  }
			});
        }

    }

	public abstract class RelationConflict{
		private IConstr constr;
		private IRNode node; 
		private ArrayList<IRNode> edges;
		public RelationConflict(IConstr cons, IRNode p,ArrayList<IRNode> e) {
			constr = cons;
			node = p;
			edges = e;
		}
		public RelationConflict(IConstr cons, IRNode p) {
			constr = cons;
			node = p;
			edges = new ArrayList<IRNode>();
		}
		public IRNode getNode() { return node; }
		public int getNodeId() {
			Version.saveVersion(tracker.getVersion());
			int id = node.getIntSlotValue(FMComponent.nodeUidAttr);
			Version.restoreVersion();
			return id;
		}
		public String getNodeName() {
			Version.saveVersion(tracker.getVersion());
			String s = node.getSlotValue(FMComponent.nodeNameAttr);
			Version.restoreVersion();
			return s;
		}
		public Widget getNodeWidget() { return findWidget(node); }
		public void setNode(IRNode parent) { this.node = parent; }
		public void addEdge(IRNode e){ edges.add(e); }
		public ArrayList<IRNode> getEdges() { return edges; }
		public ArrayList<ConnectionWidget> getEdgeWidgets() {
			ArrayList<ConnectionWidget> lw = new ArrayList<ConnectionWidget>();
			for (IRNode e : edges) {
				lw.add((ConnectionWidget)findWidget(e));
			}
			return lw;
		}
		public ArrayList<FeatureWidget> getTargetWidgets(){
			ArrayList<ConnectionWidget> edges =  getEdgeWidgets();
			ArrayList<FeatureWidget> widgets = new ArrayList<FeatureWidget>();
			for(ConnectionWidget c :edges){
				Widget w = c.getTargetAnchor().getRelatedWidget();
				if (w instanceof FeatureWidget){
					widgets.add((FeatureWidget)w);
				}
			}
			return widgets;
		}


		public IConstr getConstraints() { return constr; }

//		protected String excludedBy(){
//			String excludedBy = " in conflict or excluded by ";
//			if (child.getIntSlotValue(TestTreeGraph.deriveAttr) == TestTreeGraph.COMP_EXCLUDE){
//				excludedBy = "USER";
//			} else {
//				Iterator<IRNode> parentEdges = TestTreeGraph.graph.parentEdges(child);
//				while(parentEdges.hasNext()){
//					IRNode pEdge = parentEdges.next();
//					IRNode p = TestTreeGraph.graph.getSource(pEdge); 
//					int pId = p.getIntSlotValue(TestTreeGraph.nodeUidAttr);
//					//if (pEdge.getIntSlotValue(TestTreeGraph.edgeNameAttr) ==TestTreeGraph.REL_EXCLUDES){
//						if (solver.model(pId)){
//							excludedBy+= p.getSlotValue(TestTreeGraph.nodeNameAttr) + ", ";
//						}
//				//	} else {
//				//		if (solver.model(pId)){
//				//			excludedBy+= p.getSlotValue(TestTreeGraph.nodeNameAttr) + ", ";
//				//		}
//					}
//			}
//			return excludedBy;
//		}
//	
//
//	protected String requiredBy(){
//		SlotInfo<Integer> deriveAttr = TestTreeGraph.deriveAttr;
//		SlotInfo<Integer> nIdAttr = TestTreeGraph.nodeUidAttr;
//		SlotInfo<Integer> edgeTypeAttr = TestTreeGraph.edgeNameAttr;
//			String requiredBy = "";
//			if (child.getIntSlotValue(deriveAttr) == TestTreeGraph.COMP_REQUIRE){
//				requiredBy = "the USER";
//			} else {
//				Iterator<IRNode> parentEdges = TestTreeGraph.graph.parentEdges(child);
//				while(parentEdges.hasNext()){
//					IRNode pEdge = parentEdges.next();
//					int edgeType = pEdge.getIntSlotValue(edgeTypeAttr);
//					if (edgeType ==TestTreeGraph.REL_ALTERNATIVE 
//							  || edgeType == TestTreeGraph.REL_IMPLIES
//							  || edgeType == TestTreeGraph.REL_MANDATORY 
//							  || edgeType == TestTreeGraph.REL_OPTIONAL 
//							  || edgeType == TestTreeGraph.REl_OR){
//						IRNode p = TestTreeGraph.graph.getSource(pEdge); 
//						int pId = p.getIntSlotValue(nIdAttr);
//						if (solver.model(pId)){
//							requiredBy+= p.getSlotValue(TestTreeGraph.nodeNameAttr) + ", ";
//						}
//					}
//				}
//			}
//			return requiredBy;
//	}
	}

	public class DeadFeature extends RelationConflict{
		public DeadFeature(IRNode node){super(null,node);}
		@Override
		public String toString(){
			return getNodeName() + " is a dead feature.";
		}
	}
	public class FalseOptionalFeature extends RelationConflict{
		public FalseOptionalFeature(IRNode node){super(null,node);}
		@Override
		public String toString(){
			return getNodeName() + " is a false optional feature.";
		}
	}
	public class ImpliesRedundantFeature extends RelationConflict{
		public ImpliesRedundantFeature(IRNode node){super(null,node);}
		@Override
		public String toString(){
			return getNodeName() + " doesn't need implies.";
		}
	}
	public class ExcludedByUser extends RelationConflict{
		public ExcludedByUser(IConstr con, IRNode p){ super(con, p); }
		@Override
		public String toString(){ return getNodeName() + ": can't be excluded by the USER."; }
	}
	public class IncludedByUser extends RelationConflict{
		public IncludedByUser(IConstr con, IRNode p){ super(con, p); }
		@Override
		public String toString(){ return getNodeName() + ": can't included by the USER."; }
	}
	public class MandatoryConflict extends RelationConflict{
		public MandatoryConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p, e); }
		@Override
		public String toString() {
			String s = getNodeName() + " is unable to fulfill mandatory request with " + getTargetWidgets().get(0).getLabel()+".";
			return s;
		}
	}
	public class NoneSelectedOrConflict extends RelationConflict{
		public NoneSelectedOrConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p,e); }
		public NoneSelectedOrConflict(IConstr con, IRNode p){super(con, p);}
		@Override
		public String toString(){ return getNodeName() + ": must select a least one."; }
	}
	public class NoneSelectedAlternativeConflict extends RelationConflict{
		public NoneSelectedAlternativeConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p,e); }
		public NoneSelectedAlternativeConflict(IConstr con, IRNode p){super(con,p);}
		@Override
		public String toString(){ return getNodeName() + ": must select one alternative."; }
	}
	public class MoreThanOneSelectedConflict extends RelationConflict{
		public MoreThanOneSelectedConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p,e); }
		public MoreThanOneSelectedConflict(IConstr con, IRNode p){super(con,p);}
		@Override
		public String toString(){ return  getNodeName() + ": must select only one alternative."; }
	}
	public class ImpliesConflict extends RelationConflict{
		public ImpliesConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p,e); }
		public ImpliesConflict(IConstr con, IRNode p){ super(con, p); }
		@Override
		public String toString(){ return getNodeName() + " is unable to fulfill implies request with " + getTargetWidgets().get(0).getLabel()+"."; }
	}
	public class ExcludesConflict extends RelationConflict{
		public ExcludesConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p,e);}
		public ExcludesConflict(IConstr con, IRNode p ){ super(con, p);}
		@Override
		public String toString() {
			if (getTargetWidgets().isEmpty()) return "";
			return getNodeName() + " and " + 
					  getTargetWidgets().get(0).getLabel() + 
					  " can not exclude one another (both required or mandated by the model).";
		}
	}

	private ArrayList<Widget> highLightedNodes = new ArrayList<Widget>();
	private ArrayList<Color>  savedBackgrounds = new ArrayList<Color>();
	private ArrayList<Color>  savedForegrounds = new ArrayList<Color>();
	private ArrayList<Widget> conflictingEdges = new ArrayList<Widget>();

	private Stroke savedStroke;
	private Color savedEdgeColor;
	private Color savedWidgetLabelColor;
	private Border savedBorder;

	private void resetHighlightedNodes() {
		for (Widget w : highLightedNodes) {
			w.setBorder(FeatureWidget.BORDER_DEFAULT);
			w.setToolTipText(null);
		}
		highLightedNodes.clear();
	}

	private void resetConflictEdgeHighlits() {
		for (Widget e : conflictingEdges) {
			e.setForeground(FeatureWidget.COLOR_DEFAULT);
			e.setToolTipText(null);
		}
		conflictingEdges.clear();
	}	


	public void checkForDeadFeatures(final Xplain solver, 
			  final ArrayList<RelationConflict> conflictList,
			  final ArrayList<IRNode> seen) throws TimeoutException {
		final ArrayList<RelationConflict> list = new ArrayList<RelationConflict>();

				Collection<IConstr> problem;
				for (IRNode e : edges) {
					try {
						int edgeType = e.getIntSlotValue(FMComponent.edgeTypeAttr);
						//might use optional edge.  in the future, we may construct constraints from complex booleans (NOT, AND, OR)
						if (edgeType == FMComponent.EDGE_EXCLUDES) {
							IRNode target = FMComponent.graph.getSink(e);
							VecInt v = new VecInt();
							if (target ==null) continue; //edge is probably deleted 
							int id = target.getIntSlotValue(FMComponent.nodeUidAttr);
							v.push(id);
							if (!solver.isSatisfiable(v)) {
								if (seen.contains(id2node.get(e))) continue;
								FeatureWidget widget = (FeatureWidget) id2widget.get(id);
								highLightedNodes.add(widget);
								//widget.setBackground(Color.CYAN);
								widget.setBorder(FeatureWidget.BORDER_DEAD_FEATURE);
								widget.setToolTipText(widget.getToolTipText() + "dead");
								conflictList.add(new DeadFeature(id2node.get(id)));
								//System.out.println(((FeatureWidget)id2widget.get(id)).getName() + " is dead.");
							}
						}
					} catch (TimeoutException ex) {
						Exceptions.printStackTrace(ex);
					}
				}

	}
		
	public void checkForDeadFeatures2(final Xplain solver, 
			  final ArrayList<RelationConflict> conflictList, 
			  final ArrayList<IRNode> seen) throws TimeoutException {
		final ArrayList<RelationConflict> list = new ArrayList<RelationConflict>();
		checkForDeadFeatures(solver, conflictList, seen);

				Collection<IConstr> problem;
				for (int i = 2; i < FMComponent.curId; i++) {
					IRNode node = id2node.get(i);
					if (node == null) return;
					if (!FMComponent.graph.hasParents(node)) {
						if (seen.contains(id2node.get(i))) continue;
						FeatureWidget widget = (FeatureWidget) id2widget.get(i);
						highLightedNodes.add(widget);
						//widget.setBackground(Color.CYAN);
						widget.setBorder(FeatureWidget.BORDER_DEAD_FEATURE);
						widget.setToolTipText("dead");
						conflictList.add(new DeadFeature(node));
						System.out.println(((FeatureWidget) id2widget.get(i)).getName() + " is dead.");
						continue;
					}
				}
	}

	public void checkForCommonFeatures(final Xplain solver, final ArrayList<RelationConflict> conflictList, 
			  final ArrayList<IRNode> seen) throws TimeoutException {
		final ArrayList<RelationConflict> list = new ArrayList<RelationConflict>();

				Collection<IConstr> problem;
				for (IRNode e : edges) {
					try {
						int edgeType = e.getIntSlotValue(FMComponent.edgeTypeAttr);
						if (edgeType == FMComponent.EDGE_IMPLIES) {
							IRNode target = FMComponent.graph.getSink(e);
							VecInt v = new VecInt();
							int id = target.getIntSlotValue(FMComponent.nodeUidAttr);
							v.push(-id);
							if (!solver.isSatisfiable(v)) {
								if (seen.contains(id2node.get(e))) continue;
								FeatureWidget widget = (FeatureWidget) id2widget.get(id);
								highLightedNodes.add(widget);
								widget.setBorder(FeatureWidget.BORDER_COMMON_FEATURE); 
								//widget.setBackground(Color.YELLOW);
								widget.setToolTipText("false optional");
								conflictList.add(new FalseOptionalFeature(id2node.get(e)));
							//		System.out.println(((FeatureWidget) id2widget.get(id)).getName() + " is common in product line.");
							}
						}
					} catch (TimeoutException ex) {
						Exceptions.printStackTrace(ex);
					}
				}

		
	}
	
	public void checkForCommonFeatures(){

		Collection<IConstr> problem;
		for (IRNode e : edges) {
			int edgeType = e.getIntSlotValue(FMComponent.edgeTypeAttr);
			if (edgeType == FMComponent.EDGE_IMPLIES) {
				IRNode target = FMComponent.graph.getSink(e);
			}
		}
	}
	
	public void validateModel(){
		
		Version.saveVersion(tracker.getVersion());
		ArrayList<RelationConflict> conflictList = new ArrayList<RelationConflict>();
		ArrayList<IRNode> seen = new ArrayList<IRNode>();
		boolean sat = false;
		Collection<IConstr> problem;
		try {
			solver = new Xplain<ISolver>(SolverFactory.newDefault());
			solver.setMinimizationStrategy(new QuickXplainStrategy());
			solver.newVar(FMComponent.curId);
			genCNF(root);
			resetHighlightedNodes();
			resetConflictEdgeHighlits();
			sat = solver.isSatisfiable();
			if (sat) {
				int[] model = solver.model();
				for (int i = 0; i < model.length; i++) {
					if (model[i] > 0) {
						FeatureWidget widget = (FeatureWidget) id2widget.get(model[i]);
					//	widget.setBorder(BorderFactory.createLineBorder(Color.GREEN));
					//	widget.setForeground(Color.GREEN);
					//	widget.setBorder(BorderFactory.createLineBorder(Color.green, 2));
						widget.setBorder(FeatureWidget.BORDER_SELECTED);
					   highLightedNodes.add(widget);	
					}
				}

				checkForDeadFeatures2(solver, conflictList, seen);
				checkForCommonFeatures(solver, conflictList, seen);

			//	return true;
			} else {
				while(!solver.isSatisfiable()){
					problem = solver.explain();
					for (IConstr c : problem) {
						RelationConflict r = conflicts.get(c);
						if (r != null) {
						//   if (seen.contains(r.node)) continue;
					//		seen.add(r.node);
							conflictList.add(r);
							Widget widget = r.getNodeWidget();
							highLightedNodes.add(widget);
							//widget.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
							widget.setBorder(FeatureWidget.BORDER_CONFLICT);
							ArrayList<ConnectionWidget> cons = r.getEdgeWidgets();
							for (Widget con : cons) {
								if (con != null) {
									con.setForeground(FeatureWidget.COLOR_CONFLICT);
									con.setToolTipText(r.toString());
									conflictingEdges.add(con);
								}
							}
						}
						//remove only the constraint in this conflict
						if ((r instanceof ExcludesConflict) || (r instanceof ImpliesConflict)){ 
							solver.removeConstr(c);
						}
					}
				}
			checkForDeadFeatures2(solver, conflictList, seen);
			checkForCommonFeatures(solver, conflictList, seen);
			}
		//	return false;
		} catch (TimeoutException ex) {
			Exceptions.printStackTrace(ex);
		}
		Version.restoreVersion();
	//	validate();
		for(FMValidationListener l:listeners){
			l.validation(sat, conflictList);
		}
		//return false;
	}	
	
	
	private void genCNF(final IRNode root){
//		tracker.executeIn(new Runnable(){

//			public void run() {
		//Version.saveVersion(tracker.getVersion());
		ArrayList<IRNode> seen = new ArrayList<IRNode>();
		try {

			VecInt clause1 = new VecInt(1);
			clause1.push(root.getIntSlotValue(FMComponent.nodeUidAttr));
			solver.addClause(clause1);
			genCNFHelper(root, null, seen);
		} catch (ContradictionException ex) {
			Exceptions.printStackTrace(ex);
		}
		//Version.restoreVersion();
//			}
//		});
	}
	
	/**
	 * Traverse graph starting at node n.  
	 * if parentEdge is an exclusive edge, create clauses for the child but then stop.
	 * otherwise, continue traversing the graph.
	 * @param n
	 * @param parentEdge
	 * @param clauses
	 * @param seen 
	 */

	private void genCNFHelper(IRNode n, IRNode parentEdge, ArrayList<IRNode> seen) 
			  throws ContradictionException {
		if (seen.contains(n)) {
			return;
		}
		seen.add(n);
		genNodeCNF(n);
		Iteratable<IRNode> it = FMComponent.graph.childEdges(n);
		while (it.hasNext()) {
			IRNode edge = it.next();
			IRNode sink = FMComponent.graph.getSink(edge);
			genCNFHelper(sink, edge, seen);
		}

	}

	// for given node, generate CNF for it
	private void genNodeCNF(final IRNode n) throws ContradictionException {
		final SymmetricEdgeDigraph graph     = FMComponent.graph;
		final SlotInfo<String> nodeNameAttr  = FMComponent.nodeNameAttr;
		final SlotInfo<Integer> edgeNameAttr = FMComponent.edgeTypeAttr;
		final SlotInfo<Integer> nodeUidAttr  = FMComponent.nodeUidAttr;


		String parentName     = n.getSlotValue(nodeNameAttr);
		int parentId          = n.getIntSlotValue(nodeUidAttr);
		Iteratable<IRNode> it = graph.childEdges(n);

		ArrayList<IRNode> mandatoryEdges   = new ArrayList<IRNode>();
		ArrayList<IRNode> optionalEdges    = new ArrayList<IRNode>();
		ArrayList<IRNode> orEdges          = new ArrayList<IRNode>();
		ArrayList<IRNode> alternativeEdges = new ArrayList<IRNode>();
		ArrayList<IRNode> impliesEdges     = new ArrayList<IRNode>();
		ArrayList<IRNode> excludesEdges    = new ArrayList<IRNode>();

		while (it.hasNext()) {
			IRNode edge = it.next();
			switch (edge.getIntSlotValue(edgeNameAttr)) { 
			case FMComponent.EDGE_MANDATORY: { mandatoryEdges.add(edge); break;} 
			case FMComponent.EDGE_OPTIONAL: { optionalEdges.add(edge); break;} 
			case FMComponent.EDGE_OR: { orEdges.add(edge); break;}
			case FMComponent.EDGE_ALTERNATIVE: { alternativeEdges.add(edge); break;}
			case FMComponent.EDGE_IMPLIES: { impliesEdges.add(edge); break;}
			case FMComponent.EDGE_EXCLUDES: { excludesEdges.add(edge); break;}
			}
		}

		IConstr c = null;

		//Mandatory
		if (mandatoryEdges.size() > 0) {
			for (int i = 0; i < mandatoryEdges.size(); i++) {
				IRNode child = graph.getSink(mandatoryEdges.get(i));
				String childName = child.getSlotValue(nodeNameAttr);
				int childId = child.getIntSlotValue(nodeUidAttr);

				String desc = parentName + " requires " + childName;
				VecInt clause1 = new VecInt(2);
				clause1.push(-parentId);
				clause1.push(childId);
				c = solver.addClause(clause1);
				

				ArrayList<IRNode> edges = new ArrayList<IRNode>();
				edges.add(mandatoryEdges.get(i));
				conflicts.put(c, new MandatoryConflict(c, n, edges)); 

				VecInt clause2 = new VecInt(2);
				clause2.push(-childId);
				clause2.push(parentId);

				c = solver.addClause(clause2);
				conflicts.put(c, new MandatoryConflict(c, n, edges));
			//	System.out.println(clause1);
			//	System.out.println(clause2);
			}
		}

		//Optional
		if (optionalEdges.size() > 0) {
			for (int i = 0; i < optionalEdges.size(); i++) {
				IRNode child = graph.getSink(optionalEdges.get(i));
				String childName = child.getSlotValue(nodeNameAttr);
				String desc = childName + " is optional for " + parentName;
				int childId = child.getIntSlotValue(nodeUidAttr);
				VecInt clause1 = new VecInt(2);
				clause1.push(-childId);
				clause1.push(parentId);
				c = solver.addClause(clause1);
			//	System.out.println(clause1);
			}
		}

		//Or
		if (orEdges.size() > 0) {
			VecInt clause1 = new VecInt(orEdges.size() + 1);
			clause1.push(-parentId);
			for (IRNode edge : orEdges) {
				IRNode child = graph.getSink(edge);
				int childId = child.getIntSlotValue(nodeUidAttr);
				clause1.push(childId);
			}
			//should we attach a relation object to this constraint?
			c = solver.addClause(clause1);
			conflicts.put(c, new NoneSelectedOrConflict(c, n, orEdges));

			for (IRNode edge : orEdges) {
				IRNode child = graph.getSink(edge);
				int childId = child.getIntSlotValue(nodeUidAttr);
				VecInt clause2 = new VecInt(2);
				clause2.push(-childId);
				clause2.push(parentId);
				c = solver.addClause(clause2);
			//	System.out.println(clause2);
			}
		}

		//alternatives
		if (alternativeEdges.size() > 0) {
			VecInt clause1 = new VecInt(alternativeEdges.size() + 1);
			for (IRNode edge : alternativeEdges) {
				IRNode child = graph.getSink(edge);
				int childId = child.getIntSlotValue(nodeUidAttr);
				clause1.push(childId);
			}
			clause1.push(-parentId);
			//System.out.println(clause1);
			//clauses.add(clause1);
			//should we attach a relation object here?
			c = solver.addClause(clause1);

			for (int j = 0; j < alternativeEdges.size() - 1; j++) {
				IRNode child1 = graph.getSink(alternativeEdges.get(j));
				int c1Id = child1.getSlotValue(nodeUidAttr);
				for (int k = j + 1; k < alternativeEdges.size(); k++) {
					IRNode child2 = graph.getSink(alternativeEdges.get(k));
					int c2Id = child2.getSlotValue(nodeUidAttr);
					VecInt clause2 = new VecInt(2);
					clause2.push(-c1Id);
					clause2.push(-c2Id);
					System.out.println(clause2);
					c = solver.addClause(clause2);
					conflicts.put(c, new MoreThanOneSelectedConflict(c,n , alternativeEdges));
				}
				VecInt clause3 = new VecInt(2);
				clause3.push(-c1Id);
				clause3.push(parentId);
			//	System.out.println(clause3);
				c = solver.addClause(clause3);
			}
			VecInt clause4 = new VecInt(2);
			int c2Id = graph.getSink(alternativeEdges.get(alternativeEdges.size() - 1)).
					  getSlotValue(nodeUidAttr);
			clause4.push(-c2Id);
			clause4.push(parentId);
			//System.out.println(clause4);
			c = solver.addClause(clause4);
		}

		// implies edges
		if (impliesEdges.size() > 0) {
			for (int i = 0; i < impliesEdges.size(); i++) {
				IRNode child = graph.getSink(impliesEdges.get(i));
				int childId = child.getIntSlotValue(nodeUidAttr);
				VecInt clause1 = new VecInt(2);
				clause1.push(-parentId);
				clause1.push(childId);
				//clauses.add(clause1);
				c = solver.addClause(clause1);
				ImpliesConflict conflict = new ImpliesConflict(c,n);
				conflict.addEdge(impliesEdges.get(i));
				conflicts.put(c, conflict);
			//	System.out.println(clause1);
			}
		}

		// exclude edges
		if (excludesEdges.size() > 0) {
			for (int i = 0; i < excludesEdges.size(); i++) {
				IRNode child = graph.getSink(excludesEdges.get(i));
				int childId = child.getIntSlotValue(nodeUidAttr);
				VecInt clause1 = new VecInt(2);
				clause1.push(-parentId);
				clause1.push(-childId);
				//clauses.add(clause1);
				c = solver.addClause(clause1);
				ExcludesConflict conflict = new ExcludesConflict(c,n);
				conflict.addEdge(excludesEdges.get(i));
				conflicts.put(c, conflict);

				VecInt clause2 = new VecInt(2);
				clause2.push(-childId); 
				clause2.push(-parentId);
				c = solver.addClause(clause2);
				ExcludesConflict conflict2 = new ExcludesConflict(c,child);
				conflict.addEdge(excludesEdges.get(i));
				conflicts.put(c, conflict2);

			//	System.out.println(clause1);
			}
		}
	}
}
