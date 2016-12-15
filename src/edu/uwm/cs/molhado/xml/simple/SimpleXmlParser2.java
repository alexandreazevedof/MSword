package edu.uwm.cs.molhado.xml.simple;

import edu.cmu.cs.fluid.ir.ConstantSlotFactory;
import edu.cmu.cs.fluid.ir.IRIntegerType;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRNodeType;
import edu.cmu.cs.fluid.ir.IRObjectType;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.IRSequenceType;
import edu.cmu.cs.fluid.ir.IRStringType;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SimpleSlotFactory;
import edu.cmu.cs.fluid.ir.SlotAlreadyRegisteredException;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

/*
 *
 * We want to compare performance when children are stored in an arraylist
 * instead of a versioned IRSequence. Performance didn't seem to improved that
 * much
 */

/**
 * Parse XML files or synchonize files with the XML in memory using node ID
 *
 * @author chengt
 */
public class SimpleXmlParser2 {// extends FluidRegistryLoading {

  public final static String PARENT = "bxml.tree.parent";
  public final static String CHILDREN = "bxml.tree.children";
  public final static String MOUID   = "bxml.mouid";
  public final static String TAGNAME = "bxml.name";
  public final static String ATTRS   = "bxml.attrs";
  public final static String CHANGE  = "bxml.change";
  public final static String IDNAME  = "bmouid";

  public final static SlotInfo<IRNode> parentAttr;
  public final static SlotInfo<List<IRNode>> childrenAttr;
  public final static SlotInfo<Integer> mouidAttr;
  public final static SlotInfo<String> tagNameAttr;
  public final static VersionedChangeRecord changeRecord;

  public final static SlotInfo<AttributeList> attrListAttr;
  public final static VersionedSlotFactory vsf = VersionedSlotFactory.prototype;
  public final static ConstantSlotFactory csf = ConstantSlotFactory.prototype;
  public final static SimpleSlotFactory ssf = SimpleSlotFactory.prototype;
  static {
    SlotInfo<Integer> mouidAttrX = null;
    SlotInfo<String> tagNameX = null;
    SlotInfo<IRNode> parentAttrX= null;
    SlotInfo<List<IRNode>> childrenAttrX = null;
    SlotInfo<IRSequence<Property>> attrsX = null;
    VersionedChangeRecord changeRecordX = null;
    SlotInfo<AttributeList> attrListAttrX= null;
    try {
      attrListAttrX= vsf.newAttribute("xml.attrs.list", new IRObjectType<AttributeList>());
      mouidAttrX = ssf.newAttribute(MOUID, IRIntegerType.prototype);
      parentAttrX = vsf.newAttribute(PARENT, IRNodeType.prototype);
      childrenAttrX = vsf.newAttribute(CHILDREN, new IRObjectType<List<IRNode>>());
      tagNameX = vsf.newAttribute(TAGNAME, IRStringType.prototype);
      attrsX = ssf.newAttribute(ATTRS, new IRSequenceType(IRPropertyType.prototype));
      changeRecordX = (VersionedChangeRecord) vsf.newChangeRecord(CHANGE);
    } catch (SlotAlreadyRegisteredException ex) {
      Exceptions.printStackTrace(ex);
    }
    mouidAttr = mouidAttrX;
    tagNameAttr = tagNameX;
    parentAttr = parentAttrX;
    childrenAttr = childrenAttrX;
    attrListAttr = attrListAttrX;
    changeRecord = changeRecordX;
  }

  private static Hashtable<Integer, IRNode> nodeTable = new Hashtable<Integer,IRNode>();

  private int mouid = 0;

  public SimpleXmlParser2(int nodeCount){
    this.mouid = nodeCount;
  }

  public void setMouid(int id){
    mouid = id;
  }

  private XMLReader xmlReader = null;

  private XMLReader getReader() throws SAXException {
    if (xmlReader == null){
    String parserName = "org.apache.xerces.parsers.SAXParser";
    xmlReader = XMLReaderFactory.createXMLReader(parserName);
    xmlReader.setFeature("http://xml.org/sax/features/namespaces",false);
    xmlReader.setFeature("http://xml.org/sax/features/validation",false);
    }
    return xmlReader;
  }

  public static void test1() throws Exception{

    String input1="<a mouid='1' x='0' y='10'><b mouid='2' z='10'/></a>";
    String input2="<a mouid='1' x='100' y='10'><b mouid='2' z='10'/><c/></a>";
    String input3="<a mouid='1' x='100' y='10'><b mouid='2' z='10'><d/></b></a>";
    SimpleXmlParser2 p = new SimpleXmlParser2(0);

    IRNode root = p.parse(input1);
    Version v0 = Version.getVersion();

    p.parse(root, input2);

    Version v1 = Version.getVersion();
    
    tagNameAttr.addDefineObserver(changeRecord);
    attrListAttr.addDefineObserver(changeRecord);
    childrenAttr.addDefineObserver(changeRecord);

    Version.saveVersion(v0);
    p.parse(root, input3);
    Version v2 = Version.getVersion();

    Version.restoreVersion();

    Version.saveVersion(v2);
    System.out.print(SimpleXmlParser2.toStringWithID(root));
    Version.restoreVersion();

  }

  public static void main(String[] args) throws XmlParseException, Exception {
    test2();
  }
  public static void test2() throws Exception{

    long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    SimpleXmlParser2 p = new SimpleXmlParser2(0);
    long t0 = System.currentTimeMillis();
    IRNode root = p.parse(new File("/home/chengt/test/10000.xml"));
    tagNameAttr.addDefineObserver(changeRecord);
    attrListAttr.addDefineObserver(changeRecord);
    //PropagateUpTree.attach(changeRecord, tree);
    long t1 = System.currentTimeMillis();
    p.parse(root, new File("/home/chengt/test/10000.xml"));
    long t2 = System.currentTimeMillis();
    p.parse(root, new File("/home/chengt/test/10000.xml"));
    long t3 = System.currentTimeMillis();
    System.out.println("first: " + (t1-t0));
    System.out.println("second: " + (t2-t1));
    System.out.println("third: " + (t3-t2));
    System.out.println("total: " + (t3-t0));

    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    System.out.println("============");
    System.out.println("Memory: " + (endMem-startMem));
  }

  private static String indent(int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) { sb.append(" "); }
    return sb.toString();
  }

  private static void dumpAttrs(Writer w, int indent, IRNode n) throws IOException {
    AttributeList list = n.getSlotValue(attrListAttr);
    for(int i=0; i<list.size(); i++){
      Attribute a = list.get(i);
      w.write("\n");
      w.write(indent(indent + 2) + a.getName());
      w.write("=\"");
      w.write(a.getValue());
      w.write("\" ");
    }
  }

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
    w.write(indent(indent) + "<");
    w.write(tagName);
    if (withID) w.write(" "+IDNAME+"=\"" + n.getSlotValue(SimpleXmlParser2.mouidAttr) + "\"");
    dumpAttrs(w, indent, n);
    List<IRNode> children = getChildren(n);
    if (children!=null && children.size()>0){
      w.write(">\n");
      for (int i = 0; i < children.size(); i++) {
        IRNode c = children.get(i);
        dumpContent(w, indent + 2, c, withID);
      }
      w.write( indent(indent) + "</");
      w.write(tagName);
      w.write(">\n");
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
    FileWriter w = new FileWriter(new File(file));
    w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    dumpContent(w, 0, n, true);
    w.close();
  }
  public static String toString(IRNode n) {
    StringWriter writer = new StringWriter();
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    try { dumpContent(writer, 0, n, false);
    } catch (IOException ex) { Exceptions.printStackTrace(ex); }
    return writer.toString();
  }

  public static String toStringWithID(IRNode n) {
    StringWriter writer = new StringWriter();
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    try { dumpContent(writer, 0, n, true);
    } catch (IOException ex) { Exceptions.printStackTrace(ex); }
    return writer.toString();
  }
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


  public void initNode2(IRNode node, String tagName, Attributes attrs){
    node.setSlotValue(tagNameAttr, tagName);
    AttributeList attrList = new AttributeList();
    boolean mouidFound = false;
    for(int i=0; attrs!=null && i<attrs.getLength(); i++){
      String name = attrs.getQName(i);
      String val = attrs.getValue(i);
      if (name.equals(SimpleXmlParser2.IDNAME)){
        mouid = Integer.parseInt(val);
        node.setSlotValue(mouidAttr, mouid);
        nodeTable.put(mouid, node);
        mouid++;
        mouidFound = true;
      } else {
        attrList.addAttribute(new Attribute(name, val));
      }
    }
    node.setSlotValue(attrListAttr, attrList);
    if (!mouidFound){
      node.setSlotValue(mouidAttr, mouid++);
    }
  }
  private IRNode createElementNode(String tagName, Attributes attrs) {
    IRNode node = new PlainIRNode();
    initNode2(node, tagName, attrs);
    return node;
  }


  public static void addChild(IRNode parent, IRNode child){
    List<IRNode> children = null;
    if (parent.valueExists(childrenAttr)){
      children = parent.getSlotValue(childrenAttr);
    } else {
      children = new ArrayList<IRNode>();
      parent.setSlotValue(childrenAttr, children);
    }
    propagateChange(child);
    child.setSlotValue(parentAttr, parent);
    children.add(child);
    //now notify that parent has changed!!
  }

  public static void removeChild(IRNode parent, IRNode child){
    List<IRNode> children = null;
    if (parent.valueExists(childrenAttr)){
      children = parent.getSlotValue(childrenAttr);
    } else {
      children = new ArrayList<IRNode>();
      parent.setSlotValue(childrenAttr, children);
    }
    propagateChange(child);
    child.setSlotValue(parentAttr, null);
    children.remove(child);
    //now notify that parent has changed.
  }

  public static void propagateChange(IRNode n){
    if (n.valueExists(parentAttr)){
      IRNode p = n.getSlotValue(parentAttr);
      if (p!=null){
        changeRecord.setChanged(p);
        propagateChange(p);
      }
    }
  }

  public static List<IRNode> getChildren(IRNode node){
    if (node.valueExists(childrenAttr)){
      return node.getSlotValue(childrenAttr);
    }
    return null;
  }

  private class NormalParsingHandler extends DefaultHandler2 {
    private Stack<IRNode> stack = new Stack<IRNode>();
    private IRNode root;
    public IRNode getRoot() { return root; }

    @Override
    public void startElement(String nameSpaceURI, String simpleName,
            String qualifiedName, Attributes attrs) throws SAXException {
     IRNode node = createElementNode(qualifiedName, attrs);
      if (!stack.isEmpty()) {
        addChild(stack.peek(),  node);
      } else {
        root = node;
      }
      stack.push(node);
    }

    @Override
    public void endElement(String namespaceURI, String simpleName,
            String qualifiedName) throws SAXException {
      stack.pop();
    }

  }

  private class SyncParsingHandler extends DefaultHandler2 {
    private IRNode root;
    private Stack<IRNode> stack = new Stack<IRNode>();
    private Stack<List<IRNode>> oldChildrenStack = new Stack<List<IRNode>>();
    private Stack<List<IRNode>> newChildrenStack = new Stack<List<IRNode>>();

    public SyncParsingHandler(IRNode root) {
      this.root = root;
    }


    @Override
    public void startElement(String nameSpaceURI, String simpleName,
            String qualifiedName, Attributes attrs) throws SAXException {

      String uid = attrs.getValue(IDNAME);
      IRNode node = null;
      List<IRNode> oldChildren = null;
      if (uid != null) {
        node = nodeTable.get(Integer.parseInt(uid));
        if (node == null) {
          throw new SAXException("Can't find node");
        }

        if (!node.getSlotValue(tagNameAttr).equals(qualifiedName)){
          node.setSlotValue(tagNameAttr, qualifiedName);
          propagateChange(node);
        }

        updateAttrs2(node, attrs);
        oldChildren = getChildren(node);
      } else {
        node = createElementNode(qualifiedName, attrs);
      }

      oldChildrenStack.push(oldChildren);
      if (!newChildrenStack.isEmpty()) newChildrenStack.peek().add(node);
      newChildrenStack.push(new ArrayList<IRNode>());
      stack.push(node);
    }

    @Override
    public void endElement(String namespaceURI, String simpleName,
            String qualifiedName) throws SAXException {

      final IRNode n = stack.pop();
      final List<IRNode> oldChildren = oldChildrenStack.pop();
      final List<IRNode> newChildren = newChildrenStack.pop();

      if (oldChildren != null && oldChildren.equals(newChildren)) { return; }

      n.setSlotValue(childrenAttr, newChildren);
      //propagateChange(n);
    }

    private void updateAttrs2(IRNode n, Attributes attrs){
      AttributeList old = n.getSlotValue(SimpleXmlParser2.attrListAttr);
      AttributeList newList = new AttributeList(attrs.getLength());
      for(int i=0; i<attrs.getLength(); i++){
        newList.addAttribute(new Attribute(attrs.getQName(i), attrs.getValue(i)));
      }
      if (old.equals(newList)) return;
      n.setSlotValue(SimpleXmlParser2.attrListAttr, newList);
      //propagateChange(n);
    }
 
  }

}