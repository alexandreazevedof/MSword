package edu.uwm.cs.molhado.merge;

import edu.cmu.cs.fluid.ir.IRLocation;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.util.Attribute;
import edu.uwm.cs.molhado.util.AttributeList;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.version.VersionSupport;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * False conflicts:
 * Version 1                           Version 2
 * +1. node n is deleted              1. node n is deleted
 * +2. node n is moved to location    2. node n is moved to location x of the
 *    x of the same parent.             the same parent.
 * +3. node n is moved to location x  3. node n is moved to locatoin x of
 *    of subtree S                      subtree S
 * +4. node n is deleted              4. node x is moved next to node n
 * +5. node n is moved                5. node x is moved next to node n
 * +6. attribute x of n is update     6. attribute x of n is updated(same value)
 * +7. node n is deleted              7. a grand child of node n is deleted
 *
 * True conflicts
 * Version v1                         Version v2
 * +1.  update attribute x of node n   1. update of attribute x of node n (not equal)
 * +2.  n is deleted                   2. n is moved.
 * +3.  n is deleted                   3. attribute x of n is updated
 * +4.  node n is deleted              4. x is added to node n [now n is just deleted]
 * +5.  node n is deleted              5. x is added as grandchild of n
 * +6.  add x as third child of n      6. add y as third child of n (x not same as y)
 * +7.  node x moved as third child of 7. node y moved as third child of node n
 *     n                                  x and y are not the same node
 * +8.  node n moved to p1             8. node n is moved to p2 (p1 not same as p2)
 * +9.  node n moved to location x     9. node n moved to location y of same parent
 *     of the same parent                x and y are not the same location
 *
 * @author chengt
 */


public class XmlDocMerge {
  private final Tree tree;
  private final VersionedChangeRecord changeRecord;
  private final IRNode root;
  private final Vector<ConflictInfo> conflicts = new Vector<ConflictInfo>();
  private final Hashtable<IRNode, Vector<Chunk<IRNode>>> chunksTable = new Hashtable<IRNode, Vector<Chunk<IRNode>>>();

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
    public ConflictInfo(IRNode n, int type, String des, Chunk<IRNode> chunk){
      this.node = n; this.type = type; this.description = des;
      this.attrs = new Vector<String>();
    }
    public ConflictInfo(IRNode n, int type, String des, Vector<String> attrs){
      this(n, type, des, (Chunk<IRNode>)null);
      this.attrs = attrs;
    }
  }

  public XmlDocMerge(Tree tree, VersionedChangeRecord changeRecord, IRNode root,
          VersionTracker tracker1, VersionTracker tracker0, VersionTracker tracker2){
    this.tree = tree;
    this.changeRecord = changeRecord;
    this.root = root;
    this.tracker1 = tracker1;
    this.tracker0 = tracker0;
    this.tracker2 = tracker2;
  }

  public void ignoreOrder(IRNode n) {
    throw new UnsupportedOperationException("Not yet implemented");
  }


//  public static Vector<String> conflictingAttributes2(Version v0, Version v1, Version v2, IRNode n){
//    Vector<String> attrs = new Vector<String>();
//
//    String name0 = null;
//    AttributeList attrs0 = null;
//    Version.saveVersion(v0);
//    if (n.valueExists(SimpleXmlParser.tagNameAttr)){ name0 = n.getSlotValue(SimpleXmlParser.tagNameAttr); }
//    if (n.valueExists(SimpleXmlParser.attrListAttr)){ attrs0 = n.getSlotValue(SimpleXmlParser.attrListAttr); }
//    Version.restoreVersion();
//
//    String name1 = null;
//    AttributeList attrs1 = null;
//    Version.saveVersion(v1);
//    if (n.valueExists(SimpleXmlParser.tagNameAttr)){ name1 = n.getSlotValue(SimpleXmlParser.tagNameAttr); }
//    if (n.valueExists(SimpleXmlParser.attrListAttr)){ attrs1 = n.getSlotValue(SimpleXmlParser.attrListAttr); }
//    Version.restoreVersion();
//
//    String name2 = null;
//    AttributeList attrs2 = null;
//    Version.saveVersion(v2);
//    if (n.valueExists(SimpleXmlParser.tagNameAttr)){ name2 = n.getSlotValue(SimpleXmlParser.tagNameAttr); }
//    if (n.valueExists(SimpleXmlParser.attrListAttr)){ attrs2 = n.getSlotValue(SimpleXmlParser.attrListAttr); }
//    Version.restoreVersion();
//
//    if (name1!=null && name2 !=null){
//      if (!name1.equals(name2) && !name2.equals(name0) && !name1.equals(name0)){
//        attrs.add("name");
//      }
//    }
//
//    if (attrs1 != null && attrs2!= null && attrs1 != attrs0 && attrs2 != attrs0){
//      for(int i=0; i<attrs1.size(); i++){
//        Attribute attr1 = attrs1.get(i);
//        String val1 = attr1.getValue();
//        String val0 = attrs0.getValue(attr1.getName());
//        String val2 = attrs2.getValue(attr1.getName());
//        if (val2 == null) continue;
//        if (val0 == null && !val1.equals(val2)){
//          attrs.add(attr1.getName());
//          continue;
//        }
//        if (!val0.equals(val1) && !val0.equals(val2)) attrs.add(attr1.getName());
//      }
//    }
//
//    return attrs;
//  }
//
  private Vector<String> conflictingAttributes(Version v0, Version v1, Version v2,IRNode n){

    Vector<String> attrs = new Vector<String>();

    Version.saveVersion(v0);
    String v0name = null;
    Vector<String> v0attrNames = new Vector<String>();
    Vector<String> v0attrValues = new Vector<String>();
    if (n.valueExists(SimpleXmlParser3.tagNameAttr) && n.valueExists(SimpleXmlParser3.attrsSeqAttr)){
      v0name = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
      IRSequence<Property> v0seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
      for(int i=0; i<v0seq.size(); i++){
        v0attrNames.add(v0seq.elementAt(i).getName());
        v0attrValues.add(v0seq.elementAt(i).getValue());
      }
    }
    Version.restoreVersion();

    Version.saveVersion(v1);
    String v1name = null;
    Vector<String> v1attrNames = new Vector<String>();
    Vector<String> v1attrValues = new Vector<String>();
    if (n.valueExists(SimpleXmlParser3.tagNameAttr) && n.valueExists(SimpleXmlParser3.attrsSeqAttr)){
      v1name = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
      IRSequence<Property> v1seq = n.getSlotValue( SimpleXmlParser3.attrsSeqAttr);
      for(int i=0; i<v1seq.size(); i++){
        v1attrNames.add(v1seq.elementAt(i).getName());
        v1attrValues.add(v1seq.elementAt(i).getValue());
      }
    }
    Version.restoreVersion();

    Version.saveVersion(v2);
    String v2name = null;
    Vector<String> v2attrNames = new Vector<String>();
    Vector<String> v2attrValues = new Vector<String>();
    if (n.valueExists(SimpleXmlParser3.tagNameAttr) && n.valueExists(SimpleXmlParser3.attrsSeqAttr)){
      v2name = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
      IRSequence<Property> v2seq = n.getSlotValue( SimpleXmlParser3.attrsSeqAttr);
      for(int i=0; i<v2seq.size(); i++){
        v2attrNames.add(v2seq.elementAt(i).getName());
        v2attrValues.add(v2seq.elementAt(i).getValue());
      }
    }
    Version.restoreVersion();

    if (v1name != null && v2name != null) {
      if (!v1name.equals(v2name) && !v2name.equals(v0name)
              && !v1name.equals(v0name)){
        attrs.add("name");
      }
    }

    for(int i=0; i<v1attrNames.size(); i++){
      String s = v1attrNames.get(i);
      int index = v2attrNames.indexOf(s);
      if (index < 0) continue;
      String v1val = v1attrValues.get(i);
      String v2val = v2attrValues.get(index);
      if (v1val.equals(v2val)) continue;
      index = v0attrNames.indexOf(s);
      if (index < 0) {
        if (!v1val.equals(v2val)) attrs.add(s);
        continue;
      }
      String v0val = v0attrValues.get(index);
      if (!v0val.equals(v1val) && !v0val.equals(v2val)) attrs.add(s);
    }

    return attrs;
  }

  private Vector<ConflictInfo> getConflictingInfos(){

    v0 = tracker0.getVersion();
    v1 = tracker1.getVersion();
    v2 = tracker2.getVersion();

    conflictTable.clear();
    conflictingNodes.clear();

    Iterator<IRNode> changes = changeRecord.iterator(tree, root, v0, v1);
    int z=1;
    while(changes.hasNext()){
      IRNode n = changes.next();



      Vector<String> conflictAttrs = conflictingAttributes(v0,v1,v2,n);
      if (conflictAttrs.size() > 0){
        StringBuilder sb = new StringBuilder();
        for(String s:conflictAttrs){
          sb.append(s);
          sb.append(",");
        }
        ConflictInfo ci = new ConflictInfo(n, Chunk.TRUELY_CONFLICTING,
                "Conflicting attribute values for: " + sb.toString(),
                conflictAttrs);
        conflictingNodes.add(ci);
      }
      if (!isInVersion(n, v0)) {
        continue;
      }

 //     if (!SimpleXmlParser.childrenChange.changed(n, v0, v1)) continue;

      Vector<IRNode> childrenInV0 = collectChildren(n, v0);
      Vector<IRNode> childrenInV1 = collectChildren(n, v1);
      Vector<IRNode> childrenInV2 = collectChildren(n, v2);

      Vector<Chunk<IRNode>> chunks = getDiff3(childrenInV1, childrenInV0, childrenInV2);

      chunksTable.put(n, chunks);

      SlotInfo si = SimpleXmlParser3.tagNameAttr;
      for(Chunk<IRNode> c:chunks){


        /**
         * Node A is deleted in v1 but update in v2 (attribute or child update)
         */

        //FIX ME:
        Vector<IRNode> tx = null;
//        if (c.getType() == Chunk.CHANGE_IN_A ){
//          for(IRNode o:c.getO()){
//            if (!c.getA().contains(o)){
//              if (changeRecord.changed(o,v0,v2)){
//                ConflictInfo ci = new ConflictInfo(o, Chunk.TRUELY_CONFLICTING,
//                        "Conflicting changes to node", c);
//                conflictingNodes.add(ci);
//              }
//            }
//          }
//        } else if ( c.getType() == Chunk.CHANGE_IN_B){
//          for(IRNode o:c.getO()){
//            if (!c.getB().contains(o)){
//              if (changeRecord.changed(o,v0,v1)){
//                ConflictInfo ci = new ConflictInfo(o, Chunk.TRUELY_CONFLICTING,
//                        "Conflicting changes to node", c);
//                conflictingNodes.add(ci);
//              }
//            }
//          }
//        }

        if (c.getType() == Chunk.FALSELY_CONFLICTING){
          Vector<IRNode> O = c.getO();
          for(int i=0; i<O.size(); i++){
            IRNode o = O.get(i);


            ConflictInfo ci = conflictTable.get(o);
            if (ci!=null){
              if (ci.type == Chunk.FALSELY_CONFLICTING){
                conflictTable.remove(o);
                conflictingNodes.remove(ci);
              } else {
                /*
                 * check if parents are the same.. => intra move
                 * if parents are different => inter move
                 **/
                Version.saveVersion(v1);
                IRNode aParent = tree.getParent(o);
                Version.restoreVersion();

                Version.saveVersion(v0);
                IRNode oParent = tree.getParent(o);
                Version.restoreVersion();

                Version.saveVersion(v2);
                IRNode bParent = tree.getParent(o);
                Version.restoreVersion();

                if (oParent == aParent){
                  ci.description = "Conflicting moves: moving to different locations in the same parent.";
                } else {
                  ci.description = "Conflicting moves: moving to different locations in the new parent";
                }
              }
            }else {

              Version.saveVersion(v1);
              IRNode aParent = tree.getParent(o);
              Version.restoreVersion();

              Version.saveVersion(v0);
              IRNode oParent = tree.getParent(o);
              Version.restoreVersion();

              Version.saveVersion(v2);
              IRNode bParent = tree.getParent(o);
              Version.restoreVersion();

              if (aParent == null && bParent == null) continue;
              String desc = "Conflicting moves:";
              if (aParent!=bParent){
                desc = desc+ "Node moved to different parents in both versions.";
              } else if (aParent == oParent && oParent == bParent){
                desc = desc+ "Node moved to different locations in same parent in both versions.";
              }
              ci = new ConflictInfo(o, c.getType(), desc, c);
              conflictTable.put(o, ci);
              conflictingNodes.add(ci);
            }
          }
          Vector<IRNode> A = c.getA();
          for(int i=0; i<A.size(); i++){
            IRNode a = A.get(i);
            ConflictInfo ci = conflictTable.get(a);
            if (ci!=null){
              if (ci.type == Chunk.FALSELY_CONFLICTING){
                conflictTable.remove(a);
                conflictingNodes.remove(ci);
              } else {
                /*
                 * check if parents are the same.. => intra move
                 * if parents are different => inter move
                 **/
                Version.saveVersion(v1);
                IRNode aParent = tree.getParent(a);
                Version.restoreVersion();

                Version.saveVersion(v0);
                IRNode oParent = tree.getParent(a);
                Version.restoreVersion();

                Version.saveVersion(v2);
                IRNode bParent = tree.getParent(a);
                Version.restoreVersion();

                if (oParent == aParent){
                  ci.description = "Conflicting moves: moving to different locations in the same parent.";
                } else {
                  ci.description = "Conflicting moves: moving to different locations in the new parent";
                }
              }
            }else {

              Version.saveVersion(v1);
              IRNode aParent = tree.getParent(a);
              Version.restoreVersion();

              Version.saveVersion(v0);
              IRNode oParent = tree.getParent(a);
              Version.restoreVersion();

              Version.saveVersion(v2);
              IRNode bParent = tree.getParent(a);
              Version.restoreVersion();

              if (aParent == null && bParent == null) continue;
              String desc = "Conflicting moves:";
              if (aParent!=bParent){
                desc = desc+ "Node moved to different parents in both versions.";
              } else if (aParent == oParent && oParent == bParent){
                desc = desc+ "Node moved to different locations in same parent in both versions.";
              }
              ci = new ConflictInfo(a, c.getType(), desc,c);
              conflictTable.put(a, ci);
              conflictingNodes.add(ci);
            }
          }
        } else if (c.getType() == Chunk.TRUELY_CONFLICTING){
          Vector<IRNode> A = c.getA();
          Vector<IRNode> O = c.getO();
          Vector<IRNode> B = c.getB();

          /**
           * a node n is moved up in one version, and moved down in another version.
           * this results in two conflicts.  but should only be one.  so code below
           * removes the false conflict.
           */
          Vector<IRNode> unique = new Vector<IRNode>();
          for(IRNode o:O){
            if (!A.contains(o) && !B.contains(o)){
              unique.add(o);
            }
          }
          for(IRNode a:A){
            if (!B.contains(a)){
              unique.add(a);
            }
          }
          for(IRNode b:B){
            if (!A.contains(b)){
              unique.add(b);
            }
          }

          for(IRNode u:unique){
            ConflictInfo ci = new ConflictInfo(u, c.getType(),
                    "Conflicting moves: node moved in same parent but different locations",c);
            conflictingNodes.add(ci);
            conflictTable.put(u, ci);
          }

          /**
           * Handle false conflict where node n is moved or deleted in one version,
           * node x is moved next to node n in another version.
           */
          if ((A.isEmpty() && !O.isEmpty()) || (B.isEmpty() && !O.isEmpty())){
            Version v = null;
            Vector<IRNode> T = null;
            if (A.isEmpty()){
              T=B; v=v1;
            }else {
              T=A; v=v2;
            }

            Vector<IRNode> tmp = new Vector<IRNode>();
            for(IRNode o:O){
              Version.setVersion(v);
              IRNode parent = tree.getParent(o);
              Version.restoreVersion();
              tmp.add(o);
            }

            for(IRNode t:tmp){
              T.remove(t);
              O.remove(t);
              ConflictInfo ci = conflictTable.get(t);
              conflictTable.remove(t);
              conflictingNodes.remove(ci);
            }

            for(IRNode t:T){
              ConflictInfo ci = conflictTable.get(t);
              conflictTable.remove(t);
              conflictingNodes.remove(ci);
            }

            if (T == A){
              c.setType(Chunk.CHANGE_IN_A);
            }else {
              c.setType(Chunk.CHANGE_IN_B);
            }
          } //if

          /**
           * Node A and B are siblings.  Node A is deleted in one version and
           * Node B is deleted in the other version.  This is a conflict in
           * diff3, but it is a false conflict.  We need to make this a
           * non-conflict operation.
           */
          if (!A.isEmpty() && !O.isEmpty() && !B.isEmpty()){
            Vector<IRNode> t = new Vector<IRNode>();
            for(IRNode o:O){
              if (A.contains(o) && B.contains(o)){
              } else if (A.contains(o) || B.contains(o)){
                t.add(o);
              }
            }
            for(IRNode x:t){
              A.remove(x);
              O.remove(x);
              B.remove(x);
              ConflictInfo ci = conflictTable.get(x);
              conflictTable.remove(x);
              conflictingNodes.remove(ci);
            }
            if (A.equals(O) && O.equals(B)){
              c.setType(Chunk.STABLE);
            } else if (A.equals(O) && !O.equals(B)) {
              c.setType(Chunk.CHANGE_IN_B);
            } else if (!A.equals(O) && O.equals(B)){
              c.setType(Chunk.CHANGE_IN_A);
            }
          }
        }


      }
    }
    return conflictingNodes;
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
    Iterator<IRNode> changes = changeRecord.iterator(tree, root, v0, v1);
    outer:while (changes.hasNext()) {
      IRNode node = changes.next();
			//System.out.println("change: " + node.getSlotValue(SimpleXmlParser3.tagNameAttr));
      mergeAttributes(node);
      if (!isInVersion(node, v0)) {
        //is this even possible?
        continue;
      }
  //    if (!SimpleXmlParser.childrenChange.changed(node, v0, v1)) continue;
      Hashtable<IRNode, IRNode> removed = new Hashtable<IRNode, IRNode>();
      Vector<Chunk<IRNode>> chunks = chunksTable.get(node);
      Vector<IRNode> mergeList = getMergeList(chunks);
      mergeChildren(node, mergeList, removed);
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

  private Vector<Chunk<IRNode>> getDiff3(Vector A, Vector O, Vector B) {

    NodeLCS seq1 = new NodeLCS(O, A, v0, v1, tree);
    NodeLCS seq2 = new NodeLCS(O, B, v0, v2, tree);
    seq1.backtrack();
    seq2.backtrack();
    int MA[][] = seq1.b;
    int MB[][] = seq2.b;

    Vector<Chunk<IRNode>> chunks = new Vector<Chunk<IRNode>>();

    int lo = 0, la = 0, lb = 0;

    //step 2,
    step2:
    while (lo <= O.size() && la <= A.size() && lb <= B.size()) {
      boolean i_exists = false;
      int i = 0;
      while (!i_exists && lo+i<O.size()&&la+i<A.size()&&lb+i < B.size()) {
        ++i;
        if (MA[lo + i][la + i] == 0 || MB[lo + i][lb + i] == 0) {
          i_exists = true;
        }
      }
      if (!i_exists) {
        //print stable chunk
        if (lo + 1 <= lo + i) {
          Chunk<IRNode> chunk = makeChunk(A,O,B,la+1,lo+1,lb+1,la+i,lo+i,lb+i);
          chunks.add(chunk);
        }
        lo = lo + i;
        la = la + i;
        lb = lb + i;
        break step2; //go to step 3, print remaining unstable chunk
      }
      if (i == 1) { // (a)
        for (int o = lo + 1; o <= O.size(); o++) {
          for (int a = la + 1; a <= A.size(); a++) {
            for (int b = lb + 1; b <= B.size(); b++) {
              if (MA[o][a] == 1 && MB[o][b] == 1) {
                //unstable chunk
                Chunk<IRNode> chunk = makeChunk(A,O,B,la+1,lo+1,lb+1,a-1,o-1,b-1);
                chunks.add(chunk);
                lo = o - 1;
                la = a - 1;
                lb = b - 1;
                continue step2;
              }
            }
          }
        }
        break; //o does not exist, go to step 3
      } else if (i > 1) { // (b)
        //stable chunk
        Chunk<IRNode> chunk = makeChunk(A,O,B,la+1,lo+1,lb+1,la+i-1,lo+i-1,lb+i-1);
        chunks.add(chunk);
        lo = lo + i - 1;
        la = la + i - 1;
        lb = lb + i - 1;
        continue step2;
      }
    }

    //step3: print unstable chunks left over
    if ((lo < O.size() || la < A.size() || lb < B.size())) {
      Chunk<IRNode> chunk = makeChunk(A,O,B,la+1,lo+1,lb+1,A.size(),O.size(),B.size());
      chunks.add(chunk);
    }

    return chunks;
  }

  private Chunk<IRNode> makeChunk( Vector<IRNode> A,Vector<IRNode> O,
          Vector<IRNode> B, int la, int lo, int lb, int sa, int so, int sb) {
    Vector<IRNode> a = new Vector<IRNode>();
    Vector<IRNode> b = new Vector<IRNode>();
    Vector<IRNode> o = new Vector<IRNode>();

    for (int i = la; i <= sa; i++) {
      a.add(A.get(i - 1));
    }
    for (int i = lb; i <= sb; i++) {
      b.add(B.get(i - 1));
    }
    for (int i = lo; i <= so; i++) {
      o.add(O.get(i - 1));
    }

    Chunk<IRNode> chunk = new Chunk<IRNode>();
    if (o.equals(b) && !b.equals(a)) {
      chunk.setType(Chunk.CHANGE_IN_A);
    } else if (o.equals(a) && !a.equals(b)) {
      chunk.setType(Chunk.CHANGE_IN_B);
    } else if (!o.equals(a) && a.equals(b)) {
      chunk.setType(Chunk.FALSELY_CONFLICTING);
    } else if (!o.equals(a) && !a.equals(b) && !b.equals(o)) {
      chunk.setType(Chunk.TRUELY_CONFLICTING);
    }else {
      chunk.setType(Chunk.STABLE);
    }
    chunk.setA(a);
    chunk.setB(b);
    chunk.setO(o);
    return chunk;
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
//		System.out.println("Merglist: " + mergeList);
//		System.out.println(node.getSlotValue(SimpleXmlParser3.tagNameAttr ) + " :Merge children called");
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

       // mergeAttributes(node);

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
//
//
//  private void mergeAttributes2(IRNode n){
//    Version.saveVersion(v0);
//    String name0 = null;
//		IRSequence<Property> attrsSeq0 = null;
//    AttributeList attrs0 = null;
//
//    if (n.valueExists(SimpleXmlParser3.tagNameAttr)){ name0 = n.getSlotValue(SimpleXmlParser3.tagNameAttr); }
//    if (n.valueExists(SimpleXmlParser3.attrsSeqAttr)){ attrsSeq0 = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr); }
//		attrs0 = copySeqToAttrList(attrsSeq0);
//
//    Version.restoreVersion();
//
//    Version.saveVersion(v1);
//    String name1 = null;
//		IRSequence<Property> attrsSeq1 = null;
//    AttributeList attrs1 = null;
//    if (n.valueExists(SimpleXmlParser3.tagNameAttr)){ name1 = n.getSlotValue(SimpleXmlParser3.tagNameAttr); }
//    if (n.valueExists(SimpleXmlParser3.attrsSeqAttr)){ attrsSeq1 = n.getSlotValue(SimpleXmlParser3.attrSeqAttr); }
//    Version.restoreVersion();
//
//    Version.saveVersion(v2);
//    String name2 = null;
//		IRSequence<Property> attrsSeq2 = null;
//    AttributeList attrs2 = null;
//    if (n.valueExists(SimpleXmlParser3.tagNameAttr)){ name2 = n.getSlotValue(SimpleXmlParser3.tagNameAttr); }
//    if (n.valueExists(SimpleXmlParser3.attrsSeqAttr)){ attrsSeq2 = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr); }
//    Version.restoreVersion();
//
//    if (name1 != null && name2 == null){
//      n.setSlotValue(SimpleXmlParser3.tagNameAttr,name1);
//    }else if (name1 ==null && name2 != null){
//      //nothing to do here.
//    } else if (name1.equals(name2)){
//    }else if (name0 != null && name1.equals(name0)){
//      n.setSlotValue(SimpleXmlParser3.tagNameAttr,name2);
//    } else if (name0 != null && name2.equals(name0)){
//      n.setSlotValue(SimpleXmlParser3.tagNameAttr, name1);
//    }
//
//
//    if (attrs1 == null) return ;
//    if (attrs2 == null) {
//			//need to create a new sequence that have the same value as attrsSeq1 and
//			//then set it here.
//      n.setSlotValue(SimpleXmlParser3.attrsSeqAttr, attrsSeq1);
//      return;
//    }
//
//    AttributeList newList  = new AttributeList(attrs2.size());
//    for(int i=0; i<attrs2.size(); i++){ newList.append(attrs2.get(i)); }
//    boolean changed = false;
//
//    //update
//    for(int i=0; i<attrs0.size(); i++){
//      String s= attrs0.getName(i);
//      String val0 = attrs0.getValue(i);
//      String val1 = attrs1.getValue(s);
//      if (val1 == null) continue;
//      String val2 = attrs2.getValue(s);
//      if (val2 == null) continue;
//
//      if (!val1.equals(val0) && val0.equals(val2)){
//        newList.remove(s);
//        newList.addAttribute(attrs1.get(i));
//        changed = true;
//      }
//    }
//
//    //addition
//    for(int i=0; i<attrs1.size(); i++){
//      Attribute a = attrs1.get(i);
//      if (attrs0.indexOf(a)<0 && attrs2.indexOf(a)<0){
//        newList.addAttribute(a);
//        changed = true;
//      }
//    }
//
//    //delete
//    for(int i=0; i<attrs0.size(); i++){
//      if(attrs1.indexOf(attrs0.get(i))<0 && attrs2.indexOf(attrs0.get(i))>=0){
//        newList.remove(attrs0.getName(i));
//        changed = true;
//      }
//    }
//
//    if (changed) {
//  //    n.setSlotValue(SimpleXmlParser3.attrsSeqAttr, newList);
//    }
//
//  }

  private void mergeAttributes(IRNode n){

    boolean changed = false;

    Version.saveVersion(v0);
    String v0name = null;
    Vector<String> v0attrNames = new Vector<String>();
    Vector<String> v0attrValues = new Vector<String>();
    if (n.valueExists(SimpleXmlParser3.tagNameAttr) && n.valueExists(SimpleXmlParser3.attrsSeqAttr)){
      v0name = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
      IRSequence<Property> v0seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
      for(int i=0; i<v0seq.size(); i++){
        Property pair = v0seq.elementAt(i);
        v0attrNames.add(pair.getName());
        v0attrValues.add(pair.getValue());
      }
    }
    Version.restoreVersion();

    Version.saveVersion(v1);
    String v1name = null;
    Vector<String> v1attrNames = new Vector<String>();
    Vector<String> v1attrValues = new Vector<String>();
    if (n.valueExists(SimpleXmlParser3.tagNameAttr) && n.valueExists(SimpleXmlParser3.attrsSeqAttr)){
      v1name = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
      IRSequence<Property> v1seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
      for(int i=0; i<v1seq.size(); i++){
        Property pair = v1seq.elementAt(i);
        v1attrNames.add(pair.getName());
        v1attrValues.add(pair.getValue());
      }
    }

    Version.restoreVersion();

    String v2name = null;
    if (n.valueExists(SimpleXmlParser3.tagNameAttr)){
      v2name = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
    }
    IRSequence<Property> v2seq = null;
    if (n.valueExists(SimpleXmlParser3.attrsSeqAttr)){
      v2seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
    } else {
      v2seq = VersionedSlotFactory.prototype.newSequence(-1);
      n.setSlotValue(SimpleXmlParser3.attrsSeqAttr, v2seq);
    }
    Vector<Property> v2pairs = new Vector<Property>(v2seq.size());
    Vector<String> v2attrNames = new Vector<String>(v2seq.size());
    Vector<String> v2attrValues = new Vector<String>(v2seq.size());
    for(int i=0; i<v2seq.size(); i++){
      Property pair = v2seq.elementAt(i);
      v2pairs.add(pair);
      v2attrNames.add(pair.getName());
      v2attrValues.add(pair.getValue());
    }


    if (v1name != null && v2name == null){
      n.setSlotValue(SimpleXmlParser3.tagNameAttr,v1name);
    }else if (v1name ==null && v2name != null){
      //nothing to do here.
    } else if (v1name.equals(v2name)){
    }else if (v0name != null && v1name.equals(v0name)){
      n.setSlotValue(SimpleXmlParser3.tagNameAttr,v2name);
    } else if (v0name != null && v2name.equals(v0name)){
      n.setSlotValue(SimpleXmlParser3.tagNameAttr, v1name);
    }

    //update
    for(int i=0; i<v0attrNames.size(); i++){
      String s = v0attrNames.get(i);
      //System.out.print("v0attr:"+s +"=");
      int index1 = v1attrNames.indexOf(s);
      if (index1 < 0) break;
      int index2 = v2attrNames.indexOf(s);
      if (index2 < 0) break;
      String v0val = v0attrValues.get(i);
      String v1val = v1attrValues.get(index1);
      //System.out.println(v1val);
      String v2val = v2attrValues.get(index2);
      if (!v1val.equals(v0val) && v0val.equals(v2val)){
       // System.out.println(v1val +":" +v2val);
        Property pair = new Property(s, v1val);
        IRLocation loc = v2seq.location(index2);
        v2seq.insertElementBefore(pair, loc);
        v2seq.removeElementAt(loc);
        changed = true;
      }
    }

    //addition
    for(int i=0; i<v1attrNames.size(); i++){
      String s = v1attrNames.get(i);
      if (!v0attrNames.contains(s) && !v2attrNames.contains(s)){
        v2seq.appendElement(new Property(s, v1attrValues.get(i)));
        v2attrNames.add(s);
        v2attrValues.add(v1attrValues.get(i));
        changed = true;
      }
    }

    //delete
    for(String s:v0attrNames){
      if (!v1attrNames.contains(s) && v2attrNames.contains(s)){
        for(IRLocation l=v2seq.lastLocation(); ; l = v2seq.prevLocation(l)){
          if (s.equals(v2seq.elementAt(l).getName())){
            v2seq.removeElementAt(l);
            changed = true;
            break;
          }
          if (l == v2seq.firstLocation()) break;
        }
      }
    }

    if (changed){
      n.setSlotValue(SimpleXmlParser3.attrsSeqAttr, v2seq);
    }

  }

  private IRNode copyNode(IRNode node, Hashtable<IRNode, IRNode> removed) {

    tree.initNode(node);
    Enumeration list = SimpleXmlParser3.bundle.attributes();
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
