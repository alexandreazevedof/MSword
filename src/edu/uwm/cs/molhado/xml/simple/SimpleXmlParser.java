package edu.uwm.cs.molhado.xml.simple;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.ConstantSlotFactory;
import edu.cmu.cs.fluid.ir.IRIntegerType;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parse XML files or synchonize files with the XML in memory using node ID
 *
 * @author chengt
 */
public class SimpleXmlParser {

  public final static String TREE    = "axml.tree";
  public final static String NODETYPE= "axml.type";
  public final static String MOUID   = "axml.mouid";
  public final static String TAGNAME = "axml.name";
  public final static String TEXT    = "axml.text";
  public final static String ATTRS   = "axml.attrs";
  public final static String CHANGE  = "axml.change";
  public final static String CHILDREN_CHANGE  = "axml.children_change";
  public final static String BUNDLE  = "bundle";
  //public final static String IDNAME  = "mouid";
  public final static String IDNAME = "rsidR";
  
  public final static Tree tree;
  public final static SlotInfo<String> nodeTypeAttr;
  public final static SlotInfo<Integer> mouidAttr;
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
    SlotInfo<Integer> mouidAttrX = null;
    SlotInfo<String> tagNameX = null;
    SlotInfo<String> textAttrX = null;
    SlotInfo<IRSequence<Property>> attrsX = null;
    SlotInfo<AttributeList> attrListAttrX= null;
    VersionedChangeRecord changeRecordX = null;
//    VersionedChangeRecord childrenChangeX = null;
    Tree treeX = null;
    try {
      treeX = new Tree(TREE, vsf);
      nodeTypeAttrX =csf.newAttribute(NODETYPE, IRStringType.prototype);
//      mouidAttrX = csf.newAttribute(MOUID, IRIntegerType.prototype);
      mouidAttrX = vsf.newAttribute(MOUID, IRIntegerType.prototype);
      tagNameX = vsf.newAttribute(TAGNAME, IRStringType.prototype);
      textAttrX = vsf.newAttribute(TEXT, IRStringType.prototype);
 //     attrsX = vsf.newAttribute(ATTRS,
//              new IRSequenceType(IRPropertyType.prototype));
      attrListAttrX= vsf.newAttribute("xml.attrs.list", new IRObjectType<AttributeList>());
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
  private static Hashtable<Integer, IRNode> nodeTable = new Hashtable<Integer,IRNode>();
  private int mouid = 0;

  public SimpleXmlParser(int nodeCount){
    this.mouid = nodeCount;
  }

  public void setMouid(int id){
    mouid = id;
  }

  private XMLReader xmlReader = null;

  private  String parserName = "org.apache.xerces.parsers.SAXParser";
  private XMLReader getReader() throws SAXException {
    if (xmlReader == null){
    xmlReader = XMLReaderFactory.createXMLReader(parserName);
    xmlReader.setFeature("http://xml.org/sax/features/namespaces",false);
    xmlReader.setFeature("http://xml.org/sax/features/validation",false);
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


    long startMem = Runtime.getRuntime().totalMemory() -
            Runtime.getRuntime().freeMemory();
    long t0 = System.currentTimeMillis();
    
    IRNode root = p.parse(new File("/home/alex/NetBeansProjects/momerge/dist/t0.xml"));
    Version v0 = Version.getVersion();
    tagNameAttr.addDefineObserver(changeRecord);
//    attrsSeqAttr.addDefineObserver(changeRecord);
    attrListAttr.addDefineObserver(changeRecord);
    tree.addObserver(changeRecord);
//    tree.addObserver(childrenChange);
    PropagateUpTree.attach(changeRecord, tree);
    Version.saveVersion(v0);
    
    long t1 = System.currentTimeMillis();
    p.parse(root, new File("/home/alex/NetBeansProjects/momerge/dist/t1.xml"));
    Version v1 = Version.getVersion();
   long t2 = System.currentTimeMillis();
    Version.saveVersion(v0);
    
    p.parse( root, new File("/home/alex/NetBeansProjects/momerge/dist/t2.xml"));
    long t3 = System.currentTimeMillis();
    //nodeTable = null;
    Version v2 = Version.getVersion();
    Version.saveVersion(v0);
 
    IRTreeMerge merge = new IRTreeMerge(SimpleXmlParser.tree, SimpleXmlParser.changeRecord,
            root, new VersionMarker(v1), new VersionMarker(v0), new VersionMarker(v2) );

    Version v3 = merge.merge();
    long t4 = System.currentTimeMillis();
    Version.saveVersion(v3);
    SimpleXmlParser.writeToFile("/home/alex/NetBeansProjects/momerge/dist/out.xml",root);
    long t5 = System.currentTimeMillis();

    long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    System.out.println("parse: " + (t3-t0));
    //System.out.println("second: " + (t2-t1));
    //System.out.println("third: " + (t3-t2));
    System.out.println("merge: " + (t4-t3));
    System.out.println("write: " + (t5-t4));
    System.out.println("total: " + (t5-t0));



    System.out.println("memory: " + (endMem-startMem));
    //System.out.println(nodeTable.size());
  }

  private static String indent( int n) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) { sb.append(" "); }
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
 private static String dumpAttrs2(int indent, IRNode n) throws IOException{
   StringBuilder sb = new StringBuilder();
    AttributeList sq = n.getSlotValue(SimpleXmlParser.attrListAttr);
    sb.append(" ");
    for(int i=0; i<sq.size(); i++){
      Attribute p = sq.get(i);
     // w.println();
     // indent(w,indent + 2);
      sb.append(p.getName());
      sb.append("=\"");
      sb.append(p.getValue());
      sb.append("\" ");
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
  private static void dumpContent(Writer w, int indent, IRNode n, boolean withID) throws IOException {
    String tagName = n.getSlotValue(tagNameAttr);
   // indent(w,indent);
    w.write("<" );
    w.write(tagName);
    if (withID) w.write(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser.mouidAttr) + "\"");
    w.write(dumpAttrs2(indent, n));

    int numChildren = tree.numChildren(n);
    if (numChildren >0){
      w.write(">\n");
      for (int i = 0; i < numChildren; i++) {
        IRNode c = tree.getChild(n,i);
        dumpContent(w, indent + 2, c, withID);
      }
      w.write(indent(indent));
      w.write("</");
      w.write(tagName);
      w.write(">");
   //   w.write("</"+tagName+">");
    } else {
      w.write(" />\n");
    }
  }

  public static void writeToFile(File file, IRNode n) throws IOException{
    FileWriter w = new FileWriter(file);
    w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    dumpContent(w, 0, n, true);
    w.close();
  }

  public static void writeToFile(String file, IRNode n) throws IOException{
		writeToFile(new File(file), n);
  }

  public static String toString(IRNode n) {
    StringWriter writer = new StringWriter();
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//    try { dumpContent(writer, 0, n, true);
//    } catch (IOException ex) { Exceptions.printStackTrace(ex); }
    return writer.toString();
  }

  public static String toStringWithID(IRNode n) {
    StringWriter w= new StringWriter();
    //w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    //try { dumpContent(writer, 0, n, true);
    //} catch (IOException ex) { Exceptions.printStackTrace(ex); }
    //w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		try {
			dumpContent(w, 0, n, true);
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
  public IRNode parse(File file) throws Exception { return parse(new FileInputStream(file)); }
  public IRNode parse(Reader reader) throws Exception { return parse(new InputSource(reader)); }
  public IRNode parse(InputStream is) throws Exception { return parse(new InputSource(is)); }
  public IRNode parse(String str) throws Exception { return parse(new StringReader(str)); }
  public IRNode parse(IRNode root, File file) throws Exception { return parse(root, new FileInputStream(file)); }
  public IRNode parse(IRNode root, Reader reader) throws Exception { return parse(root, new InputSource(reader)); }
  public IRNode parse(IRNode root, InputStream is) throws Exception { return parse(root, new InputSource(is)); }
  public IRNode parse(IRNode root, String str) throws Exception { return parse(root, new StringReader(str)); }

  private IRNode parse(InputSource is) throws Exception {
    XMLReader parser = getReader();
    NormalParsingHandler handler = new NormalParsingHandler();
    parser.setContentHandler(handler);
    parser.parse(is);
    return handler.getRoot();
  }
  private IRNode parse(IRNode root, InputSource is) throws Exception {
    XMLReader parser = getReader();
    parser.setContentHandler(new SyncParsingHandler(root));
    parser.parse(is);
    return root;
  }

  /**
   * This method looks odd but the idea is not to duplicate code for creating
   * an XML node among different classes.  A class that needs to create an XML
   * node within a version tracker needs to :
   * <code>
   * final static IRNode newNode = new PlainIRNode();
   * tracker.executeIn(new Runnable() {
   *     public void run() {
   *       SimpleXmlParser.initNode(newNode, "noname", null);
   *     tree.addChild(node, newNode);
   *   }
   * })
   * </code>
   * @param node
   * @param tagName
   * @param attrs
   */
public void initNode2(IRNode node, String tagName, Attributes attrs, boolean newNode){
    node.setSlotValue(tagNameAttr, tagName);
    AttributeList attrList = new AttributeList(4);
//    boolean mouidFound = false;
    for(int i=0; attrs!=null && i<attrs.getLength(); i++){
      String name = attrs.getQName(i);
      String val = attrs.getValue(i);
      //if (name.equals("rsid")){
      if (name.equals(SimpleXmlParser.IDNAME)){
        mouid = Integer.parseInt(val);
        if (newNode){
            node.setSlotValue(mouidAttr, mouid);
        }else{
             node.setSlotValue(mouidAttr, mouid);
            nodeTable.put(mouid, node);
        }
//        mouid++;
//        mouidFound = true;
        
      } else {
        attrList.addAttribute(new Attribute(name, val));
      }
    }
    node.setSlotValue(attrListAttr, attrList);
    
    tree.initNode(node);
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

  private IRNode createElementNode(String tagName, Attributes attrs, boolean newNode) {
    IRNode node = new PlainIRNode();
    initNode2(node, tagName, attrs, newNode);
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
    public IRNode getRoot() { return root; }

    private Vector<String> prefixList= new Vector<String>();
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

//      if (inTextMode){
//        String txt = stringBuffer.toString();
//        stringBuffer = null;
//        if (!txt.trim().equals("")){
//          IRNode n = createTextNode(txt);
//          tree.addChild(stack.peek(), n);
//        }
//      }
//      inTextMode = false;
     boolean newNode = false;
     IRNode node = createElementNode(qualifiedName, attrs, newNode);
     nodeTable.put(node.getSlotValue(SimpleXmlParser.mouidAttr), node);

      //handling namespaces
      if (!prefixList.isEmpty()){
        for(int i=0; i<prefixList.size(); i++){
          String prefix = prefixList.get(i);
          prefix = prefix.equals("") ? "xmlns" : "xmlns:"+prefix;
          String uri = uriList.get(i);
          AttributeList seq =
                  node.getSlotValue(SimpleXmlParser.attrListAttr);
          seq.addAttribute(new Attribute(prefix, uri));
        }
        prefixList.clear();
        uriList.clear();
      }

      /*8888888888888888888888888888*/

      if (!stack.isEmpty()) { tree.addChild(stack.peek(), node);
      } else { root = node; }
      stack.push(node);

      /*8888888888888888888888888888*/
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
      stack.pop();
    }

    private boolean inTextMode = false;
    private StringBuilder stringBuffer = null;
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
//      if (!inTextMode){
//        stringBuffer = new StringBuilder();
//      }
//      stringBuffer.append(ch, start, length);
//      inTextMode = true;
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

      String uid = attrs.getValue(IDNAME);
      IRNode node = null;
      List<IRNode> oldChildren = null;
      boolean newNode = false;
      
     // if (uid != null && node !=null) {
        if (uid != null){
            node = nodeTable.get(Integer.parseInt(uid));
        if (node == null) {
            newNode=true;
            node = createElementNode(qualifiedName, attrs, newNode);
//          throw new SAXException("Can't find node");
        }else{
        
            if (!node.getSlotValue(tagNameAttr).equals(qualifiedName)){
              node.setSlotValue(tagNameAttr, qualifiedName);
            }

            updateAttrs2(node, attrs);

            int numChildren = tree.numChildren(node);
            oldChildren = new Vector<IRNode>(numChildren);
            for (int i = 0; i < numChildren; i++) {
              oldChildren.add(tree.getChild(node, i));
            }
        }
      } 
//        else {
//        node = createElementNode(qualifiedName, attrs);
//      }

      //handling namespaces
//      if (!prefixList.isEmpty()){
//        for(int i=0; i<prefixList.size(); i++){
//          String prefix = prefixList.get(i);
//          prefix = prefix.equals("") ? "xmlns" : "xmlns:"+prefix;
//          String uri = uriList.get(i);
//          IRSequence<Property> seq =
//                  node.getSlotValue(SimpleXmlParser.attrsSeqAttr);
//          seq.appendElement(new Property(prefix, uri));
//        }
//        prefixList.clear();
//        uriList.clear();
//      }
//
//      if (inTextMode){
//        String txt = stringBuffer.toString();
//        inTextMode = false;
//        stringBuffer = null;
//        if (!txt.trim().equals("")){
//          IRNode n = txtNodes.get(txt, stack.peek());
//          if (n == null){
//            n = createTextNode(txt);
//          }
//          newChildrenStack.peek().add(n);
//        }
//      }

      oldChildrenStack.push(oldChildren);
      if (!newChildrenStack.isEmpty()) newChildrenStack.peek().add(node);
      newChildrenStack.push(new Vector<IRNode>());
      stack.push(node);

    }

    @Override
    public void endElement(String namespaceURI, String simpleName,
            String qualifiedName) throws SAXException {

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

      if (oldChildren != null && oldChildren.equals(newChildren)) { return; }
      if (oldChildren != null) { tree.removeChildren(n); }

      for (IRNode c : newChildren) {
        IRNode t = nodeTable.get(c.getSlotValue(SimpleXmlParser.mouidAttr));
        if (t!=null) {
          IRNode p = tree.getParent(c);
          if (p!=null) tree.removeChild(p, c);
        }
        tree.addChild(n, c);
      }

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
  private void updateAttrs2(IRNode n, Attributes attrs){
      AttributeList old = n.getSlotValue(SimpleXmlParser.attrListAttr);
      AttributeList newList = new AttributeList(attrs.getLength());
      for(int i=0; i<attrs.getLength(); i++){
        if (!attrs.getQName(i).equals(SimpleXmlParser.IDNAME))
        newList.addAttribute(new Attribute(attrs.getQName(i), attrs.getValue(i)));
      }
      if (old.equals(newList)) return;
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

