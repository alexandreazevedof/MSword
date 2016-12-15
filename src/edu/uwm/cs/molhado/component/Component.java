package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.tree.PropagateUpTree;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.*;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

public abstract class Component extends Observable{


  /* unique ID for exporting file  and synchronizing */
  public final static SlotInfo<Integer> mouidAttr; //const

	/* component name usually a filename */
	public final static SlotInfo<String> nameAttr; //versioned

	/* component type specifically a class name*/
	public final static SlotInfo<String> typeAttr; //const

	/* Date component was created */
	//public final static SlotInfo<String> createdDateAttr;

	/* Date component was modified */
	//public final static SlotInfo<String> modifiedDateAttr;

	/* User who created this component */
	//public final static SlotInfo<String> createdByAttr;

	/* User who modified this comonent */
	//public final static SlotInfo<String> modifiedByAttr;
	
	/* Attribute that holds the document root */
	public final static SlotInfo<IRNode> docRootAttr; //versioned

	/* region that holds the project ir nodes */
	public final static SlotInfo<VersionedRegion> regionAttr; //const

	/* node being shared from another project */
	public final static SlotInfo<IRNode> sharedNodeAttr; //const

	/* the ID of the project the shared node belongs to */
	public final static SlotInfo<String> sharedProjectIdAttr; //const

	/* version of shared version, if null, use the lastest version from trunk */
	public final static SlotInfo<Version> sharedVersionAttr; //versioned

	/* IR tree that represents the structure of the project */
	public final static Tree projectTree;;

	/* Bundle holding the attributes of a node in a project tree */
	public final static Bundle projectBundle = new Bundle();

	public final static VersionedChangeRecord treeChangeRecord;

	public final static VersionedChangeRecord compChangeRecord;

	public final static VersionedChangeRecord nameChangeRecord;

	public final static VersionedChangeRecord allChangeRecord;

	public final static VersionedChangeRecord shareVersionChangeRecord;

	static {
    SlotInfo<Integer> mouidAttrX = null;
		SlotInfo<String> nameAttrX = null;
		SlotInfo<String> createdDateAttrX = null;
		SlotInfo<String> modifiedDateAttrX = null;
		SlotInfo<String> createdByAttrX = null;
		SlotInfo<String> modifiedByAttrX = null;
		SlotInfo<String> typeAttrX = null;
		SlotInfo<IRNode> rootAttrX = null;
		SlotInfo<VersionedRegion> regionAttrX = null;
		SlotInfo<IRNode> sharedNodeAttrX = null;
		SlotInfo<String> sharedProjectIdAttrX = null;
		SlotInfo<Version> sharedVersionAttrX = null;
		VersionedChangeRecord treeChangeRecordX = null;
		VersionedChangeRecord compChangeRecordX = null;
		VersionedChangeRecord nameChangeRecordX = null;
		VersionedChangeRecord allChangeRecordX = null;
		VersionedChangeRecord shareVersionChangeRecordX = null;


		VersionedSlotFactory vf = VersionedSlotFactory.prototype;
		ConstantSlotFactory cf = ConstantSlotFactory.prototype;

		Tree treeX = null;
		try {
			mouidAttrX = SimpleSlotFactory.prototype.newAttribute("proj.comp.umoid", IRIntegerType.prototype, 0);
			nameAttrX = vf.newAttribute("proj.comp.name", IRStringType.prototype);
			createdDateAttrX = vf.newAttribute("proj.comp.created-date", IRStringType.prototype);
			modifiedDateAttrX = vf.newAttribute("proj.comp.modified-date", IRStringType.prototype);
			createdByAttrX = vf.newAttribute("proj.comp.created-by", IRStringType.prototype);
			modifiedByAttrX = vf.newAttribute("proj.comp.modified-by", IRStringType.prototype);
			typeAttrX = vf.newAttribute("proj.comp.type", IRStringType.prototype);
			rootAttrX = vf.newAttribute("proj.comp.root", IRNodeType.prototype);
			regionAttrX = cf.newAttribute("proj.comp.region", IRPersistentReferenceType.<VersionedRegion>getInstance());
			sharedNodeAttrX = vf.newAttribute("proj.com.share.node", IRNodeType.prototype);
			sharedProjectIdAttrX = vf.newAttribute("proj.com.share.id", IRStringType.prototype);
			sharedVersionAttrX = vf.newAttribute("proj.com.share.version", IRVersionType.prototype);
			treeX = new Tree("proj.comp.tree", VersionedSlotFactory.prototype);
			treeChangeRecordX = (VersionedChangeRecord) vf.newChangeRecord("proj.tree.change");
			compChangeRecordX = (VersionedChangeRecord) vf.newChangeRecord("proj.com.change");
			nameChangeRecordX = (VersionedChangeRecord) vf.newChangeRecord("proj.com.namechange");
			allChangeRecordX = (VersionedChangeRecord) vf.newChangeRecord("proj.com.allchange");
			shareVersionChangeRecordX = (VersionedChangeRecord) vf.newChangeRecord("proj.com.shareversionchange");
		} catch (SlotAlreadyRegisteredException ex) {
			ex.printStackTrace();
		}

		mouidAttr                = mouidAttrX;
		nameAttr                 = nameAttrX;
//		createdDateAttr          = createdDateAttrX;
//		modifiedDateAttr         = modifiedDateAttrX;
//		createdByAttr            = createdByAttrX;
//		modifiedByAttr           = modifiedByAttrX;
		typeAttr                 = typeAttrX;
		docRootAttr              = rootAttrX;
		regionAttr               = regionAttrX;
		sharedNodeAttr           = sharedNodeAttrX;
		sharedProjectIdAttr      = sharedProjectIdAttrX;
		sharedVersionAttr        = sharedVersionAttrX;
		treeChangeRecord         = treeChangeRecordX;
		compChangeRecord         = compChangeRecordX;
		nameChangeRecord         = nameChangeRecordX;
		allChangeRecord          = allChangeRecordX;
		shareVersionChangeRecord = shareVersionChangeRecordX;

		projectBundle.saveAttribute(nameAttr);
		projectBundle.saveAttribute(mouidAttr);
		//projectBundle.saveAttribute(createdDateAttr);
		//projectBundle.saveAttribute(modifiedDateAttr);
		//projectBundle.saveAttribute(createdByAttr);
		//projectBundle.saveAttribute(modifiedByAttr);
		projectBundle.saveAttribute(typeAttr);
		projectBundle.saveAttribute(docRootAttr);
		projectBundle.saveAttribute(regionAttr);
		projectBundle.saveAttribute(sharedNodeAttr);
		projectBundle.saveAttribute(sharedProjectIdAttr);
		projectBundle.saveAttribute(sharedVersionAttr);
		projectTree = treeX;
		projectTree.saveAttributes(projectBundle);
		projectBundle.setName("comp");

		//to notify that this component has changed, we need to reset the root node
		docRootAttr.addDefineObserver(compChangeRecord);
		docRootAttr.addDefineObserver(allChangeRecord);

		//name change
		nameAttr.addDefineObserver(nameChangeRecord);
		nameAttr.addDefineObserver(allChangeRecord);

		//children change
		projectTree.addObserver(treeChangeRecord);
		projectTree.addObserver(allChangeRecord);

		sharedVersionAttr.addDefineObserver(shareVersionChangeRecord);
		sharedVersionAttr.addDefineObserver(allChangeRecord);

		//propagate tree changes: children
		PropagateUpTree.attach(treeChangeRecord, treeX);

		//propagate all changes: name, content, children
		PropagateUpTree.attach(allChangeRecord, treeX);

	}

	//ensure class and static code get executed.
	public static void ensureClassLoaded() {
		//		SharedComponent.ensureClassLoaded();
	}

	protected boolean isNew = true;
	protected boolean isLoaded = false;
	protected boolean isStored = false;

	/* node representing this component */
	protected IRNode shadowNode;

	/* the project this component belongs to */
	protected Project project;

  /* project shared component originates */
	protected Project sharedProject;

	protected UniqueID sharedProjectId;

//	/* IRNode representings the shared component */
//	protected IRNode    sharedNode;
//

	/* the shared root component. Use for children to access it's context */
  protected SharedDirectoryComponent sharedRoot;

/* original component */
	protected Component sharedComponent;

/* version being shared */
	protected Version   sharedVersion;

	/* true if component is the root component being shared */
	protected boolean isSharedRoot = false;

	protected Component(){

	}
	/* used when creating component from IRNode */
	protected Component(Project project, IRNode node) {
		this.project = project;
		this.shadowNode = node;
		this.isNew = false;
	}

	/**
	 * Creating a new component with a given parent component
	 * @param project  the project the component will be added
	 * @param parent   the parent component
	 * @param name     the name of the new component
	 */
	public Component(Project project, DirectoryComponent parent, String name) {
		this.project = project;
		shadowNode = new PlainIRNode(project.getRegion());
		projectTree.initNode(shadowNode);
		if (parent != null) {
			parent.addChild(this);
		}
		setType(this.getClass().getCanonicalName());
		project.addComponent(this);
		ComponentSlotInfo.setComponent(shadowNode, this);
		setName(name);
	}

	/**
	 * Given an IRNode, return the component representation of that node
	 * @param project  the project the node belongs to
	 * @param n        the IRNode itself
	 * @return         the component that represents n
	 */
	public static Component getComponent(Project project, IRNode n) {
		Component c = null;
		if (n.valueExists(typeAttr)) {
			String type = n.getSlotValue(typeAttr);
			if (n.valueExists(sharedVersionAttr)){
				//this is a shared component

			}

			try {
				c = (Component) Class.forName(type).getConstructor(Project.class,
					IRNode.class).newInstance(project, n);
				if (c != null) {
					project.loadedComponents.add(c);
				}
			} catch (Exception e) {
				System.out.println(type);
				System.out.println(project);
				System.out.println(n);
				e.printStackTrace();
			}
		}
		return c;
	}

	/**
	 * @return the component name
	 */
	public String getName() {
		return shadowNode.getSlotValue(nameAttr);
	}

	public final void setName(String name) {
		shadowNode.setSlotValue(nameAttr, name);
		setChanged();
		notifyObservers(name);
	}

	/**
	 * @return the class name of the component
	 */
	protected final String getType() {
		return shadowNode.getSlotValue(typeAttr);
	}

	/**
	 * assign the class name to type
	 * @param type
	 */
	private void setType(String type) {
		shadowNode.setSlotValue(typeAttr, type);
	}


	public Component getParentComponent(){
		IRNode p = projectTree.getParentOrNull(shadowNode);
		if (p!=null) return p.getSlotValue(project.compAttr);
		return null;
	}


	public IRNode getShadowNode(){
		return shadowNode;
	}

	/**
	 *
	 * @return project that this component belongs to
	 */
	public final Project getProject() {
		return project;
	}

	/**
	 * true if this component is shared (from another project)
	 * @return
	 */
	public boolean isShared(){
		return false;
	}

	/**
	 * shared node is the actual node from another project that is being
	 * shared.  It is not the same as the shadow node.
	 * @param node
	 */
	protected final void setSharedNode(IRNode node){
		this.shadowNode.setSlotValue(sharedNodeAttr,node);
	}

	protected final IRNode getSharedNode(){
		if (shadowNode.valueExists(sharedNodeAttr)) {
			return shadowNode.getSlotValue(sharedNodeAttr);
		}
		return null;

	}

	public Project getSharedProject(){
		return sharedProject;
	}

	public String getPath(){
		return project.getPath(this);
	}

	public String getParentPath(){
		return new File(project.getPath(this)).getParent();
	}
	/**
	 * shared project is the project where the node originates
	 * @param id
	 */
	protected final void setSharedProjectId(UniqueID id){
		if (shadowNode != null)
			shadowNode.setSlotValue(sharedProjectIdAttr, id.toString());
	}

	protected final void setSharedProjectId(String id){
		if (shadowNode != null) shadowNode.setSlotValue(sharedProjectIdAttr, id);
	}

	protected UniqueID getSharedProjectId(){
		if (shadowNode == null) return sharedRoot.getSharedProjectId();
		if (shadowNode.valueExists(sharedProjectIdAttr)) {
			return UniqueID.parseUniqueID(shadowNode.getSlotValue(sharedProjectIdAttr));
		}
		return null;
	}

	protected final void setSharedVersion(Version v){
		if (shadowNode != null) shadowNode.setSlotValue(sharedVersionAttr, v);
	}

	protected final Version getSharedVersion(){
		//if this is just an empty proxy component
		if (shadowNode == null) return sharedRoot.getSharedVersion();

		//if normal shared component
		if (shadowNode.valueExists(sharedVersionAttr)) {
			return shadowNode.getSlotValue(sharedVersionAttr);
		}
		return null;
	}

	/* return true if component represents a directory */
	public abstract boolean isDirectory();

	public abstract void store(Version v) throws IOException;

	public abstract void load(Version v) throws IOException;

	public abstract void unload();

	/*share current component */
	protected abstract SharedComponent makeSharedComponent(SharedComponent sharedRoot, Version v);
}
