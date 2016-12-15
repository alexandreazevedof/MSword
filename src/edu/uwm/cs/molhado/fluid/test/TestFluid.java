package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.tree.TreeInterface;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.cmu.cs.fluid.ir.*;
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

public class TestFluid extends FluidRegistryLoading //implements LookupListener {
{

  private static IRNode rootNode;
  private final static String ERA_NAME = "era";
  private final static String REGION_NAME = "region";
  private final static String STORE = "/tmp/ir-data";
  private final static String PROJ_FILE = "proj.info";
  private static TrueZipFileLocator floc = new TrueZipFileLocator(STORE);
  private static VersionedRegion region;
  private final static Bundle bundle = new Bundle();
  private final static Tree tree;
  private static SlotInfo<String> nodeNameAttr;
  private static Era era;
  private static Version rootVersion;
//  private static Lookup.Result<Version> result;


  static {
    SlotInfo<String> nodeNameAttrX = null;
    Tree treeX = null;
    try {
      treeX = new Tree("project.tree", VersionedSlotFactory.prototype);
      nodeNameAttrX = VersionedSlotFactory.prototype.newAttribute("proj.node.name", IRStringType.prototype);
    } catch (SlotAlreadyRegisteredException ex) {
      ex.printStackTrace();
    }
    tree = treeX;
    nodeNameAttr = nodeNameAttrX;
    tree.saveAttributes(bundle);
    bundle.saveAttribute(nodeNameAttr);
    bundle.setName("proj");
  }

  private static IRNode createNode(String name) {
    IRNode n = new PlainIRNode(region);
    n.setSlotValue(nodeNameAttr, name);
    tree.initNode(n);
    return n;
  }

  private static IRNode createNode(IRNode parent, String name) {
    IRNode n = createNode(name);
    tree.appendChild(parent, n);
    return n;
  }

  public static void createStructure2() throws OverlappingEraException, IOException {
    era = new Era(Version.getInitialVersion());
    era.setName(ERA_NAME);
    Version.setDefaultEra(era);
    region = new VersionedRegion();
    region.setName(REGION_NAME);
    
    Version init = Version.getInitialVersion();
    IRNode a = createNode("a");
    Version v11 = VersionSupport.commit("", "initial version");
    rootVersion = v11;
    rootNode = a;

    Version.setVersion(v11);
    IRNode b = createNode(a, "b");
    Version v12 = VersionSupport.commit("tag", "b is added");
    
    Version.setVersion(v11);
    IRNode d = createNode(a, "d");
    Version v1111 = VersionSupport.commitAsBranch("tag", "new branched.  d is added");

    Version v13 = VersionSupport.merge(v1111, v12, "", "");
    Version mergeSrc = VersionSupport.getMergeSource(v13);
    System.out.println("merge source=" + mergeSrc);

    era.complete();
    
  }

  public static void createStructure() throws OverlappingEraException,
          IOException {

    era = new Era(Version.getInitialVersion());
    era.setName(ERA_NAME);
    Version.setDefaultEra(era);
    region = new VersionedRegion();
    region.setName(REGION_NAME);

    Version init = Version.getInitialVersion();
    IRNode a = createNode("a");
    Version v11 = VersionSupport.commit("", "initial version");
    rootVersion = v11;
    rootNode = a;

    Version.setVersion(v11);
    IRNode b = createNode(a, "b");
    Version v12 = VersionSupport.commit("tag", "b is added");

    Version.setVersion(v12);
    IRNode d = createNode(a, "d");
    Version v1211 = VersionSupport.commitAsBranch("tag", "new branched.  d is added");

    Version.setVersion(v1211);
    IRNode e = createNode(d, "e");
    Version v1212 = VersionSupport.commit("tag", "add e");

    Version.setVersion(v12);
    IRNode c = createNode(a, "c");
    Version v13 = VersionSupport.commit("", "c is added");

    Version.setVersion(v13);
    IRNode i = createNode(a, "i");
    Version v21 = VersionSupport.commit(2, "", "add i, new release");

    Version.setVersion(v21);
    IRNode j = createNode(b, "j");
    Version v2111 = VersionSupport.commitAsBranch("", "new branch, j added");

    Version.setVersion(v21);
    IRNode k = createNode(c, "k");
    Version v22 = VersionSupport.commit("", "add k");

    Version.setVersion(v21);
    IRNode l = createNode(i, "l");
    Version v2121 = VersionSupport.commitAsBranch("", "add l");

    Version v23 = VersionSupport.merge(v2111, v22, "", "merged of v2111 and v22");
    
    Version.setVersion(v23);
    IRNode m = createNode(i, "m");
    Version v24 = VersionSupport.commit("", "added m");

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
    TestFluid tf = new TestFluid();
//    result.addLookupListener(tf);
//    tf.resultChanged(null);
    f.setVisible(true);
  }

  public static void main(String[] args) throws OverlappingEraException,
          IOException, ClassNotFoundException {

    if (args[0].equals("create")) {
      TestFluid.createStructure();
			TestFluid.printVersionTree();
      TestFluid.displayVersionGraph();
    } else if (args[0].equals("store")) {
      TestFluid.createStructure();
      TestFluid.storeStructure();
			TestFluid.printVersionTree();
      TestFluid.displayVersionGraph();
    } else if (args[0].equals("load")) {
      TestFluid.loadStructure();
			TestFluid.printVersionTree();
      TestFluid.displayVersionGraph();
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
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < n; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

	public static void printVersionTree(Version v){
		System.out.println(VersionSupport.getVersionNumber(v));
		if (v!= Version.getInitialVersion()) printTree(v);
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

  public static void printTree(int ind, IRNode n) {
    System.out.println(spaces(ind) + n.getSlotValue(nodeNameAttr));
    int nc = tree.numChildren(n);
    for (int i = 0; i < nc; i++) {
      printTree(ind + 2, tree.getChild(n, i));
    }
  }
}
