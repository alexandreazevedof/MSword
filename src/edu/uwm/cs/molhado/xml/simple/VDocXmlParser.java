/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.xml.simple;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.delta.AttributeAddition;
import edu.uwm.cs.molhado.delta.AttributeDeletion;
import edu.uwm.cs.molhado.delta.AttributeUpdate;
import edu.uwm.cs.molhado.delta.ChildrenUpdate;
import edu.uwm.cs.molhado.delta.NameUpdate;
import edu.uwm.cs.molhado.delta.NodeAddition;
import edu.uwm.cs.molhado.delta.Revision;
import edu.uwm.cs.molhado.delta.RevisionHistory;
import edu.uwm.cs.molhado.util.Property;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.UUID;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author chengt
 */
public class VDocXmlParser extends SimpleXmlParser3 {

	private boolean update_parse = false; 
	
	public VDocXmlParser() {
		super(0, SimpleXmlParser3.VDOC_PARSING);
	}

	@Override
	public void initNode(IRNode node, String tagName, Attributes attrs) {

		node.setSlotValue(tagNameAttr, tagName);
		IRSequence<Property> sq = VersionedSlotFactory.prototype.newSequence(-1);
		boolean mouidFound = false;

		for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
			String name = attrs.getQName(i);
			String val = attrs.getValue(i);
			if (name.equals(SimpleXmlParser3.IDNAME)) {
				int id = Integer.parseInt(val);
				node.setSlotValue(mouidAttr, id);
				mouidFound = true;
			} else {
				 sq.appendElement(new Property(name, val));
			}
		}

		node.setSlotValue(nodeTypeAttr, "element");
		node.setSlotValue(attrsSeqAttr, sq);
		if (!mouidFound) node.setSlotValue(mouidAttr, mouid++);

		tree.initNode(node);
	}
	public static void writeToFileWithDelta(File file, IRNode n, RevisionHistory rh) throws IOException {
		FileWriter w = new FileWriter(file);
		w.write(ToStringWithDelta(n, rh));
		w.close();
	}

	public static void writeToFileWithDelta(String file, IRNode n, RevisionHistory rh) throws IOException {
		writeToFileWithDelta(new File(file), n, rh);
	}

	public static String ToStringWithDelta(IRNode n, RevisionHistory rh) {
		StringWriter w = new StringWriter();
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		boolean withID = true;
		int indent = 2;
		try {
			String tagName = n.getSlotValue(tagNameAttr);
			w.write("<");
			w.write(tagName);
			if (withID) {
				w.write(" " + IDNAME + "=\"" + n.getSlotValue(SimpleXmlParser3.mouidAttr) + "\"");
			}
			if (!hasAttribute(n, "xmlns:molhado")) {
				appendAttribute(n, "xmlns:molhado", "http://www.cs.uwm.edu/molhado");
			}
			w.write(dumpAttrs(3, n, withID));
			w.write(">\n");
			w.write(rh.toString());
			w.write("\n");
			int numChildren = tree.numChildren(n);
			if (numChildren > 0) {
				for (int i = 0; i < numChildren; i++) {
					IRNode c = tree.getChild(n, i);
					dumpContent(w, indent + 2, c, withID);
				}
				//	w.write("\n");
				w.write(indent(indent));
				w.write("</");
				w.write(tagName);
				w.write(">\n");
			}

			//dumpContent(w, 0, n, true);
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
		return w.toString();
	}

	public RevisionHistory parseRevisionHistory(File file) throws Exception {
		update_parse = false;
		RevisionHistory history = new RevisionHistory();
		XMLReader parser = getReader();
		history.setParser(this);
		VDocVersionDataParseHandler handler = new VDocVersionDataParseHandler(history);
		parser.setContentHandler(handler);
		try {
			parser.parse(new InputSource(new FileInputStream(file)));
		} catch (Exception e) {
			throw e;
		}
		return history;
	}

	public IRNode parseNewVDocContent(IRNode root, File file) throws Exception {
		update_parse= true;
		XMLReader parser = getReader();
		VDocUpdateParseHandler handler = new VDocUpdateParseHandler(root);
		parser.setContentHandler(handler);
		try {
			parser.parse(new InputSource(new FileInputStream(file)));
		} catch (Exception e) {
			throw e;
		}
		return root;
	}

	public IRNode parse(File file, RevisionHistory eh) throws Exception {
		return parse(new FileInputStream(file), eh);
	}

	public IRNode parse(InputStream is, RevisionHistory eh) throws Exception {
		return parse(new InputSource(is), eh);
	}

	public IRNode parse(String str, RevisionHistory eh) throws Exception {
		return parse(new StringReader(str), eh);
	}

	public IRNode parse(Reader reader, RevisionHistory eh) throws Exception {
		return parse(new InputSource(reader), eh);
	}

	public IRNode parse(InputSource is, RevisionHistory eh) throws Exception {
		update_parse = false;
		XMLReader parser = getReader();
		eh.setParser(this);
		VDocParseHandler handler = new VDocParseHandler(eh);
		parser.setContentHandler(handler);
		try {
			parser.parse(is);
		} catch (Exception e) {
			throw e;
		}
		return handler.getRoot();
	}

	private IRNode parse(IRNode root, InputSource is) throws Exception {
		update_parse = true;
		XMLReader parser = getReader();
		parser.setContentHandler(new VDocUpdateParseHandler(root));
		int curId = mouid;
		try {
			parser.parse(is);
		} catch (Exception e) {
			mouid = curId;
			throw e;
		}
		return root;
	}

	private void handleRevisionHistory(RevisionHistory editHistory, Attributes attrs) {
		String sUid = attrs.getValue("cur-rev-id");
		if (sUid != null && !sUid.trim().isEmpty()) {
			editHistory.setCurRevisionId(UUID.fromString(sUid));
		}
		int maxId = Integer.parseInt(attrs.getValue("max-id"));
		mouid = maxId;
		String curUser = attrs.getValue("cur-user");
		editHistory.setCurRevisionUser(curUser);
		String sPuids = attrs.getValue("cur-parents");
		if (sPuids != null && !sPuids.trim().isEmpty()) {
			String[] result = sPuids.split(",");
			for (int i = 0; i < result.length; i++) {
				UUID uid = UUID.fromString(result[i].trim());
				editHistory.addCurParentIds(uid);
			}
		}
	}

	/**
	 * Only extract the version history and not the content of the document.
	 * Useful for getting revision data but not the content.
	 */
	private class VDocVersionDataParseHandler extends NormalParsingHandler {

		private RevisionHistory editHistory;

		public VDocVersionDataParseHandler() {
			parse_type = SimpleXmlParser3.VDOC_PARSING;
		}

		public VDocVersionDataParseHandler(RevisionHistory editHistory) {
			this.editHistory = editHistory;
		}

		@Override
		public void startElement(String nameSpaceURI, String simpleName, String qualifiedName, Attributes attrs)
				  throws SAXException {
			if (qualifiedName.equals("molhado:revision-history")) {
				handleRevisionHistory(editHistory, attrs);
			} else if (qualifiedName.equals("molhado:revision")) {
				handleRevisionElement(attrs);
			}
		}

		@Override
		public void endElement(String namespaceURI, String simpleName, String qualifiedName)
				  throws SAXException {
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
		}

		private void handleRevisionElement(Attributes attrs) {
			UUID id = UUID.fromString(attrs.getValue("id"));
			String name = attrs.getValue("name");
			String parents = attrs.getValue("parents");
			Revision r = null;
			if (parents == null || parents.isEmpty()) {
				r = editHistory.newRevision(id, name);
			} else {
				String[] pIds = parents.split(",");
				ArrayList<UUID> parentIds = new ArrayList<UUID>();
				for (String pid : pIds) {
					UUID parentId = UUID.fromString(pid);
					parentIds.add(parentId);
				}
				r = editHistory.newRevision(id, parentIds, name);
			}
			if (r != null) {
				r.setUser(attrs.getValue("user"));
			}
		}
	}

	/**
	 * Use this handler to parse a Version-aware document.  It assumes all
	 * node have IDs and that the IDs are not necessary in sequential order. 
	 * It simply reuse the IDs and keep track of the largest ID.  It also extracts
	 * the version data.
	 */
	private class VDocParseHandler extends NormalParsingHandler {

		private RevisionHistory editHistory;
		private Revision curRevision;
		private boolean inHistoryNode;

		public VDocParseHandler(RevisionHistory editHistory) {
			this.editHistory = editHistory;
			parse_type = SimpleXmlParser3.VDOC_PARSING;
			mouid = editHistory.getMaxNodeId();
		}

		@Override
		public void startElement(String nameSpaceURI, String simpleName, String qualifiedName, Attributes attrs)
				  throws SAXException {
			if (!inHistoryNode && qualifiedName.equals("molhado:revision-history")) {
				inHistoryNode = true;
				handleRevisionHistory(editHistory, attrs);
			} else if (inHistoryNode && qualifiedName.equals("molhado:revision")) {
				handleRevisionElement(attrs);
			} else if (inHistoryNode && qualifiedName.equals("molhado:name-update")) {
				handleNameUpdate(attrs);
			} else if (inHistoryNode && qualifiedName.equals("molhado:attr-update")) {
				handleAttrUpdate(attrs);
			} else if (inHistoryNode && qualifiedName.equals("molhado:attr-del")) {
				handleAttrDel(attrs);
			} else if (inHistoryNode && qualifiedName.equals("molhado:attr-add")) {
				handleAttrAdd(attrs);
			} else if (inHistoryNode && qualifiedName.equals("molhado:children-update")) {
				handleChildrenUpdate(attrs);
			} else if (inHistoryNode && qualifiedName.equals("molhado:node-add")) {
				handleNodeAdd(attrs);
			} else if (!inHistoryNode) {
				super.startElement(nameSpaceURI, simpleName, qualifiedName, attrs);
			}
		}

		@Override
		public void endElement(String namespaceURI, String simpleName, String qualifiedName)
				  throws SAXException {
			if (inHistoryNode && qualifiedName.equals("molhado:revision-history")) {
				inHistoryNode = false;
			} else if (inHistoryNode && qualifiedName.equals("molhado:revision")) {
				curRevision = null;
			} else if (!inHistoryNode) {
				super.endElement(namespaceURI, simpleName, qualifiedName);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (!inHistoryNode) {
				super.characters(ch, start, length);
			}
		}

		private void handleRevisionElement(Attributes attrs) {
			UUID id = UUID.fromString(attrs.getValue("id"));
			String parentsAttr = attrs.getValue("parents");
			String name = attrs.getValue("name");
			String user = attrs.getValue("user");

			if (parentsAttr != null && !parentsAttr.equals("")) {
				String[] parents = parentsAttr.split(",");
				if (parents.length != 0) {
					ArrayList<UUID> parentsIds = new ArrayList<UUID>();
					for (String parentSID : parents) {
						System.out.println("Parent UUID: " + parentSID);
						UUID parentId = UUID.fromString(parentSID.trim());
						parentsIds.add(parentId);
					}
					curRevision = editHistory.newRevision(id, parentsIds, name);
				}
			} else {
				curRevision = editHistory.newRevision(id, name);
			}
			curRevision.setUser(user);
		}

		private void handleNameUpdate(Attributes attrs) {
			int nodeid = Integer.parseInt(attrs.getValue("nodeid"));
			String oldvalue = attrs.getValue("oldvalue");
			String newvalue = attrs.getValue("newvalue");
			NameUpdate e = new NameUpdate(nodeid, oldvalue, newvalue);
			curRevision.addEdit(e);
		}

		private void handleAttrUpdate(Attributes attrs) {
			int nodeid = Integer.parseInt(attrs.getValue("nodeid"));
			String attr = attrs.getValue("attr");
			String oldvalue = attrs.getValue("oldvalue");
			String newvalue = attrs.getValue("newvalue");
			AttributeUpdate e = new AttributeUpdate(nodeid, attr, oldvalue, newvalue);
			curRevision.addEdit(e);
		}

		private void handleAttrDel(Attributes attrs) {
			int nodeid = Integer.parseInt(attrs.getValue("nodeid"));
			String attr = attrs.getValue("attr");
			String value = attrs.getValue("value");
			AttributeDeletion e = new AttributeDeletion(nodeid, attr, value);
			curRevision.addEdit(e);
		}

		private void handleAttrAdd(Attributes attrs) {
			int nodeid = Integer.parseInt(attrs.getValue("nodeid"));
			String attr = attrs.getValue("attr");
			String value = attrs.getValue("value");
			AttributeAddition e = new AttributeAddition(nodeid, attr, value);
			curRevision.addEdit(e);
		}

		private void handleChildrenUpdate(Attributes attrs) {
			int nodeid = Integer.parseInt(attrs.getValue("nodeid"));
			String oldchildren = attrs.getValue("oldchildren");
			String newchildren = attrs.getValue("newchildren");
			ArrayList<Integer> oldchildrenIds = parseChildrenListString(oldchildren);
			ArrayList<Integer> newchildrenIds = parseChildrenListString(newchildren);
			ChildrenUpdate e = new ChildrenUpdate(nodeid, oldchildrenIds, newchildrenIds);
			curRevision.addEdit(e);
		}

		private void handleNodeAdd(Attributes attrs) {
			int nodeid = Integer.parseInt(attrs.getValue("nodeid"));
			String name = attrs.getValue("name");
			NodeAddition e = new NodeAddition(nodeid, name);
			curRevision.addEdit(e);
		}

		private ArrayList<Integer> parseChildrenListString(String children) {
			ArrayList<Integer> childrenIds = new ArrayList<Integer>();
			if (children.length() == 2) {
				return childrenIds;
			}
			children = children.substring(1, children.length() - 1);
			String[] result = children.split(",");
			for (String r : result) {
				int id = Integer.parseInt(r.trim());
				childrenIds.add(id);
			}
			return childrenIds;
		}
	}

	/**
	 * Use this handler to read version-aware document that has been modified
	 * by an editor.  The new nodes do not have IDs and needed to assign ID
	 * to them.  It's possible to have ID collision when a document is copied and
	 * modified in paralleled.  Eventually, we will update one of the document's
	 * IDs so that there will be no collision.
	 */
	private class VDocUpdateParseHandler extends NormalUpdateParseHandler {

		private boolean inHistoryNode = false;

		public VDocUpdateParseHandler(IRNode root) {
			super(root);
			parse_type = SimpleXmlParser3.VDOC_PARSING;
		}

		@Override
		public void startElement(String nameSpaceURI, String simpleName, String qualifiedName, Attributes attrs)
				  throws SAXException {
			if (!inHistoryNode && qualifiedName.equals("molhado:revision-history")) {
				inHistoryNode = true;
			} else if (!inHistoryNode) {
				super.startElement(nameSpaceURI, simpleName, qualifiedName, attrs);
			}
		}

		@Override
		public void endElement(String namespaceURI, String simpleName, String qualifiedName)
				  throws SAXException {
			if (inHistoryNode && qualifiedName.equals("molhado:revision-history")) {
				inHistoryNode = false;
			} else if (!inHistoryNode) {
				super.endElement(namespaceURI, simpleName, qualifiedName);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (!inHistoryNode) {
				super.characters(ch, start, length);
			}
		}
	}
}
