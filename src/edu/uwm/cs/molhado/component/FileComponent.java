package edu.uwm.cs.molhado.component;

import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedRegion;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public abstract class FileComponent extends Component{

	protected VersionedRegion region;

  protected FileComponent(){

  }

	protected FileComponent(Project project, IRNode node) {
	  super(project, node);
	  region = getRegion();
		try {
			region.load(project.getFileLocator());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public FileComponent(Project project, DirectoryComponent parent, String name){
		super(project, parent, name);
		region = new VersionedRegion();
	   setRegion(region);
	}

	public FileComponent(Project project, DirectoryComponent parent, File file)
			  throws IOException{
		super(project, parent, file.getName());
		region = new VersionedRegion();
		setRegion(region);
		importFile(file);
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	public IRNode getRootNode(){
		ensureLoaded();
		if (shadowNode.valueExists(docRootAttr)){
			return shadowNode.getSlotValue(docRootAttr);
		}
		return null;
	}

	public void setRootNode(IRNode n){
		ensureLoaded();
		shadowNode.setSlotValue(docRootAttr, n);
	}

	private VersionedRegion getRegion(){
		return shadowNode.getSlotValue(regionAttr);
	}

	private void setRegion(VersionedRegion region){
		shadowNode.setSlotValue(regionAttr, region);
	}

	public abstract void importFile(File file) throws IOException;

	protected void ensureLoaded(){
		if (isNew) return;
		IRLoadingUtil.load(region, getBundle(), project.getFileLocator());
	}

	//must be called by subclass for demand loading to work
	public void dumpContent(){
     ensureLoaded();
	}

	public abstract String getContent();

  public void exportToFile(File parent){
     ensureLoaded();
  }

	protected abstract Bundle getBundle();

	public void load(Version v) throws IOException{
     IRLoadingUtil.load(region, getBundle(), v, project.getFileLocator());
	}

	public void store(Version v) throws IOException{
		if (!region.isStored()) {
			region.store(project.getFileLocator());
		}
		IRLoadingUtil.store(region, getBundle(), v, project.getFileLocator());
	}

	public void unload(){
		region.unload();
	}

}
