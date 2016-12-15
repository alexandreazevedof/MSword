package edu.uwm.cs.molhado.fm;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRFloatType;
import edu.cmu.cs.fluid.ir.IRIntegerType;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRNodeType;
import edu.cmu.cs.fluid.ir.IRStringType;
import edu.cmu.cs.fluid.ir.SlotAlreadyRegisteredException;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.SymmetricEdgeDigraph;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.util.FileLocator;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.uwm.cs.molhado.util.TrueZipFileLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author chengt
 */
public class FMComponent {
	
	//node type (singular, or feature group, alternative feature group)
public static final int NODE_SINGULAR    = 0;
public static final int NODE_OR_GROUP    = 1;
public static final int NODE_ALT_GROUP   = 2;
public static final int NODE_CONST_OR    = 3;
public static final int NODE_CONST_AND   = 4;

	//edge type (mandatory, optional, or, alternative, implies, excludes)
public static final int EDGE_MANDATORY   = 3;
public static final int EDGE_OPTIONAL    = 4;
public static final int EDGE_OR          = 5;
public static final int EDGE_ALTERNATIVE = 6;
public static final int EDGE_IMPLIES     = 7;
public static final int EDGE_EXCLUDES    = 8;
public static final int EDGE_CONST_OR    = 9;
public static final int EDGE_CONST_AND   = 10;

private final static String ERA_NAME = "era";
private final static String REGION_NAME = "region";
private final static String STORE = "/home/chengt/tmp/ir-data";
private final static String PROJ_FILE = "proj.info";
private final static TrueZipFileLocator floc = new TrueZipFileLocator(STORE);
//protected static VersionedRegion region;
public final static Bundle bundle = new Bundle();

public final static Tree tree;

public final static SymmetricEdgeDigraph graph;  

public final static SlotInfo<String>  nodeNameAttr;
public final static SlotInfo<String>  nodeDescAttr;
public final static SlotInfo<Integer> nodeTypeAttr;
public final static SlotInfo<Integer> edgeTypeAttr;
public final static SlotInfo<Integer> nodeUidAttr;
public final static SlotInfo<Integer> deriveAttr;
public final static SlotInfo<Integer> xAttr;
public final static SlotInfo<Integer> yAttr;
public final static SlotInfo<IRNode>  fileAttr;

protected static int curId = 1;
//private static Era era;
private static Version rootVersion;

private static final IRStringType         irStringType     = IRStringType.prototype;
private static final IRIntegerType        irIntegerType    = IRIntegerType.prototype;
private static final IRFloatType          irFloatType      = IRFloatType.prototype;
private static final IRNodeType           irNodeType       = IRNodeType.prototype;
private static final VersionedSlotFactory versionedFactory = VersionedSlotFactory.prototype;
  
  
	static {
		FluidRegistryLoading.ensureStaticLoaded();
		SlotInfo<String>  nodeNameAttrX = null;
		SlotInfo<String>  nodeDescAttrX = null;
		SlotInfo<Integer> nodeTypeAttrX = null;
		SlotInfo<Integer> edgeTypeAttrX = null;
		SlotInfo<Integer> nodeUidAttrX  = null;
		SlotInfo<Integer> deriveAttrX   = null;
		SlotInfo<Integer>   xAttrX      = null;
		SlotInfo<Integer>   yAttrX      = null;
		SlotInfo<IRNode>  fileAttrX     = null;

		Tree              treeX         = null;
		SymmetricEdgeDigraph graphX     = null;
		try {
			nodeNameAttrX = versionedFactory.newAttribute("fm.node.name", irStringType);
			nodeDescAttrX = versionedFactory.newAttribute("fm.node.desc", irStringType);
			nodeTypeAttrX = versionedFactory.newAttribute("fm.node.type", irIntegerType);
			edgeTypeAttrX = versionedFactory.newAttribute("fm.edge.type", irIntegerType);
			nodeUidAttrX  = versionedFactory.newAttribute("fm.node.id",   irIntegerType);
			deriveAttrX   = versionedFactory.newAttribute("fm.node.derive", irIntegerType, 0);
			xAttrX        = versionedFactory.newAttribute("fm.node.x",    irIntegerType,   0);
			yAttrX        = versionedFactory.newAttribute("fm.node.y",    irIntegerType,   0);
			fileAttrX     = versionedFactory.newAttribute("fm.node.file", irNodeType);
			treeX         = new Tree("fm.tree", versionedFactory);
			graphX        = new SymmetricEdgeDigraph("fm.graph", versionedFactory);
		} catch (SlotAlreadyRegisteredException ex) {
		}
		tree         = treeX;
		graph        = graphX;
		tree.saveAttributes(bundle);
		graph.saveAttributes(bundle);

		bundle.saveAttribute(nodeNameAttr = nodeNameAttrX);
		bundle.saveAttribute(nodeDescAttr = nodeDescAttrX);
		bundle.saveAttribute(nodeTypeAttr = nodeTypeAttrX);
		bundle.saveAttribute(edgeTypeAttr = edgeTypeAttrX);
		bundle.saveAttribute(nodeUidAttr  = nodeUidAttrX);
		bundle.saveAttribute(deriveAttr   = deriveAttrX);
		bundle.saveAttribute(xAttr        = xAttrX);
		bundle.saveAttribute(yAttr        = yAttrX);
		bundle.saveAttribute(fileAttr     = fileAttrX);
		bundle.setName("fm");
	}


	public static void load(VersionedRegion region, Version v, FileLocator loc){
		IRLoadingUtil.load(region, bundle, loc);
	}

	public static void store(VersionedRegion region, Version v, FileLocator loc) throws IOException{
		IRLoadingUtil.store(region, bundle, v, loc);
	}

	protected static List<IRNode> getEdge(IRNode parent, IRNode child) {
		ArrayList<IRNode> edges = new ArrayList<IRNode>();
		Iteratable<IRNode> it = graph.childEdges(child);
		while (it.hasNext()) {
			IRNode e = it.next();
			if (graph.getSink(e).equals(child)) {
				edges.add(e);
			}
		}
		return edges;
	}

	protected static ArrayList<IRNode> getNodes(final IRNode root, VersionTracker tracker) {
	  final ArrayList<IRNode> nodes = new ArrayList<IRNode>();
	  final ArrayList<IRNode> seen = new ArrayList<IRNode>();
	  
		nodes.add(root);
		seen.add(root);

//	  tracker.executeIn(new Runnable(){
//
//			public void run() {
//				Iterator<IRNode> it = graph.children(root);
//				while (it.hasNext()) {
//					IRNode c = it.next();
//					nodes.add(c);
//					getNodes(c, nodes, seen);
//				}
//			}
//		});


		Version.saveVersion(tracker.getVersion());
		Iterator<IRNode> it = graph.children(root);
		while (it.hasNext()) {
			IRNode c = it.next();
			nodes.add(c);
			getNodes(c, nodes, seen);
		}

		Version.restoreVersion();
		
	  return nodes;
  }

	protected static void getNodes(IRNode p, ArrayList<IRNode> nodes, 
			  ArrayList<IRNode> seen) {
		if (seen.contains(p)) {
			return;
		}
		seen.add(p);
		Iterator<IRNode> it = graph.children(p);
		while (it.hasNext()) {
			IRNode c = it.next();
			nodes.add(c);
			getNodes(c, nodes, seen);
		}
	}

	protected static ArrayList<IRNode> getEdges(final IRNode root, VersionTracker tracker) {
	  final ArrayList<IRNode> edges = new ArrayList<IRNode>();
	  final ArrayList<IRNode> seen = new ArrayList<IRNode>();
	  seen.add(root);
//	  tracker.executeIn(new Runnable(){
//
//			public void run() {
//				Iteratable<IRNode> it2 = graph.childEdges(root);
//				while(it2.hasNext()){
//					IRNode e = it2.next();
//					edges.add(e);
//				}
//				Iterator<IRNode> it = graph.children(root);
//				while (it.hasNext()) {
//					IRNode c = it.next();
//					//  graph.
//					getEdges(c, edges, seen);
//				}
//			}
//		});

		Version.saveVersion(tracker.getVersion());
		Iteratable<IRNode> it2 = graph.childEdges(root);
		while (it2.hasNext()) {
			IRNode e = it2.next();
			edges.add(e);
		}
		Iterator<IRNode> it = graph.children(root);
		while (it.hasNext()) {
			IRNode c = it.next();
			//  graph.
			getEdges(c, edges, seen);
		}
	  Version.restoreVersion();
	  
	  return edges;
  }

	protected static void getEdges(IRNode p, ArrayList<IRNode> edges, 
			  ArrayList<IRNode> seen) {
		if (seen.contains(p)) {
			return;
		}
		seen.add(p);
		Iteratable<IRNode> it2 = graph.childEdges(p);
		while (it2.hasNext()) {
			IRNode e = it2.next();
			edges.add(e);
		}

		Iterator<IRNode> it = graph.children(p);
		while (it.hasNext()) {
			IRNode c = it.next();
			getEdges(c, edges, seen);
		}
	}


}
