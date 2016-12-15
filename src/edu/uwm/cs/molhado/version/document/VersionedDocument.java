package edu.uwm.cs.molhado.version.document;

import de.schlichtherle.io.File;
import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.Era;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChunk;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.uwm.cs.molhado.util.IRObjectInputStream;
import edu.uwm.cs.molhado.util.IRObjectOutputStream;
import edu.uwm.cs.molhado.util.TrueZipFileLocator;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Tricky Situations:
 *
 * 1.  Interaction of VersionedRegion and Version/version tree
 * Interaction of VersionedRegion and version tree.  When a new VersionedRegion
 * is created, it automatically increment and create a new initial version. This
 * can be a problem when loading.  If we create a VersionedRegion before loading,
 * we will ended with more versions than we saved.  Hence, we must avoid creating
 * VersionedRegion if not needed.
 *
 * 2.  Clearing the version tree (there's no way to clear a version
 * tree in memory.  We just have to restore it so that our new
 * version will be a child of the root which is version 0. The new version create
 * will start with the highest version number which is static.  Once we saved
 * and load in the next session, the version ID will start at 1.
 *
 * @author chengt
 */
public abstract class VersionedDocument extends FluidRegistryLoading{
	protected TrueZipFileLocator floc;
	protected IRNode rootNode;
	protected Version previousVersion;
	protected Version latestVersion;
	protected VersionedRegion region;
	protected boolean isNew = true;
	private final static String docInfoFile = "doc.info";
	protected int lastUsedId = 0;
	private static ArrayList<Era> eras = new ArrayList<Era>();

	private String name;

	public VersionedDocument(){
		this.name = "untitled";
		
		//need this so new version will be child of root version
		if (Version.getTotalVersions() > 1) Version.restoreVersion();
		//has to come before restore version
		region = new VersionedRegion();
		PlainIRNode.setCurrentRegion(region);
		previousVersion = Version.getInitialVersion();
		latestVersion = previousVersion;
		isNew = true;
	}

	//existing file
	protected VersionedDocument(String path) throws IOException{
		this.floc = new TrueZipFileLocator(path);
		isNew = false;
		this.name = path.substring(0, path.lastIndexOf("."));
		load();
	}

	//don't use.  although eras are unloaded, versions still exist
	//in memory.  this will cause an exeption when saving 
	//(versions must have eras to be serialized)
	private void unloadEras(){
		for(Era e:eras) e.unload();
		eras.clear();
	}

	public final String getName(){
		return name;
	}

	public final boolean isNew(){ return isNew; }

	public abstract String getContent();

	public final String getContent(Version v) {
		Version.saveVersion(v);
		String content = getContent();
		Version.restoreVersion();
		return content;
	}

	public ArrayList<Version> getVersions(){
		ArrayList<Version> list = new ArrayList<Version>();
		for(Version v=latestVersion; v != Version.getInitialVersion(); v=v.parent()){
			list.add(v);
		}
		return list;
	}

	protected abstract void updateContent(String newContent) throws Exception;

	protected abstract Bundle getBundle();

	//existing file
	public final void save(String newContent, String path, String name) throws Exception{
		updateContent(newContent);
		this.name = name;
		floc = new TrueZipFileLocator(path + "/" + name);
		store();
	}

	//save as or file is new
	public final void save(String newContent) throws Exception{
		if (floc == null) throw new RuntimeException("FileLocator is NULL");
		updateContent(newContent);
		store();
	}

	private final void store() throws IOException {
		Era era = null;
		//latestVersion = Version.getVersion();
		latestVersion = VersionSupport.commit("","");
		try {
			era = new Era(previousVersion, new Version[]{latestVersion});
			Version.printVersionTree();
			era.store(floc);
			eras.add(era);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		VersionSupport.storeEraShadowRegion(era, floc);

		IRObjectOutputStream out = floc.getObjectOutputStream(docInfoFile);
		out.writeVersion(latestVersion);
		out.writeVersionedRegion(region, floc);
		out.writeNode(rootNode);
		out.writeInt(getLastUsedId());
		out.close();
		VersionedChunk.get(region, getBundle()).getDelta(era).store(floc);
		isNew = false;
		previousVersion = latestVersion;
		Version.printVersionTree();
	}

	private void load() throws IOException{
		loadAllErasFiles();
		IRObjectInputStream in = floc.getObjectInputStream(docInfoFile);
		latestVersion = previousVersion = in.readVersion();
		Version.setVersion(latestVersion);
		region = in.readVersionedRegion(floc);
		PlainIRNode.setCurrentRegion(region);
		rootNode = in.readNode();
		lastUsedId=in.readInt();
		in.close();
		IRLoadingUtil.load(region, getBundle(), previousVersion, floc);
		Version.printVersionTree();
	}

	//load all era files.  era files describe the version tree
	private void loadAllErasFiles() throws IOException {
		for (File f : floc.listFiles(".era")) {
			String fname = f.getName();
			String eraId = fname.substring(0, fname.lastIndexOf('.'));
			Era e = Era.loadEra(UniqueID.parseUniqueID(eraId), floc);
			eras.add(e);
			VersionSupport.loadEraShadowRegion(e, floc);
		}
	}

	protected abstract int getLastUsedId();

}
