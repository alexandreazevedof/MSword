package edu.uwm.cs.molhado.merge;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.uwm.cs.molhado.component.Component;
import edu.uwm.cs.molhado.util.Attribute;
import edu.uwm.cs.molhado.util.AttributeList;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class FileTreeMerge {
  private final Tree tree;
  private final IRNode root;
  private final Vector<ConflictInfo> conflicts = new Vector<ConflictInfo>();

  private final Vector<IRNode> ignoreOrder = new Vector<IRNode>();
  private final VersionTracker tracker1;
  private final VersionTracker tracker0;
  private final VersionTracker tracker2;
  private Version v0, v1, v2;
  Hashtable<IRNode, ConflictInfo> conflictTable = new Hashtable<IRNode, ConflictInfo>();
  Vector<ConflictInfo> conflictingNodes = new Vector<ConflictInfo>();


  public static class ConflictInfo{
    public static final int ATTR_CONFLICT = 10;
    public IRNode node;
    public int type;
    public String description;
    public Vector<String> attrs;

		public ConflictInfo(IRNode n, String des){
			this.node = n;
			this.description = des;
		}

    public ConflictInfo(IRNode n, int type, String des, Chunk<IRNode> chunk){
      this.node = n; this.type = type; this.description = des;
      this.attrs = new Vector<String>();
    }
    public ConflictInfo(IRNode n, int type, String des, Vector<String> attrs){
      this(n, type, des, (Chunk<IRNode>)null);
      this.attrs = attrs;
    }
  }

  public FileTreeMerge(Tree tree, IRNode root,
          VersionTracker tracker1, VersionTracker tracker0, VersionTracker tracker2){
    this.tree = tree;
    this.root = root;
    this.tracker1 = tracker1;
    this.tracker0 = tracker0;
    this.tracker2 = tracker2;
  }

	private boolean isNameConflict(IRNode n, Version v0, Version v1, Version v2){

    Version.saveVersion(v0);
    String v0name = n.getSlotValue(Component.nameAttr);
    Version.restoreVersion();

    Version.saveVersion(v1);
    String  v1name = n.getSlotValue(Component.nameAttr);
    Version.restoreVersion();

    Version.saveVersion(v2);
    String  v2name = n.getSlotValue(Component.nameAttr);
    Version.restoreVersion();

    if (v1name != null && v2name != null) {
      if (!v1name.equals(v2name) && !v2name.equals(v0name)
              && !v1name.equals(v0name)){
				return true;
      }
    }
		return false;
	}

  private Vector<ConflictInfo> getConflictingInfos(){

    v0 = tracker0.getVersion();
    v1 = tracker1.getVersion();
    v2 = tracker2.getVersion();

    conflictTable.clear();
    conflictingNodes.clear();

//name conflicts??

		ArrayList<IRNode> nodesNameChange = nodesWithNameChange(root, v1, v0);
		for(IRNode n:nodesNameChange){
			System.out.println(n);
			if (isNameConflict(n, v0, v1, v2)){
				ConflictInfo ci = new ConflictInfo(n, "file naming conflict");
				conflictingNodes.add(ci);
			}
		}

		//content conflicts??
		ArrayList<IRNode> nodesContentChange = nodesWithContentChange(root, v1, v0);
		for(IRNode n:nodesNameChange){
	//		if (isNameConflict(n, v0, v1, v2)){
	//			ConflictInfo ci = new ConflictInfo(n, "moved to different parents");
	//			conflictingNodes.add(ci);
	//		}
		}

    Iterator<IRNode> changes = Component.treeChangeRecord.iterator(tree, root, v1, v0);
    int z=1;
    while(changes.hasNext()){
      IRNode n = changes.next();

			System.out.println("Examining node "+n);

			if (!isInVersion(n, v0)) {
				continue;
			}

			Vector<IRNode> O= collectChildren(n, v0);
			Vector<IRNode> A= collectChildren(n, v1);
			Vector<IRNode> B= collectChildren(n, v2);
			System.out.println("A: " + A.toString());
			System.out.println("O: " + O.toString());
			System.out.println("B: " + B.toString());

			for (IRNode e:O){
				ConflictInfo ci = null;
				if (B.contains(e) && !A.contains(e) && !isInVersion(e, v1) && isChanged(e, v2, v0)){
					//e is deleted in version 1 but has changed in v2 => a conflict
					ci = new ConflictInfo(e, "deleted in one version but changed in the other version");
					conflictingNodes.add(ci);
				}else if (A.contains(e) && !B.contains(e) && !isInVersion(e, v2) && isChanged(e, v1, v0)){
					//e is deleted in version 2 but has changed in v1 => a conflict
					ci = new ConflictInfo(e, "deleted in one version but changed in the other version");
					conflictingNodes.add(ci);
				}else if (!A.contains(e) && !B.contains(e)){
					//node no longer children, could be that both have been deleted or moved
					//1. both deleted - OK
					//2. one deleted, one moved - Conflict
					//3. one moved, the other moveed to the same parent - OK
					//4. one moved, the other moved to different parents - Confict
					IRNode parentA = getParent(e, v1);
					IRNode parentO = getParent(e, v0);
					IRNode parentB = getParent(e, v2);
					if (parentA != null && parentB != null && parentA != parentB){
						//moved to different parents
						ci = new ConflictInfo(e, "moved to different parents");
						conflictingNodes.add(ci);
					} else if (parentA == null && parentB != null){
						//node deleted in A but moved in B
						ci = new ConflictInfo(e, "deleted in one version, moved in another");
						conflictingNodes.add(ci);
					} else if (parentA != null && parentB == null){
						//node moved in A but deleted in B
						ci = new ConflictInfo(e, "deleted in one version, moved in another");
						conflictingNodes.add(ci);
					}
				}
			}

		}
		return conflictingNodes;
	}

	public boolean isChanged(IRNode n, Version vn, Version v0){
		return Component.allChangeRecord.changed(n, vn, v0);
	}

  public Vector<ConflictInfo> getConflicts(){
    return conflictingNodes;
  }

  public Version merge() {
    v0 = tracker0.getVersion();
    v1 = tracker1.getVersion();
    v2 = tracker2.getVersion();

    getConflictingInfos();

    if (conflictingNodes.size() > 0) return null;

    //Version.setVersion(v2);
		Version.saveVersion(v2);

		//name merged
		//file merged
		//structural merge

    Iterator<IRNode> changes = Component.treeChangeRecord.iterator(tree, root, v0, v1);
    outer:while (changes.hasNext()) {
      IRNode node = changes.next();
      mergeName(node);
      if (!isInVersion(node, v0)) {
        //is this even possible?
        continue;
      }
      Hashtable<IRNode, IRNode> removed = new Hashtable<IRNode, IRNode>();
//      Vector<IRNode> mergeList = getMergeList(chunks);
 //     mergeChildren(node, mergeList, removed);
    }
    //return Version.getVersion();
		//Version mv = VersionSupport.commit("merge", "merged");
		//Version.restoreVersion();
		Version mv = VersionSupport.merge(v1, v2, "merge", "merge");
		Version.restoreVersion();
		return mv;
  }

  private Vector<IRNode> getMergeList(Vector<Chunk<IRNode>> chunks) {
    Vector<IRNode> list = new Vector<IRNode>();
    for (Chunk c : chunks) {
      list.addAll(c.getMergeList());
    }
    return list;
  }

  private boolean isInVersion( IRNode node, Version v) {
    if (node == root) {
      return true;
    }
    Version.saveVersion(v);
    IRNode parent = tree.getParentOrNull(node);
    Version.restoreVersion();

    return (parent != null);
  }

  private void mergeChildren(IRNode node, Vector<IRNode> mergeList, Hashtable<IRNode, IRNode> removed) {
		System.out.println("Merglist: " + mergeList);
		System.out.println(node.getSlotValue(Component.nameAttr ) + " :Merge children called");
    //remove nodes in v2 that are not in merge list
    Iterator<IRNode> it = tree.children(node);
    while (it.hasNext()) {
      IRNode x = it.next();
      boolean found = false;
      for (int j = 0; j < mergeList.size(); j++) {
        Object y = mergeList.get(j);
        if (x == y) {
          found = true;
          break;
        }
      }
      if (!found) {
        tree.removeChild(node, x);
        removed.put(x, x);
      }
    }

    //if tree.children(node) == mergeList, no need to do anything
    Vector<IRNode> children = new Vector<IRNode>(tree.childList(node));
    if (children.equals(mergeList)){
      return;
    }

    it = tree.children(node);
    while(it.hasNext()){
      IRNode x = it.next();
      tree.removeChild(node, x);
      removed.put(x,x);
    }

    for(int i=0; i<mergeList.size(); i++){
      IRNode child = mergeList.get(i);
      if (!isInCurrentVersion(child, removed) && !isInVersion(child, v0)){
        child = copyNode(child, removed);
      } else {
        //node exists in v3.. so this must be a moved.
        //need to delete its parent.. before we move it.
        IRNode oparent = tree.getParent(child);
        if (oparent != null) tree.removeChild(oparent, child);

       // mergeName(node);

      }
      tree.addChild(node, child);
    }

  }

	private AttributeList copySeqToAttrList(IRSequence<Property> seq){
		AttributeList list = new AttributeList(seq.size());
		for(int i=0; i<seq.size(); i++){
			Property p = seq.elementAt(i);
			Attribute a = new Attribute(p.getName(), p.getValue());
			list.addAttribute(a);
		}
		return list;
	}

  private void mergeName(IRNode n){

    boolean changed = false;

    Version.saveVersion(v0);
    String  v0name = n.getSlotValue(Component.nameAttr);
    Version.restoreVersion();

    Version.saveVersion(v1);
    String  v1name = n.getSlotValue(Component.nameAttr);
    Version.restoreVersion();

    String v2name = null;
    if (n.valueExists(Component.nameAttr)){
      v2name = n.getSlotValue(Component.nameAttr);
    }

    if (v1name != null && v2name == null){
      n.setSlotValue(Component.nameAttr,v1name);
    }else if (v1name ==null && v2name != null){
      //nothing to do here.
    } else if (v1name.equals(v2name)){
    }else if (v0name != null && v1name.equals(v0name)){
      n.setSlotValue(Component.nameAttr,v2name);
    } else if (v0name != null && v2name.equals(v0name)){
      n.setSlotValue(Component.nameAttr, v1name);
    }
  }

  private IRNode copyNode(IRNode node, Hashtable<IRNode, IRNode> removed) {

    tree.initNode(node);
    Enumeration list = Component.projectBundle.attributes();
    while (list.hasMoreElements()) {
      SlotInfo si = (SlotInfo) list.nextElement();
      Version.saveVersion(v1);
      Object val = null;
      if (node.valueExists(si)) {
        val = node.getSlotValue(si);
      }
      Version.restoreVersion();
      if (val != null) {
		//		System.out.println(si.type());;
		//		System.out.println(si.getClass().getName());
		//		System.out.println(si.name());
        node.setSlotValue(si, val);
      }
    }

    Version.saveVersion(v1);
    int numChildren = tree.numChildren(node);
    IRNode[] children = new IRNode[numChildren];
    for(int i=0; i<numChildren; i++){
      children[i] = tree.getChild(node, i);
    }
    Version.restoreVersion();

    for (int i = 0; i < numChildren; i++) {
      // Node is moved here.
      IRNode child = children[i];
      if (isInCurrentVersion(child, removed)) {
        IRNode oldParent = tree.getParent(child);
        if (oldParent != null) {
          tree.removeChild(oldParent, child);
        }
      } else {
        //node only exists in v1
        //tree.initNode(child);
        //is there really anything to be done here?
      }
      child = copyNode(child, removed);
      tree.addChild(node, child);
    }
    return node;
  }

  private boolean isInCurrentVersion(IRNode node,Hashtable<IRNode, IRNode> removed) {
    if (node == root) {
      return true;
    }
    //node is removed from tree, but still in the current version.
    if (removed.get(node) != null) {
      return true;
    }
    IRNode parent = tree.getParentOrNull(node);
    return (parent != null);
  }

	private IRNode getParent(IRNode n, Version v){
		Version.saveVersion(v);
		IRNode p = Component.projectTree.getParentOrNull(n);
		Version.restoreVersion();
		return p;
	}

	private ArrayList<IRNode> nodesWithNameChange(IRNode root, Version v1, Version v0){
		ArrayList<IRNode> list = new ArrayList<IRNode>();
		VersionedChangeRecord rc = Component.nameChangeRecord;
		Iterator<IRNode> it = Component.projectTree.depthFirstSearch(root);
		while (it.hasNext()) {
			IRNode n = it.next();
			if (rc.changed(n, v1, v0)) {
				list.add(n);
			}
		}
		return list;
	}

	private ArrayList<IRNode> nodesWithContentChange(IRNode root, Version v1, Version v0){
		ArrayList<IRNode> list = new ArrayList<IRNode>();
		VersionedChangeRecord rc = Component.compChangeRecord;
		Iterator<IRNode> it = Component.projectTree.depthFirstSearch(root);
		while (it.hasNext()) {
			IRNode n = it.next();
			if (rc.changed(n, v1, v0)) {
				list.add(n);
			}
		}
		return list;
	}

	private boolean isNameChange(IRNode n, Version v1, Version v0){
		return Component.nameChangeRecord.changed(n, v1, v2);
	}

	private boolean isFileChange(IRNode n, Version v1, Version v0){
		return Component.compChangeRecord.changed(n, v1, v2);
	}

  private Vector<IRNode> collectChildren(IRNode n,Version v) {
    Vector<IRNode> children = new Vector<IRNode>();
    Version.saveVersion(v);
    try {
      int numChildren = tree.numChildren(n);
      for (int i = 0; i < numChildren; i++) {
        children.add(tree.getChild(n, i));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    Version.restoreVersion();
    return children;
  }
}
