package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class XmlComponent extends FileComponent{

	//each document has its own parser.
	protected SimpleXmlParser3 parser = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);

//	static{
//		SimpleXmlParser3 parserX = null;
//		parserX = new SimpleXmlParser3(0);
//		parser = parserX;
//	}

  protected XmlComponent(){

  }

	public XmlComponent(Project project, IRNode node) {
	  super(project, node);
	}

	public XmlComponent(Project project, DirectoryComponent parent,
			  String name ){
		super(project, parent, name);
	}

	//create an XML component from a file
	public XmlComponent(Project project, DirectoryComponent parent, File file)
			   throws IOException{
		super(project, parent, file);
	}

	public void importFile(File path) throws IOException{
		PlainIRNode.pushCurrentRegion(region);
		try {
			if (parser == null) parser = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);
			IRNode root = parser.parse(path);
			setRootNode(root);
			shadowNode.setSlotValue(mouidAttr, parser.getMouid());
		} catch (Exception ex) {
			System.out.println("Fail to parse file: " + path.getAbsolutePath());
			ex.printStackTrace();
		}
		PlainIRNode.popCurrentRegion();
	}

	@Override
	public void dumpContent(){
		super.dumpContent();
    System.out.println(SimpleXmlParser3.toStringWithID(this.getRootNode()));
	}

	public String getContent(){
		super.dumpContent();
		return SimpleXmlParser3.toStringWithID(this.getRootNode());
	}

  @Override
  public void exportToFile(File parent){
    try {
      super.exportToFile(parent);
      File f = new File(parent, this.getName());
      SimpleXmlParser3.writeToFile(f, this.getRootNode());
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
  }


	public void updateContent(String newContent) throws Exception{
		ensureLoaded();
		PlainIRNode.pushCurrentRegion(region);
		parser.setMouid(shadowNode.getSlotValue(mouidAttr));
		if (getRootNode() == null){
			setRootNode(parser.parse(newContent));
		} else {
			IRNode n = parser.parse(getRootNode(), newContent);
			setRootNode(n);
		}
		shadowNode.setSlotValue(mouidAttr, parser.getMouid());
		PlainIRNode.popCurrentRegion();
		setChanged();
		notifyObservers();
	}

	public void copyTo(Version fromVersion, XmlComponent toComponent){
		Version.saveVersion(fromVersion);
		super.dumpContent();
		String content = SimpleXmlParser3.toStringWithID(this.getRootNode());
		Version.restoreVersion();
		try {
			toComponent.updateContent(content);
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	protected Bundle getBundle(){
		return SimpleXmlParser3.bundle;
	}

	@Override
	protected SharedComponent makeSharedComponent(SharedComponent sharedRoot, Version v) {
		return new SharedXmlComponent((SharedDirectoryComponent)sharedRoot, this, v);
	}

}
