package edu.uwm.cs.molhado.version.document;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class VersionedXmlDocument extends VersionedDocument {

	private SimpleXmlParser3 parser = null;//new SimpleXmlParser3(0);
	private final static Bundle bundle = SimpleXmlParser3.bundle;

	//constructor for new file
	public VersionedXmlDocument() {
		super();
	}

	//constructor for loading existing file
	private VersionedXmlDocument(String path) throws IOException {
		super(path);
	}

	//load existing file
	public static VersionedXmlDocument open(String path) throws IOException {
		return new VersionedXmlDocument(path);
	}

	@Override
	public String getContent() {
		return SimpleXmlParser3.toStringWithID(rootNode);
	}



	@Override
	protected void updateContent(String newContent) throws Exception {
		if (parser == null) {
			parser = new SimpleXmlParser3(lastUsedId, SimpleXmlParser3.NORMAL_PARSING);
		}
		if (rootNode == null) {
			rootNode = parser.parse(newContent);
		} else {
			parser.parse(rootNode, newContent);
		}
	}

	@Override
	protected Bundle getBundle() {
		return bundle;
	}

	protected int getLastUsedId() {
		return parser.getMouid();
	}
}
