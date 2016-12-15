package edu.uwm.cs.molhado.version;

import edu.cmu.cs.fluid.tree.DigraphEvent;
import org.openide.util.Lookup;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.DigraphListener;
import edu.cmu.cs.fluid.tree.NewNodeEvent;
import edu.cmu.cs.fluid.version.Version;
import java.awt.*;
import java.util.Collections;
import javax.swing.BorderFactory;
import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.anchor.*;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.*;
import org.netbeans.api.visual.widget.*;
import org.openide.util.lookup.*;
/**
 *
 * @author chengt
 */
public class VersionGraphScene extends GraphScene<Version, VersionEdge>
        implements DigraphListener {

  private final static float TRUNK_EDGE_SIZE = 3f;
  private final static float[] MERGE_EDGE_DASHES = {3f, 3f, 3f, 3f};
  private final static BasicStroke MERGE_STROKE = new BasicStroke(1f, 1, 1, 1,
			                              MERGE_EDGE_DASHES, 2);
  private LayerWidget mainLayer;
  private LayerWidget connectionLayer;
  private LayerWidget interactionLayer;
  private Version rootVersion;
  private WidgetAction hoverAction;
  private WidgetAction selectAction;
  private WidgetAction connectAction;
  private Widget selectedWidget;
  private Lookup lookup;

  public VersionGraphScene(Version v) {
    connectionLayer = new LayerWidget(this);
    mainLayer = new LayerWidget(this);
    interactionLayer = new LayerWidget(this);
    addChild(connectionLayer);
    addChild(mainLayer);
    addChild(interactionLayer);
    this.getActions().addAction(ActionFactory.createWheelPanAction());
    this.getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.5));
    hoverAction = ActionFactory.createHoverAction(new MyHoverProvider());
    selectAction = ActionFactory.createSelectAction(new MySelectProvider());
    connectAction = ActionFactory.createExtendedConnectAction(interactionLayer,
				                new SceneConnectProvider(this));
    this.getActions().addAction(hoverAction);
    this.rootVersion = v;
    lookup = new AbstractLookup(content);
  }

  @Override
  protected Widget attachNodeWidget(Version arg0) {
    VersionWidget widget = new VersionWidget(this, arg0);
    widget.setOpaque(true);
    widget.getActions().addAction(connectAction);
    widget.getActions().addAction(ActionFactory.createMoveAction());
    widget.getActions().addAction(ActionFactory.createZoomAction());
    widget.getActions().addAction(hoverAction);
    widget.getActions().addAction(selectAction);
    widget.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    widget.setBackground(Color.LIGHT_GRAY);

    widget.setToolTipText(VersionSupport.getTag(arg0) + ":" + VersionSupport.getLog(arg0));
    mainLayer.addChild(widget);
    return widget;
  }

  @Override
  protected Widget attachEdgeWidget(VersionEdge arg0) {
    VersionEdgeWidget widget = new VersionEdgeWidget(this, arg0);
    // widget.setSourceAnchor(AnchorFactory.createCircularAnchor (first, 32));
    //    widget.setTargetAnchor(AnchorFactory.createCircularAnchor (second, 32));
    //  widget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
    connectionLayer.addChild(widget);
    return widget;
  }

  @Override
  protected void attachEdgeSourceAnchor(VersionEdge edge, Version oldv, Version newv) {
    ConnectionWidget c = (ConnectionWidget) findWidget(edge);
    Widget widget = findWidget(newv);
    //Anchor a = AnchorFactory.createCenterAnchor(widget);
    Anchor a = AnchorFactory.createCircularAnchor(widget, 0);
    c.setSourceAnchor(a);
  }

  @Override
  protected void attachEdgeTargetAnchor(VersionEdge edge, Version oldv, Version newv) {
    ConnectionWidget c = (ConnectionWidget) findWidget(edge);
    Widget widget = findWidget(newv);
    // Anchor a = AnchorFactory.createCenterAnchor(widget);
    Anchor a = AnchorFactory.createCircularAnchor(widget, 15);
    c.setTargetAnchor(a);
    c.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
  }
  private InstanceContent content = new InstanceContent();

  @Override
  public Lookup getLookup() {
    return lookup;
  }
  // private static TreeInterface t = Version.getShadowTree();

	@SuppressWarnings("unchecked")
  public void buildGraph() {
    this.addNode(rootVersion);
    buildGraphHelper(rootVersion);
    GraphLayout l = GraphLayoutFactory.createHierarchicalGraphLayout(this, false, false, 10, 40);
    l.layoutGraph(this);
  }

  private void buildGraphHelper(Version v) {
    for (Version child : VersionSupport.getChildren(v)) {
      Widget vwidget = this.findWidget(child);
      if (vwidget == null) {
        this.addNode(child);
      }
      VersionEdge edge = new VersionEdge(v, child);
      this.addEdge(edge);
      this.setEdgeSource(edge, v);
      this.setEdgeTarget(edge, child);
      if (VersionSupport.inTrunk(child)) {
        ConnectionWidget c = (ConnectionWidget) findWidget(edge);
        c.setLineColor(Color.BLUE);
        c.setStroke(new BasicStroke(3f));
      }

      Version mergeSource = VersionSupport.getMergeSource(child);

      if (mergeSource != null) {
        VersionWidget vw = (VersionWidget) findWidget(mergeSource);
        if (vw == null) {
          this.addNode(mergeSource);
        }
        edge = new VersionEdge(mergeSource, child);
        this.addEdge(edge);
        this.setEdgeSource(edge, mergeSource);
        this.setEdgeTarget(edge, child);
        float[] dashes = {3f, 3f, 3f, 3f};
        BasicStroke bs = new BasicStroke(1f, 1, 1, 1, dashes, 2);
        ConnectionWidget cw = (ConnectionWidget) this.findWidget(edge);
        cw.setStroke(bs);
      }
      buildGraphHelper(child);
    }
  }

	@Override
  public void handleDigraphEvent(DigraphEvent e) {
    if (e instanceof NewNodeEvent) {
      IRNode shadowNode = ((NewNodeEvent) e).getNode();
      Version newVersion = Version.getShadowVersion(shadowNode);
      Version pV = newVersion.parent();
      this.addNode(newVersion);
      VersionEdge edge = new VersionEdge(pV, newVersion);
      this.addEdge(edge);
      this.setEdgeSource(edge, pV);
      this.setEdgeTarget(edge, newVersion);
      this.repaint();
    }
  }

  private class MyHoverProvider implements TwoStateHoverProvider {

		@Override
    public void unsetHovering(Widget widget) {
      if (widget != null && widget != selectedWidget) {
        widget.setBackground(Color.LIGHT_GRAY);
        widget.setForeground(Color.BLACK);
      }
    }

		@Override
    public void setHovering(Widget widget) {
      if (widget != null && widget != selectedWidget) {
        widget.setBackground(Color.YELLOW);
        widget.setForeground(Color.BLACK);
      }
    }
  };

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
      if (selectedWidget != null) {
        selectedWidget.setBackground(Color.LIGHT_GRAY);
        selectedWidget.setForeground(Color.BLACK);
      }
      selectedWidget = arg0;
      selectedWidget.setBackground(Color.BLACK);
      selectedWidget.setForeground(Color.WHITE);
      Version version = ((VersionWidget) arg0).getVersion();
      // content.set(Collections.singleton(arg0), null);
      //System.out.println("MySelectorProvider: select:" + VersionSupport.getVersionNumber(version));
      content.set(Collections.singleton(version), null);
    }
  }

  private class SceneConnectProvider implements ConnectProvider {

    private Version source = null;
    private Version target = null;
    private VersionGraphScene scene;

    public SceneConnectProvider(VersionGraphScene scene) {
      this.scene = scene;
    }

		@Override
    public boolean isSourceWidget(Widget sourceWidget) {
      Object object = scene.findObject(sourceWidget);
      source = scene.isNode(object) ? (Version) object : null;
      return source != null;
    }

		@Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
      Object object = scene.findObject(targetWidget);
      target = scene.isNode(object) ? (Version) object : null;
      if (target != null) {
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
    public void createConnection(Widget sourceWidget, Widget targetWidget) {
      Version sourceVersion = ((VersionWidget) sourceWidget).getVersion();
      Version targetVersion = ((VersionWidget) targetWidget).getVersion();
      if (sourceVersion == targetVersion) {
        return;
      }
//      if (VersionSupport.comesFrom2(sourceVersion, target)) return;
      //if (target.comesFrom(sourceVersion)) return;
//      if (VersionSupport.comesFrom2(target, source)) return ;
      Version resultVersion = VersionSupport.merge(source, target, "merge", "Merged " +
              VersionSupport.getVersionNumber(sourceVersion) + " and " +
              VersionSupport.getVersionNumber(targetVersion));
      scene.addNode(resultVersion);
      scene.validate();
      Widget sourceW = scene.findWidget(sourceVersion);
      Widget targetW = scene.findWidget(targetVersion);
      Widget resultW = scene.findWidget(resultVersion);
      Point sourceLoc = sourceW.getPreferredLocation();
      Point targetLoc = targetW.getPreferredLocation();
      Point loc = new Point();
      loc.x = (sourceLoc.x + targetLoc.x) / 2;
      loc.y = Math.max(sourceLoc.y, targetLoc.y) + 50;
      resultW.setPreferredLocation(loc);
      scene.validate();
      VersionEdge sourceEdge = new VersionEdge(sourceVersion, resultVersion);
      VersionEdge targetEdge = new VersionEdge(targetVersion, resultVersion);
      scene.addEdge(sourceEdge);
      scene.addEdge(targetEdge);

      scene.setEdgeSource(sourceEdge, sourceVersion);
      scene.setEdgeTarget(sourceEdge, resultVersion);
      scene.setEdgeSource(targetEdge, targetVersion);
      scene.setEdgeTarget(targetEdge, resultVersion);
      scene.validate();
      if (VersionSupport.inTrunk(resultVersion)) {
        ConnectionWidget c = (ConnectionWidget) findWidget(targetEdge);
        c.setLineColor(Color.BLUE);
        c.setStroke(new BasicStroke(3f));
      }

      float[] dashes = {3f, 3f, 3f, 3f};
      BasicStroke bs = new BasicStroke(1f, 1, 1, 1, dashes, 2);
      ConnectionWidget cw = (ConnectionWidget) scene.findWidget(sourceEdge);
      cw.setStroke(bs);

      scene.getLayout().layout(scene);
      scene.validate();
    }
  }
}
