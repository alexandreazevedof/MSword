package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.util.FileLocator;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.*;
import edu.uwm.cs.molhado.util.*;
import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.uwm.cs.molhado.version.VersionSupport;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.util.Exceptions;

/**
 * @author chengt
 */
public class Project extends FluidRegistryLoading {

	protected final static HashMap<UniqueID, Project> idProjectMapping = new HashMap<UniqueID, Project>();
	protected final static HashMap<File, Project> pathProjectMapping = new HashMap<File, Project>();
	protected static HashMap<String, Version> versions = new HashMap<String, Version>();
	private HashMap<Version, Boolean> loaded = new HashMap<Version, Boolean>();
	/* name of project description file */
	private final static String PROJ_FILE = "proj.info";

	/* name of project */
	private String name;

	/* unique id of this project */
	private final UniqueID id;

	/* path to project */
	private File path;

	/* location of storage for project */
	protected TrueZipFileLocator floc;

	/* true if name has been changed during session */
	private boolean nameChanged = false;

	/* true if project is new, false for loaded project */
	/* demand loading only load delta if project is not new */
	private boolean isNew = true;
	private boolean isValid = true;
	private boolean isChanged = false;

	/* A derived attribute that gives a component */
	protected final SlotInfo<Component> compAttr = new ComponentSlotInfo(this);

	/* pseudo root component in project */
	private final DirectoryComponent rootComponent;

	/* intial version for project */
	private final Version initialVersion;

	/* region holding project nodes */
	protected final VersionedRegion projRegion;

	/* new versions are added to this list and clear when stored. */
	protected final ArrayList<Version> newVersions = new ArrayList<Version>();

	/* new components, list is cleared after they are stored */
	private final ArrayList<Component> newComponents = new ArrayList<Component>();
	protected final ArrayList<Component> loadedComponents = new ArrayList<Component>();

	public Project() {
		id = new UniqueID();
		this.name = "untitle";
		Version.saveVersion(Version.getInitialVersion());
		projRegion = new VersionedRegion();
		rootComponent = new DirectoryComponent(this, null, "untitle");
		initialVersion = this.commitAsBranch("1.0", "1.0");
		Version.restoreVersion();

		isNew = true;
		idProjectMapping.put(id, this);
		isChanged = true;
	}

	/**
	 * Constructor for creating a new project.
	 * @param path
	 * @param name
	 */
	public Project(File path, String name) {
		this.path = path;
		id = new UniqueID();
		this.name = name;
		Version.saveVersion(Version.getInitialVersion());
		projRegion = new VersionedRegion();
		rootComponent = new DirectoryComponent(this, null, name);
		initialVersion = this.commitAsBranch(name + " initial", "initial version");
		subClassInit();
		Version.restoreVersion();

		floc = new TrueZipFileLocator(path);
		isNew = true;
		idProjectMapping.put(id, this);
		pathProjectMapping.put(path, this);
		isChanged = true;
	}

	void subClassInit(){}

	/**
	 * Load a project given the path.  This constructor is used for loading
	 * existing project from persistent.
	 * @param path
	 * @throws java.io.IOException
	 */
	protected Project(File path) throws IOException {
		this.path = path;
		isNew = false;  //this must be initialize for demand loading to work
		floc = new TrueZipFileLocator(path);
		loadAllErasFiles(floc);

		IRObjectInputStream in = floc.getObjectInputStream(PROJ_FILE);
		id = in.readUniqueId();
		name = in.readUTF();
		projRegion = in.readVersionedRegion(floc);
		initialVersion = in.readVersion();
		IRNode rootNode = in.readNode();
		in.close();

		Version.saveVersion(initialVersion);
		this.rootComponent = (DirectoryComponent) getComponent(rootNode);
		Version.restoreVersion();

		idProjectMapping.put(id, this);
		pathProjectMapping.put(path, this);
	}

	public static Project[] getAllProjects() {
		return idProjectMapping.values().toArray(new Project[0]);
	}

	public static Project findProject(UniqueID id) {
		return idProjectMapping.get(id);
	}

	/**
	 * Load an exist project from file.
	 * @param path The path of directory or zip file containing the project
	 * @return  The project loaded from file
	 * @throws java.io.IOException
	 */
	public static Project load(File path) throws IOException {
		Project p = pathProjectMapping.get(path);
		if (p == null) {
			p = new Project(path);
		}
		return p;
	}

	public boolean isChanged() {
		return isChanged;
	}

	public String getName() {
		return name;
	}

	public UniqueID getId() {
		return id;
	}

	public File getPath() {
		return path;
	}

	public VersionedRegion getRegion(){
		return projRegion;
	}

	public void setPath(File path) {
		this.path = path;
		floc = new TrueZipFileLocator(path);
	}

	public void setName(String name) {
		this.name = name;
		nameChanged = true;
		isChanged = true;
	}

	public void loadVersion(Version v) {
		if (loaded.get(v)) {
			return;
		}
		try {
			IRLoadingUtil.load(projRegion, Component.projectBundle, v, floc);
			loaded.put(v, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Version getLatestVersion() {
		return VersionSupport.getDeepestDescendentInTrunk(initialVersion);
	}

	public DirectoryComponent createDirectory(String name) {
		isChanged = true;
		return createDirectory(rootComponent, name);
	}

	public DirectoryComponent createDirectory(DirectoryComponent parent, String name) {
		isChanged = true;
		return new DirectoryComponent(this, parent, name);
	}

	public XmlComponent createXmlDocument() {
		SimpleXmlParser3 p = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);
		IRNode root = null;
		try {
			root = p.parse("<doc/>");
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}
//		XmlComponent c = new XmlComponent(this, rootComponent, root);

		throw new UnsupportedOperationException("Has yet to be implemented");
	}

	/**
	 * concate all the terms with / and return it.
	 *
	 * @param parts
	 * @return
	 */
	private String buildPath(String[] parts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < parts.length - 1; i++) {
			sb.append(parts[i]);
			sb.append("/");
		}
		sb.append(parts[parts.length - 1]);
		//System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * Get component of given path at given version
	 * @param version
	 * @param path
	 * @return component or null if not found
	 */
	public Component getComponent(Version version, String path) {
		Version.saveVersion(version);
		Component c = getComponent(path);
		Version.restoreVersion();
		return c;
	}

	/**
	 * return component that is specified by the path
	 * @param path the path
	 * @return component if found.  Else null.
	 */
	public Component getComponent(String path) {
		//TODO: use substring instead of split
		if (path == null) {
			return null;
		}
		if (path.equals("/")) {
			return this.getRootComponent();
		}
		String[] parts = path.split("/");
		return getComponent(this.getRootComponent(), buildPath(parts));
	}

	/**
	 * Given a path, return the component specified by that path
	 * @param parent  current directory
	 * @param path  the path
	 * @return  the component if exists or null
	 */
	public Component getComponent(DirectoryComponent parent, String path) {
		if (path.trim().isEmpty()) {
			return null;
		}
		String[] parts = path.split("/");
		Component child = findChild(parent, parts[0]);
		if (child == null) {
			return null;
		}
		if (child instanceof DirectoryComponent && parts.length > 1) {
			return getComponent((DirectoryComponent) child, buildPath(parts));
		} else if (parts.length == 1) {
			return child;
		}
		return null;
	}

	/**
	 * Return the child with the given name
	 * @param parent
	 * @param child
	 * @return the child or null if not found
	 */
	public Component findChild(DirectoryComponent parent, String child) {
		Component[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (child.equals(children[i].getName())) {
				return children[i];
			}
		}
		return null;
	}

	public void mkdir(DirectoryComponent parent, String path){
		String[] r = path.split("/");
		if (r.length == 0) return;
		Component[] children = parent.getChildren();
		path = path.substring(path.indexOf("/")+1);
		for(Component c:children){
			if (c.getName().equals(r[0])){
				if (c instanceof DirectoryComponent){
					mkdir((DirectoryComponent)c, path);
					return ;
				}
			}
		}

		DirectoryComponent dir = createDirectory(parent, r[0]);
		if (r.length == 1) return;
		System.out.println(path);
		mkdir(dir, path);
	}
	public void mkdir(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		mkdir(getRootComponent(), path);
	}

	protected void describe() {
	}

	protected FileLocator getFileLocator() {
		return floc;
	}

	public Version getInitialVersion() {
		return initialVersion;
	}

	private void ensureLoaded() {
//		Boolean isLoaded = loaded.get(Version.getVersionLocal());
//		boolean loaded = false;
//		if (isLoaded != null && isLoaded.equals(true)){
//			loaded = true;
//		}
		if (isNew ) {
			return;
		}
		IRLoadingUtil.load(projRegion, Component.projectBundle, floc);
	}

	protected Component getComponent(IRNode node) {
		ensureLoaded();
		return node.getSlotValue(compAttr);
	}

	public ArrayList<Component> getAllComponents(){
		Iteratable<IRNode> it = Component.projectTree.topDown(getRootComponent().shadowNode);
		ArrayList<Component> list = new ArrayList<Component>();
		while(it.hasNext()){
			IRNode n = it.next();
			Component c = n.getSlotValue(compAttr);
			list.add(c);
		}
		return list;
	}

	public String getPath(Component c){
		Iteratable<IRNode> it = Component.projectTree.rootWalk(c.shadowNode);
		ArrayList<String> names = new ArrayList<String>();
		while(it.hasNext()){
			IRNode n = it.next();
			if (n != getRootComponent().getShadowNode()){
				Component x = getComponent(n);
				names.add(x.getName());
			}
		}
		String path = "";
		Collections.reverse(names);
		for(String name:names){
			path =  path + "/" + name;
		}
		return path;
	}

	private void createEraForVersion(Version v) {
		isChanged = true;
		try {
			Era e = new Era(v.parent(), new Version[]{v});
		} catch (OverlappingEraException ex) {
		} catch (DisconnectedEraException ex) {
		}
	}

	public final Version commitAsBranch(String tag, String log) {
		isChanged = true;
		Version v = VersionSupport.commitAsBranch(tag, log);
		createEraForVersion(v);
		newVersions.add(v);
		return v;
	}

	public final Version commit(String tag, String log) {
		isChanged = true;
		Version v = VersionSupport.commit(tag, log);
		createEraForVersion(v);
		newVersions.add(v);
		return v;
	}

	public DirectoryComponent getRootComponent() {
		ensureLoaded();
		return rootComponent;
	}

	protected void addComponent(Component aThis) {
		isChanged = true;
		newComponents.add(aThis);
	}

	public void removeComponent(Component c) {
		if (c.getParentComponent() == null) {
			System.out.println("paent == null");
		}
		((DirectoryComponent) c.getParentComponent()).removeChild(c);
	}

	public void storeVersion(Version v) throws IOException {
		IRLoadingUtil.store(projRegion, Component.projectBundle, v, floc);
	}

	protected void storeNewDelta() throws IOException {
		for (Version v : newVersions) {
			Era e = v.getEra();
			e.store(floc);
			VersionSupport.storeEraShadowRegion(e, floc); //delta for version tree
			VersionedChunk vc = VersionedChunk.get(projRegion, Component.projectBundle);
			VersionedDelta vd = vc.getDelta(e); //delta for project tree
			vd.store(floc);

			//store component delta
			for (Component c : newComponents) {
				System.out.println("saving component " + c);
				c.store(v);
			}
			storeOptionalNewDelta(v);
		}
		newVersions.clear();
	}

	/**
	 * Store a project file description.  The description file contains the
	 * initial version, and the root IR node representing the root component
	 * in the project.  The project file is created once when the project was
	 * first stored.  If the project had been stored before, only the new
	 * deltas are stored.
	 * 
	 * @throws java.io.IOException
	 */
	public void store() throws IOException {
		if (floc == null) {
			throw new RuntimeException("Need a location to save to");
		}

		if (isNew || nameChanged || floc.locateFile(PROJ_FILE, false) == null) {
			//only need to create a porject description file for new project
			IRObjectOutputStream out = floc.getObjectOutputStream(PROJ_FILE);
			out.writeUniqueId(id);
			out.writeUTF(name);
			out.writeVersionedRegion(projRegion, floc);
			storeNewDelta();
			out.writeVersion(initialVersion);
			out.writeNode(rootComponent.shadowNode);
			out.close();
			storeOptional();
		} else {
			//only need to store delta for old project
			storeNewDelta();
		}
		isNew = false;
	}


	protected void storeOptionalNewDelta(Version v){

	}

	protected void storeOptional(){

	}

	public boolean isValid() {
		return isValid;
	}

	public void close() {
		idProjectMapping.remove(id);
		pathProjectMapping.remove(getPath());
		projRegion.unload();
		newComponents.clear();
		loadedComponents.clear();
		isValid = false;
	}

	/**
	 * Load all eras found at floc.  Potential problem is that it may attempt 
	 * to load files with .era exten that are not reall era files or files from
	 * a different project that was not deleted.
	 *
	 * @param floc
	 * @throws java.io.IOException
	 */
	private void loadAllErasFiles(TrueZipFileLocator floc) throws IOException {
		for (File f : floc.listFiles(".era")) {
			String fname = f.getName();
			System.out.println("Loading era: " + fname);
			String eraId = fname.substring(0, fname.lastIndexOf('.'));
			Era e = Era.loadEra(UniqueID.parseUniqueID(eraId), floc);
			VersionSupport.loadEraShadowRegion(e, floc);
		}
	}

	/**
	 * Import files and append them as children of the root component
	 * @param file
	 * @return
	 * @throws java.io.IOException
	 */
	public Component importFile(File file) throws IOException {
		return importFile(rootComponent, file);
	}

	public void importFiles(File[] files) throws IOException{
		for(File f:files){
			importFile(rootComponent, f);
		}
	}

	/**
	 * Import files recursively
	 * @param parent  Parent will new file will be appended to
	 * @param file    The file to import
	 * @return        The component representing the imported file
	 * @throws java.io.IOException
	 */
	public Component importFile(DirectoryComponent parent, File file)
					throws IOException {
		Component component = null;
		if (file.isDirectory()) {
			component = new DirectoryComponent(this, parent, file.getName());
			for (File f : file.listFiles()) {
				Component c = importFile((DirectoryComponent) component, f);
			}
		} else if (file.getName().endsWith(".java")) {
			component = new JavaComponent(this, parent, file);
		} else if (file.getName().endsWith(".xml")
						|| file.getName().endsWith(".svg")
						|| file.getName().endsWith("dita")
						|| file.getName().endsWith("ditamap")) {
			component = new XmlComponent(this, parent, file);
		} else {
			throw new IOException(file.getName() + ":File type is not supported.");
		}
		return component;
	}

	public void unloade() {
		for (Component c : loadedComponents) {
			c.unload();
			projRegion.unload();
		}
		loadedComponents.clear();
		newVersions.clear();
		newComponents.clear();
	}

	public void exportToFile(File path, Version v) {
		if (path.exists() && !path.isDirectory()) {
			throw new RuntimeException("File exist and not directory.");
		}
		path.mkdir();
		Version.saveVersion(v);
		path = new File(path, name);
		if (path.exists()){
			if (path.listFiles().length > 0) path.delete();
		}
		path.mkdir();
		exportComponents(path, getRootComponent());
		Version.restoreVersion();
	}

	private void exportComponents(File path, Component comp) {
		Component[] children = null;
		if (comp.isDirectory()) {
			children = ((DirectoryComponent) comp).getChildren();
			for (Component c : children) {
				if (c instanceof DirectoryComponent) {
					File dir = new File(path, c.getName());
					if (!dir.exists()) {
						dir.mkdir();
					}
					exportComponents(dir, c);
				} else {
					FileComponent f = (FileComponent) c;
					f.exportToFile(path);
				}
			}
		} else {
			FileComponent f = (FileComponent) comp;
			f.exportToFile(path);
		}
	}

	//===debug functions ====
	public void printVersionTree() {
		System.out.println("===Version Tree===");
		printVersionTree(0, initialVersion);
	}

	private void printVersionTree(int indent, Version v) {
		System.out.println(spaces(indent) + VersionSupport.getVersionNumber(v));
		for (Version c : VersionSupport.getChildren(v)) {
			printVersionTree(indent + 2, c);
		}
	}

	public void printProjectTree() {
		System.out.println("==Project Tree==");
		printProjectTree(initialVersion);
	}

	public void printProjectTreeAtVersion(Version v) {
		System.out.println("--project at version " + VersionSupport.getVersionNumber(v));
		printProjectTree(0, v, getRootComponent());
	}

	private void printProjectTree(Version v) {
		System.out.println("---project tree at version " + VersionSupport.getVersionNumber(v));
		Version.saveVersion(v);
		Component root = getRootComponent();
		Version.restoreVersion();
		printProjectTree(0, v, root);
		for (Version childVersion : VersionSupport.getChildren(v)) {
			printProjectTree(childVersion);
		}
	}

	private void printProjectTree(int indent, Version v, Component comp) {
		Version.saveVersion(v);
		System.out.println(spaces(indent) + comp.getName());
		Component[] children = null;
		if (comp.isDirectory()) {
			if (comp instanceof SharedDirectoryComponent) {
				//	System.out.println("yes for " + comp.getName());
				children = ((SharedDirectoryComponent) comp).getChildren();
			} else {
				children = ((DirectoryComponent) comp).getChildren();
			}
			for (Component c : children) {
				if (c == null) {
					System.out.println("c == null");
				}
				printProjectTree(indent + 2, v, c);
			}
		} else {
			FileComponent f = (FileComponent) comp;
			f.dumpContent();
		}
		Version.restoreVersion();
	}

	private String spaces(int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}
}
