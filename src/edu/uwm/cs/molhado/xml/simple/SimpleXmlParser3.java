package edu.uwm.cs.molhado.xml.simple;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.ConstantSlotFactory;
import edu.cmu.cs.fluid.ir.IRIntegerType;
import edu.cmu.cs.fluid.ir.IRLocation;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.IRSequenceType;
import edu.cmu.cs.fluid.ir.IRStringType;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SimpleSlotFactory;
import edu.cmu.cs.fluid.ir.SlotAlreadyRegisteredException;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.PropagateUpTree;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.util.Hashtable2;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
//import edu.uwm.cs.molhado.delta.RevisionHistory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * We store attribute in an IRSequence instead of the AttributeList object.
 * We can't save AttributeList object, so need iRSequence if we need persistent.
 *
 * Parse XML files or synchronize files with the XML in memory using node ID
 *
 * @author chengt
 */
public class SimpleXmlParser3 {

	public final static String TREE = "xml.tree";
	public final static String DOCROOTELEMENT = "xml.doctype.rootelement";
	public final static String DOCPUBLICID = "xml.doctype.publicid";
	public final static String DOCSYSTEMID = "xml.doctype.systemid";
	public final static String NODETYPE = "xml.type";
	public final static String MOUID = "xml.mouid";
	public final static String TAGNAME = "xml.name";
	public final static String TEXT = "xml.text";
	public final static String ATTRS = "xml.attrs";
	public final static String CHANGE = "xml.change";
	public final static String CHILDREN_CHANGE = "xml.children_change";
	public final static String BUNDLE = "bundle";
	public final static String IDNAME = "molhado:id";
	public final static Tree tree;
	//<!DOCTYPE name publicId systemId>
	public final static SlotInfo<String> docRootElementNameAttr;
	public final static SlotInfo<String> docPublicIdAttr;
	public final static SlotInfo<String> docSystemIdAttr;
	public final static SlotInfo<String> nodeTypeAttr;
	public final static SlotInfo<Integer> mouidAttr;
	public final static SlotInfo<String> tagNameAttr;
	public final static SlotInfo<String> textAttr;
	public final static SlotInfo<IRSequence<Property>> attrsSeqAttr;
	public final static VersionedChangeRecord changeRecord;
	public final static Bundle bundle = new Bundle();
	public final static VersionedSlotFactory vsf = VersionedSlotFactory.prototype;
	public final static ConstantSlotFactory csf = ConstantSlotFactory.prototype;
	public final static SimpleSlotFactory ssf = SimpleSlotFactory.prototype;

	static {
		SlotInfo<String> docRootElementNameAttrX = null;
		SlotInfo<String> docPublicIdAttrX = null;
		SlotInfo<String> docSystemIdAttrX = null;
		SlotInfo<String> nodeTypeAttrX = null;
		SlotInfo<Integer> mouidAttrX = null;
		SlotInfo<String> tagNameX = null;
		SlotInfo<String> textAttrX = null;
		SlotInfo<IRSequence<Property>> attrsX = null;
		VersionedChangeRecord changeRecordX = null;
		Tree treeX = null;
		try {
			treeX = new Tree(TREE, vsf);
			docRootElementNameAttrX = vsf.newAttribute(DOCROOTELEMENT, IRStringType.prototype);
			docPublicIdAttrX = vsf.newAttribute(DOCPUBLICID, IRStringType.prototype);
			docSystemIdAttrX = vsf.newAttribute(DOCSYSTEMID, IRStringType.prototype);
			nodeTypeAttrX = vsf.newAttribute(NODETYPE, IRStringType.prototype);
			mouidAttrX = vsf.newAttribute(MOUID, IRIntegerType.prototype);
			tagNameX = vsf.newAttribute(TAGNAME, IRStringType.prototype);
			textAttrX = vsf.newAttribute(TEXT, IRStringType.prototype);
			attrsX = vsf.newAttribute(ATTRS, new IRSequenceType(IRPropertyType.prototype));
			changeRecordX = (VersionedChangeRecord) vsf.newChangeRecord(CHANGE);
		} catch (SlotAlreadyRegisteredException ex) {
			Exceptions.printStackTrace(ex);
		}
		bundle.setName(BUNDLE);
		bundle.saveAttribute(docRootElementNameAttr = docRootElementNameAttrX);
		bundle.saveAttribute(docPublicIdAttr = docPublicIdAttrX);
		bundle.saveAttribute(docSystemIdAttr = docSystemIdAttrX);
		bundle.saveAttribute(nodeTypeAttr = nodeTypeAttrX);
		bundle.saveAttribute(mouidAttr = mouidAttrX);
		bundle.saveAttribute(tagNameAttr = tagNameX);
		bundle.saveAttribute(textAttr = textAttrX);
		bundle.saveAttribute(attrsSeqAttr = attrsX);
		changeRecord = changeRecordX;
		tree = treeX;
		tree.saveAttributes(bundle);
		SimpleXmlParser3.tagNameAttr.addDefineObserver(SimpleXmlParser3.changeRecord);
		SimpleXmlParser3.attrsSeqAttr.addDefineObserver(SimpleXmlParser3.changeRecord);
		SimpleXmlParser3.tree.addObserver(SimpleXmlParser3.changeRecord);
		PropagateUpTree.attach(SimpleXmlParser3.changeRecord, SimpleXmlParser3.tree);
	}
//	private static HashMap<Integer, IRNode> nodeTable = new HashMap<Integer, IRNode>();
	protected int mouid = 0;

	/* true if we are parsing a normal file.  It's set by the different handlers*/
	/* it is used by the initNode method.  If it is normal, ignore mouid */
//	private boolean normalParsing = true;

	public final static int NORMAL_PARSING = 0;
	public final static int VDOC_PARSING = 2;

	protected int parse_type = 0; 


	public SimpleXmlParser3(int nodeCount, int parseType) {
		this.mouid = nodeCount;
		parse_type = parseType;
	}

	public static void main(String[] args) throws XmlParseException, Exception {

		SimpleXmlParser3 p = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);
		String s = "<a><b/><c/></a>";
		IRNode n = p.parse(s);
		System.out.println(SimpleXmlParser3.toStringWithID(n));

		String t = "<a molhado:id='0'><x/><y/><b molhado:id='1' /><z/><c molhado:id='2' /></a>";
		p.parse(n, t);
		System.out.println("=======================================");
		System.out.println(SimpleXmlParser3.toStringWithID(n));

	}

	public int getMouid() {
		return mouid;
	}

	public void setMouid(int id) {
		mouid = id;
	}

	public void setParseType(int type){
		parse_type = type;
	}

	public int getParseType(){
		return parse_type;
	}

	public static String getAttributeValue(IRNode node, String name) {
		IRSequence<Property> seq = node.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
		//System.out.println(seq.size());
		for (int i = 0; i < seq.size(); i++) {
			Property p = seq.elementAt(i);
			//	System.out.println(p.getName() + ":" + p.getValue());
			if (p.getName().equals(name)) {
				return p.getValue();
			}
		}
		return null;
	}

	
	private XMLReader xmlReader = null;
	private String parserName = "org.apache.xerces.parsers.SAXParser";

	private class DummyEntityResolver implements EntityResolver2 {

		public InputSource resolveEntity(String publicID, String systemID)
						throws SAXException {
			return new InputSource(new StringReader(""));
		}

		public InputSource getExternalSubset(String name, String baseURI) 
				  throws SAXException, IOException {
			return new InputSource(new StringReader(""));
		}

		public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
				  throws SAXException, IOException {
			return new InputSource(new StringReader(""));
		}
	}

	protected XMLReader getReader() throws SAXException {
		if (xmlReader == null) {
			xmlReader = XMLReaderFactory.createXMLReader(parserName);
			xmlReader.setFeature("http://xml.org/sax/features/namespaces", false);
			xmlReader.setFeature("http://xml.org/sax/features/validation", false);
			//ignoring DTD
			xmlReader.setEntityResolver(new DummyEntityResolver());
		}
		return xmlReader;
	}

	protected static String indent(int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	protected static String dumpAttrs(int indent, IRNode n, boolean molhadoNS) {
		StringBuilder sb = new StringBuilder();
		IRSequence<Property> sq = n.getSlotValue(attrsSeqAttr);
		for (int i = 0; i < sq.size(); i++) {
			Property p = sq.elementAt(i);
			if (!molhadoNS && p.getName().equals("xmlns:molhado")) {
				continue;
			}
			sb.append("\n");
			sb.append(indent(indent + 3)).append(p.getName());
			sb.append("=\"");
			sb.append(p.getValue());
			sb.append("\" ");
		}
		return sb.toString();
	}

	protected static void dumpContent(Writer w, int indent, IRNode n, boolean withID) 
			  throws IOException {
		String tagName = n.getSlotValue(tagNameAttr);
		if (tagName.equals("char")) {
			String text = n.getSlotValue(textAttr);
			w.write(indent(indent));
			w.write(text);
			w.write("\n");
		} else {
			w.write(indent(indent));
			w.write("<");
			w.write(tagName);
			if (withID) {
				w.write(" " + IDNAME + "=\"" + n.getSlotValue(SimpleXmlParser3.mouidAttr) + "\"");
			}
			w.write(dumpAttrs(indent, n, withID));

			int numChildren = tree.numChildren(n);
			if (numChildren > 0) {
				w.write(">\n");
				for (int i = 0; i < numChildren; i++) {
					IRNode c = tree.getChild(n, i);
					dumpContent(w, indent + 2, c, withID);
				}
				//	w.write("\n");
				w.write(indent(indent));
				w.write("</");
				w.write(tagName);
				w.write(">\n");
			} else {
				w.write(" />\n");
			}
		}
	}

	public static String docTypeDecl(IRNode n) {
		if (n.valueExists(docRootElementNameAttr)) {
			return "<!DOCTYPE " + n.getSlotValue(docRootElementNameAttr)
							+ " PUBLIC \"" + n.getSlotValue(docPublicIdAttr)
							+ "\" \"" + n.getSlotValue(docSystemIdAttr) + "\">\n";
		}
		return "";
	}

	public static void writeToFile(File file, IRNode n) throws IOException {
		FileWriter w = new FileWriter(file);
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		w.write(docTypeDecl(n));
		dumpContent(w, 0, n, false);
		w.close();
	}

	public static void writeToFileWithID(File file, IRNode n) throws IOException {
		FileWriter w = new FileWriter(file);
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		w.write(docTypeDecl(n));
		if (!hasAttribute(n, "xmlns:molhado")) {
			appendAttribute(n, "xmlns:molhado", "http://www.cs.uwm.edu/molhado");
		}
		dumpContent(w, 0, n, true);
		w.close();
	}

	public static void writeToFile(String file, IRNode n) throws IOException {
		writeToFile(new File(file), n);
	}


	public static String toString(IRNode n) {
		StringWriter w = new StringWriter();
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		w.write(docTypeDecl(n));
		try {
			dumpContent(w, 0, n, false);
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
		return w.toString();
	}

	public static void appendAttribute(IRNode n, String attr, String val) {
		IRSequence<Property> seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
		seq.appendElement(new Property(attr, val));
	}

	public static String toStringWithID(IRNode n) {
		StringWriter w = new StringWriter();
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		w.write(docTypeDecl(n));
		try {
			dumpContent(w, 0, n, true);
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
		return w.toString();
	}

	public static boolean hasAttribute(IRNode n, String attr) {
		IRSequence<Property> slotValue = n.getSlotValue(attrsSeqAttr);
		for (int i = 0; i < slotValue.size(); i++) {
			Property p = slotValue.elementAt(i);
			if (p.getName().equals(attr)) {
				return true;
			}
		}
		return false;
	}




	public IRNode parse(File file) throws Exception {
		return parse(new FileInputStream(file));
	}

	public IRNode parse(InputStream is) throws Exception {
		return parse(new InputSource(is));
	}

	public IRNode parse(String str) throws Exception {
		return parse(new StringReader(str));
	}

	public IRNode parse(Reader reader) throws Exception {
		return parse(new InputSource(reader));
	}

	public IRNode parse(IRNode root, File file) throws Exception {
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
	
	private IRNode parse(IRNode root, InputSource is) throws Exception {
		XMLReader parser = getReader();
		parse_type = SimpleXmlParser3.NORMAL_PARSING;
	//	if (parse_type == SimpleXmlParser3.NORMAL_PARSING) {
			parser.setContentHandler(new NormalUpdateParseHandler(root));
	//	} else if (parse_type == SimpleXmlParser3.VDOC_PARSING) {
	//		parser.setContentHandler(new VDocUpdateParseHandler(root));
	//	}
		int curId = mouid;
		try {
			parser.parse(is);
		} catch (Exception e) {
			mouid = curId;
			throw e;
		}
		return root;
	}

	private IRNode parse(InputSource is) throws Exception {
		XMLReader parser = getReader();
		NormalParsingHandler handler = null;
		handler = new NormalParsingHandler();
		parser.setContentHandler(handler);
		parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
		int curId = mouid;
		try {
			parser.parse(is);
		} catch (Exception e) {
			mouid = curId;
			throw e;
		}
		return handler.getRoot();
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
	/**
	//	public void initNode2(IRNode node, String tagName, Attributes attrs) {
	//		node.setSlotValue(tagNameAttr, tagName);
	//		AttributeList attrList = new AttributeList(4);
	//		boolean mouidFound = false;
	//		for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
	//			String name = attrs.getQName(i);
	//			String val = attrs.getValue(i);
	//			if (name.equals(SimpleXmlParser2.IDNAME)) {
	//				mouid = Integer.parseInt(val);
	//				node.setSlotValue(mouidAttr, mouid);
	//				nodeTable.put(mouid, node);
	//				mouid++;
	//				mouidFound = true;
	//			} else {
	//				attrList.addAttribute(new Attribute(name, val));
	//			}
	//		}
	//		node.setSlotValue(attrListAttr, attrList);
	//		if (!mouidFound) {
	//			node.setSlotValue(mouidAttr, mouid++);
	//		}
	//		tree.initNode(node);
	//	}
	 *
	 */
	/**
	 * Creating a new node with given tag name and attributes.  It's possible that
	 * we may be given a document that already has mouid assigned to every node.
	 * In that case, we reuse the mouid.  Alternatively, we could just ignore the
	 * the existing mouid and assign new mouid.
	 *
	 * @param node
	 * @param tagName
	 * @param attrs
	 */
	public void initNode(IRNode node, String tagName, Attributes attrs) {

		node.setSlotValue(tagNameAttr, tagName);
		IRSequence<Property> sq = VersionedSlotFactory.prototype.newSequence(-1);
		boolean mouidFound = false;

		for (int i = 0; attrs != null && i < attrs.getLength(); i++) {

			String name = attrs.getQName(i);
			String val = attrs.getValue(i);

			if (name.equals(SimpleXmlParser3.IDNAME)){
				 mouid = Integer.parseInt(val);
             node.setSlotValue(mouidAttr, mouid);
             mouid++;
             mouidFound = true;
			} else {
				 sq.appendElement(new Property(name, val));
			}
		}

		node.setSlotValue(nodeTypeAttr, "element");
		node.setSlotValue(attrsSeqAttr, sq);

		if (!mouidFound) {
			node.setSlotValue(mouidAttr, mouid++);
		}

		tree.initNode(node);
	}

	private IRNode createElementNode(String tagName, Attributes attrs) {
		IRNode node = new PlainIRNode();
		initNode(node, tagName, attrs);
		return node;
	}

	private IRNode createTextNode(String text) {
		IRNode node = new PlainIRNode();
		node.setSlotValue(tagNameAttr, "char");
		node.setSlotValue(nodeTypeAttr, "char");
		node.setSlotValue(textAttr, text);
		//not sure if textNode needs ID
		node.setSlotValue(mouidAttr, mouid++);
		tree.initNode(node);
		return node;
	}

	/**
	 * Use this handler when reading the base document in normal 3-way merge.
	 * Do not use this in version-document parsing.  
	 */
	protected class NormalParsingHandler extends DefaultHandler2 {

		private String rootElementName;
		private String publicId;
		private String systemId;

		public NormalParsingHandler() {
			parse_type = SimpleXmlParser3.NORMAL_PARSING;
		}
		private Stack<IRNode> stack = new Stack<IRNode>();
		private IRNode root;

		public IRNode getRoot() {
			return root;
		}
		private ArrayList<String> prefixList = new ArrayList<String>();
		private ArrayList<String> uriList = new ArrayList<String>();

		@Override
		public void startPrefixMapping(String prefix, String uri)
						throws SAXException {
			prefixList.add(prefix);
			uriList.add(uri);
		}


		/* extracts doctype information, this is important for dita files */
		@Override
		public void startDTD(String name, String publicId, String systemId) throws SAXException {
			rootElementName = name;
			this.publicId = publicId;
			this.systemId = systemId;
			//System.out.println("<!DOCTYPE " + name + " PUBLIC " + publicId + " " + systemId + ">");
		}

		@Override
		public void endDocument() throws SAXException {
			if (rootElementName != null && publicId != null && systemId != null) {
				root.setSlotValue(docRootElementNameAttr, rootElementName);
				root.setSlotValue(docPublicIdAttr, publicId);
				root.setSlotValue(docSystemIdAttr, systemId);
			}
		}

		@Override
		public void startElement(String nameSpaceURI, String simpleName,
						String qualifiedName, Attributes attrs) throws SAXException {

			if (inTextMode) {
				String txt = stringBuffer.toString();
				stringBuffer = null;
				if (!txt.trim().equals("")) {
					IRNode n = createTextNode(txt.trim());
					tree.addChild(stack.peek(), n);
				}
			}
			inTextMode = false;
			IRNode node = createElementNode(qualifiedName, attrs);
//			nodeTable.put(node.getSlotValue(SimpleXmlParser3.mouidAttr), node);

			//handling namespaces
			if (!prefixList.isEmpty()) {
				for (int i = 0; i < prefixList.size(); i++) {
					String prefix = prefixList.get(i);
					prefix = prefix.equals("") ? "xmlns" : "xmlns:" + prefix;
					String uri = uriList.get(i);
					IRSequence<Property> seq = node.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
					seq.appendElement(new Property(prefix, uri));
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
		}

		@Override
		public void endElement(String namespaceURI, String simpleName,
						String qualifiedName) throws SAXException {
			if (inTextMode) {
				String txt = stringBuffer.toString();
				stringBuffer = null;
				inTextMode = false;
				if (!txt.trim().equals("")) {
					IRNode n = createTextNode(txt.trim());
					tree.addChild(stack.peek(), n);
				}
			}
			stack.pop();
		}
		private boolean inTextMode = false;
		private StringBuilder stringBuffer = null;

		@Override
		public void characters(char[] ch, int start, int length)
						throws SAXException {
			if (!inTextMode) {
				stringBuffer = new StringBuilder();
			}
			stringBuffer.append(ch, start, length);
			inTextMode = true;
		}
	}

	/**
	 * Use to update existing IR tree with changed XML content.  Use this under
	 * normal 3-way merge and not in VDOC.
	 */
	protected class NormalUpdateParseHandler extends DefaultHandler2 {

		private IRNode root;
		private Stack<IRNode> stack = new Stack<IRNode>();
		private Stack<List<IRNode>> oldChildrenStack = new Stack<List<IRNode>>();
		private Stack<List<IRNode>> newChildrenStack = new Stack<List<IRNode>>();
		private HashMap<Integer, IRNode> nodeTable = new HashMap<Integer, IRNode>();
		private Hashtable2<String, IRNode, IRNode> txtNodes = new Hashtable2<String, IRNode, IRNode>();
		private ArrayList<String> prefixList = new ArrayList<String>();
		private ArrayList<String> uriList = new ArrayList<String>();

		public NormalUpdateParseHandler(IRNode root) {
			//normalParsing = false;
			parse_type = SimpleXmlParser3.NORMAL_PARSING;
			this.root = root;
			buildTable(root);
		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
						throws SAXException {
			prefixList.add(prefix);
			uriList.add(uri);
		}


		@Override
		public void startElement(String nameSpaceURI, String simpleName,
						String qualifiedName, Attributes attrs) throws SAXException {

			List<IRNode> oldChildren = null;

			IRNode node = null;
			String uid = attrs.getValue(IDNAME);
			if (uid != null) {
				node = nodeTable.get(Integer.parseInt(uid));

				if (node == null) {
					node = createElementNode(qualifiedName, attrs);
				} else {

					if (!node.getSlotValue(tagNameAttr).equals(qualifiedName)) {
						node.setSlotValue(tagNameAttr, qualifiedName);
					}

					updateAttrs(node, attrs);

					int numChildren = tree.numChildren(node);
					oldChildren = new ArrayList<IRNode>(numChildren);
					for (int i = 0; i < numChildren; i++) {
						oldChildren.add(tree.getChild(node, i));
					}
				}
			} else {
				node = createElementNode(qualifiedName, attrs);
			}

			//handling namespaces
			if (!prefixList.isEmpty()) {
				for (int i = 0; i < prefixList.size(); i++) {
					String prefix = prefixList.get(i);
					prefix = prefix.equals("") ? "xmlns" : "xmlns:" + prefix;
					String uri = uriList.get(i);
					IRSequence<Property> seq =
									node.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
					seq.appendElement(new Property(prefix, uri));
				}
				prefixList.clear();
				uriList.clear();
			}

			if (inTextMode) {
				String txt = stringBuffer.toString().trim();
				inTextMode = false;
				stringBuffer = null;
				if (!txt.trim().equals("")) {
					IRNode n = txtNodes.get(txt, stack.peek());
					if (n == null) {
						n = createTextNode(txt);
					}
					newChildrenStack.peek().add(n);
				}
			}

			oldChildrenStack.push(oldChildren);
			if (!newChildrenStack.isEmpty()) {
				newChildrenStack.peek().add(node);
			}
			newChildrenStack.push(new ArrayList<IRNode>());
			stack.push(node);

		}

		@Override
		public void endElement(String namespaceURI, String simpleName,
						String qualifiedName) throws SAXException {

			final IRNode n = stack.pop();
			if (inTextMode) {
				String txt = stringBuffer.toString().trim();
				stringBuffer = null;
				inTextMode = false;
				if (!txt.trim().equals("")) {
					IRNode tn = txtNodes.get(txt, n);
					if (tn == null) {
						tn = createTextNode(txt);
					}
					newChildrenStack.peek().add(tn);
				}
			}

			final List<IRNode> oldChildren = oldChildrenStack.pop();
			final List<IRNode> newChildren = newChildrenStack.pop();

			if (oldChildren != null && oldChildren.equals(newChildren)) {
				return;
			}
			if (oldChildren != null) {
				tree.removeChildren(n);
			}

			for (IRNode c : newChildren) {
				IRNode t = nodeTable.get(c.getSlotValue(SimpleXmlParser3.mouidAttr));
				if (t != null) {
					IRNode p = tree.getParentOrNull(c);
					if (p != null) {
						tree.removeChild(p, c);
					}
				}
				tree.addChild(n, c);
			}

		}

		private void buildTable(IRNode n) {
			String nodeType = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
			if (nodeType.equals("char")) {
				txtNodes.put(n.getSlotValue(SimpleXmlParser3.textAttr), tree.getParent(n), n);
			} else {
				nodeTable.put(n.getIntSlotValue(SimpleXmlParser3.mouidAttr), n);
				for (int i = 0; i < tree.numChildren(n); i++) {
					IRNode c = tree.getChild(n, i);
					//nodeTable.put(c.getIntSlotValue(SimpleXmlParser3.mouidAttr), c);
					buildTable(c);
				}
			}
		}
		private boolean inTextMode = false;
		private StringBuilder stringBuffer = null;

		@Override
		public void characters(char[] ch, int start, int length)
						throws SAXException {
			if (!inTextMode) {
				stringBuffer = new StringBuilder();
			}
			stringBuffer.append(ch, start, length);
			inTextMode = true;
		}

		private void updateAttrs(IRNode n, Attributes attrs) {

			String tagname = n.getSlotValue(tagNameAttr);
			boolean hasAttr = n.valueExists(SimpleXmlParser3.attrsSeqAttr);
			int uuid = n.getSlotValue(SimpleXmlParser3.mouidAttr);

			IRSequence<Property> seq = n.getSlotValue(SimpleXmlParser3.attrsSeqAttr);
			boolean changed = false;
			boolean loop = true;
			for (IRLocation l = seq.lastLocation(); loop && seq.size() > 0;
							l = seq.prevLocation(l)) {
				boolean found = false;
				Property p = seq.elementAt(l);
				for (int i = 0; i < attrs.getLength(); i++) {
					String name = attrs.getQName(i);
					if (name.equals(IDNAME)) {
						continue;
					}

					if (name.equals(p.getName())) {
						found = true;
						if (!attrs.getValue(i).equals(p.getValue())) {
							Property p2 = new Property(name, attrs.getValue(i));
							seq.insertElementBefore(p2, l);
							seq.removeElementAt(l);
							changed = true;
						}
						break;
					}
				}

				if (l == seq.firstLocation()) {
					loop = false;
				}

				if (!found) {
					seq.removeElementAt(l);
					changed = true;
				}
			}

			for (int i = 0; i < attrs.getLength(); i++) {
				String name = attrs.getQName(i);
				boolean found = false;
				for (int j = 0; j < seq.size(); j++) {
					Property p = seq.elementAt(j);
					if (name.equals(p.getName())) {
						found = true;
						break;
					}
				}
				if (!found && !name.equals(IDNAME)) {
					Property p = new Property(name, attrs.getValue(i));
					seq.appendElement(p);
					changed = true;
				}
			}

			if (changed) {
				n.setSlotValue(SimpleXmlParser3.attrsSeqAttr, seq);
			}

		}
	}
}
