/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.relation;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.SymmetricEdgeDigraph;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.fluid.test.TestTreeGraph;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.HoverProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDFactory;
import org.netbeans.api.visual.vmd.VMDGraphScene;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.xplain.Xplain;

/**
 *
 * @author chengt
 */
public class RelationGraphScene extends VMDGraphScene {
	 private static final Image GLYPH_INCLUDED = Utilities.loadImage ("icons/included.gif"); 
	 private static final Image GLYPH_EXCLUDED = Utilities.loadImage ("icons/excluded.gif"); 
	 private static final Image GLYPH_DEFAULT = Utilities.loadImage ("icons/default.gif"); 

	Xplain<ISolver> solver;
//	IVecInt assumptions = new VecInt();
	HashMap<IConstr, RelationConflict> conflicts = new HashMap<IConstr, RelationConflict>();

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


		private SlotInfo<String> nodeNameAttr = TestTreeGraph.nodeNameAttr;
		private SlotInfo<Integer> nodeIdAttr = TestTreeGraph.nodeUidAttr;
		private SlotInfo<Integer> edgeTypeAttr = TestTreeGraph.edgeNameAttr;

		public IRNode getNode() {
			return node;
		}

		public int getNodeId() {
			Version.setVersion(tracker.getVersion());
			return node.getIntSlotValue(nodeIdAttr);
		}

		public String getNodeName() {
			Version.setVersion(tracker.getVersion());
			return node.getSlotValue(nodeNameAttr);
		}

		public VMDNodeWidget getNodeWidget() {
			return (VMDNodeWidget)findWidget(getNodeId() + "");
		}

		public void setNode(IRNode parent) {
			this.node = parent;
		}

		public void addEdge(IRNode e){
			edges.add(e);
		}

		public ArrayList<IRNode> getEdges() {
			return edges;
		}

		public ArrayList<VMDConnectionWidget> getEdgeWidgets() {
			ArrayList<VMDConnectionWidget> lw = new ArrayList<VMDConnectionWidget>();
			for (IRNode e : edges) {
				lw.add((VMDConnectionWidget)findWidget(genEdgeId(e) + ""));
			}
			return lw;
		}

		public IConstr getConstraints() {
			return constr;
		}

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

	public class ExcludedByUser extends RelationConflict{
		public ExcludedByUser(IConstr con, IRNode p){
			super(con, p);
		}
		@Override
		public String toString(){
			return getNodeName() + ": can't be excluded by the USER."; 
		}
	}
	
	public class IncludedByUser extends RelationConflict{
		public IncludedByUser(IConstr con, IRNode p){
			super(con, p);
		}
		@Override
		public String toString(){
			return getNodeName() + ": can't included by the USER."; 
		}
	}
	public class MandatoryConflict extends RelationConflict{
		public MandatoryConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p, e); }
		@Override
		public String toString() {

			Version.setVersion(tracker.getVersion());
			//return getNodeName() + " requires " + 
			//		  getChildName() + " but " + getChildName() + " is " + excludedBy();
			return getNodeName() + ": unable to fulfill mandatory request.";
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
		public String toString(){ return  getNodeName() + ": must select only one alternative."; }
	}
	public class ImpliesConflict extends RelationConflict{
		public ImpliesConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p,e); }
		public ImpliesConflict(IConstr con, IRNode p){ super(con, p); }
		public String toString(){ return getNodeName() + ": unable to fulfill implies request. "; }
	}
	public class ExcludesConflict extends RelationConflict{
		public ExcludesConflict(IConstr con, IRNode p, ArrayList<IRNode> e){ super(con, p,e);}
		public ExcludesConflict(IConstr con, IRNode p ){ super(con, p);}
		public String toString() {
			Version.setVersion(tracker.getVersion());
			return getNodeName() + ": exclusion conflicted.";
		}
	}

	private VersionTracker tracker;
//	private Random rand = new Random();
	private final LayerWidget interactionLayer;
	private final LayerWidget connectionLayer;
	private final WidgetAction connectAction;
	private final WidgetAction selectAction;
	private final WidgetAction hoverAction = null;
	
	private final HashMap<String, IRNode> nodeId2irnode = new HashMap<String, IRNode>();
	private final HashMap<Widget, IRNode> widget2irnode = new HashMap<Widget, IRNode>();
	private final HashMap<String, IRNode> edgeId2iredge = new HashMap<String, IRNode>();
	private final HashMap<Widget, IRNode> widget2iredge = new HashMap<Widget, IRNode>();

	private final ArrayList<String> selectedEdges = new ArrayList<String>();
	private final ArrayList<String> selectedNodes = new ArrayList<String>();
	private final ArrayList<VMDNodeWidget> highLightedNodes = new ArrayList<VMDNodeWidget>();
	private final ArrayList<VMDConnectionWidget> conflictingEdges = new ArrayList<VMDConnectionWidget>();
	private Stroke savedStroke;
	private Color savedEdgeColor;
	private Color savedWidgetLabelColor;
	private Border savedBorder;

	private boolean activeValidate = false;

	private int MODE;
	private final int INITIAL = 0;
	private final int INTERACTIVE = 1;

	public RelationGraphScene(VersionTracker tracker) {
		super(VMDFactory.getNetBeans60Scheme());
		this.tracker = tracker;
		connectionLayer = new LayerWidget(this);
		interactionLayer = new LayerWidget(this);
		connectAction = ActionFactory.createExtendedConnectAction(interactionLayer,
				  new SceneConnectProvider());
		selectAction = ActionFactory.createSelectAction(new MySelectProvider());
		//hoverAction = ActionFactory.createHoverAction(new MyHoverProvider());
		//Chain actions = getActions();

		//getActions().addAction(hoverAction);
		addChild(interactionLayer);
		addChild(connectionLayer);
		getActions().addAction(scenePopupAction);
		//interactionLayer.getActions().addAction(scenePopupAction);
	}
	private WidgetAction edgePopupAction = ActionFactory.createPopupMenuAction(
			  new PopupMenuProvider() {

				  public JPopupMenu getPopupMenu(final Widget widget, Point localLocation) {
					  JMenuItem removeMenuItem = new JMenuItem("Remove");
					  removeMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  removeEdge(widget);
							  if(activeValidate)  validateRelationships();
						  }
					  });
					  JPopupMenu popup = new JPopupMenu();
					  popup.add(removeMenuItem);
					  return popup;
				  }
			  });

	
	private WidgetAction widgetPopupAction = ActionFactory.createPopupMenuAction(new PopupMenuProvider() {

		public JPopupMenu getPopupMenu(final Widget widget, Point point) {
			final JPopupMenu popup = new JPopupMenu();
			final IRNode n = widget2irnode.get(widget);
			tracker.executeIn(new Runnable() {
				public void run() {
					JMenuItem requireMenuItem = null;
					int derive = n.getIntSlotValue(TestTreeGraph.deriveAttr);
					if (derive == TestTreeGraph.COMP_DEFAULT || derive == TestTreeGraph.COMP_EXCLUDE) {
						requireMenuItem = new JMenuItem("Make Required");
						requireMenuItem .addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) { 
								setDeriveAttribute(widget, "Required", TestTreeGraph.COMP_REQUIRE); }
						});
					} else { 
						requireMenuItem = new JMenuItem("Make Unrequired");
						requireMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setDeriveAttribute(widget, null, TestTreeGraph.COMP_DEFAULT);
							}
						});
					}
					popup.add(requireMenuItem);

					JMenuItem excludeMenuItem = null;
					if (derive == TestTreeGraph.COMP_DEFAULT || derive == TestTreeGraph.COMP_REQUIRE) {
						excludeMenuItem = new JMenuItem("Make Excluded");
						excludeMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setDeriveAttribute(widget, "Excluded", TestTreeGraph.COMP_EXCLUDE);
							}
						});
					} else { 
						excludeMenuItem = new JMenuItem("Make Unexcluded");
						excludeMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								setDeriveAttribute(widget, null, TestTreeGraph.COMP_DEFAULT);
							}
						});
					}
					popup.add(excludeMenuItem);
				}
			});

			return popup;
		}
	});
	private WidgetAction scenePopupAction = ActionFactory.createPopupMenuAction(
			  new PopupMenuProvider() {

				  public JPopupMenu getPopupMenu(final Widget widget, final Point localLocation) {

					  JPopupMenu popup = new JPopupMenu();
					  JMenuItem removeMenuItem = new JMenuItem("New Component");
					  removeMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  createNewNode(localLocation);
						  }
					  });
					  popup.add(removeMenuItem);

					  JMenuItem collapseMenuItem = new JMenuItem("Collapse All");
					  collapseMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  Collection<String> nodes = getNodes();
							  for (String n : nodes) {
								  ((VMDNodeWidget) findWidget(n)).collapseWidget();
							  }
						  }
					  });
					  popup.add(collapseMenuItem);


					  JMenuItem expandMenuItem = new JMenuItem("Expand All");
					  expandMenuItem.addActionListener(new ActionListener() {

						  public void actionPerformed(ActionEvent e) {
							  Collection<String> nodes = getNodes();
							  for (String n : nodes) {
								  ((VMDNodeWidget) findWidget(n)).expandWidget();
							  }
						  }
					  });
					  popup.add(expandMenuItem);

					  return popup;
				  }
			  });


	private void setDeriveAttribute(final Widget widget, final String desc, final int val) {
		tracker.executeIn(new Runnable() {
			public void run() {
				IRNode node = widget2irnode.get(widget);
				node.setSlotValue(TestTreeGraph.deriveAttr, val);
				switch(val){
					case TestTreeGraph.COMP_REQUIRE:{
						((VMDNodeWidget) widget).setNodeImage(GLYPH_INCLUDED);
						break;
					}
					case TestTreeGraph.COMP_EXCLUDE:{
						((VMDNodeWidget) widget).setNodeImage(GLYPH_EXCLUDED);
						break;
					} 
					case TestTreeGraph.COMP_DEFAULT:{
						((VMDNodeWidget) widget).setNodeImage(GLYPH_DEFAULT);
						break;
					}
				}
				((VMDNodeWidget) widget).setNodeType(desc);
			}
		});
		if (activeValidate) validateRelationships();
	}

	private Collection<IConstr> constraints;


	public void setActiveSelect(boolean b){
		activeValidate = b;
	}

	
	private void resetHighlighteddNodes(){
		for(VMDNodeWidget w:highLightedNodes){
			w.getHeader().setBorder(savedBorder);
		}
	}

	private void resetConflictEdgeHighlits(){
		for(VMDConnectionWidget e:conflictingEdges){
			e.setForeground(savedEdgeColor);
			e.setToolTipText(null);
		}
	}
	
	public boolean validateRelationships() {
		boolean sat = false;
		try {
			constraints = null;
			solver = new Xplain<ISolver>(SolverFactory.newDefault());
			solver.newVar(TestTreeGraph.curId);
			//solver.setExpectedNumberOfClauses(clauses.size());
			genSAT();
			resetHighlighteddNodes();
			resetConflictEdgeHighlits();
			highLightedNodes.clear();
			sat = solver.isSatisfiable();
			if (sat) {
				int[] model = solver.model();
				for (int i = 0; i < model.length; i++) {
					if (model[i] > 0) {
						VMDNodeWidget widget = (VMDNodeWidget) findWidget(model[i] + "");
						//widget.setBorder(BorderFactory.createLineBorder(Color.GREEN));
						//widget.getHeader().setForeground(Color.GREEN);
						widget.getHeader().setBorder(BorderFactory.createLineBorder(Color.green, 2));
					   highLightedNodes.add(widget);	
					}
				}
			} else {
				constraints = solver.explain();
				for (IConstr c : constraints) {
				RelationConflict r = conflicts.get(c);
					if (r !=null) {
						VMDNodeWidget widget = r.getNodeWidget();
						highLightedNodes.add(widget);
						widget.getHeader().setBorder(BorderFactory.createLineBorder(Color.RED, 2));
						ArrayList<VMDConnectionWidget> cons = r.getEdgeWidgets();
						for (VMDConnectionWidget con : cons) {
							if (con != null) {
								con.setForeground(Color.RED);
								con.setToolTipText(r.toString());
								conflictingEdges.add(con);
							}
						}


				//		for (int i = 0; i < c.size (); i++){
				//			int var = Math.abs(c.get(i));
				//				VMDNodeWidget widget = (VMDNodeWidget) findWidget(var + "");
				//				if (widget != null && !solver.model(var)) {
				//					widget.getHeader().setBorder(BorderFactory.createLineBorder(Color.RED, 2));
				//					requiredNodes.add(widget);
				//				}
				//		}
					}
				}
			}
		} catch (TimeoutException ex) {
			Exceptions.printStackTrace(ex);
		}
		validate();
		return sat;
	}

	public ArrayList<RelationConflict> getConflicts(){
		if (constraints == null) {
			return null;
		}
		ArrayList<RelationConflict> l = new ArrayList<RelationConflict>();
		for (IConstr c : constraints) {
			RelationConflict r = conflicts.get(c);
			if (r != null) {
				l.add(r);
			}
		}
		return l;
	}

	private void createNewNode(final Point localLocation) {

		JOptionPane pane = new JOptionPane(
				  "Component Name:", JOptionPane.QUESTION_MESSAGE);
		pane.setWantsInput(true);
		JDialog dialog = pane.createDialog(null, "Enter Text");
	//	dialog.setLocation(localLocation);
		dialog.setLocationRelativeTo(this.getView());
      dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setVisible(true);
		final String name = (String) pane.getInputValue();

		tracker.executeIn(new Runnable() {

			public void run() {
				IRNode n = TestTreeGraph.createNode(name);
				VMDNodeWidget wiget = createNode(n);
				//wiget.setVisible(false);
				wiget.setPreferredLocation(localLocation);
				validate();
			//	wiget.setVisible(true);
				
			}
		});
	}

	public VMDNodeWidget createNode(IRNode node) {
		String nodeId = node.getIntSlotValue(TestTreeGraph.nodeUidAttr) + "";
		nodeId2irnode.put(nodeId, node);

		VMDNodeWidget widget = (VMDNodeWidget) addNode(nodeId);
		widget2irnode.put(widget, node);
		widget.getActions().addAction(widgetPopupAction);
		savedBorder = widget.getHeader().getBorder();

		widget.setNodeName(node.getSlotValue(TestTreeGraph.nodeNameAttr));
		widget.setNodeImage(GLYPH_DEFAULT);
		Widget pin = addPin(nodeId, nodeId + VMDGraphScene.PIN_ID_DEFAULT_SUFFIX);
		validate();

		widget.getActions().addAction(connectAction);
		createPin(nodeId, nodeId + "mandatory", "mandatory", null);
		createPin(nodeId, nodeId + "optional", "optional", null);
		createPin(nodeId, nodeId + "or", "or", null);
		createPin(nodeId, nodeId + "alternative", "alternative", null);
		createPin(nodeId, nodeId + "implies", "implies", null);
		createPin(nodeId, nodeId + "excludes", "excludes", null);
		return widget;
	}

	private void createPin(String nodeID, String pinID, String name, String type) {
		VMDPinWidget pin = (VMDPinWidget) addPin(nodeID, pinID);
		Font f = pin.getPinNameWidget().getFont();
		f = f.deriveFont(6);
		Font font = new Font("Dialog", Font.PLAIN, 10);
		pin.getPinNameWidget().setFont(font);
		pin.setPinName(name);
		pin.getActions().addAction(connectAction);
		pin.getActions().addAction(selectAction);
		pin.setProperties(name, null);
	}

	public void removeEdge(final Widget widget) {
		final Object obj = findObject(widget);
		if (!isEdge(obj)) { return; }
		ConnectionWidget con = (ConnectionWidget) widget;

		tracker.executeIn(new Runnable() {

			public void run() {
				IRNode edge = widget2iredge.get(widget);
				IRNode parent = TestTreeGraph.graph.getSource(edge);
				IRNode child = TestTreeGraph.graph.getSink(edge);

				TestTreeGraph.graph.removeChildEdge(parent, edge);
				TestTreeGraph.graph.removeParentEdge(child, edge);
				removeEdge((String) obj);
				widget.removeFromParent();

				//now if it's exclusive, we must remove the other edge
				int type = edge.getIntSlotValue(TestTreeGraph.edgeNameAttr);
				if (type == TestTreeGraph.REL_EXCLUDES) {
					int numChildren = TestTreeGraph.graph.numChildren(child);
					IRNode edge2 = null;
					IRNode parent2 = null;
					for (int i = 0; i < numChildren; i++) {
						edge2 = TestTreeGraph.graph.getChildEdge(child, i);
						parent2 = TestTreeGraph.graph.getSink(edge2);
						if (parent2 == parent) {
							break;
						}
					}

					String edge2Id = genEdgeId(edge2);
					Widget edge2Widget = findWidget(edge2Id);
					TestTreeGraph.graph.removeChildEdge(child, edge2);
					TestTreeGraph.graph.removeParentEdge(parent2, edge2);
					removeEdge(edge2Id);
					edge2Widget.removeFromParent();

				}
			}
		});

	}

	private String genEdgeId(IRNode edge) {
		IRNode src = TestTreeGraph.graph.getSource(edge);
		IRNode target = TestTreeGraph.graph.getSink(edge);
		String type = TestTreeGraph.REL_NAMES[edge.getIntSlotValue(TestTreeGraph.edgeNameAttr)];
		String sourcePinID = src.getIntSlotValue(TestTreeGraph.nodeUidAttr) + type;
		String targetNodeID = target.getIntSlotValue(TestTreeGraph.nodeUidAttr) + VMDGraphScene.PIN_ID_DEFAULT_SUFFIX;
		String edgeID = "edge" + sourcePinID + targetNodeID;
		return edgeID;
	}

	public void createEdge(IRNode src, IRNode target, IRNode edge, String type) {
		String sourcePidID = src.getIntSlotValue(TestTreeGraph.nodeUidAttr) + type;
		String targetID = target.getIntSlotValue(TestTreeGraph.nodeUidAttr) + VMDGraphScene.PIN_ID_DEFAULT_SUFFIX;
		//IRNode edge = TestTreeGraph.graph.
		createEdge(sourcePidID, targetID, edge);
	}

	private void createEdge(String sourcePinID, String targetNodeID, IRNode edge) {
		String edgeID = "edge" + sourcePinID + targetNodeID;
		edgeId2iredge.put(edgeID, edge);
		VMDConnectionWidget widget = (VMDConnectionWidget) addEdge(edgeID);

		//((ConnectionWidget) widget).setRoutingPolicy(ConnectionWidget.RoutingPolicy.ALWAYS_ROUTE);
		savedStroke = ((ConnectionWidget) widget).getStroke();
		savedEdgeColor = widget.getForeground();
		widget2iredge.put(widget, edge);

		setEdgeSource(edgeID, sourcePinID);
		setEdgeTarget(edgeID, targetNodeID);
		List<WidgetAction> actions = widget.getActions().getActions();
		for (WidgetAction a : actions) {
			if (a.getClass().equals(TwoStateHoverProvider.class)) {
				widget.getActions().removeAction(a);
			}
			if (a.getClass().equals(HoverProvider.class)) {
				widget.getActions().removeAction(a);
			}
		}
		//	widget.getActions().addAction(hoverAction);

		widget.getActions().addAction(edgePopupAction);
	}

	public void createNewEdge(final String srcId, final String targetId, final String type) {
		final IRNode source = nodeId2irnode.get(srcId);
		final IRNode target = nodeId2irnode.get(targetId);
		if (source == target) {
			return;
		}
		tracker.executeIn(new Runnable() {

			public void run() {
				if (TestTreeGraph.graphChild(source, target)) {
					return;
				}
				String[] names = TestTreeGraph.REL_NAMES;
				int typeId = 0;
				for (int i = 0; i < names.length; i++) {
					if (type.equals(names[i])) {
						typeId = i;
						break;
					}
				}
				IRNode edge1 = TestTreeGraph.connectGraphNodes(source, target, typeId);
				createEdge(source, target, edge1, type);
				//exclusive requires two edges
				if (typeId == TestTreeGraph.REL_EXCLUDES) {
					IRNode edge2 = TestTreeGraph.connectGraphNodes(target, source, typeId);
					createEdge(target, source, edge2, type);
				} 
			}
		});
	}

	/*
	private class MyHoverProvider implements TwoStateHoverProvider {

		private Widget edge;
		private Color color;
		private Stroke stroke;

		@Override
		public void unsetHovering(Widget widget) {
			if (widget != null && widget != edge && isEdge(findObject(widget))) {
				System.out.println("Unsettin Hoverring");
//			System.out.println(color.toString());
				((VMDConnectionWidget) widget).setStroke(stroke);
				((VMDConnectionWidget) widget).setForeground(color);
			}
		}

		@Override
		public void setHovering(Widget widget) {
			if (widget != null && widget != edge && isEdge(findObject(widget))) {
				System.out.println("Setting hovering");
				color = ((VMDConnectionWidget) widget).getForeground();
				//System.out.println(color.toString());
				stroke = ((ConnectionWidget) widget).getStroke();
				((VMDConnectionWidget) widget).setStroke(savedStroke);
				((VMDConnectionWidget) widget).setForeground(Color.BLACK);
			}
		}
	}

	 */

	private class SceneConnectProvider implements ConnectProvider {

		private String source = null;
		private String target = null;

		@Override
		public boolean isSourceWidget(Widget sourceWidget) {
			Object object = findObject(sourceWidget);
			source = isPin(object) ? (String) object : null;
			return source != null;
		}

		@Override
		public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
			Object object = findObject(targetWidget);
			target = isNode(object) ? (String) object : null;
			if (target != null) {
				if (findObject(sourceWidget.getParentWidget()).equals(target)) {
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

		@Override
		//first is a pin, second is a node
		public void createConnection(Widget sourceWidget, Widget targetWidget) {

			String srcId = (String) findObject(sourceWidget.getParentWidget());
			String targetId = (String) findObject(targetWidget);
			String type = ((VMDPinWidget) sourceWidget).getPinName();
			createNewEdge(srcId, targetId, type);
			if (activeValidate){
				validateRelationships();
			}
		}
	}

	private class MySelectProvider implements SelectProvider {

		@Override
		public boolean isAimingAllowed(Widget arg0, Point arg1, boolean arg2) {
			return true;
		}

		@Override
		public boolean isSelectionAllowed(Widget arg0, Point arg1, boolean arg2) {
			return true;
		}

		@Override
		public void select(Widget arg0, Point arg1, boolean arg2) {

			for (String e : selectedEdges) {
				ConnectionWidget w = (ConnectionWidget) findWidget(e);
				if (w == null) {
					continue; //probably was deleted
				}
				w.setForeground(savedEdgeColor);
				w.setStroke(savedStroke);
			}
			for (String n : selectedNodes) {
				VMDNodeWidget w = (VMDNodeWidget) findWidget(n);
				w.getNodeNameWidget().setBackground(savedWidgetLabelColor);
				w.getNodeNameWidget().setOpaque(false);

			}
			selectedNodes.clear();
			selectedEdges.clear();
			Object obj = findObject(arg0);
			if (isPin(obj)) {
				VMDPinWidget pin = (VMDPinWidget) arg0;
				String pinId = (String) findObject(pin);
				Collection<String> edges = getEdges();
				for (String e : edges) {
					String edgeSource = getEdgeSource(e);
					if (pinId.equals(edgeSource)) {
						selectedEdges.add(e);
						String n = getEdgeTarget(e);

						VMDConnectionWidget con = (VMDConnectionWidget) findWidget(e);
						con.setForeground(Color.RED);
						BasicStroke bs = new BasicStroke(2f, 1, 1, 1, null, 2);
						con.setStroke(bs);

						String nodeId = getEdgeTarget(e).split("#")[0];
						selectedNodes.add(nodeId);
						VMDNodeWidget widget = (VMDNodeWidget) findWidget(nodeId);
						//widget.getNodeNameWidget().setBackground(Color.YELLOW);
						//widget.getNodeNameWidget().setOpaque(true);
					}
				}
			}
		}
	}

//	void buildGraphFromTree(IRNode root) {
//		createNode(root);
//		Iteratable<IRNode> it = TestTreeGraph.tree.depthFirstSearch(root);
//		while (it.hasNext()) {
//			IRNode n = it.next();
//			createNode(n);
//		}
//		it = TestTreeGraph.tree.depthFirstSearch(root);
//		while (it.hasNext()) {
//			IRNode n = it.next();
//			createChildEdges(n);
//		}
//	}

	public void performLayout() {
		GridGraphLayout<String, String> graphLayout = new GridGraphLayout<String, String>();
		SceneLayout sceneGraphLayout = LayoutFactory.createSceneGraphLayout(this, graphLayout);
		sceneGraphLayout.invokeLayout();
	}

	void createChildEdges(IRNode n) {
		Iteratable<IRNode> it = TestTreeGraph.graph.childEdges(n);
		while (it.hasNext()) {
			IRNode edge = it.next();
			IRNode child = TestTreeGraph.graph.getSink(edge);
			switch (edge.getIntSlotValue(TestTreeGraph.edgeNameAttr)) {
				case TestTreeGraph.REL_MANDATORY: {
					createEdge(n, child, edge, "mandatory");
					break;
				}
				case TestTreeGraph.REL_OPTIONAL: {
					createEdge(n, child, edge, "optional");
					break;
				}
				case TestTreeGraph.REl_OR: {
					createEdge(n, child, edge, "or");
					break;
				}
				case TestTreeGraph.REL_ALTERNATIVE: {
					createEdge(n, child, edge, "alternate");
					break;
				}
				case TestTreeGraph.REL_IMPLIES: {
					createEdge(n, child, edge, "implies");
					break;
				}
				case TestTreeGraph.REL_EXCLUDES: {
					createEdge(n, child, edge, "excludes");
					break;
				}
			}
		}
		performLayout();
	}

	//doesn't work on forest graphs
	void buildGraph(IRNode n, ArrayList<IRNode> seen) {

		if (seen.contains(n)) {
			return;
		}
		createNode(n);
		seen.add(n);
		if (TestTreeGraph.graph.numChildren(n) == 0) {
			return;
		}

		Iteratable<IRNode> i = TestTreeGraph.graph.childEdges(n);
		while (i.hasNext()) {
			IRNode edge = i.next();
			IRNode child = TestTreeGraph.graph.getSink(edge);
			if (!seen.contains(child)) {
				seen.add(child);
				createNode(child);
			}

		}
		Iteratable<IRNode> it = TestTreeGraph.graph.childEdges(n);
		while (it.hasNext()) {
			IRNode edge = it.next();
			if (seen.contains(edge)) { continue; }
			seen.add(edge);
			IRNode child = TestTreeGraph.graph.getSink(edge);
			int edgeType = edge.getIntSlotValue(TestTreeGraph.edgeNameAttr);
			createEdge(n, child, edge, TestTreeGraph.REL_NAMES[edgeType]);
		//	
		//	switch (edge.getIntSlotValue(TestTreeGraph.edgeNameAttr)) {
		//		case TestTreeGraph.REL_MANDATORY: {
		//			createEdge(n, child, edge, "mandatory"); break; }
		//		case TestTreeGraph.REL_OPTIONAL: {
		//			createEdge(n, child, edge, "optional"); break; }
		//		case TestTreeGraph.REl_OR: {
		//			createEdge(n, child, edge, "or"); break; }
		//		case TestTreeGraph.REL_ALTERNATIVE: {
		//			createEdge(n, child, edge, "alternative"); break; }
		//		case TestTreeGraph.REL_IMPLIES: {
		//			createEdge(n, child, edge, "implies"); break; }
		//		case TestTreeGraph.REL_EXCLUDES: {
		//			createEdge(n, child, edge, "excludes"); break; }
		//	}
		}

		performLayout();
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
	private void genCNFHelper(IRNode n, IRNode parentEdge, ArrayList<IRNode> seen) throws ContradictionException {
		if (seen.contains(n)) { return; }
		seen.add(n);
		Iteratable<IRNode> it = TestTreeGraph.graph.childEdges(n);
		while (it.hasNext()) {
			IRNode edge = it.next();
			IRNode sink = TestTreeGraph.graph.getSink(edge);
			genNodeCNF(n );
			if (edge != null && edge.getSlotValue( TestTreeGraph.edgeNameAttr) == TestTreeGraph.REL_EXCLUDES){
				continue;
			}
			genCNFHelper(sink, edge, seen);
		}

	}

	public void genSAT() {
		final ArrayList<IRNode> seen = new ArrayList<IRNode>();
		final Collection<IRNode> nodes = nodeId2irnode.values();
		tracker.executeIn(new Runnable() {
			public void run() {
				try {
					for (IRNode n : nodes) {
						int val = n.getIntSlotValue(TestTreeGraph.deriveAttr);
						int nodeId = n.getIntSlotValue(TestTreeGraph.nodeUidAttr);
						VecInt clause = new VecInt(1);
						if (val == TestTreeGraph.COMP_REQUIRE) {
							clause.push(nodeId);
							IConstr c = solver.addClause(clause);
							conflicts.put(c, new IncludedByUser(c, n));
							genCNFHelper(n, null, seen);
						} else if (val == TestTreeGraph.COMP_EXCLUDE) {
							clause.push(-nodeId);
							IConstr c = solver.addClause(clause);
							conflicts.put(c, new ExcludedByUser(c,n));
						}
					}
				} catch (Exception e) {
				}
			}
		});
	}


	/**
	 * Takes an IRNode and create CNF clauses for it's child edges.
	 * @param n
	 * @param clauses   
	 */
	private void genNodeCNF(final IRNode n /*, final ArrayList<VecInt> clauses*/) throws ContradictionException {
		final SymmetricEdgeDigraph graph = TestTreeGraph.graph;
		final SlotInfo<String> nodeNameAttr = TestTreeGraph.nodeNameAttr;
		final SlotInfo<Integer> edgeNameAttr = TestTreeGraph.edgeNameAttr;
		final SlotInfo<Integer> nodeUidAttr = TestTreeGraph.nodeUidAttr;


		String parentName = n.getSlotValue(nodeNameAttr);
		int parentId = n.getIntSlotValue(nodeUidAttr);
		Iteratable<IRNode> it = graph.childEdges(n);

		ArrayList<IRNode> mandatoryEdges = new ArrayList<IRNode>();
		ArrayList<IRNode> optionalEdges = new ArrayList<IRNode>();
		ArrayList<IRNode> orEdges = new ArrayList<IRNode>();
		ArrayList<IRNode> alternativeEdges = new ArrayList<IRNode>();
		ArrayList<IRNode> impliesEdges = new ArrayList<IRNode>();
		ArrayList<IRNode> excludesEdges = new ArrayList<IRNode>();

		while (it.hasNext()) {
			IRNode edge = it.next();
			switch (edge.getIntSlotValue(edgeNameAttr)) { 
			case TestTreeGraph.REL_MANDATORY: { mandatoryEdges.add(edge); break;} 
			case TestTreeGraph.REL_OPTIONAL: { optionalEdges.add(edge); break;} 
			case TestTreeGraph.REl_OR: { orEdges.add(edge); break;}
			case TestTreeGraph.REL_ALTERNATIVE: { alternativeEdges.add(edge); break;}
			case TestTreeGraph.REL_IMPLIES: { impliesEdges.add(edge); break;}
			case TestTreeGraph.REL_EXCLUDES: { excludesEdges.add(edge); break;}
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
					c = solver.addClause(clause2);
					conflicts.put(c, new MoreThanOneSelectedConflict(c,n , alternativeEdges));
				}
				VecInt clause3 = new VecInt(2);
				clause3.push(-c1Id);
				clause3.push(parentId);
				c = solver.addClause(clause3);
			}
			VecInt clause4 = new VecInt(2);
			int c2Id = graph.getSink(alternativeEdges.get(alternativeEdges.size() - 1)).
					  getSlotValue(nodeUidAttr);
			clause4.push(-c2Id);
			clause4.push(parentId);
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
			}
		}
	}
}
