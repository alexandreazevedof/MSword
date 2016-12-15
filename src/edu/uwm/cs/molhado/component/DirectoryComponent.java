package edu.uwm.cs.molhado.component;

import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class DirectoryComponent extends Component{

  protected DirectoryComponent(){

  }

	public DirectoryComponent(Project project, IRNode node) {
	  super(project, node);
	}

	public DirectoryComponent(Project project, String name){
		super(project, null, name);
	}

	public DirectoryComponent(Project project, DirectoryComponent parent, String name){
		super(project, parent, name);
	}
	@Override
	public boolean isDirectory() {
		return true;
	}

	private void ensureLoaded(){
		if (isNew) return;
		IRLoadingUtil.load(project.getRegion(), projectBundle, project.getFileLocator());
	}

	public Component[] getChildren(){
		ensureLoaded();
		int nc = projectTree.numChildren(shadowNode);
		Component[] children = new Component[nc];
		for(int i=0; i<nc; i++){
			IRNode childNode = projectTree.getChild(shadowNode, i);
			children[i] = project.getComponent(childNode);
		}
		return children;
	}

	public void addChild(Component child){
		ensureLoaded();
		//projectTree.initNode(child.shadowNode);
		projectTree.appendChild(shadowNode, child.shadowNode);
		setChanged();
		notifyObservers(child);
	}

	public void removeChild(Component child){
		ensureLoaded();
		Component.projectTree.removeChild(shadowNode, child.shadowNode);
		setChanged();
		notifyObservers(child);
	}

	@Override
	public void unload() {
	}

	@Override
	public void load(Version v) {
	}

	@Override
	public void store(Version v) throws IOException {
		}

	public void copyTo(Version fromVersion, DirectoryComponent destination){

	}

   protected SharedComponent makeSharedComponent(SharedComponent sharedRoot, Version v){
    //return new SharedDirectoryComponent(this.sharedNode, this);
    return new SharedDirectoryComponent((SharedDirectoryComponent)sharedRoot, this, v);
  }

}

