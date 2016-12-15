package edu.uwm.cs.molhado.merge;

import java.util.List;
import java.util.Vector;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.merge.LongestCommonSubsequence.DiffEntry;
import edu.uwm.cs.molhado.merge.LongestCommonSubsequence.DiffType;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser;

class NodeLCS extends LongestCommonSubsequence {

   private Vector<Object>      x;
   private Vector<Object>      y;
   private Version             v0;
   private Version             v1;
   private Tree tree;

   public NodeLCS(Vector<Object> from, Vector<Object> to, Version v0,
         Version v1, Tree tree) {
      this.x = from;
      this.y = to;
      this.v0 = v0;
      this.v1 = v1;
      this.tree = tree;
   }

   @Override
   protected int lengthOfX() {
      return x.size();
   }

   @Override
   protected int lengthOfY() {
      return y.size();
   }

   @Override
   protected Object valueOfX(int index) {
      return x.get(index);
   }

   @Override
   protected Object valueOfY(int index) {
      return y.get(index);
   }

  @Override
  protected boolean equals(Object x1, Object y1) {
    return super.equals(x1, y1);
  }
   public String getHtmlDiff() {
      DiffType type = null;
      List<DiffEntry<Object>> diffs = diff();
      StringBuffer buf = new StringBuffer();

      for (DiffEntry<Object> entry : diffs) {
         if (type != entry.getType()) {
            if (type != null) {
               buf.append("</span>");
            }
            buf.append("<span class=\"" + entry.getType().getName() + "\">");
            type = entry.getType();
         }
         String s = entry.getValue().toString().split("@")[1];
         // buf.append(" " + entry.getValue() + " ");
         buf.append(" " + s + "; ");
      }
      buf.append("</span>");
      return buf.toString();
   }

   protected String getNodeValue(Object o) {
     if (o instanceof IRNode){
       IRNode n = (IRNode) o;
       if (n.valueExists(SimpleXmlParser.tagNameAttr)){
         return n.getSlotValue(SimpleXmlParser.tagNameAttr);
       }
     }
     return null;
   }

   @Override
   protected boolean isInX(Object o) {
      return isIn(o, v0);
   }

   @Override
   protected boolean isInY(Object o) {
      return isIn(o, v1);
   }

   protected boolean isIn(Object o, Version version) {
      boolean isIn = false;
      Version.saveVersion(version);
      if (o instanceof IRNode) {
         IRNode parent = tree.getParentOrNull((IRNode) o);
         isIn = parent == null ? false : true;
      }
      Version.restoreVersion();
      return isIn;
   }

}
