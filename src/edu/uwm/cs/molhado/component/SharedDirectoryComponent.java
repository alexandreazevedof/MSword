package edu.uwm.cs.molhado.component;

/**
 *
 * @author chengt
 */
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.IOException;

public class SharedDirectoryComponent extends DirectoryComponent
        implements SharedComponent{



  /**
	 * Create a shared component from a given component, project, and version.
	 * @param to      the project this component is being shared with
	 * @param parent  the parent component that the shared component be a child of
	 * @param comp    the component to be shared
	 * @param version the specific version of the component being shared
	 */
  public SharedDirectoryComponent(Project to, DirectoryComponent parent,
          DirectoryComponent comp, Version version){
  //  this(to, parent, comp);
		super(to, parent, "");
    sharedComponent = comp;
    sharedProject = comp.getProject();
		sharedRoot = this; //this will make it the default sharedroot
		isSharedRoot = true;
		setSharedVersion(version);
    this.setSharedProjectId(sharedProject.getId());
    this.setSharedNode(comp.shadowNode);
    this.setSharedVersion(version);
  }

	/**
	 * Loading a shared component by factory method call
	 * @param project
	 * @param shadowNode
	 */
	public SharedDirectoryComponent(Project project, IRNode shadowNode){
		super(project, shadowNode);
		sharedProject = Project.findProject(getSharedProjectId());
		isSharedRoot = true;
		sharedRoot = this;
		IRNode sharedNode = getSharedNode();
		loadSharedDelta();
		Version.saveVersion(getSharedVersion());
		sharedComponent = sharedProject.getComponent(sharedNode);
		Version.restoreVersion();
	}

  /**
	 * Created a shared component.  Node that we do not have a shadowNode
	 * associated with this.  This is because this shared component is a
	 * proxy component for the original node.  It's parent-child relationship
	 * depends on the context of the shared project (thus we have no
	 * shadowNode for the projected that the shared component is being shared
	 * with).
   * @param comp
   */
  protected SharedDirectoryComponent(SharedDirectoryComponent sharedRoot,
					DirectoryComponent comp,
					Version v){
		super();
    sharedComponent = comp;
    sharedProject = comp.getProject();
		sharedVersion = v;
		this.sharedRoot = sharedRoot;
  }


  @Override
  public String getName() {
    loadSharedDelta();
    String name= "";
    Version.saveVersion(getSharedVersion());
    name =sharedComponent.getName();
    Version.restoreVersion();
    return name;
  }

  private void loadSharedDelta() {
		try {
			IRLoadingUtil.load(sharedProject.getRegion(), projectBundle, 
							getSharedVersion(), sharedProject.getFileLocator());
		} catch (IOException ex) {
      ex.printStackTrace();
		}
  }



  @Override
  public Component[] getChildren() {
		//we are not going to create all proxy for all levels of the tree
		//just for the children being asked.
    loadSharedDelta();
    Version.saveVersion(getSharedVersion());
    Component[] childrenX = ((DirectoryComponent) sharedComponent).getChildren();
    Component[] children = new Component[childrenX.length];
    for (int i = 0; i < childrenX.length; i++) {
      Component component = childrenX[i];
       children[i] = (Component) component.makeSharedComponent(sharedRoot, getSharedVersion());
    }
    Version.restoreVersion();
    return children;
  }

	public boolean isSharedRoot() {
		return isSharedRoot;
	}

	public void updateToLatest() {
		Version v = VersionSupport.getDeepestDescendentInTrunk(getSharedVersion());
		this.setSharedVersion(v);
	}

	public void updateToVersion(Version v) {
		this.setSharedVersion(v);
	}

	public void forwardChange() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void backwardChangePropagation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
