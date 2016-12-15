package edu.uwm.cs.molhado.xml;

import edu.cmu.cs.fluid.FluidError;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;
import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.tree.*;
import edu.cmu.cs.fluid.tree.SyntaxTreeInterface;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.xml.operator.*;

public class XmlParser {

	public final static VersionedChangeRecord changeRecord;

	/* name of an XML element */
	public final static SlotInfo<String> elementNameAttr;

	/* name of an XML attribute */
	public final static SlotInfo<String> attrNameAttr;

	/* value of an XML attribute */
	public final static SlotInfo<String> attrValueAttr;

	/* character data of a XML text node */
	public final static SlotInfo<String> textValueAttr;

	/* a typed tree */
	public static final  SyntaxTreeInterface tree;

	public static final Bundle bundle = new Bundle();

	static{
		SlotInfo<String> elementNameAttrX = null;
		SlotInfo<String> attrNameAttrX = null;
		SlotInfo<String> attrValueAttrX = null;
		SlotInfo<String> textValueAttrX = null;
		SyntaxTreeInterface treeX = null;
		VersionedChangeRecord changeRecordX  = null;
		try {
			treeX = new SyntaxTree("XmlDocument", VersionedSlotFactory.prototype);
			changeRecordX = (VersionedChangeRecord)
							VersionedSlotFactory.prototype.newChangeRecord("xml.changed");
      treeX.addObserver(changeRecordX);
      PropagateUpTree.attach(changeRecordX, (Tree) treeX);
			elementNameAttrX = VersionedSlotFactory.prototype.
					  newAttribute("comp.xml.element.name", IRStringType.prototype);
			attrNameAttrX = VersionedSlotFactory.prototype.
					  newAttribute("comp.xml.attr.name", IRStringType.prototype);
			attrValueAttrX = VersionedSlotFactory.prototype.
					  newAttribute("comp.xml.attr.value", IRStringType.prototype);
			textValueAttrX = VersionedSlotFactory.prototype.
					  newAttribute("comp.text.value", IRStringType.prototype);
		} catch (SlotAlreadyRegisteredException e) {
			throw new FluidError("Parse slots already registered " + e);
		}
		tree = treeX;
		changeRecord = changeRecordX;
		((Tree)tree).saveAttributes(bundle);
		bundle.saveAttribute(elementNameAttr=elementNameAttrX);
		bundle.saveAttribute(attrNameAttr=attrNameAttrX);
		bundle.saveAttribute(attrValueAttr=attrValueAttrX);
		bundle.saveAttribute(textValueAttr=textValueAttrX);
		bundle.setName("bundle"); //eXtensible Markedup Language
//		elementNameAttr = new DemandLoadingSlotInfo<String>(elementNameAttrX);
//		attrNameAttr = new DemandLoadingSlotInfo<String>(attrNameAttrX);
//		attrValueAttr = new DemandLoadingSlotInfo<String>(attrValueAttrX);
//		textValueAttr = new DemandLoadingSlotInfo<String>(textValueAttrX);
	}

	private XMLReader parser;

	private Stack<IRNode> stack;

	private IRNode root;

	public XmlParser() throws XmlParseException {

		stack = new Stack<IRNode>();
		try {
			String parserName = "org.apache.xerces.parsers.SAXParser";
			parser = XMLReaderFactory.createXMLReader(parserName);
			Handler handler = new Handler();
			parser.setContentHandler(handler);
			parser.setDTDHandler(handler);
			parser.setErrorHandler(handler);
		} catch (Exception e) {
			e.printStackTrace();
			throw new XmlParseException("Unable to create parser.");
		}

	}

	public IRNode parse(File file) throws Exception {
		return parse(new FileInputStream(file));
	}

	public IRNode parse(Reader reader) throws Exception {
		return parse(new InputSource(reader));
	}

	public IRNode parse(InputStream is) throws Exception {
		return parse(new InputSource(is));
	}

	public IRNode parse(String str) throws Exception {
		return parse(new StringReader(str));
	}

	private IRNode parse(InputSource is) throws Exception {
		parser.parse(is);
		return root;
	}
	
	public static void dumpXmlTree(int indent, final SyntaxTreeInterface tree,
			final IRNode node) {
	
		Operator o = tree.getOperator(node);
		if (o instanceof DocumentOp) {
			System.out.println("Document " );
			DocumentOp doc = DocumentOp.prototype;
			Iterator<IRNode> it = DocumentOp.getChildIterator(node);
			while (it.hasNext()) {
				dumpXmlTree(indent + 3, tree, it.next());
			}
		} else if (o instanceof ElementOp) {
			ElementOp e = ElementOp.prototype;
			String name = e.getElementName(node);
			System.out.print(makeIndent(indent) + "<" + name);
			
			Iterator<IRNode> ait = ElementOp.getAttrIterator(node);
			while (ait.hasNext()) {
				printAttr(tree, ait.next());
			}
			System.out.println(">");

			Iterator<IRNode> it = ElementOp.getChildIterator(node);
			while (it.hasNext()) {
				dumpXmlTree(indent + 3, tree, it.next());
			}

			System.out.println(makeIndent(indent) + "</" + name + ">");

		} else if (o instanceof TextOp) {
			String text = TextOp.prototype.getData(node);
			System.out.print(makeIndent(indent) + text);
		}else if (o instanceof CommentOp) {
			String text = CommentOp.prototype.getData(node);
			System.out.println(makeIndent(indent) + "<!-- " + text + " -->");
		}

	}

	private static void printAttr(SyntaxTreeInterface tree, IRNode node) {
		String name = AttrOp.prototype.getName(node);
		String val = AttrOp.prototype.getValue(node);
		System.out.print(" " + name + "=\"" + val + "\" ");
	}

	private static String makeIndent(int len) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	private class Handler extends DefaultHandler2 {

		private void addChild(IRNode parent, IRNode node) {
			Operator op = tree.getOperator(parent);

			if (op instanceof DocumentOp) {
				DocumentOp.prototype.addChild(parent, node);
			} else if (op instanceof ElementOp) {
				ElementOp.prototype.addChild(parent, node);
			}
		}

		@Override
		public void startDocument() throws SAXException {
			root = DocumentOp.prototype.createNode();
			
			stack.push(root);
		}

		@Override
		public void endDocument() throws SAXException {
			stack.pop();
		}

		@Override
		public void startElement(String nameSpaceURI, String simpleName,
				String qualifiedName, Attributes attrs) throws SAXException {

			IRNode parent = null;
			if (!stack.isEmpty()) {
				parent = stack.peek();
			}

			ElementOp el = ElementOp.prototype;
			IRNode node = el.createElementNode(tree, qualifiedName);
			addChild(parent, node);

			for (int i = 0; i < attrs.getLength(); i++) {
				String attrName = attrs.getQName(i);
				String attrValue = attrs.getValue(i);
				IRNode attrNode = AttrOp.prototype.createAttrNode(attrName,
						attrValue);
				el.addAttribute(node, attrNode);
			}
			stack.push(node);
		}

		@Override
		public void endElement(String namespaceURI, String simpleName,
				String qualifiedName) throws SAXException {
			stack.pop();
		}

		@Override
		public void characters(char buf[], int offset, int len)
				throws SAXException {
			String data = new String(buf, offset, len);
			if (data.trim().equals("")) {
				return;
			}
			IRNode parent = stack.peek();
			IRNode node = TextOp.prototype.createCharacterDataNode(tree, data);
			addChild(parent, node);

		}

		public void error(XmlParseException e) throws XmlParseException {
			e.printStackTrace();
		}

		public void warning(XmlParseException e) throws XmlParseException {
			e.printStackTrace();
		}

		public void fetalError(XmlParseException e) throws XmlParseException {
			e.printStackTrace();
		}

		@Override
		public void startCDATA() throws SAXException {
		}

		@Override
		public void endCDATA() throws SAXException {
		}

		@Override
		public void startDTD(String name, String publicId, String systemId)
				throws SAXException {
		}

		@Override
		public void endDTD() throws SAXException {
		}

		@Override
		public void startEntity(String name) throws SAXException {
		}

		@Override
		public void endEntity(String name) throws SAXException {
		}

		@Override
		public void comment(char ch[], int start, int length)
				throws SAXException {
		}

		// SAX2 ext-1.0 DeclHandler

		@Override
		public void attributeDecl(String eName, String aName, String type,
				String mode, String value) throws SAXException {
		}

		@Override
		public void elementDecl(String name, String model) throws SAXException {
		}

		@Override
		public void externalEntityDecl(String name, String publicId,
				String systemId) throws SAXException {
		}

		@Override
		public void internalEntityDecl(String name, String value)
				throws SAXException {
		}

		// SAX2 ext-1.1 EntityResolver2

		@Override
		public InputSource getExternalSubset(String name, String baseURI)
				throws SAXException, IOException {
			return null;
		}

		@Override
		public InputSource resolveEntity(String name, String publicId,
				String baseURI, String systemId) throws SAXException,
				IOException {
			return null;
		}

		// SAX1 EntityResolver

		@Override
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			return resolveEntity(null, publicId, null, systemId);
		}

		@Override
		public void notationDecl(String name, String publicId, String systemId)
				throws SAXException {
			// no op
		}

		@Override
		public void unparsedEntityDecl(String name, String publicId,
				String systemId, String notationName) throws SAXException {
			// no op
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			// no op
		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			// no op
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// no op
		}

		@Override
		public void ignorableWhitespace(char ch[], int start, int length)
				throws SAXException {
			// no op
		}

		@Override
		public void processingInstruction(String target, String data)
				throws SAXException {
			// no op
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			// no op
		}

		////////////////////////////////////////////////////////////////////
		// Default implementation of the ErrorHandler interface.
		////////////////////////////////////////////////////////////////////
		@Override
		public void warning(SAXParseException e) throws SAXException {

		}

		@Override
		public void error(SAXParseException e) throws SAXException {
			System.out.println(e.getMessage());
			System.out.println("at line " + e.getLineNumber() + " column "
					+ e.getColumnNumber());
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			throw e;
		}

	};

}
