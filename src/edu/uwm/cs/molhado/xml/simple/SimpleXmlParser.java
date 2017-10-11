package edu.uwm.cs.molhado.xml.simple;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.ConstantSlotFactory;
import edu.cmu.cs.fluid.ir.IRIntegerType;
import edu.cmu.cs.fluid.ir.IRLongType;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRObjectType;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.IRSequenceType;
import edu.cmu.cs.fluid.ir.IRStringType;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SimpleSlotFactory;
import edu.cmu.cs.fluid.ir.SlotAlreadyRegisteredException;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.PropagateUpTree;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.merge.IRTreeMerge;
import edu.uwm.cs.molhado.util.Attribute;
import edu.uwm.cs.molhado.util.AttributeList;
import edu.uwm.cs.molhado.util.IRPropertyType;
import edu.uwm.cs.molhado.xml.XmlParseException;
import edu.uwm.cs.molhado.util.Property;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;
import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl;

/**
 * Parse XML files or synchonize files with the XML in memory using node ID
 *
 * @author chengt
 */
public class SimpleXmlParser {

    public int c = 0;
    public final static String TREE = "axml.tree";
    public final static String NODETYPE = "axml.type";
    public final static String MOUID = "axml.mouid";
    public final static String TAGNAME = "axml.name";
    public final static String TEXT = "axml.text";
    public final static String ATTRS = "axml.attrs";
    public final static String CHANGE = "axml.change";
    public final static String CHILDREN_CHANGE = "axml.children_change";
    public final static String BUNDLE = "bundle";
    //public final static String IDNAME  = "mouid";
    public final static String IDNAME = "w:rsidR";
    public final static String DOCUMENT_TAG = "w:document";
    public final static String BODY_TAG = "w:body";
    public final static String W_R = "w:r";
    public final static String W_P = "w:p";
    public final static String W_pPR = "w:pPr";
    public final static String RSIDRPR = "w:rsidRPr";

    public final static Tree tree;
    public final static SlotInfo<String> nodeTypeAttr;
    public final static SlotInfo<Long> mouidAttr;
    public final static SlotInfo<String> tagNameAttr;
    public final static SlotInfo<String> textAttr;
    public final static SlotInfo<AttributeList> attrListAttr;
    public final static VersionedChangeRecord changeRecord;
//  public final static VersionedChangeRecord childrenChange;
    public final static Bundle bundle = new Bundle();

    public final static VersionedSlotFactory vsf = VersionedSlotFactory.prototype;
    public final static ConstantSlotFactory csf = ConstantSlotFactory.prototype;
    public final static SimpleSlotFactory ssf = SimpleSlotFactory.prototype;

    static {
        SlotInfo<String> nodeTypeAttrX = null;
        SlotInfo<Long> mouidAttrX = null;
        SlotInfo<String> tagNameX = null;
        SlotInfo<String> textAttrX = null;
        SlotInfo<IRSequence<Property>> attrsX = null;
        SlotInfo<AttributeList> attrListAttrX = null;
        VersionedChangeRecord changeRecordX = null;
//    VersionedChangeRecord childrenChangeX = null;
        Tree treeX = null;
        try {
            treeX = new Tree(TREE, vsf);
            nodeTypeAttrX = csf.newAttribute(NODETYPE, IRStringType.prototype);
//      mouidAttrX = csf.newAttribute(MOUID, IRIntegerType.prototype);
            mouidAttrX = vsf.newAttribute(MOUID, IRLongType.prototype);
            tagNameX = vsf.newAttribute(TAGNAME, IRStringType.prototype);
            textAttrX = vsf.newAttribute(TEXT, IRStringType.prototype);
            //     attrsX = vsf.newAttribute(ATTRS,
//              new IRSequenceType(IRPropertyType.prototype));
            attrListAttrX = vsf.newAttribute("xml.attrs.list", new IRObjectType<AttributeList>());
            changeRecordX = (VersionedChangeRecord) vsf.newChangeRecord(CHANGE);
//      childrenChangeX= (VersionedChangeRecord) vsf.newChangeRecord(CHILDREN_CHANGE);
        } catch (SlotAlreadyRegisteredException ex) {
//      Exceptions.printStackTrace(ex);
            ex.printStackTrace();
        }
        //bundle.setName(BUNDLE);
        bundle.saveAttribute(nodeTypeAttr = nodeTypeAttrX);
        bundle.saveAttribute(mouidAttr = mouidAttrX);
        bundle.saveAttribute(tagNameAttr = tagNameX);
        bundle.saveAttribute(textAttr = textAttrX);
        bundle.saveAttribute(attrListAttr = attrListAttrX);
        changeRecord = changeRecordX;
//    childrenChange = childrenChangeX;
        tree = treeX;
        tree.saveAttributes(bundle);
    }
    private static Hashtable<Long, IRNode> nodeTable = new Hashtable<Long, IRNode>();
    private static Hashtable<Long, String> idTextTable = new Hashtable<Long, String>();
    private static Hashtable<Integer, String> idsTable = new Hashtable<Integer, String>();
    private static Hashtable<Integer, String> idTRTable = new Hashtable<Integer, String>();  //avoid rsidR repetition on w:tr syncparse
    private int mouid = 0;
    private int wrapid = 0;
    private boolean wtc_ID_used = false;
    private static int tableid = 0;
    private String uid = "";
    private String rsidR = "";
    private String rsidRPr = "";
    private String tableID = "";
    private boolean ignoreTag = false;
    private String XMLstring = "";
    private int tablesCount = 0;
    private int tableColumnCount = 0;
    private Hashtable<Long, IRNode> tblGrid_tags = new Hashtable<Long, IRNode>();
    private Hashtable<Long, IRNode> tblGrid_tags_copy = new Hashtable<Long, IRNode>();
    private List<TableGrid> tableGridList = new ArrayList<TableGrid>();
    private static boolean stamp = false;
    private static boolean isT1 = true;
    
    private static List<Tag> tagListT0 = new ArrayList<Tag>();
    private static List<Tag> tagListT1 = new ArrayList<Tag>();
    private static List<Tag> tagListT2 = new ArrayList<Tag>();
    
    public SimpleXmlParser(int nodeCount) {
        this.mouid = nodeCount;
    }

    public void setMouid(int id) {
        mouid = id;
    }

    private XMLReader xmlReader = null;

    private String parserName = "org.apache.xerces.parsers.SAXParser";

    private XMLReader getReader() throws SAXException {
        if (xmlReader == null) {
            xmlReader = XMLReaderFactory.createXMLReader(parserName);
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", false);
            xmlReader.setFeature("http://xml.org/sax/features/validation", false);
        }
        return xmlReader;
    }

    public static void main(String[] args) throws XmlParseException, Exception {
        SimpleXmlParser p = new SimpleXmlParser(0);
//		IRNode doc = p.parse(new File("/home/chengt/merge-demo/gallardoX.svg"));
//		Version vx = Version.getVersion();
//		p.writeToFile(new File("/home/chengt/out.svg"), doc);
//
//		if (true) return;

        // String input = "<a x='10'><b y='20' z='30'/></a>";
        // IRNode root = p.parse(input);
        // System.out.println(p.toString(root));
        String dir = "big_doc\\thesis";
        //parse, add ids and create temporary xml
        
//        stamp = true;
//        IRNode t0_stamped = p.parse(new File("C:\\Users\\agaze\\Documents\\Faculdade\\UWM - Master\\Thesis\\Xml samples\\" + dir + "\\t0.xml"));
//        SimpleXmlParser.writeToFile("C:\\Users\\agaze\\Documents\\Faculdade\\UWM - Master\\Thesis\\Xml samples\\" + dir + "\\t0_stamped.xml", t0_stamped, true);
//        p = new SimpleXmlParser(0);
        long startMem = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
        long t0 = System.currentTimeMillis();

//    IRNode root = p.parse(new File("/home/alex/NetBeansProjects/momerge/dist/t0.xml"));
        IRNode root = p.parse(new File("C:\\Users\\agaze\\Documents\\Faculdade\\UWM - Master\\Thesis\\Xml samples\\" + dir + "\\t0_stamped.xml"));

        Version v0 = Version.getVersion();
        tagNameAttr.addDefineObserver(changeRecord);
//    attrsSeqAttr.addDefineObserver(changeRecord);
        attrListAttr.addDefineObserver(changeRecord);
        tree.addObserver(changeRecord);
//    tree.addObserver(childrenChange);
        PropagateUpTree.attach(changeRecord, tree);
        Version.saveVersion(v0);

        long t1 = System.currentTimeMillis();
//    p.parse(root, new File("/home/alex/NetBeansProjects/momerge/dist/t1.xml"));
        p.parse(root, new File("C:\\Users\\agaze\\Documents\\Faculdade\\UWM - Master\\Thesis\\Xml samples\\" + dir + "\\t1.xml"));
        Version v1 = Version.getVersion();
        long t2 = System.currentTimeMillis();
        Version.saveVersion(v0);

        isT1 = false;
//    p.parse( root, new File("/home/alex/NetBeansProjects/momerge/dist/t2.xml"));
        p.parse(root, new File("C:\\Users\\agaze\\Documents\\Faculdade\\UWM - Master\\Thesis\\Xml samples\\" + dir + "\\t2.xml"));
        long t3 = System.currentTimeMillis();
        //nodeTable = null;
        Version v2 = Version.getVersion();
        Version.saveVersion(v0);

        IRTreeMerge merge = new IRTreeMerge(SimpleXmlParser.tree, SimpleXmlParser.changeRecord,
                root, new VersionMarker(v1), new VersionMarker(v0), new VersionMarker(v2));
        
        System.out.println("");
        System.out.println("");
        System.out.println("");
        for(int i=0; i< tagListT0.size(); i++){
            if(tagListT0.get(i).id != tagListT1.get(i).id){
                System.out.println("Conflict:  T0-T1 ");
                System.out.println("TagT0: "+tagListT0.get(i).tag + " ID:"+tagListT0.get(i).id);
                System.out.println("TagT1: "+tagListT1.get(i).tag + " ID:"+tagListT1.get(i).id);
                System.out.println("");
            }
            
//            if(tagListT0.get(i).id != tagListT2.get(i).id){
//                System.out.println("Conflict:  T0-T2 ");
//                System.out.println("Tag: "+tagListT2.get(i).tag + " ID:"+tagListT2.get(i).id);
//                System.out.println("");
//            }
//            
//            if(tagListT1.get(i).id != tagListT2.get(i).id){
//                System.out.println("Conflict:  T1-T2 ");
//                System.out.println("Tag: "+tagListT2.get(i).tag + " ID:"+tagListT2.get(i).id);
//                System.out.println("");
//            }
            
        }
        Version v3 = merge.merge();
        long t4 = System.currentTimeMillis();
        Version.saveVersion(v3);
        SimpleXmlParser.writeToFile("C:\\Users\\agaze\\Documents\\Faculdade\\UWM - Master\\Thesis\\Xml samples\\" + dir + "\\output.xml", root, false);
        long t5 = System.currentTimeMillis();

        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.out.println("parse: " + (t3 - t0));
        //System.out.println("second: " + (t2-t1));
        //System.out.println("third: " + (t3-t2));
        System.out.println("merge: " + (t4 - t3));
        System.out.println("write: " + (t5 - t4));
        System.out.println("total: " + (t5 - t0));

        System.out.println("memory: " + (endMem - startMem));
        //System.out.println(nodeTable.size());
    }

    private static String indent(int n) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
//
//  private static void dumpAttrs(FileWriter w, int indent, IRNode n) throws IOException{
//    IRSequence<Property> sq = n.getSlotValue(attrsSeqAttr);
//    for(int i=0; i<sq.size(); i++){
//      Property p = sq.elementAt(i);
//      w.write("\n");
//      indent(w, indent + 2);
//      w.write(p.getName());
//      w.write("=\"");
//      w.write(p.getValue());
//      w.write("\" ");
//      w.flush();
//    }
//  }

    private static String dumpAttrs2(int indent, IRNode n) throws IOException {
        StringBuilder sb = new StringBuilder();
        AttributeList sq = n.getSlotValue(SimpleXmlParser.attrListAttr);
        if (sq.size() == 0) {
            return "";
        }
        sb.append(" ");
        for (int i = 0; i < sq.size(); i++) {
            Attribute p = sq.get(i);
            // w.println();
            // indent(w,indent + 2);
            if (!p.getName().equals(SimpleXmlParser.RSIDRPR)) {
                sb.append(p.getName());
                sb.append("=\"");
                sb.append(p.getValue());
                sb.append("\" ");
            }
        }
        return sb.toString();
    }
//
//  private static String dumpAttrs(int indent, IRNode n){
//    StringBuilder sb = new StringBuilder();
//    IRSequence<Property> sq = n.getSlotValue(attrsSeqAttr);
//    for(int i=0; i<sq.size(); i++){
//      Property p = sq.elementAt(i);
//      sb.append("\n");
//      nd(indent(indent + 2) + p.getName());
//      sb.append("=\"");
//      sb.append(p.getValue());
//      sb.append("\" ");
//    }
//    return sb.toString();
//  }

//  private static void dumpInkscapeTextNode(FileWriter w, int indent, IRNode n, boolean withID) throws IOException{
//    String type = n.getSlotValue(nodeTypeAttr);
//    String tagName = n.getSlotValue(tagNameAttr);
//    if (tagName.equals("text")){
//      w.write(indent(indent)+"<");
//      w.write(tagName);
//      if (withID) w.write(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser.mouidAttr) + "\"");
//      w.write(dumpAttrs(indent, n));
//      int num = tree.numChildren(n);
//      if (num>0){
//        w.write(">");
//        for (int i = 0; i < num; i++) {
//          IRNode c = tree.getChild(n, i);
//          dumpInkscapeTextNode(w, indent + 2, c, withID);
//        }
//        w.write("</");
//        w.write(tagName);
//        w.write(">\n");
//      } else {
//        w.write(" />\n");
//      }
//    }else if (tagName.equals("tspan")){
//      w.write("<");
//      w.write(tagName);
//      if (withID) w.write(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser.mouidAttr) + "\"");
//      w.write(dumpAttrs(indent, n));
//      int num = tree.numChildren(n);
//      if (num>0){
//        w.write(">");
//        for (int i = 0; i < num; i++) {
//          IRNode c = tree.getChild(n, i);
//          dumpInkscapeTextNode(w, indent + 2, c, withID);
//        }
//        w.write("</");
//        w.write(tagName);
//        w.write(">");
//      } else {
//        w.write(" />");
//      }
//    } else if (tagName.equals("char")){
//      w.write(n.getSlotValue(textAttr));
//    }
//  }
//  private static void dumpInkscapeTextNode(StringBuilder sb, int indent, IRNode n, boolean withID){
//    String type = n.getSlotValue(nodeTypeAttr);
//    String tagName = n.getSlotValue(tagNameAttr);
//    if (tagName.equals("text")){
//      sb.append(indent(indent)+"<");
//      sb.append(tagName);
//      if (withID) sb.append(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser.mouidAttr) + "\"");
//      sb.append(dumpAttrs(indent, n));
//      int num = tree.numChildren(n);
//      if (num>0){
//        sb.append(">");
//        for (int i = 0; i < num; i++) {
//          IRNode c = tree.getChild(n, i);
//          dumpInkscapeTextNode(sb, indent + 2, c, withID);
//        }
//        sb.append("</");
//        sb.append(tagName);
//        sb.append(">\n");
//      } else {
//        sb.append(" />\n");
//      }
//    }else if (tagName.equals("tspan")){
//      sb.append("<");
//      sb.append(tagName);
//      if (withID) sb.append(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser.mouidAttr) + "\"");
//      sb.append(dumpAttrs(indent, n));
//      int num = tree.numChildren(n);
//      if (num>0){
//        sb.append(">");
//        for (int i = 0; i < num; i++) {
//          IRNode c = tree.getChild(n, i);
//          dumpInkscapeTextNode(sb, indent + 2, c, withID);
//        }
//        sb.append("</");
//        sb.append(tagName);
//        sb.append(">");
//      } else {
//        sb.append(" />");
//      }
//    } else if (tagName.equals("char")){
//      sb.append(n.getSlotValue(textAttr));
//    }
//  }
//  private static void dumpContent(FileWriter w, int indent, IRNode n, boolean withID) throws IOException {
//   // StringBuilder sb = new StringBuilder();
//    String type = n.getSlotValue(nodeTypeAttr);
//    String tagName = n.getSlotValue(tagNameAttr);
//    if (!type.equals("text")){
//      if (tagName.equals("text")){
//      //  dumpInkscapeTextNode(w, indent, n, withID);
//      } else {
//        w.write(indent(indent) + "<");
//        w.write(tagName);
//        //if (withID) sb.write(" uid='" + n.hashCode() + "'");
//        if (withID) w.write(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser.mouidAttr) + "\"");
//        w.write(dumpAttrs(indent, n));
//        int num = tree.numChildren(n);
//        if (num>0){
//          w.write(">\n");
//          for (int i = 0; i < num; i++) {
//            IRNode c = tree.getChild(n, i);
//            dumpContent(w, indent + 2, c, withID);
//          }
//          w.write( indent(indent) + "</");
//          w.write(tagName);
//          w.write(">\n");
//        } else {
//          w.write(" />\n");
//        }
//      }
//    } else if (type.equals("char")){
//      w.write(n.getSlotValue(textAttr));
//    }
//    //return sb.toString();
//  }
//  private static void dumpContent(StringBuilder sb, int indent, IRNode n, boolean withID) {
//   // StringBuilder sb = new StringBuilder();
//    String type = n.getSlotValue(nodeTypeAttr);
//    String tagName = n.getSlotValue(tagNameAttr);
//    if (!type.equals("text")){
//      if (tagName.equals("text")){
//        dumpInkscapeTextNode(sb, indent, n, withID);
//      } else {
//        sb.append(indent(indent) + "<");
//        sb.append(tagName);
//        //if (withID) sb.append(" uid='" + n.hashCode() + "'");
//        if (withID) sb.append(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser.mouidAttr) + "\"");
//        sb.append(dumpAttrs(indent, n));
//        int num = tree.numChildren(n);
//        if (num>0){
//          sb.append(">\n");
//          for (int i = 0; i < num; i++) {
//            IRNode c = tree.getChild(n, i);
//            dumpContent(sb, indent + 2, c, withID);
//          }
//          sb.append( indent(indent) + "</");
//          sb.append(tagName);
//          sb.append(">\n");
//        } else {
//          sb.append(" />\n");
//        }
//      }
//    } else if (type.equals("char")){
//      sb.append(n.getSlotValue(textAttr));
//    }
//    //return sb.toString();
//  }
//    private static String indent(int n) {
//    StringBuilder sb = new StringBuilder();
//    for (int i = 0; i < n; i++) { sb.append(" "); }
//    return sb.toString();
//  }
//  private static void dumpAttrs(Writer w, int indent, IRNode n) throws IOException {
//    AttributeList list = n.getSlotValue(attrListAttr);
//    for(int i=0; i<list.size(); i++){
//      Attribute a = list.get(i);
//      w.write("\n");
//      w.write(indent(indent + 2) + a.getName());
//      w.write("=\"");
//      w.write(a.getValue());
//      w.write("\" ");
//    }
//  }
//  private static void dumpInkscapeTextNode(Writer w, int indent, IRNode n, boolean withID) throws IOException{
//    //String type = n.getSlotValue(nodeTypeAttr);
//    String tagName = n.getSlotValue(tagNameAttr);
//    if (tagName.equals("text")){
//      w.write(indent(indent)+"<");
//      w.write(tagName);
//      if (withID) w.write(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser2.mouidAttr) + "\"");
//      dumpAttrs(w, indent, n);
//      List<IRNode> children = getChildren(n);
//      if (children != null && children.size()>0){
//        w.write(">");
//        for (int i = 0; i < children.size(); i++) {
//          IRNode c = children.get(i);
//          dumpInkscapeTextNode(w, indent + 2, c, withID);
//        }
//        w.write("</");
//        w.write(tagName);
//        w.write(">\n");
//      } else {
//        w.write(" />\n");
//      }
//    }else if (tagName.equals("tspan")){
//      w.write("<");
//      w.write(tagName);
//      if (withID) w.write(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser2.mouidAttr) + "\"");
//      dumpAttrs(w,indent, n);
//      List<IRNode> children = getChildren(n);
//      if (children !=null && children.size()>0){
//        w.write(">");
//        for (int i = 0; i < num; i++) {
//          IRNode c = tree.getChild(n, i);
//          dumpInkscapeTextNode(w, indent + 2, c, withID);
//        }
//        w.write("</");
//        w.write(tagName);
//        w.write(">");
//      } else {
//        w.write(" />");
//      }
//    } else if (tagName.equals("char")){
//  //    w.write(n.getSlotValue(textAttr));
//    }
//  }
    private static void dumpContent(Writer w, int indent, IRNode n, boolean withID, boolean assignID) throws IOException {
        String tagName = n.getSlotValue(tagNameAttr);
        long longid;
        int wrapid;
        String hex;

        if (tagName.equals("w:tblCaption")) {
            return;
        }
        if (tagName.equals("w:tbl")) {
            longid = n.getSlotValue(SimpleXmlParser.mouidAttr);
            tableid = (int) longid;
        }

        w.write("<");
        w.write(tagName);
        if (tagName.equals(SimpleXmlParser.W_R)) {
            longid = n.getSlotValue(SimpleXmlParser.mouidAttr);
//            int rsid = (int) (longid >> 32);
            wrapid = (int) longid;
            hex = String.format("%08X", wrapid);
            if (withID) {
                w.write(" " + RSIDRPR + "=\"" + hex + "\"");
            }
        } else if (tagName.equals("w:p")) {
            longid = n.getSlotValue(SimpleXmlParser.mouidAttr);
            int rsid = (int) (longid >> 32);
            hex = String.format("%08X", rsid);
            if (withID) {
                w.write(" " + IDNAME + "=\"" + hex + "\"");
            }
        } else if (tagName.equals("w:tr")) {
            longid = n.getSlotValue(SimpleXmlParser.mouidAttr);
            wrapid = (int) longid;
            hex = String.format("%08X", wrapid);
            w.write(" " + IDNAME + "=\"" + hex + "\"");
        }
        w.write(dumpAttrs2(indent, n));

        int numChildren = tree.numChildren(n);
        if (numChildren > 0) {
            w.write(">\n");
            if (tagName.equals("w:tblPr")) {
                hex = String.format("%08X", tableid);
                w.write("<w:tblCaption w:val=\"ID:" + hex + "\"/>");
            }
            for (int i = 0; i < numChildren; i++) {
                IRNode c = tree.getChild(n, i);
                dumpContent(w, indent + 2, c, withID, assignID);
            }
            w.write(indent(indent));
            w.write("</");
            w.write(tagName);
            w.write(">");
            //   w.write("</"+tagName+">");
        } else {
            if (tagName.equals("w:t") || tagName.equals("w:instrText")) {
                w.write(">");
                if (idTextTable.get(n.getSlotValue(SimpleXmlParser.mouidAttr)) != null) {
                    w.write(idTextTable.get(n.getSlotValue(SimpleXmlParser.mouidAttr)));
                }

                w.write("</");
                w.write(tagName);
                w.write(">");
                return;
            }
            w.write(" />\n");
        }
    }

    public static void writeToFile(File file, IRNode n, boolean assignID) throws IOException {
        FileWriter w = new FileWriter(file);
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        dumpContent(w, 0, n, true, assignID);
        w.close();
    }

    public static void writeToFile(String file, IRNode n, boolean assignID) throws IOException {
        writeToFile(new File(file), n, assignID);
    }

    public static String toString(IRNode n) {
        StringWriter writer = new StringWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//    try { dumpContent(writer, 0, n, true);
//    } catch (IOException ex) { Exceptions.printStackTrace(ex); }
        return writer.toString();
    }

    public static String toStringWithID(IRNode n) {
        StringWriter w = new StringWriter();
        //w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        //try { dumpContent(writer, 0, n, true);
        //} catch (IOException ex) { Exceptions.printStackTrace(ex); }
        //w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        try {
            dumpContent(w, 0, n, true, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        // w.close();
        return w.toString();
    }

//
//  public static void writeToFile(File file, IRNode n) throws IOException{
//    FileWriter w = new FileWriter(file);
//    w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//    dumpContent(w, 0, n, true);
//    w.close();
//  }
//  public static void writeToFile(String file, IRNode n) throws IOException{
//    FileWriter w = new FileWriter(new File(file));
//    w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//    dumpContent(w, 0, n, true);
//    w.close();
//  }
//  public static String toString(IRNode n) {
//    StringBuilder sb = new StringBuilder();
//    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//    dumpContent(sb, 0, n, false);
//    return sb.toString();
//  }
//
//  public static String toStringWithID(IRNode n) {
//    StringBuilder sb = new StringBuilder();
//    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//    dumpContent(sb, 0, n, true);
//    return sb.toString();
//  }
    public IRNode parse(File file) throws Exception {
        return parse(new FileInputStream(file));
    }

    public IRNode parse(Reader reader) throws Exception {
        return parse(new InputSource(reader));
    }

    public IRNode parse(InputStream is) throws Exception {
        Tuple t = convertStreamToString(is);
        XMLstring = t.str;
        return parse(new InputSource(t.is));
    }

    public IRNode parse(String str) throws Exception {
        return parse(new StringReader(str));
    }

    public IRNode parse(IRNode root, File file) throws Exception {
        Tuple t = convertStreamToString(new FileInputStream(file));
        XMLstring = t.str;
        return parse(root, new FileInputStream(file));
    }

    public IRNode parse(IRNode root, Reader reader) throws Exception {
        return parse(root, new InputSource(reader));
    }

    public IRNode parse(IRNode root, InputStream is) throws Exception {
        return parse(root, new InputSource(is));
    }

    public IRNode parse(IRNode root, String str) throws Exception {
        return parse(root, new StringReader(str));
    }

    //object create just to return InputStream and string from method convertStreamToString
    private static class Tuple {

        InputStream is;
        String str;

        private Tuple(InputStream is, String str) {
            this.is = is;
            this.str = str;
        }

    }
    
    private static class Tag{
        String tag;
        Long id;
        
        public Tag(String tag, Long id){
            this.tag = tag;
            this.id = id;
        }
    }

    private static class TableGrid{
            
            public int tableID;
            public Long insideTagID;
            public IRNode node;
            
            private TableGrid(int tableID, Long insideTagID, IRNode node){
                this.tableID = tableID;
                this.insideTagID = insideTagID;
                this.node = node;
            }
        }
    
    public Tuple convertStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        is = new ByteArrayInputStream(result.toByteArray());
        // StandardCharsets.UTF_8.name() > JDK 7
        return new Tuple(is, result.toString("UTF-8"));
    }

    private IRNode parse(InputSource is) throws Exception {
        tablesCount = 0;
        tableColumnCount = 0;
        XMLReader parser = getReader();
        NormalParsingHandler handler = new NormalParsingHandler();
        parser.setContentHandler(handler);
        parser.parse(is);
        return handler.getRoot();
    }

    private IRNode parse(IRNode root, InputSource is) throws Exception {
        tablesCount = 0;
        tableColumnCount = 0;
        tblGrid_tags_copy =  (Hashtable<Long, IRNode>) tblGrid_tags.clone();
        XMLReader parser = getReader();
        parser.setContentHandler(new SyncParsingHandler(root));
        parser.parse(is);
        return root;
    }

    /**
     * This method looks odd but the idea is not to duplicate code for creating
     * an XML node among different classes. A class that needs to create an XML
     * node within a version tracker needs to :      <code>
     * final static IRNode newNode = new PlainIRNode();
     * tracker.executeIn(new Runnable() {
     *     public void run() {
     *       SimpleXmlParser.initNode(newNode, "noname", null);
     *     tree.addChild(node, newNode);
     *   }
     * })
     * </code>
     *
     * @param node
     * @param tagName
     * @param attrs
     */
    public void initNode2(IRNode node, String tagName, Attributes attrs, boolean newNode, boolean styleOut, boolean styleOutTable, boolean wtcChield) {
        
        
        //todo
//         make sectPr work
                
                
        node.setSlotValue(tagNameAttr, tagName);
        AttributeList attrList = new AttributeList(4);

        long longid = -1;

        //document and body tag
        if (tagName.equals(SimpleXmlParser.DOCUMENT_TAG) || tagName.equals(SimpleXmlParser.BODY_TAG)) {
            
            wrapid++;
            longid = (((long) mouid) << 32) | (wrapid & 0xffffffffL);

        } else if ((attrs.getValue(SimpleXmlParser.RSIDRPR) == null) && tagName.equals(SimpleXmlParser.W_R)) {
            
            //creates new random generated rsidRPr for the <w:r> tags when rsidRPr is not present
            do {
                wrapid = (int) Long.parseLong(getRandomHexString(8), 16);
                longid = (((long) mouid) << 32) | (wrapid & 0xffffffffL);
            } while (nodeTable.containsKey(longid) || idsTable.containsKey(wrapid));

            idsTable.put(wrapid, tagName);
            if (newNode) {
                rsidRPr = String.format("%08X", wrapid);
            }
            
        } else if (tagName.equals("w:p")) {
            
            //do nothing
            
        } else if (tagName.equals("w:tbl")) {
            
            String tableIDs = getTableID(tablesCount);
            if (tableIDs.equals("")) {
                do {
                    tableid = (int) Long.parseLong(getRandomHexString(8), 16);
                    longid = (((long) tagName.hashCode()) << 32) | (tableid & 0xffffffffL);
                } while (nodeTable.containsKey(longid) || idsTable.containsKey(tableid));
            } else {
                tableid = (int) Long.parseLong(tableIDs, 16);
                longid = (((long) tagName.hashCode()) << 32) | (tableid & 0xffffffffL);
            }
            idsTable.put(tableid, tagName);
            
        } else if (tagName.equals("w:tr")) {
            System.out.println();
            
            // do nothing
            
        } else if (tagName.equals("w:tc")) {
            
            wtc_ID_used = false;
            if (!stamp) {
                mouid = (int) Long.parseLong(getTableColumnID(tableColumnCount), 16);
            } else {
                mouid = (int) Long.parseLong(getRandomHexString(8), 16);
            }
            
            longid = (((long) tagName.hashCode()) << 32) | (mouid & 0xffffffffL);
            while (nodeTable.containsKey(longid) || nodeTable.containsKey((((long) mouid << 32) | (0 & 0xffffffffL))) ) {
                mouid = (int) Long.parseLong(getRandomHexString(8), 16);
                longid = (((long) tagName.hashCode()) << 32) | (mouid & 0xffffffffL);
            }

        } else {
            
            if (styleOut || wtcChield) {
                longid = (((long) mouid) << 32) | (tagName.hashCode() & 0xffffffffL);
                while (nodeTable.containsKey(longid)) {
                    longid = (((long) mouid) << 32) | ((int) Long.parseLong(getRandomHexString(8), 16) & 0xffffffffL);
                }
            } else if (styleOutTable) {
                longid = (((long) tableid) << 32) | (tagName.hashCode() & 0xffffffffL);
                while (nodeTable.containsKey(longid)) {
                    longid = (((long) tableid) << 32) | ((int) Long.parseLong(getRandomHexString(8), 16) & 0xffffffffL);
                }
            } else {
                longid = (((long) wrapid) << 32) | (tagName.hashCode() & 0xffffffffL);
            }
        }

        for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
            String name = attrs.getQName(i);
            String val = attrs.getValue(i);
            if (name.equals(SimpleXmlParser.IDNAME) && tagName.equals("w:p")) {
                //Combining two integer to Long
                //hi = mouid   lo= wrapid
                // wrapid for rsidR nodes will be always zero
                
                if (wtcChield && !wtc_ID_used) {
                    //use mouid calculated when w:tc was parsed
                    wtc_ID_used = true;
                } else {
                    mouid = (int) Long.parseLong(val, 16);
                }
                
                wrapid = 0;
                longid = (((long) mouid) << 32) | (wrapid & 0xffffffffL);
                while (nodeTable.containsKey(longid) || idsTable.containsKey(mouid)) {
                    mouid = (int) Long.parseLong(getRandomHexString(8), 16);
                    longid = (((long) mouid) << 32) | (wrapid & 0xffffffffL);
                }
                idsTable.put(mouid, tagName);

            } else if (tagName.equals(SimpleXmlParser.W_R) && name.equals(SimpleXmlParser.RSIDRPR)) {

                wrapid = (int) Long.parseLong(val, 16);
                longid = (((long) mouid) << 32) | (wrapid & 0xffffffffL);
//                -6891720328633701136
                /*if rsidRPr is alredy present, generate a new one*/
                while (nodeTable.containsKey(longid) || idsTable.containsKey(wrapid)) {
                    wrapid = (int) Long.parseLong(getRandomHexString(8), 16);
                    longid = (((long) mouid) << 32) | (wrapid & 0xffffffffL);
                }
                //7548144
                idsTable.put(wrapid, tagName);
                if (newNode) {
                    rsidRPr = String.format("%08X", wrapid);
                }
//                
            } else if (tagName.equals("w:tr") && name.equals("w:rsidR")) {

                wrapid = (int) Long.parseLong(val, 16);
                longid = (((long) tableid) << 32) | (wrapid & 0xffffffffL);
                while (nodeTable.containsKey(longid)) {
                    wrapid = (int) Long.parseLong(getRandomHexString(8), 16);
                    longid = (((long) tableid) << 32) | (wrapid & 0xffffffffL);
                }
                idsTable.put(wrapid,tagName);
            } else {
                attrList.addAttribute(new Attribute(name, val));
            }
        }
        
        System.out.println(tagName+" "+longid);
        
        
        if (longid != -1) {
            if (newNode) {
                node.setSlotValue(mouidAttr, longid);
            } else {
                node.setSlotValue(mouidAttr, longid);
                nodeTable.put(longid, node);
            }
        }

        node.setSlotValue(attrListAttr, attrList);

//        if(!newNode){
            tree.initNode(node);
//        }

        
    }

    private String getRandomHexString(int numchars) {
        UUID r = UUID.randomUUID();
        int rand = (int) r.getLeastSignificantBits();
        return String.format("%08X", rand);
    }

    private String getTableID(int tableCount) {
        int tableStartIndex = XMLstring.indexOf("<w:tbl>");
        if(tableStartIndex == -1){
            return "";
        }
        while (--tableCount > 0 && tableStartIndex != -1) {
            tableStartIndex = XMLstring.indexOf("<w:tbl>", tableStartIndex + 1);
        }
        if(tableStartIndex == -1){
            return "";
        }
        int tableEndIndex = XMLstring.indexOf("</w:tbl>", tableStartIndex + 1);

        String table = XMLstring.substring(tableStartIndex, tableEndIndex + 1);

        //<w:tblCaption w:val="ID:1"/>
        int CaptionIndex = table.indexOf("w:tblCaption");
        if (CaptionIndex == -1) {
            return "";

        }
        String caption = table.substring(CaptionIndex - 1, table.indexOf("/>", CaptionIndex + 1) + 2);

        if (caption.contains("ID:")) {
            return caption.substring(caption.indexOf("ID:") + 3, caption.indexOf("\"/>"));
        } else {
            return "";
        }

    }
    
    private List<String> getTableColumnListID(int tcCount){
        
        ArrayList<String> list = new ArrayList<String>();
        int startIndex = XMLstring.indexOf("<w:tc>");
        int tableEndIndex;
        int index;
        
        while (--tcCount > 0 && startIndex != -1) {
            startIndex = XMLstring.indexOf("<w:tc>", startIndex + 1);
        }
        
        if(startIndex != -1){
            tableEndIndex = XMLstring.indexOf("</w:tc>", startIndex + 1);
            String tc = XMLstring.substring(startIndex, tableEndIndex + 7);
            
            index = tc.indexOf("w:rsidR=");
            while (index != -1) {
                list.add(tc.substring(index + 9, index + 17));
            
                index = tc.indexOf("w:rsidR=", index + 17);
            }
        }
        
        return list;
    }
    
    private String getTableColumnID(int tcCount) {

        int startIndex = XMLstring.indexOf("<w:tc>");
        while (--tcCount > 0 && startIndex != -1) {
            startIndex = XMLstring.indexOf("<w:tc>", startIndex + 1);
        }
        int tableEndIndex = XMLstring.indexOf("</w:tc>", startIndex + 1);

        String tc = XMLstring.substring(startIndex, tableEndIndex + 1);

        int index = tc.indexOf("w:rsidR");

        return tc.substring(index + 9, index + 17);
    }
//  public void initNode(IRNode node, String tagName, Attributes attrs){
//    node.setSlotValue(tagNameAttr, tagName);
//    IRSequence<Property> sq = VersionedSlotFactory.prototype.newSequence(-1);
//    boolean mouidFound = false;
//    for(int i=0; attrs!=null && i<attrs.getLength(); i++){
//      String name = attrs.getQName(i);
//      String val = attrs.getValue(i);
//      if (name.equals(SimpleXmlParser.IDNAME)){
//        mouid = Integer.parseInt(val);
//        node.setSlotValue(mouidAttr, mouid);
//       // nodeTable.put(mouid, node);
//        mouid++;
//        mouidFound = true;
//      } else {
//        sq.appendElement(new Property(name, val));
//
//      }
//    }
//    node.setSlotValue(nodeTypeAttr, "element");
//    node.setSlotValue(attrsSeqAttr, sq);
//    if (!mouidFound){
//      node.setSlotValue(mouidAttr, mouid++);
//    }
//    tree.initNode(node);
//  }

    private IRNode createElementNode(String tagName, Attributes attrs, boolean newNode, boolean styleOut, boolean styleOutTable, boolean wtcChield) {
        IRNode node = new PlainIRNode();
        initNode2(node, tagName, attrs, newNode, styleOut, styleOutTable, wtcChield);
        return node;
    }

//  private IRNode createTextNode(String text){
//    IRNode node = new PlainIRNode();
//    node.setSlotValue(tagNameAttr, "char");
//    //node.setSlotValue(nodeTypeAttr, "text");
//    node.setSlotValue(textAttr, text);
//    node.setSlotValue(mouidAttr, mouid++);
//    tree.initNode(node);
//    return node;
//  }
    private class NormalParsingHandler extends DefaultHandler2 {

        private Stack<IRNode> stack = new Stack<IRNode>();
        private IRNode root;

        public IRNode getRoot() {
            return root;
        }

        private Vector<String> prefixList = new Vector<String>();
        private Vector<String> uriList = new Vector<String>();

        @Override
        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            prefixList.add(prefix);
            uriList.add(uri);
        }

        @Override
        public void startElement(String nameSpaceURI, String simpleName,
                String qualifiedName, Attributes attrs) throws SAXException {

            boolean newNode = false;

            boolean WRchild = false;
            boolean WPchild = false;
            boolean WpPRchild = false;
            boolean WtbPRchild = false;
            boolean WTBchild = false;
            boolean WtbGridchild = false;
            boolean WTCchild = false;
            
//            if(qualifiedName.equals("w:tr")){
//                System.out.println("");
//            }
            
            if(qualifiedName.equals("w:bookmarkStart") || qualifiedName.equals("w:bookmarkEnd")) return;
            
            //Inform how many tables and table column have been seen in the XML
            if (qualifiedName.equals("w:tbl")) {
                tablesCount++;
            }
            if (qualifiedName.equals("w:tc")) {
                tableColumnCount++;
            }
            
            //
            //Identify Who is/are the parent(s) of current tag
            //
            if (!qualifiedName.equals(SimpleXmlParser.DOCUMENT_TAG) && !qualifiedName.equals(SimpleXmlParser.BODY_TAG) && !qualifiedName.equals(SimpleXmlParser.W_R)) {
                
                if(qualifiedName.equals("w:p")) WPchild=true;
                
                if (qualifiedName.equals(SimpleXmlParser.W_pPR)) WpPRchild = true; 
                
                if (qualifiedName.equals("w:tbl")) WTBchild = true;
                
                if (qualifiedName.equals("w:tblPr")) WtbPRchild = true;
                
                if(qualifiedName.equals("w:tblGrid")) WtbGridchild=true;

                Stack<IRNode> temp = (Stack<IRNode>) stack.clone();
                while (!temp.isEmpty()) {

                    if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_R)) {
                        WRchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_P)) {
                        WPchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_pPR)) {
                        WpPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tbl")) {
                        WTBchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblPr")) {
                        WtbPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblGrid")) {
                        WtbGridchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tc")) {
                        WTCchild = true;
                        break;
                    }
                    temp.pop();
                }
                

                if (!WRchild && !WPchild && !WpPRchild && !WtbPRchild && !WTBchild && !WtbGridchild && !WTCchild) {
                    return;
                }
            }
            
            boolean styleOut = (WPchild && WpPRchild && !WRchild);
            boolean styleOutTable = WtbPRchild || WtbGridchild;

            IRNode node = createElementNode(qualifiedName, attrs, newNode, styleOut, styleOutTable, WTCchild);
            
            nodeTable.put(node.getSlotValue(SimpleXmlParser.mouidAttr), node);
            
            //Tags inside tblGrid can repeat themself. To keep track, tblGrid has it own Hashtable
            if(WtbGridchild && !qualifiedName.equals("w:tblGrid")){
                tblGrid_tags.put(node.getSlotValue(SimpleXmlParser.mouidAttr), node);
                
                tableGridList.add(new TableGrid(tableid, node.getSlotValue(SimpleXmlParser.mouidAttr), node));
            }
            
            
            System.out.println(qualifiedName+" - "+node.getSlotValue(SimpleXmlParser.mouidAttr)+" => "+node.toString());
            
            //handling namespaces
            if (!prefixList.isEmpty()) {
                for (int i = 0; i < prefixList.size(); i++) {
                    String prefix = prefixList.get(i);
                    prefix = prefix.equals("") ? "xmlns" : "xmlns:" + prefix;
                    String uri = uriList.get(i);
                    AttributeList seq
                            = node.getSlotValue(SimpleXmlParser.attrListAttr);
                    seq.addAttribute(new Attribute(prefix, uri));
                }
                prefixList.clear();
                uriList.clear();
            }

            /*8888888888888888888888888888*/
            if (!stack.isEmpty()) {
                tree.addChild(stack.peek(), node);
            } else {
                root = node;
            }

            stack.push(node);

            /*8888888888888888888888888888*/
            
            tagListT0.add(new Tag(qualifiedName, node.getSlotValue(SimpleXmlParser.mouidAttr)));
        }

        @Override
        public void endElement(String namespaceURI, String simpleName,
                String qualifiedName) throws SAXException {
//      if (inTextMode){
//        String txt = stringBuffer.toString();
//        stringBuffer = null;
//        inTextMode = false;
//        if (!txt.trim().equals("")){
//          IRNode n = createTextNode(txt);
//          tree.addChild(stack.peek(), n);
//        }
//      }
            boolean newNode = false;

            boolean WRchild = false;
            boolean WPchild = false;
            boolean WpPRchild = false;
            boolean WtbPRchild = false;
            boolean WTBchild = false;
            boolean WtbGridchild = false;
            boolean WTCchild = false;
            
            if(qualifiedName.equals("w:bookmarkStart") || qualifiedName.equals("w:bookmarkEnd")) return;

            //
            //Identify Who is/are the parent(s) of current tag
            //
            if (!qualifiedName.equals(SimpleXmlParser.DOCUMENT_TAG) && !qualifiedName.equals(SimpleXmlParser.BODY_TAG) && !qualifiedName.equals(SimpleXmlParser.W_R)) {
                
                if(qualifiedName.equals("w:p")) WPchild=true;
                
                if (qualifiedName.equals(SimpleXmlParser.W_pPR)) WpPRchild = true; 
                
                if (qualifiedName.equals("w:tbl")) WTBchild = true;
                
                if (qualifiedName.equals("w:tblPr")) WtbPRchild = true;
                
                if(qualifiedName.equals("w:tblGrid")) WtbGridchild=true;

                Stack<IRNode> temp = (Stack<IRNode>) stack.clone();
                while (!temp.isEmpty()) {

                    if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_R)) {
                        WRchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_P)) {
                        WPchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_pPR)) {
                        WpPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tbl")) {
                        WTBchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblPr")) {
                        WtbPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblGrid")) {
                        WtbGridchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tc")) {
                        WTCchild = true;
                        break;
                    }
                    temp.pop();
                }
                

                if (!WRchild && !WPchild && !WpPRchild && !WtbPRchild && !WTBchild && !WtbGridchild && !WTCchild) {
                    return;
                }
            }

            stack.pop();
        }

        private boolean inTextMode = false;
        private StringBuilder stringBuffer = null;

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
//            if (!inTextMode) {
//                stringBuffer = new StringBuilder();
//            }

            if (stack.peek().getSlotValue(tagNameAttr).equals("w:t") || stack.peek().getSlotValue(tagNameAttr).equals("w:instrText")) {
                long longId = stack.peek().getSlotValue(SimpleXmlParser.mouidAttr);

                String s = new String(ch, start, length);

                if (idTextTable.containsKey(longId)) {

                    String t = idTextTable.get(longId);
                    if (!t.equals(s) && !t.contains(s)) {
                        t = t + "" + new String(ch, start, length);
                        idTextTable.put(longId, t);
                    }
                    return;
                } else {
                    idTextTable.put(longId, s);
                }
            }
//            stringBuffer.append(ch, start, length);
//            inTextMode = true;
        }

    }

    private class SyncParsingHandler extends DefaultHandler2 {

        private IRNode root;
        private Stack<IRNode> stack = new Stack<IRNode>();
        private Stack<List<IRNode>> oldChildrenStack = new Stack<List<IRNode>>();
        private Stack<List<IRNode>> newChildrenStack = new Stack<List<IRNode>>();
        // private Hashtable<Integer, IRNode> nodes = new Hashtable<Integer, IRNode>();
        //private Hashtable2<String, IRNode, IRNode> txtNodes =
        //        new Hashtable2<String, IRNode, IRNode>();

        private Vector<String> prefixList = new Vector<String>();
        private Vector<String> uriList = new Vector<String>();

        public SyncParsingHandler(IRNode root) {
            this.root = root;
            //buildTable(root);
        }

//    @Override
//    public void startPrefixMapping(String prefix, String uri)
//            throws SAXException {
//      prefixList.add(prefix);
//      uriList.add(uri);
//    }
        @Override
        public void startElement(String nameSpaceURI, String simpleName,
                String qualifiedName, Attributes attrs) throws SAXException {
            if(qualifiedName.equals("w:tr")){
                System.out.println("");
            }
//            System.out.println(qualifiedName);
            IRNode node = null;
            List<IRNode> oldChildren = null;
            boolean newNode = false;


            boolean WRchild = false;
            boolean WPchild = false;
            boolean WpPRchild = false;
            boolean WtbPRchild = false;
            boolean WTBchild = false;
            boolean WtbGridchild = false;
            boolean WTCchild = false;
            
            
            if(qualifiedName.equals("w:bookmarkStart") || qualifiedName.equals("w:bookmarkEnd")) return;
            
            //Inform how many tables and table column have been seen in the XML
            if (qualifiedName.equals("w:tbl")) {
                tablesCount++;
            }
            if (qualifiedName.equals("w:tc")) {
                tableColumnCount++;
            }

            if (!qualifiedName.equals(SimpleXmlParser.DOCUMENT_TAG) && !qualifiedName.equals(SimpleXmlParser.BODY_TAG) && !qualifiedName.equals(SimpleXmlParser.W_R)) {
                
                if(qualifiedName.equals("w:p")) WPchild=true;
                
                if (qualifiedName.equals(SimpleXmlParser.W_pPR)) {
                    WpPRchild = true;
                }
                if (qualifiedName.equals("w:tbl")) {
                    WTBchild = true;
                }
                if (qualifiedName.equals("w:tblPr")) {
                    WtbPRchild = true;
                }
                
                if(qualifiedName.equals("w:tblGrid")) WtbGridchild=true;

                Stack<IRNode> temp = (Stack<IRNode>) stack.clone();
                while (!temp.isEmpty()) {

                    if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_R)) {
                        WRchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_P)) {
                        WPchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_pPR)) {
                        WpPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tbl")) {
                        WTBchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblPr")) {
                        WtbPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblGrid")) {
                        WtbGridchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tc")) {
                        WTCchild = true;
                        break;
                    }
                    temp.pop();
                }
                
                //If is not any of the valid tag, return
                if (!WRchild && !WPchild && !WpPRchild && !WtbPRchild && !WTBchild && !WtbGridchild && !WTCchild) {
                    return;
                }
            }
            
            boolean styleOut = (WPchild && WpPRchild && !WRchild);
            boolean styleOutTable = WtbPRchild || WtbGridchild ;

            int int_rsidR, int_rsidRPr, int_tableID;
            long longid = -1;
            
            if(qualifiedName.equals("w:tr")){
                System.out.print("");
            }
            
            
            if (qualifiedName.equals(SimpleXmlParser.DOCUMENT_TAG)) {
                node = nodeTable.get((long) 1);
            } else if (qualifiedName.equals(SimpleXmlParser.BODY_TAG)) {
                node = nodeTable.get((long) 2);
            } else if (qualifiedName.equals("w:p") && attrs.getValue(IDNAME) != null) {
                
                rsidR = attrs.getValue(IDNAME);
                int_rsidR = (int) Long.parseLong(rsidR, 16);
                longid = (((long) int_rsidR) << 32) | (0 & 0xFFFFFFFFL);
                
            } else if (qualifiedName.equals("w:r") ) {
                
                if(attrs.getValue(RSIDRPR) != null){
                    rsidRPr = attrs.getValue(RSIDRPR);
                    int_rsidR = (int) Long.parseLong(rsidR, 16);
                    int_rsidRPr = (int) Long.parseLong(rsidRPr, 16);
                    longid = (((long) int_rsidR) << 32) | (int_rsidRPr & 0xFFFFFFFFL);
                }
                
            } else if (qualifiedName.equals("w:tbl")) {
                tableID = getTableID(tablesCount);
                if (!tableID.equals("")) {
                    int_tableID = (int) Long.parseLong(tableID, 16);
                    longid = (((long) qualifiedName.hashCode()) << 32) | (int_tableID & 0xFFFFFFFFL);
                }
            }else if(qualifiedName.equals("w:tr")){
                if (!tableID.equals("")) {
                    int_tableID = (int) Long.parseLong(tableID, 16);
                    rsidR = attrs.getValue(IDNAME);
                    int_rsidR = (int) Long.parseLong(rsidR, 16);
                    longid = (((long) int_tableID) << 32) | (int_rsidR & 0xFFFFFFFFL);
                    if(nodeTable.get(longid) != null){
                        
                    }else if(idTRTable.containsKey(int_rsidR)){
                        longid = -1;
                    }else{
                        idTRTable.put(int_rsidR,qualifiedName);
                    }
                }
                
            }else if (qualifiedName.equals("w:tc")) {
                
                List<String> list = getTableColumnListID(tableColumnCount);
                
                for(String s : list){
                    
                    int_rsidR = (int) Long.parseLong(s, 16);
                    longid = (((long) qualifiedName.hashCode()) << 32) | (int_rsidR & 0xFFFFFFFFL);
                    
                    if(!(nodeTable.containsKey(longid) && nodeTable.containsKey((long) int_rsidR << 32 | 0 & 0xFFFFFFFFL))){
                        longid=-1;
                    }else{
                        rsidR=s;
                        break;
                    }
                }

            } 
            else {
                if (styleOut || WTCchild) {
                    
                    if(!rsidR.equals("")){
                        int_rsidR = (int) Long.parseLong(rsidR, 16);
                        longid = (((long) int_rsidR) << 32) | (qualifiedName.hashCode() & 0xFFFFFFFFL);
                    }
                } else if (styleOutTable) {
                    if(!tableID.equals("")){
                        int_tableID = (int) Long.parseLong(tableID, 16);
                        longid = (((long) int_tableID) << 32) | (qualifiedName.hashCode() & 0xFFFFFFFFL);
                    }
                } else {
                    //System.out.println(qualifiedName);
                    int_rsidRPr = (int) Long.parseLong(rsidRPr, 16);
                    longid = (((long) int_rsidRPr) << 32) | (qualifiedName.hashCode() & 0xFFFFFFFFL);
                }
            }

            if (longid != -1) {//                
                node = nodeTable.get(longid);
            }

            if (styleOut || styleOutTable) {
                //Style child of paragraph
                
                //compare nodes
                IRNode node2 = createElementNode(qualifiedName, attrs, true, styleOut, styleOutTable, WTCchild);
                
                if(WtbGridchild && !qualifiedName.equals("w:tblGrid")){
                    Enumeration<IRNode> nodeList = tblGrid_tags_copy.elements();
                    IRNode n;
                    Long aa = node2.getSlotValue(SimpleXmlParser.mouidAttr);
                    Long bb;
                    while(nodeList.hasMoreElements()){
                        n = nodeList.nextElement();
                        bb = n.getSlotValue(SimpleXmlParser.mouidAttr);
                        if (n != null && compareNodes(n, node2)) {
                            node = n;
                            tblGrid_tags_copy.remove(n.getSlotValue(SimpleXmlParser.mouidAttr));
                            break;
                        }
                    }
                    if(nodeList.hasMoreElements() && node == null){
                        node = node2;
                        newNode = true;
                    }
                }
                if (node == null || !compareNodes(node, node2)) {
                    tree.initNode(node2);
                    node = node2;
                    newNode = true;
                }
            }

            if (node == null) {
                newNode = true;
                node = createElementNode(qualifiedName, attrs, newNode, false, false, WTCchild);

                //          throw new SAXException("Can't find node");
            } else if (!newNode) {

                if (!node.getSlotValue(tagNameAttr).equals(qualifiedName)) {
                    node.setSlotValue(tagNameAttr, qualifiedName);
                }

                updateAttrs2(node, attrs);

                int numChildren = tree.numChildren(node);
                oldChildren = new Vector<IRNode>(numChildren);
                for (int i = 0; i < numChildren; i++) {
                    oldChildren.add(tree.getChild(node, i));
                }
                
            }
//            int numChildren = tree.numChildren(node);
//            oldChildren = new Vector<IRNode>(numChildren);
//            for (int i = 0; i < numChildren; i++) {
//                oldChildren.add(tree.getChild(node, i));
//            
//            }   
            
            oldChildrenStack.push(oldChildren);
            if (!newChildrenStack.isEmpty()) {
                newChildrenStack.peek().add(node);
            }
            newChildrenStack.push(new Vector<IRNode>());
            stack.push(node);
            
            System.out.println(qualifiedName+" - "+node.getSlotValue(SimpleXmlParser.mouidAttr)+" => "+node.toString());
            
            if(isT1){
                tagListT1.add(new Tag(qualifiedName,node.getSlotValue(SimpleXmlParser.mouidAttr) ));
            }else{
                tagListT2.add(new Tag(qualifiedName,node.getSlotValue(SimpleXmlParser.mouidAttr) ));
            }
        }

        @Override
        public void endElement(String namespaceURI, String simpleName,
                String qualifiedName) throws SAXException {
            
            System.out.println(qualifiedName + " - out");
            if(qualifiedName.equals("w:t")){
                System.out.println("");
            }
            boolean WRchild = false;
            boolean WPchild = false;
            boolean WpPRchild = false;
            boolean WtbPRchild = false;
            boolean WTBchild = false;
            boolean WtbGridchild = false;
            boolean WTCchild = false;
            
            if(qualifiedName.equals("w:bookmarkStart") || qualifiedName.equals("w:bookmarkEnd")) return;
            
            if (!qualifiedName.equals(SimpleXmlParser.DOCUMENT_TAG) && !qualifiedName.equals(SimpleXmlParser.BODY_TAG) && !qualifiedName.equals(SimpleXmlParser.W_R)) {

                if (qualifiedName.equals(SimpleXmlParser.W_pPR)) {
                    WpPRchild = true;
                }
                if (qualifiedName.equals("w:tbl")) {
                    WTBchild = true;
                }
                if (qualifiedName.equals("w:tblPr")) {
                    WtbPRchild = true;
                }
                if(qualifiedName.equals("w:p")) WPchild=true;
                
                if(qualifiedName.equals("w:tblGrid")) WtbGridchild=true;

                Stack<IRNode> temp = (Stack<IRNode>) stack.clone();
                while (!temp.isEmpty()) {

                    if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_R)) {
                        WRchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_P)) {
                        WPchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals(SimpleXmlParser.W_pPR)) {
                        WpPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tbl")) {
                        WTBchild = true;
                        break;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblPr")) {
                        WtbPRchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tblGrid")) {
                        WtbGridchild = true;
                    } else if (temp.peek().getSlotValue(tagNameAttr).equals("w:tc")) {
                        WTCchild = true;
                        break;
                    }
                    temp.pop();
                }

                if (!WRchild && !WPchild && !WpPRchild && !WtbPRchild && !WTBchild && !WtbGridchild && !WTCchild) {
                    return;
                }
            }

            final IRNode n = stack.pop();
//      if (inTextMode){
//        String txt = stringBuffer.toString();
//        stringBuffer = null;
//        inTextMode = false;
//        if (!txt.trim().equals("")){
//          IRNode tn = txtNodes.get(txt, n);
//          if (tn == null){
//            tn = createTextNode(txt);
//          }
//          newChildrenStack.peek().add(tn);
//        }
//      }

            final List<IRNode> oldChildren = oldChildrenStack.pop();
            final List<IRNode> newChildren = newChildrenStack.pop();

            if (oldChildren != null && oldChildren.equals(newChildren)) {
                return;
            }
            if (oldChildren != null) {
                tree.removeChildren(n);
            }

            for (IRNode c : newChildren) {
                IRNode t = nodeTable.get((long) c.getSlotValue(SimpleXmlParser.mouidAttr));
                if (t != null) {
                    IRNode p = tree.getParent(c);
                    if (p != null) {
                        tree.removeChild(p, c);
                    }
                }
                System.out.println();
                tree.addChild(n, c);
            }

        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {

            if (stack.peek().getSlotValue(tagNameAttr).equals("w:t") || stack.peek().getSlotValue(tagNameAttr).equals("w:instrText")) {
                long longId = stack.peek().getSlotValue(SimpleXmlParser.mouidAttr);
                if (idTextTable.containsKey(longId)) {

                    String t = idTextTable.get(longId);
                    if (!t.equals(new String(ch, start, length)) && !t.contains(new String(ch, start, length))) {
                        t = t + "" + new String(ch, start, length);
                        idTextTable.put(longId, t);
                    }
                    return;
                } else {
                    idTextTable.put(longId, new String(ch, start, length));
                }
            }

        }

        private boolean compareNodes(IRNode node, IRNode node2) {

            AttributeList attr1 = node.getSlotValue(SimpleXmlParser.attrListAttr);
            AttributeList attr2 = node2.getSlotValue(SimpleXmlParser.attrListAttr);
            String name1 = node.getSlotValue(tagNameAttr);
            String name2 = node2.getSlotValue(tagNameAttr);
            return attr1.equals(attr2) && name1.equals(name2);
        }
//    private void buildTable(IRNode n) {
//      String nodeType = n.getSlotValue(SimpleXmlParser.nodeTypeAttr);
//      if (nodeType.equals("text")){
//       // txtNodes.put(n.getSlotValue(SimpleXmlParser.textAttr),
//        //        tree.getParent(n), n);
//      } else {
////        nodes.put(n.hashCode(), n);
//        nodes.put(n.getIntSlotValue(SimpleXmlParser.mouidAttr), n);
//        for (int i = 0; i < tree.numChildren(n); i++) {
//          IRNode c = tree.getChild(n, i);
//          nodes.put(c.hashCode(), c);
//          buildTable(c);
//        }
//      }
//    }
//    private boolean inTextMode = false;
//    private StringBuilder stringBuffer = null;
//    @Override
//    public void characters(char[] ch, int start, int length)
//            throws SAXException {
//      if (!inTextMode){
//        stringBuffer = new StringBuilder();
//      }
//      stringBuffer.append(ch, start, length);
//      inTextMode = true;
//    }

        private void updateAttrs2(IRNode n, Attributes attrs) {
            AttributeList old = n.getSlotValue(SimpleXmlParser.attrListAttr);
            AttributeList newList = new AttributeList(attrs.getLength());
            for (int i = 0; i < attrs.getLength(); i++) {
                if (!attrs.getQName(i).equals(SimpleXmlParser.IDNAME)) {
                    newList.addAttribute(new Attribute(attrs.getQName(i), attrs.getValue(i)));
                }
            }
            if (old.equals(newList)) {
                return;
            }
            n.setSlotValue(SimpleXmlParser.attrListAttr, newList);
            //propagateChange(n);
        }

        
        
        
//
// private void updateAttrs(IRNode n, Attributes attrs){
//      IRSequence<Property> seq = n.getSlotValue(SimpleXmlParser.attrsSeqAttr);
//      boolean changed = false;
//      boolean loop= true;
//      for(IRLocation l = seq.lastLocation(); loop && seq.size() > 0;
//                     l = seq.prevLocation(l)){
//        boolean found = false;
//        Property p = seq.elementAt(l);
//        for(int i=0; i<attrs.getLength(); i++){
//          String name = attrs.getQName(i);
//          if (name.equals(IDNAME)) { continue; }
//
//          if (name.equals(p.getName())){
//            found = true;
//            if (!attrs.getValue(i).equals(p.getValue())){
//              Property p2 = new Property( name, attrs.getValue(i));
//              seq.insertElementBefore(p2, l);
//              seq.removeElementAt(l);
//              changed = true;
//            }
//            break;
//          }
//        }
//
//        if (l == seq.firstLocation()){ loop = false; }
//
//        if (!found){
//          seq.removeElementAt(l);
//          changed = true;
//        }
//      }
//
//      for(int i=0; i<attrs.getLength(); i++){
//        String name =attrs.getQName(i);
//        boolean found = false;
//        for(int j=0; j<seq.size(); j++){
//          Property p = seq.elementAt(j);
//          if (name.equals(p.getName())){
//            found = true;
//            break;
//          }
//        }
//        if (!found && !name.equals(IDNAME)){
//          Property p = new Property( name, attrs.getValue(i));
//          seq.appendElement(p);
//          changed = true;
//        }
//      }
//
//      if (changed) n.setSlotValue(SimpleXmlParser.attrsSeqAttr, seq);
//
//    }
    }

}
