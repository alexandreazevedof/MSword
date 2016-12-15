package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.tree.TreeInterface;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.tree.SymmetricEdgeDigraph;
import edu.cmu.cs.fluid.version.*;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.util.*;
import edu.uwm.cs.molhado.version.VersionSupport;
import edu.uwm.cs.molhado.version.VersionGraphViewForm;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class TestTreeGraph extends FluidRegistryLoading //implements LookupListener {
{

	
	// type of relationships among components
	public static final int REL_MANDATORY = 0;
	public static final int REL_OPTIONAL = 1;
	public static final int REl_OR = 2;
	public static final int REL_ALTERNATIVE = 3;
	public static final int REL_IMPLIES = 4;
	public static final int REL_EXCLUDES = 5;
	public static final String []REL_NAMES= {
		"mandatory", "optional", "or", "alternative", "implies", "excludes"
	};

	// whether a component is to be include, or exclude in a product
	public static final int COMP_REQUIRE = 1;
	public static final int COMP_DEFAULT = 0;
	public static final int COMP_EXCLUDE = -1;

  public static IRNode rootNode;
  private final static String ERA_NAME = "era";
  private final static String REGION_NAME = "region";
  private final static String STORE = "/home/chengt/tmp/ir-data";
  private final static String PROJ_FILE = "proj.info";
  private static TrueZipFileLocator floc = new TrueZipFileLocator(STORE);
  private static VersionedRegion region;
  private final static Bundle bundle = new Bundle();

  public final static Tree tree;
  public final static SymmetricEdgeDigraph graph;  
  public final static SlotInfo<String> nodeNameAttr;
  public final static SlotInfo<Integer> edgeNameAttr;
  public final static SlotInfo<Integer> nodeUidAttr;
  public final static SlotInfo<Integer> deriveAttr;

  public static int curId = 1;
  private static Era era;
  private static Version rootVersion;
//  private static Lookup.Result<Version> result;


  static {
    SlotInfo<String> nodeNameAttrX = null;
	 SlotInfo<Integer> edgeNameAttrX = null;
	 SlotInfo<Integer> nodeUidAttrX = null;
	 SlotInfo<Integer> deriveAttrX = null;
	 
    Tree treeX = null;
	 SymmetricEdgeDigraph graphX = null; 
    try {
      treeX = new Tree("project.tree", VersionedSlotFactory.prototype);
      nodeNameAttrX = VersionedSlotFactory.prototype.newAttribute("proj.node.name", IRStringType.prototype);
		edgeNameAttrX = VersionedSlotFactory.prototype.newAttribute("proj.edge.name", IRIntegerType.prototype);
		nodeUidAttrX = VersionedSlotFactory.prototype.newAttribute("proj.node.id", IRIntegerType.prototype);
		deriveAttrX  = VersionedSlotFactory.prototype.newAttribute("proj.node.derive", IRIntegerType.prototype, 0);
		graphX = new SymmetricEdgeDigraph("Config.graph", VersionedSlotFactory.prototype);
    } catch (SlotAlreadyRegisteredException ex) {
      ex.printStackTrace();
    }
    tree = treeX;
	 graph = graphX;
    nodeNameAttr = nodeNameAttrX;
	 edgeNameAttr = edgeNameAttrX;
	 nodeUidAttr = nodeUidAttrX;
	 deriveAttr = deriveAttrX;
    tree.saveAttributes(bundle);
	 graph.saveAttributes(bundle);
    bundle.saveAttribute(nodeNameAttr);
    bundle.setName("proj");
  }

  public static IRNode createNode(String name) {
    IRNode n = new PlainIRNode(region);
    n.setSlotValue(nodeNameAttr, name);
	 n.setSlotValue(nodeUidAttr, curId++);
    tree.initNode(n);
	 graph.initNode(n);
    return n;
  }

  public static IRNode createEdge(int type){
    IRNode n = new PlainIRNode(region);
    n.setSlotValue(edgeNameAttr, type);
	 graph.initEdge(n);
    return n;
  }

  public static IRNode connectGraphNodes(IRNode source, IRNode sink, int type){
	  IRNode edgeNode = createEdge(type);
	  graph.connect(edgeNode, source, sink);
	  return edgeNode;
  }

  private static IRNode addTreeChild(IRNode parent, String name) {
    IRNode n = createNode(name);
    tree.appendChild(parent, n);
    return n;
  }


	public static List<IRNode> getEdge(IRNode parent, IRNode child) {
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

	public static void createExample(){

    era = new Era(Version.getInitialVersion());
    era.setName(ERA_NAME);
    Version.setDefaultEra(era);
    region = new VersionedRegion();
    region.setName(REGION_NAME);
    
    Version init = Version.getInitialVersion();


    Version v = VersionSupport.commit("tag", "first version");
    era.complete();
	}

  public static Version createRelationshipGraph(){

    era = new Era(Version.getInitialVersion());
    era.setName(ERA_NAME);
    Version.setDefaultEra(era);
    region = new VersionedRegion();
    region.setName(REGION_NAME);
    
    Version init = Version.getInitialVersion();

	 IRNode p = createNode("p");
	 rootNode = p;
	 IRNode a = createNode("a");
	 connectGraphNodes(p, a, REL_MANDATORY);

	 IRNode b = createNode("b");
	 connectGraphNodes(p, b, REL_OPTIONAL);

	 IRNode c = createNode("c");
	 IRNode d = createNode("d");
	 IRNode e = createNode("e");
	 connectGraphNodes(p, c, REl_OR);
	 connectGraphNodes(p, d, REl_OR);
	 connectGraphNodes(p, e, REl_OR);
	 
	 IRNode f = createNode("f");
	 IRNode i = createNode("i");
	 IRNode j = createNode("j");
	 connectGraphNodes(p, f, REL_ALTERNATIVE);
	 connectGraphNodes(p, i, REL_ALTERNATIVE);
	 connectGraphNodes(p, j, REL_ALTERNATIVE);

	 IRNode k = createNode("k");
	 connectGraphNodes(p, k, REL_IMPLIES);
	 
	 IRNode l = createNode("l");
	 connectGraphNodes(p, l, REL_EXCLUDES);
	 connectGraphNodes(l, p, REL_EXCLUDES);

	 connectGraphNodes(k, c, REL_EXCLUDES);
	 connectGraphNodes(c, k, REL_EXCLUDES);
	 
    Version v = VersionSupport.commit("tag", "first version");
    era.complete();
	 return v;
  }



  
  public static void createStructure(){
    era = new Era(Version.getInitialVersion());
    era.setName(ERA_NAME);
    Version.setDefaultEra(era);
    region = new VersionedRegion();
    region.setName(REGION_NAME);
    
    Version init = Version.getInitialVersion();
	 IRNode n = createNode("n");
	 rootNode = n;
   // Version v11 = VersionSupport.commit("", "initial version");
    //rootVersion = v11;
	 rootVersion = init;

	 IRNode w = createNode("w");
	 IRNode x = createNode("x");
	 IRNode y = createNode("y");
	 IRNode z = createNode("z");


	 tree.addChild(n, x);
	 tree.addChild(n, y);
	 tree.addChild(y, z);

	 connectGraphNodes(n, z, REL_ALTERNATIVE);
	 connectGraphNodes(n, w, REL_EXCLUDES);
	 connectGraphNodes(w, n, REL_IMPLIES);
	 connectGraphNodes(z, y, REL_MANDATORY);
	 connectGraphNodes(y, x, REL_OPTIONAL);


    Version v = VersionSupport.commit("tag", "first version");

    era.complete();

  }

  public static void storeStructure() throws IOException, OverlappingEraException {
		IRPersistent.setDebugIO(true);
    era.setName(ERA_NAME);
    era.store(floc);
    VersionSupport.storeEraShadowRegion(era, floc);
    region.setName(REGION_NAME);
    region.store(floc);
    VersionedChunk vc = VersionedChunk.get(region, bundle);
    VersionedDelta vd = vc.getDelta(era);
    vd.store(floc);
    ObjectOutputStream oos = new ObjectOutputStream(floc.openFileWrite(PROJ_FILE));
    oos.writeObject(rootVersion);
    oos.writeObject(rootNode);
    oos.close();
  }

  public static void loadStructure() throws IOException, ClassNotFoundException {
		IRPersistent.setDebugIO(true);
    era = Era.loadEra(UniqueID.parseUniqueID(ERA_NAME), floc);
    era.setName(ERA_NAME);
    VersionSupport.loadEraShadowRegion(era, floc);
    region = VersionedRegion.loadVersionedRegion(UniqueID.parseUniqueID(REGION_NAME), floc);
    region.setName(REGION_NAME);
    VersionedChunk vc = VersionedChunk.get(region, bundle);
    VersionedDelta vd = vc.getDelta(era);
    vd.load(floc);
    ObjectInputStream ois = new ObjectInputStream(floc.openFileRead(PROJ_FILE));
    rootVersion = (Version) ois.readObject();
    rootNode = (IRNode) ois.readObject();
    ois.close();
  }

  public static void displayVersionGraph() {
    VersionGraphViewForm f = new VersionGraphViewForm(rootVersion);
//    result = f.getLookup().lookupResult(Version.class);
    TestTreeGraph tf = new TestTreeGraph();
//    result.addLookupListener(tf);
//    tf.resultChanged(null);
    f.setVisible(true);
  }

  public static void main(String[] args) throws OverlappingEraException,
          IOException, ClassNotFoundException, ContradictionException, TimeoutException {

    if (args[0].equals("create")) {
//      TestTreeGraph.createStructure();
		 TestTreeGraph.createRelationshipGraph();
		 TestTreeGraph.printGraph();
		 //TestTreeGraph.graphToSAT();
		 //TestTreeGraph.graphToCNF();
		 ArrayList<VecInt> clauses = TestTreeGraph.graphToCNF();
		 boolean sat = TestTreeGraph.isSatisfied(clauses);
		 if (sat){
			 System.out.println("Satisfiable");
		 } else {
			 System.out.println("Not satisfiable");
		 }
		 
		 System.out.println("=====nodes=============");;
		// TestTreeGraph.printNodes(rootNode);
		 System.out.println("=====edges=============");;
		 //TestTreeGraph.printEdges(rootNode);
			//TestTreeGraph.printVersionTree();
//      TestTreeGraph.displayVersionGraph();
    } else if (args[0].equals("store")) {
      TestTreeGraph.createStructure();
      TestTreeGraph.storeStructure();
			TestTreeGraph.printVersionTree();
      TestTreeGraph.displayVersionGraph();
    } else if (args[0].equals("load")) {
      TestTreeGraph.loadStructure();
		TestTreeGraph.printVersionTree();
      TestTreeGraph.displayVersionGraph();
    }
  }

//  public void resultChanged(LookupEvent ev) {
//    System.out.println("TestFljuid.resultchanged called:");
 //   Collection<? extends Version> versions = result.allInstances();
  //  if (!versions.isEmpty()) {
      //
  //    Version v = (Version) versions.iterator().next();
   //   printTree(v);
 //   }
//  }

  public static String spaces(int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

	public static void printVersionTree(Version v){
		System.out.println(VersionSupport.getVersionNumber(v));
		if (v!= Version.getInitialVersion()) {
			System.out.println("====tree======");
			printTree(v);
			System.out.println("====graph=====");
			printGraph(v);
		}
		IRNode shadowNode  = v.getShadowNode();
		TreeInterface t = Version.getShadowTree();
		int n = t.numChildren(shadowNode);
		for(int i=0; i<n; i++){
			printVersionTree(Version.getShadowVersion(t.getChild(shadowNode, i)));
		}
	}

	public static void printVersionTree(){
		printVersionTree(Version.getInitialVersion());

	}

  public static void printTree(Version v) {
    Version.saveVersion(v);
    printTree(2, rootNode);
    Version.restoreVersion();
  }

  public static void printGraph(Version v){
    Version.saveVersion(v);
	 ArrayList<IRNode> seen = new ArrayList<IRNode>();
	 System.out.println("source\tsink\tedge");
    printGraph(rootNode, seen);
    Version.restoreVersion();
  }

  public static void printGraph(){
	 ArrayList<IRNode> seen = new ArrayList<IRNode>();
	 System.out.println("source\tsink\tedge");
    printGraph(rootNode, seen);
  }
  public static void printGraph(IRNode n, ArrayList<IRNode> seen){
	  if (seen.contains(n)) return;
	  seen.add(n);
	  String parentName = n.getSlotValue(nodeNameAttr);
	  int nc = graph.numChildren(n);
	  for(int i=0; i<nc; i++){
		  IRNode edge = graph.getChildEdge(n, i);
		  IRNode child = graph.getChild(n, i);
		  String edgeName = REL_NAMES[edge.getIntSlotValue(edgeNameAttr)];
		  String childName = child.getSlotValue(nodeNameAttr);
		  System.out.println(parentName + "\t" + childName + "\t" + edgeName);
		  printGraph(child, seen);
		  //graphToSAT();
	  }
  }


  public static void graphToSAT(){
    //Version.saveVersion(v);
	 ArrayList<IRNode> seen = new ArrayList<IRNode>();
	 StringBuilder sb = new StringBuilder();
	 graphToSAT(sb, rootNode, seen);
	 System.out.println(sb.toString());
    //Version.restoreVersion();
  }
  
  public static boolean isSatisfied(ArrayList<VecInt> clauses) 
			 throws ContradictionException, TimeoutException{

	  ISolver solver = SolverFactory.newDefault();
	  solver.newVar(curId);
	  solver.setExpectedNumberOfClauses(clauses.size());
	  for(VecInt c:clauses){
		  solver.addClause(c);
	  }
	  IProblem problem = solver;
	  return problem.isSatisfiable();
  }

  /* assume we have a tree and a graph. tree of components (file structure)
	* and graph represents the relationships.
	*/ 

  public static ArrayList<VecInt> graphToCNF(){
	  ArrayList<IRNode> seen = new ArrayList<IRNode>();
	  ArrayList<VecInt> list = new ArrayList<VecInt>();

	  nodeToCNF(rootNode, list, seen);

//	  System.out.println("\n\np cnf " + (curId - 1) + " " + list.size());
//	  for (VecInt v : list) {
//		  System.out.println(v);
//	  }
	  return list;
  }
  
  /* generate the clauses for out going edges of node n */
  public static void nodeToCNF(IRNode n, ArrayList<VecInt>clauses, ArrayList<IRNode> seen){

	  if (graph.numChildren(n) == 0) return ;
	  if (seen.contains(n)) return ;
	  seen.add(n);

	  String parentName = n.getSlotValue(nodeNameAttr);
	  int parentId = n.getIntSlotValue(nodeUidAttr);
	  Iteratable<IRNode> it = graph.childEdges(n);

	  ArrayList<IRNode> mandatoryEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> optionalEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> orEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> alternateEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> impliesEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> excludesEdges = new ArrayList<IRNode>();

	  while(it.hasNext()){
		  IRNode edge = it.next();
		  switch(edge.getIntSlotValue(edgeNameAttr)){
			  case REL_MANDATORY: { mandatoryEdges.add(edge); break; }
			  case REL_OPTIONAL: { optionalEdges.add(edge); break; }
			  case REl_OR:{ orEdges.add(edge); break; }
			  case REL_ALTERNATIVE:{ alternateEdges.add(edge); break; }
			  case REL_IMPLIES:{ impliesEdges.add(edge); break; }
			  case REL_EXCLUDES:{ excludesEdges.add(edge); }
		  }
	  }

	  //ArrayList<VecInt> clauses = new ArrayList<VecInt>();

	  //Mandatory
	  if (mandatoryEdges.size() > 0){
		  for(int i=0; i<mandatoryEdges.size(); i++){
			  IRNode child = graph.getSink(mandatoryEdges.get(i));
			  int childId = child.getIntSlotValue(nodeUidAttr);

			  VecInt clause1 = new VecInt(2);
			  clause1.push(-parentId);
			  clause1.push(childId);
			  clauses.add(clause1);

			  VecInt clause2 = new VecInt(2);
			  clause2.push(-childId);
			  clause2.push(parentId);
			  clauses.add(clause2);
		  }
	  }
	  
	  //Optional
	  if (optionalEdges.size() > 0){
		  for(int i=0; i<optionalEdges.size(); i++){
			  IRNode child = graph.getSink(optionalEdges.get(i));
			  int childId = child.getIntSlotValue(nodeUidAttr);
			  VecInt clause1 = new VecInt(2);
			  clause1.push(-childId);
			  clause1.push(parentId);
			  clauses.add(clause1);
		  }
	  }

	  //Or
	  if (orEdges.size() > 0){
		  VecInt clause1 = new VecInt(orEdges.size()+1);
		  clause1.push(-parentId);
		  for(IRNode edge:orEdges){
			  IRNode child = graph.getSink(edge);
			  int childId = child.getIntSlotValue(nodeUidAttr);
			  clause1.push(childId);
		  }
		  clauses.add(clause1);

		  for(IRNode edge:orEdges){
			  IRNode child = graph.getSink(edge);
			  int childId = child.getIntSlotValue(nodeUidAttr);
			  VecInt clause2 = new VecInt(2);
			  clause2.push(-childId);
			  clause2.push(parentId);
			  clauses.add(clause2);
		  }
	  }

	  if (alternateEdges.size() > 0){
		  VecInt clause1 = new VecInt(alternateEdges.size()+1);
		  for(IRNode edge:alternateEdges){
			  IRNode child = graph.getSink(edge);
			  int childId = child.getIntSlotValue(nodeUidAttr);
			  clause1.push(childId);
		  }
		  clause1.push(-parentId);
		  clauses.add(clause1);

		  for(int j=0; j<alternateEdges.size()-1; j++){
			  int c1Id = graph.getSink(alternateEdges.get(j)).
						 getSlotValue(nodeUidAttr);
			  for (int k=j+1; k<alternateEdges.size(); k++){
				  int c2Id = graph.getSink(alternateEdges.get(k)).
							 getSlotValue(nodeUidAttr);
				  VecInt clause2 = new VecInt(2);
				  clause2.push(-c1Id);
				  clause2.push(-c2Id);
				  clauses.add(clause2);
			  }
			  VecInt clause3 = new VecInt(2);
			  clause3.push(-c1Id);
			  clause3.push(parentId);
			  clauses.add(clause3);
		  }
		  VecInt clause4 = new VecInt(2);
		  int c2Id = graph.getSink(alternateEdges.get(alternateEdges.size()-1)).
					 getSlotValue(nodeUidAttr);
		  clause4.push(-c2Id);
		  clause4.push(parentId);
		  clauses.add(clause4);
	  }

	  // implies edges
	  if (impliesEdges.size() > 0){
		  for(int i=0; i<impliesEdges.size(); i++){
			  IRNode child = graph.getSink(impliesEdges.get(i));
			  int childId = child.getIntSlotValue(nodeUidAttr);
			  VecInt clause1 = new VecInt(2);
			  clause1.push(-parentId);
			  clause1.push(childId);
			  clauses.add(clause1);
		  }
	  }

	  // exclude edges
	  if (excludesEdges.size()>0){
		  for(int i=0; i<excludesEdges.size(); i++){
			  IRNode child = graph.getSink(excludesEdges.get(i));
			  int childId = child.getIntSlotValue(nodeUidAttr);
			  VecInt clause1 = new VecInt(2);
			  clause1.push(-parentId);
			  clause1.push(-childId);
			  clauses.add(clause1);
		  }
	  }

	  
	  	  Iteratable<IRNode> childrenIt = graph.children(n);
	  while(childrenIt.hasNext()){
		  nodeToCNF(childrenIt.next(), clauses, seen);
	  }
	 // return clauses;

  }

  public static void printNodes(IRNode root){
	  ArrayList<IRNode> nodes = getNodes(root);
	  for(IRNode n:nodes){
		  System.out.println(n.getSlotValue(nodeNameAttr));
	  }
  }

  public static ArrayList<IRNode> getNodes(IRNode root){
	  ArrayList<IRNode> nodes = new ArrayList<IRNode>();
	  ArrayList<IRNode> seen = new ArrayList<IRNode>();
	  nodes.add(root);
	  seen.add(root);
	  Iterator<IRNode> it = graph.children(root);
	  while(it.hasNext()){
		  IRNode c = it.next();
		  nodes.add(c);
		  getNodes(c, nodes, seen);
	  }
	  return nodes;
  }

  private static void getNodes(IRNode p, ArrayList<IRNode> nodes, ArrayList<IRNode> seen){
	  if (seen.contains(p)) return;
	  seen.add(p);
	  Iterator<IRNode> it = graph.children(p);
	  while(it.hasNext()){
		  IRNode c = it.next();
		  nodes.add(c);
		  getNodes(c, nodes, seen);
	  }
  }

  public static void printEdges(IRNode root){
	  ArrayList<IRNode> edges = getEdges(root);
	  System.out.println("source\tsink\tedge");
	  for(IRNode e:edges){
		  System.out.print(graph.getSource(e).getSlotValue(nodeNameAttr));
		  System.out.print("\t" +graph.getSink(e).getSlotValue(nodeNameAttr));
		  System.out.println("\t" + e.getSlotValue(edgeNameAttr));
	  }
  }


  public static boolean graphChild(IRNode parent, IRNode child){
	  Iteratable<IRNode> it = graph.children(parent);
	  while(it.hasNext()){
		  if (it.next() == child) return true;
	  }
	  return false;
  }

  public static boolean treeChild(IRNode parent, IRNode child){
	  Iteratable<IRNode> it = tree.children(parent);
	  while(it.hasNext()){
		  if (it.next() == child) return true;
	  }
	  return false;
  }
  
  public static ArrayList<IRNode> getEdges(IRNode root){
	  ArrayList<IRNode> edges = new ArrayList<IRNode>();
	  ArrayList<IRNode> seen = new ArrayList<IRNode>();
	  seen.add(root);
	  Iterator<IRNode> it = graph.children(root);
	  while(it.hasNext()){
		  IRNode c = it.next();
		//  graph.
		  getEdges(c, edges, seen);
	  }
	  return edges;
  }

  private static void getEdges(IRNode p, ArrayList<IRNode> edges, ArrayList<IRNode> seen){
	  if (seen.contains(p)) return;
	  seen.add(p);
	  Iterator<IRNode> it = graph.children(p);
	  while(it.hasNext()){
		  IRNode c = it.next();
		  edges.add(c);
	  }
  }

  public static void graphToSAT(StringBuilder sb, IRNode n, ArrayList<IRNode> seen){
	  if (seen.contains(n)) return;
	  seen.add(n);
	  if (graph.numChildren(n) == 0) return;
	  String parentName = n.getSlotValue(nodeNameAttr);
	  Iteratable<IRNode> it = graph.childEdges(n);

	  ArrayList<IRNode> mandatoryEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> optionalEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> orEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> alternateEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> impliesEdges = new ArrayList<IRNode>();
	  ArrayList<IRNode> excludesEdges = new ArrayList<IRNode>();

	  while(it.hasNext()){
		  IRNode edge = it.next();
		  switch(edge.getIntSlotValue(edgeNameAttr)){
			  case REL_MANDATORY: { mandatoryEdges.add(edge); break; }
			  case REL_OPTIONAL: { optionalEdges.add(edge); break; }
			  case REl_OR:{ orEdges.add(edge); break; }
			  case REL_ALTERNATIVE:{ alternateEdges.add(edge); break; }
			  case REL_IMPLIES:{ impliesEdges.add(edge); break; }
			  case REL_EXCLUDES:{ excludesEdges.add(edge); }
		  }
	  }

	  ArrayList<StringBuilder> stringBuilders = new ArrayList<StringBuilder>();

	  //Mandatory
	  StringBuilder mandatorySB = new StringBuilder();
	  if (mandatoryEdges.size() > 0){
		  stringBuilders.add(mandatorySB);
		  for(int i=0; i<mandatoryEdges.size()-1; i++){
			  IRNode child = graph.getSink(mandatoryEdges.get(i));
			  String childName = child.getSlotValue(nodeNameAttr);
			  mandatorySB.append("(~").append(parentName).append("|").append(childName).append(")")
						 .append("^(~").append(childName).append("|").append(parentName)
						 .append(")^");
		  }
		  IRNode child = graph.getSink(mandatoryEdges.get(mandatoryEdges.size()-1));
		  String childName = child.getSlotValue(nodeNameAttr);
		  mandatorySB.append("(~").append(parentName).append("|").append(childName).append(")")
					 .append("^(~").append(childName).append("|").append(parentName)
					 .append(")");
	  }
	  
	  //Optional
	  StringBuilder optionalSB = new StringBuilder();
	  if (optionalEdges.size() > 0){
		  stringBuilders.add(optionalSB);
		  for(int i=0; i<optionalEdges.size()-1; i++){
			  IRNode child = graph.getSink(optionalEdges.get(i));
			  String childName = child.getSlotValue(nodeNameAttr);
			  optionalSB.append("(~").append(childName).append("|").append(parentName).append(")^");
		  }
		  IRNode child = graph.getSink(optionalEdges.get(optionalEdges.size()-1));
		  String childName = child.getSlotValue(nodeNameAttr);
		  optionalSB.append("(~").append(childName).append("|").append(parentName).append(")");
	  }

	  //Or
	  StringBuilder orSB = new StringBuilder();
	  if (orEdges.size() > 0){
		  stringBuilders.add(orSB);
		  orSB.append("(").append(parentName);
		  for(IRNode edge:orEdges){
			  IRNode child = graph.getSink(edge);
			  String childName = child.getSlotValue(nodeNameAttr);
			  orSB.append("|").append(childName);
		  }
		  orSB.append(")");
		  for(IRNode edge:orEdges){
			  IRNode child = graph.getSink(edge);
			  String childName = child.getSlotValue(nodeNameAttr);
		     orSB.append("^").append("(~").append(childName).append("|").
						 append(parentName).append(")");
		  }
	  }

	  StringBuilder alternateSB = new StringBuilder();
	  if (alternateEdges.size() > 0){
		  stringBuilders.add(alternateSB);
		  alternateSB.append("(");
		  for(IRNode edge:alternateEdges){
			  IRNode child = graph.getSink(edge);
			  String childName = child.getSlotValue(nodeNameAttr);
			  alternateSB.append(childName).append("|");
		  }
		  alternateSB.append("~").append(parentName).append(")");
		  for(int j=0; j<alternateEdges.size()-1; j++){
			  String c1 = graph.getSink(alternateEdges.get(j)).getSlotValue(nodeNameAttr);
			  for (int k=j+1; k<alternateEdges.size(); k++){
				  String c2 = graph.getSink(alternateEdges.get(k)).getSlotValue(nodeNameAttr);
				  alternateSB.append("^(~").append(c1).append("|~").append(c2).append(")");
			  }
			  alternateSB.append("^(~").append(c1).append("|").append(parentName).append(")");
		  }
	  }

	  // implies edges
	  StringBuilder impliesSB = new StringBuilder();
	  if (impliesEdges.size() > 0){
		  stringBuilders.add(impliesSB);
		  for(int i=0; i<impliesEdges.size()-1; i++){
			  IRNode child = graph.getSink(impliesEdges.get(i));
			  String childName = child.getSlotValue(nodeNameAttr);
			  impliesSB.append("(~").append(parentName).append("|").append(childName).append(")^");
		  }
		  IRNode child = graph.getSink(impliesEdges.get(impliesEdges.size()-1));
		  String childName = child.getSlotValue(nodeNameAttr);
		  impliesSB.append("(~").append(parentName).append("|").append(childName).append(")");
	  }

	  // exclude edges
	  StringBuilder excludesSB = new StringBuilder();
	  if (excludesEdges.size()>0){
		  stringBuilders.add(excludesSB);
		  for(int i=0; i<excludesEdges.size()-1; i++){
			  IRNode child = graph.getSink(excludesEdges.get(i));
			  String childName = child.getSlotValue(nodeNameAttr);
			  excludesSB.append("(~").append(parentName).append("|~").append(childName).append(")^");
		  }
		  IRNode child = graph.getSink(excludesEdges.get(excludesEdges.size()-1));
		  String childName = child.getSlotValue(nodeNameAttr);
		  excludesSB.append("(~").append(parentName).append("|~").append(childName).append(")");
	  }
	  if (stringBuilders.size() > 0){
		  for(int i=0; i<stringBuilders.size()-1;i++){
			  sb.append(stringBuilders.get(i).toString()).append("^");
		  }
		  sb.append(stringBuilders.get(stringBuilders.size()-1));
	  }

	  Iteratable<IRNode> childrenIt = graph.children(n);
	  while(childrenIt.hasNext()){
		  graphToSAT(sb, childrenIt.next(), seen);
	  }

  }

  public static void printTree(int ind, IRNode n) {
    System.out.println(spaces(ind) + n.getSlotValue(nodeNameAttr));
    int nc = tree.numChildren(n);
    for (int i = 0; i < nc; i++) {
      printTree(ind + 2, tree.getChild(n, i));
    }
  }
}
