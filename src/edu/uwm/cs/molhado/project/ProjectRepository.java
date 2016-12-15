package edu.uwm.cs.molhado.project;

import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.tree.*;
import edu.cmu.cs.fluid.util.*;
import edu.cmu.cs.fluid.version.*;
import java.io.*;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class ProjectRepository {

  private boolean isStored,  isLoaded;
  private String name;
  private FileLocator floc;
  private Version initialVersion;
  private Era initialEra;
  private IRNode rootNode;
  private transient VersionedRegion region;
  private final static Tree tree;
  private final static VersionedChangeRecord changeRecord;
  private final static VersionedSlotFactory VSF = VersionedSlotFactory.prototype;
  private final static IRStringType ST = IRStringType.prototype;
  public final static SlotInfo<String> compNameAttr;
  private final static Bundle bundle = new Bundle();
  private final static String PROJFILE = "proj.info";


  static {
    Tree treeX = null;
    SlotInfo<String> compNameAttrX = null;
    VersionedChangeRecord changeRecordX = null;
    try {
      treeX = new Tree("Config.components", VersionedSlotFactory.prototype);
      changeRecordX = new VersionedChangeRecord("proj.changerecord");
      compNameAttrX = VSF.newAttribute("proj.comp.name", ST, "noname");
    } catch (SlotAlreadyRegisteredException ex) {
      ex.printStackTrace();
    }
    tree = treeX;
    compNameAttr = compNameAttrX;
    changeRecord = changeRecordX;
    PropagateUpTree.attach(changeRecord, tree);
    tree.saveAttributes(bundle);
    bundle.saveAttribute(compNameAttr);
    bundle.setName("proj");
  }

  private ProjectRepository() {
  }

  //existing
  private ProjectRepository(File file) throws IOException {
    floc = toFileLocator(file, ZipFileLocator.READ);
  }

  //new
  public ProjectRepository(File file, String name) {
    this.isStored = false;
    this.name = name;
    floc = toFileLocator(new File(file, name), ZipFileLocator.WRITE);
    Version.saveVersion(Version.getInitialVersion());
    region = new VersionedRegion();
    rootNode = new PlainIRNode(region);
    tree.initNode(rootNode);
    rootNode.setSlotValue(compNameAttr, "root");
    initialVersion = Version.getVersion();
    try {
      initialEra = new Era(Version.getInitialVersion(),
              new Version[]{initialVersion});
    } catch (OverlappingEraException ex) {
      Exceptions.printStackTrace(ex);
    }
    Version.restoreVersion();
  }

  public Version getInitialVersion() {
    return initialVersion;
  }

  public Tree getTree() {
    return tree;
  }

  public IRNode getRootNode() {
    return rootNode;
  }

  public Project getProject(Version v) {
    return Project.getProject(this, v);
  }

  public static ProjectRepository load(File dir) throws IOException {
    ProjectRepository pr = new ProjectRepository(dir);
    pr.ensureLoaded();
    return pr;
  }

  public void store() throws IOException {
    ensureStored();
  }

  private void ensureLoaded() throws IOException {
    if (isLoaded) {
      return;
    }
    InputStream is = floc.openFileRead(PROJFILE);
    ObjectInputStream ois = new ObjectInputStream(is);
    name = ois.readUTF();
    UniqueID eraId = UniqueID.parseUniqueID(ois.readUTF());      //era's id
    initialEra = Era.loadEra(eraId, floc);                       //load era
    UniqueID regId = UniqueID.parseUniqueID(ois.readUTF());      //region's id
    region = VersionedRegion.loadVersionedRegion(regId, floc);   //load region
    VersionedChunk vc = VersionedChunk.get(region, bundle);
    VersionedDelta vd = vc.getDelta(initialEra);
    vd.load(floc);                                               //load delta
    try {
      rootNode = (IRNode) ois.readObject();                      //root node
      initialVersion = (Version) ois.readObject();               //root version
    } catch (ClassNotFoundException e) {
    }

    ois.close();
    isStored = true;
    isLoaded = true;
  }

  private void ensureStored() throws IOException {
    if (isStored) {
      return;
    }
    OutputStream os = floc.openFileWrite(PROJFILE);
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeUTF(name);
    oos.writeUTF(initialEra.getID().toString());                 //era's id
    initialEra.store(floc);                                      //store era
    oos.writeUTF(region.getID().toString());                     //region's id
    region.store(floc);                                          //store region
    VersionedChunk vc = VersionedChunk.get(region, bundle);
    VersionedDelta vd = vc.getDelta(initialEra);
    vd.store(floc);                                              //store delta
    oos.writeObject(rootNode);                                   //root node
    oos.writeObject(initialVersion);                             //root version
    oos.close();
    floc.commit();
    isStored = true;
  }

  public IRNode createNode(String name) {
    IRNode n = new PlainIRNode(region);
    n.setSlotValue(compNameAttr, name);
    tree.initNode(n);
    return n;
  }

  public IRNode createNode(IRNode parent, String name) {
    IRNode n = createNode(name);
    tree.appendChild(parent, n);
    return n;
  }

  private FileLocator toFileLocator(File file, int mode) {
    if (file.getName().endsWith(".far")) {
      try {
        return new ZipFileLocator(file, mode);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return new DirectoryFileLocator(file);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("projname   = " + name + "\n");
    sb.append("path       = " + floc.toString() + "\n");
    sb.append("rootVersion= " + initialVersion + "\n");
    sb.append("rootEra    = " + initialEra + "\n");
    sb.append("rootNode   = " + rootNode + "\n");
    sb.append("region     = " + region + "\n");
    return sb.toString();
  }
}
