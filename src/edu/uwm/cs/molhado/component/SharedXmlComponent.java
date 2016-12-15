package edu.uwm.cs.molhado.component;

import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.merge.XmlDocMerge;
import edu.uwm.cs.molhado.version.VersionSupport;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class SharedXmlComponent extends XmlComponent implements SharedComponent {

	/**
	 * Create a shared XML document.
	 * @param toProject project component is being shared with
	 * @param toParent  directory shared component will be child of
	 * @param fromComp  component being shared
	 * @param fromVersion version component comes from
	 */
	public SharedXmlComponent(Project toProject, DirectoryComponent toParent,
					Component fromComp, Version fromVersion) {
		super(toProject, toParent, "");
		sharedComponent = fromComp;
		sharedProject = fromComp.getProject();
		setSharedVersion(fromVersion);
		this.isSharedRoot = true;
		this.setSharedProjectId(sharedProject.getId());
		this.setSharedNode(fromComp.shadowNode);
		this.setSharedVersion(fromVersion);
	}

	/**
	 * Create a proxy XML document component to be used in another project.
	 * This happens when a shared directory offers access to its children
	 * which are XML documents but since these documents resides in another
	 * project, another version, it must create a proxy for each of the children.
	 *
	 * @param sharedRoot
	 * @param compToShare
	 * @param fromVersion
	 */
	protected SharedXmlComponent(SharedDirectoryComponent sharedRoot,
					XmlComponent compToShare, Version fromVersion) {
		super();
		this.sharedRoot = sharedRoot;
		sharedComponent = compToShare;
		sharedProject = compToShare.getProject();
		sharedVersion = fromVersion;
	}

	/**
	 * Loading a shared component by factory method call
	 * @param project
	 * @param shadowNode
	 */
	public SharedXmlComponent(Project project, IRNode shadowNode) {
		super(project, shadowNode);
		sharedProject = Project.findProject(getSharedProjectId());
		isSharedRoot = true;
		IRNode sharedNode = getSharedNode();
		loadSharedDelta();
		Version.saveVersion(getSharedVersion());
		sharedComponent = sharedProject.getComponent(sharedNode);
		Version.restoreVersion();
	}

	@Override
	public String getName() {
		loadSharedDelta();
		String name = "";
		Version.saveVersion(getSharedVersion());
		name = sharedComponent.getName();
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
	public void exportToFile(File parent) {
		Version.saveVersion(getSharedVersion());
		((FileComponent) sharedComponent).exportToFile(parent);
		Version.restoreVersion();
	}


	@Override
	public String getContent(){
		ensureLoaded();
		Version.saveVersion(getSharedVersion());
		String content = ((XmlComponent)sharedComponent).getContent();
		Version.restoreVersion();
		return content;
	}

	@Override
	public void dumpContent() {
		Version.saveVersion(getSharedVersion());
		((FileComponent) sharedComponent).dumpContent();
		Version.restoreVersion();
	}

	@Override
	public IRNode getRootNode() {
		loadSharedDelta();
		Version.setVersion(getSharedVersion());
		IRNode root = ((FileComponent) sharedComponent).getRootNode();
		Version.restoreVersion();
		return root;
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

	public void updateContent(String newContent) throws Exception {
		Version.saveVersion(getSharedVersion());
		PlainIRNode.pushCurrentRegion(sharedProject.getRegion());
		parser.setMouid(sharedComponent.getShadowNode().getSlotValue(mouidAttr));
		IRNode sharedRootNode = ((XmlComponent) sharedComponent).getRootNode();
		if (sharedRootNode == null) {
			((XmlComponent) sharedComponent).setRootNode(parser.parse(newContent));
		} else {
			IRNode n = parser.parse(sharedRootNode, newContent);
			((XmlComponent) sharedComponent).setRootNode(n);
		}
		sharedComponent.getShadowNode().setSlotValue(mouidAttr, parser.getMouid());
		Version v = VersionSupport.commitAsBranch(project.getName() + " branch", "");
		PlainIRNode.popCurrentRegion();
		Version.restoreVersion();
		setSharedVersion(v);
	}

	/**
	 * this method can be moved up
	 * Make this shared component to point to the previous version of core asset
	 * cases:
	 *   core   prod       core   prod
	 * |  a'  |  a*  | => |  a'  |  a  |
	 */
	public void revertVersionInProduct() {
		Version local = Version.getVersionLocal();
		Version.saveVersion(local);
		Version shared = getSharedVersion();
		Version parent = VersionSupport.getParentVersion(shared);
		Version.restoreVersion();
		this.setSharedVersion(parent);
	}

	//this method can be move up to component
	public void forwardChangeWithOverride() {

		Version coreVersion = VersionSupport.getDeepestDescendentInTrunk(
						sharedProject.getInitialVersion());
		//Version parent = VersionSupport.getParentVersion(getSharedVersion());
		Version pversion = getSharedVersion();
		Version common = Version.latestCommonAncestor(pversion, coreVersion);

		if (common == coreVersion) {
			//nothing to do
		} else if (common == pversion || (common != coreVersion && common != pversion)) {
			this.setSharedVersion(coreVersion);
		}
	}

	public void forwardChange() {

		Version coreVersion = VersionSupport.getDeepestDescendentInTrunk(
						sharedProject.getInitialVersion());
		//Version parent = VersionSupport.getParentVersion(getSharedVersion());
		Version pversion = getSharedVersion();
		Version common = Version.latestCommonAncestor(pversion, coreVersion);

		if (getSharedVersion() == common) {
			//case 1
			this.setSharedVersion(coreVersion);
		} else if (common != getSharedVersion() || common != coreVersion) {

			Version.saveVersion(common);
			IRNode root = ((XmlComponent) sharedComponent).getRootNode();
			Version.restoreVersion();

			XmlDocMerge merge = new XmlDocMerge(SimpleXmlParser3.tree, SimpleXmlParser3.changeRecord,
							root, new VersionMarker(coreVersion), new VersionMarker(common),
							new VersionMarker(pversion));
			Version mergedVersion = merge.merge();
			setSharedVersion(mergedVersion);
			//case 2
		} else {
			System.out.println("CCCC");
		}
	}

	/**
	 * update core asset with changes to this component
	 * cases:
	 *   core   prod       core   prod
	 * |  a'  |  a  | => |  a'  |  a'  |
	 * |  a'  |  a* | => |  a'  |  a*' |
	 */
	public void backwardChangeWithOverride() {
		try {
			Version coreVersion = VersionSupport.getDeepestDescendentInTrunk(
							sharedProject.getInitialVersion());
			String content = ((XmlComponent) sharedComponent).getContent();
			Version.saveVersion(coreVersion);
			((XmlComponent) sharedComponent).updateContent(content);
			VersionSupport.commit("", "");
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}

	}

	public void backwardChangePropagation() {
		Version coreVersion = VersionSupport.getDeepestDescendentInTrunk(
						sharedProject.getInitialVersion());
		Version pversion = getSharedVersion();
		Version common = Version.latestCommonAncestor(pversion, coreVersion);
		if (coreVersion == common) {
			//case 1
			//this.setSharedVersion(coreVersion);
			//VersionSupport.putInTrunk(getSharedVersion());
			VersionSupport.putPathInTrunk(common, getSharedVersion());
		} else if (common != getSharedVersion() || common != coreVersion) {
			//case 2
			Version.saveVersion(common);
			IRNode root = ((XmlComponent) sharedComponent).getRootNode();
			Version.restoreVersion();

			XmlDocMerge merge = new XmlDocMerge(SimpleXmlParser3.tree, SimpleXmlParser3.changeRecord,
							root, new VersionMarker(pversion), new VersionMarker(common),
							new VersionMarker(coreVersion));
			Version mergedVersion = merge.merge();
			//case 2
		} else {
			System.out.println("CCCC");

		}
	}
}