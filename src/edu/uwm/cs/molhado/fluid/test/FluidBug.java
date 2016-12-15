package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.version.*;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.util.DirectoryFileLocator;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FluidBug {

  static {
    VersionedRegion.ensureLoaded();
    VersionedChunk.ensureLoaded();
    Version.ensureLoaded();
    Era.ensureLoaded();
    Bundle.ensureLoaded();
  }
  private static IRNode rootNode;
  private final static String STORE = "/tmp/ir-data";
  private final static String PROJ_FILE = "proj.info";
  private static DirectoryFileLocator floc = new DirectoryFileLocator(STORE);
  private static VersionedRegion region;
  private final static Bundle bundle = new Bundle();
  private static Tree tree;
  private static SlotInfo<String> nodeNameAttr;
  private static Era era;
  private static Version rootVersion;


  static {
    try {
      tree = new Tree("bug.project.tree", VersionedSlotFactory.prototype);
      nodeNameAttr = VersionedSlotFactory.prototype.newAttribute("bug.proj.node.name", IRStringType.prototype);
    } catch (SlotAlreadyRegisteredException ex) {
      ex.printStackTrace();
    }
    tree.saveAttributes(bundle);
    bundle.saveAttribute(nodeNameAttr);
    bundle.setName("proj");  //assuming that bundle will set its unique id to this name.
  }

  public static void createStructure() throws IOException {

    era = new Era(Version.getInitialVersion());
    Version.setDefaultEra(era);
    region = new VersionedRegion();

    IRNode i = createNode("i");
    Version v1 = Version.getVersion();
		System.out.println(v1);

    rootVersion = v1;
    rootNode = i;

    Version.setVersion(v1);
    IRNode l = createNode(i, "l");
    Version v2 = Version.getVersion();
		System.out.println(v2);

    Version.setVersion(v1);
    IRNode m = createNode(i, "m");
    Version v3 = Version.getVersion();
		System.out.println(v3);

    era.complete();
  }

  public static void storeStructure() throws IOException {
		IRPersistent.setDebugIO(true);
		IRPersistent.setTraceIO(true);
    era.store(floc);
    region.store(floc);
    VersionedChunk.get(region, bundle).getDelta(era).store(floc);

    ObjectOutputStream oos = new ObjectOutputStream(floc.openFileWrite(PROJ_FILE));
    oos.writeObject(era.getID());
    oos.writeObject(region.getID());
    oos.writeObject(rootVersion);
    oos.writeObject(rootNode);
    oos.close();
  }

  public static void loadStructure() throws IOException, ClassNotFoundException {
		IRPersistent.setDebugIO(true);
		IRPersistent.setTraceIO(true);
    ObjectInputStream ois = new ObjectInputStream(floc.openFileRead(PROJ_FILE));
    era = Era.loadEra((UniqueID) ois.readObject(), floc);
    region = VersionedRegion.loadVersionedRegion((UniqueID) ois.readObject(), floc);
    VersionedChunk.get(region, bundle).getDelta(era).load(floc);

    rootVersion = (Version) ois.readObject();
    rootNode = (IRNode) ois.readObject();

    ois.close();
  }

  public static void main(String[] args) throws OverlappingEraException,
          IOException, ClassNotFoundException {
    if (args[0].equals("create")) {
      FluidBug.createStructure();
      FluidBug.printStructure(rootVersion);
    } else if (args[0].equals("store")) {
      FluidBug.createStructure();
      FluidBug.printStructure(rootVersion);
      FluidBug.storeStructure();
    } else if (args[0].equals("load")) {
      FluidBug.loadStructure();
      FluidBug.printStructure(rootVersion);
    }
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

  private static void printStructure(Version v) {

    printTree(v);

    IRNode n = v.getShadowNode();
    Tree t = (Tree) Version.getShadowTree();
    int nc = t.numChildren(n);
    for (int i = 0; i < nc; i++) {
      IRNode c = t.getChild(n, i);
      printStructure(Version.getShadowVersion(c));
    }
  }

  private static void printTree(Version v) {
    System.out.println("printing tree for version " + v);
    Version.saveVersion(v);
    printTree(2, rootNode);
    Version.restoreVersion();
  }

  private static void printTree(int ind, IRNode n) {
    System.out.println(spaces(ind) + n.getSlotValue(nodeNameAttr));
    int nc = tree.numChildren(n);
    for (int i = 0; i < nc; i++) {
      printTree(ind + 2, tree.getChild(n, i));
    }
  }

  private static String spaces(int n) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < n; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }
}
