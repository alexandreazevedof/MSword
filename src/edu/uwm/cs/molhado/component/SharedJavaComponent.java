/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.text.Reader;
import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class SharedJavaComponent extends JavaComponent implements SharedComponent{

	public SharedJavaComponent(Project toProject, DirectoryComponent toParent,
			  Component fromComp, Version fromVersion){
		super(toProject, toParent, "");
		sharedComponent = fromComp;
		sharedProject = fromComp.getProject();
		setSharedVersion(fromVersion);
		this.isSharedRoot = true;
		this.setSharedProjectId(sharedProject.getId());
		this.setSharedNode(fromComp.shadowNode);
		this.setSharedVersion(fromVersion);
	}

	protected SharedJavaComponent(SharedDirectoryComponent sharedRoot,
			  XmlComponent compToShare, Version fromVersion) {
		super();
		this.sharedRoot = sharedRoot;
		sharedComponent = compToShare;
		sharedProject = compToShare.getProject();
		sharedVersion = fromVersion;
	}

	public SharedJavaComponent(Project project, IRNode shadowNode) {
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

	public IRNode getRootNode() {
		loadSharedDelta();
		Version.setVersion(getSharedVersion());
		IRNode root = ((FileComponent) sharedComponent).getRootNode();
		Version.restoreVersion();
		return root;
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
	public String getContent() {
		Version.saveVersion(getSharedVersion());
		String content = ((FileComponent) sharedComponent).getContent();
		Version.restoreVersion();
		return content;
	}

	
	
	@Override
	public void dumpContent() {
		Version.saveVersion(getSharedVersion());
		((FileComponent) sharedComponent).dumpContent();
		Version.restoreVersion();
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
		IRNode sharedRootNode = ((XmlComponent) sharedComponent).getRootNode();
		if (sharedRootNode == null) {
			((XmlComponent) sharedComponent).setRootNode(Reader.readString(newContent));
		} else {
			Reader.update(sharedRootNode, newContent);
			((XmlComponent) sharedComponent).setRootNode(sharedRootNode);
		}
		Version v = VersionSupport.commitAsBranch(project.getName() + " branch", "");
		PlainIRNode.popCurrentRegion();
		Version.restoreVersion();
		setSharedVersion(v);
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
			IRNode root = ((SharedJavaComponent)sharedComponent).getRootNode();
			Version.restoreVersion();

			String s0 = Reader.getText(common, root);
			String s1 = Reader.getText(coreVersion, root);
			String s2 = Reader.getText(pversion, root);

			String s3 = Reader.merge3(s0, s1, s2);
		   Reader.update(root, s3);

			Version mv = VersionSupport.merge(coreVersion, pversion, "merge", "merge");
			setSharedVersion(mv);
			//case 2
		} else {
			System.out.println("CCCC");
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
			IRNode root = ((SharedJavaComponent)sharedComponent).getRootNode();
			Version.restoreVersion();
			String s0 = Reader.getText(common, root);
			String s1 = Reader.getText(coreVersion, root);
			String s2 = Reader.getText(pversion, root);

			String s3 = Reader.merge3(s0, s2, s1);
			Reader.update(root, s3);

			Version mv = VersionSupport.merge(pversion, coreVersion, "merge", "merge");
			setSharedVersion(mv);
			
			//case 2
		} else {
			System.out.println("CCCC");

		}
	}

	@Override
	public void exportToFile(File parent) {
		Version.saveVersion(getSharedVersion());
		((FileComponent) sharedComponent).exportToFile(parent);
		Version.restoreVersion();
	}

	@Override
	protected SharedComponent makeSharedComponent(SharedComponent sharedRoot, Version v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
