package edu.uwm.cs.molhado.spl;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.Component;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.JavaComponent;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.SharedComponent;
import edu.uwm.cs.molhado.component.XmlComponent;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class ProductLine extends Observable implements Serializable {

	private transient static final long serialVersionUID = 1l;
	private String name;
	private String path;
	private CoreAssetProject coreAssetProject;
	private ArrayList<ProductProject> productProjects = new ArrayList<ProductProject>();

	/**
	 * For loading a project
	 */
	private ProductLine() {
	}

	/**
	 * Creating a new project
	 * @param path
	 * @param name
	 */
	public ProductLine(String path, String name) {
		this.path = path + "/" + name;
		this.name = name;
		coreAssetProject = new CoreAssetProject(new File(this.path, "core"), "core");
	}

	public ProductProject createProduct(String name) {
		ProductProject project = null;
		project = new ProductProject(new File(path, name), name);
		productProjects.add(project);
		notifyObservers(project);
		return project;
	}

	public ArrayList<Project> getAllProjects(){
		ArrayList<Project> projects = new ArrayList<Project>();
		projects.add(coreAssetProject);
		projects.addAll(productProjects);
		return projects;
	}

	private ArrayList<String> collectDitaLinks(IRNode node ) {
		ArrayList<String> links = new ArrayList<String>();
		Iteratable<IRNode> it = SimpleXmlParser3.tree.topDown(node);
		while (it.hasNext()) {
			IRNode n = it.next();
			String nodeType = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
			if (nodeType.equals("char")) continue;
			String link = SimpleXmlParser3.getAttributeValue(n, "href");
			if (link == null) {
				continue;
			}
			links.add("/" + link); //TODO: hard code ditasampel
		}
		return links;
	}

	public ProductProject createJavaProduct(Version coreVersion, String name, ArrayList<String> files){
		ArrayList<Component> comps = new ArrayList<Component>();
		Version.saveVersion(coreVersion);
		for(String f:files){
			Component c = coreAssetProject.getComponent(f);
			comps.add(c);
			System.out.println(f);
			if (c instanceof JavaComponent) {((JavaComponent)c).dumpContent();};
		}
		
		Version.restoreVersion();

		ProductProject p = new ProductProject(new File(path, name), name);

		Version.setVersion(p.getInitialVersion());
		for (int i = 0; i < comps.size(); i++) {
			Component c = comps.get(i);
			String f = files.get(i);
			String parentPath = new File(f).getParent();
			DirectoryComponent dir = (DirectoryComponent) p.getComponent(parentPath);
			//System.out.println(parentPath);
			if (dir == null) {
				p.mkdir(parentPath);
			  dir = (DirectoryComponent) p.getComponent(parentPath);
			}
			if (p.getComponent(f) == null) //must be shared only once
			{
				SharedComponent sharedComp = coreAssetProject.share(c, coreVersion, p, dir);
//				System.out.println(p.getComponent(link).getPath());
				//System.out.print(link);
				if (sharedComp == null) System.out.print(" is null");
				//System.out.println();
			}
		}
		p.commit(name + " v1", "initial creation");
		productProjects.add(p);
		setChanged();
		notifyObservers(p);
		System.out.println("Notify observers");
		return p;
	}

	/**
	 * Create a dita project with the given name and given ditamap
	 * @param name
	 * @param ditamap
	 * @return return a product
	 */
	public ProductProject createDitaProduct(Version coreVersion, String name, XmlComponent ditamap) {
		Version.saveVersion(coreVersion);
		System.out.println("===========" + ditamap.getName() + "=============");
		ArrayList<String> links = new ArrayList<String>();
		links = collectDitaLinks(ditamap.getRootNode());
//		for(String l:links){
//			System.out.println(l);
//		}
//		System.out.println("============================================");
		ArrayList<Component> comps = new ArrayList<Component>();
		ArrayList<String> allLinks = new ArrayList<String>();
		for (String l : links) {
			Component c = coreAssetProject.getComponent(l);
			comps.add(c);
			allLinks.add(l);
			//		System.out.println(c.getName());
			//		System.out.println(l);
			System.out.println(coreAssetProject.getPath(c));
			if (c instanceof XmlComponent) {
				//get all dependencies
				ArrayList<String> refs = getLinks(coreAssetProject, (XmlComponent) c);
				//allLinks.addAll(refs);
				for (String r : refs) {
					File f = new File(c.getParentPath(), r);
					try {
						//System.out.println("==>" + c.getName() + " : " + r);
						//System.out.println(f.getCanonicalPath());
						//String s = r.substring(r.indexOf("/"), r.length());
						//System.out.println(" : " + s);
						c = coreAssetProject.getComponent(f.getCanonicalPath());
						allLinks.add(f.getCanonicalPath());
						//		System.out.println(f.getCanonicalPath());
					//	System.out.println(c.getPath());
						comps.add(c);
					} catch (IOException ex) {
						Exceptions.printStackTrace(ex);
					}
				}
			}
		}
		Version.restoreVersion();

		ProductProject p = new ProductProject(new File(path, name), name);
		Version.setVersion(p.getInitialVersion());
		for (int i = 0; i < comps.size(); i++) {
			Component c = comps.get(i);
			String link = allLinks.get(i);
			String parentPath = new File(link).getParent();
			DirectoryComponent dir = (DirectoryComponent) p.getComponent(parentPath);
			//System.out.println(parentPath);
			if (dir == null) {
				p.mkdir(parentPath);
			  dir = (DirectoryComponent) p.getComponent(parentPath);
			}
			if (p.getComponent(link) == null) //must be shared only once
			{
				SharedComponent sharedComp = coreAssetProject.share(c, coreVersion, p, dir);
//				System.out.println(p.getComponent(link).getPath());
				//System.out.print(link);
				if (sharedComp == null) System.out.print(" is null");
				//System.out.println();
			}
		}
		coreAssetProject.share(ditamap, coreVersion, p, p.getRootComponent());
		p.commit(name + " v1", "initial creation");
		productProjects.add(p);
		setChanged();
		notifyObservers(p);
		System.out.println("Notify observers");
		return p;
	}

	public ArrayList<String> getLinks(CoreAssetProject p, Version cv, XmlComponent comp) {
		ArrayList<String> links = new ArrayList<String>();
		Version.saveVersion(cv);
		links = getLinks(p, comp);
		Version.restoreVersion();
		return links;
	}

	public ArrayList<String> getLinks(CoreAssetProject p, XmlComponent comp) {
		ArrayList<String> links = new ArrayList<String>();
		Iteratable<IRNode> it = SimpleXmlParser3.tree.topDown(comp.getRootNode());
		while (it.hasNext()) {
			IRNode n = it.next();
			String name = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
			if (name.equals("link")) {
				String link = SimpleXmlParser3.getAttributeValue(n, "href");
				if (link == null) continue;
				links.add(link);
			}
		}
		return links;
	}

	public CoreAssetProject getCoreAsset() {
		return coreAssetProject;
	}

	public ProductProject[] getProducts() {
		return productProjects.toArray(new ProductProject[productProjects.size()]);
	}

	public ProductProject getProduct(String name) {
		for (ProductProject p : productProjects) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public void store() throws IOException {
		//need to make sure such path exists.  If not, create it.
		File f = new File(path);
		if (f.exists() && !f.isDirectory()) {
			throw new IOException(name + "exists and not a directory");
		} else if (!f.exists()) {
			boolean success = f.mkdir();
			if (!success) {
				throw new IOException("Fail to create directory " + name);
			}
		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(path, "pl.info")));
		out.writeUTF(name);
		out.writeUTF(path);
		out.writeUTF(coreAssetProject.getPath().toString());
		out.writeInt(productProjects.size());
		coreAssetProject.store();
		for (Project product : productProjects) {
			out.writeUTF(product.getPath().toString());
			product.store();
			System.out.println("Storing product:" + product.getName());
		}
		out.close();
	}

	public static ProductLine load(File path) throws IOException, ClassNotFoundException {
		System.out.println(path.toString());
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path + "/pl.info"));
		ProductLine pl = new ProductLine();
		pl.name = in.readUTF();
		System.out.println("ProductLine: " + pl.name);
		pl.path = in.readUTF();
		String cpath = in.readUTF();
		pl.loadCoreAssetProject(new File(cpath));
		int numProducts = in.readInt();
		for (int i = 0; i < numProducts; i++) {
			String ppath = in.readUTF();
			System.out.println("Product: " + ppath);
			pl.loadProduct(new File(ppath));
		}
		in.close();
		return pl;
	}

	private void loadCoreAssetProject(File path) throws IOException {
		coreAssetProject = CoreAssetProject.load(path);
	}

	private void loadProduct(File path) throws IOException {
		ProductProject p = ProductProject.load(path);
		productProjects.add(p);
	}

	public void describe() {
		System.out.println("ProductLine name: " + name);
		System.out.println("Storage path    : " + path);
		System.out.println("==============================");
		System.out.println("Core asset project tree");
		coreAssetProject.printProjectTree();

		System.out.println("===============================");
		System.out.println("Number of products: " + productProjects.size());
		for (int i = 0; i < productProjects.size(); i++) {
			productProjects.get(i).printProjectTree();
		}
	}

	private Object writeReplace() {
		return new SerializeHelper();
	}

	class SerializeHelper implements Serializable {

		ProductLine pl = new ProductLine();

		SerializeHelper() {
		}

		private void writeObject(ObjectOutputStream out) throws IOException {
			out.writeUTF(name);
			out.writeObject(coreAssetProject.getPath());
			coreAssetProject.store();
			out.writeInt(productProjects.size());
			for (Project product : productProjects) {
				//TODO: create directory path if it does not exist
				out.writeObject(product.getPath());
				product.store();
			}
		}

		private void readObject(ObjectInputStream in) throws IOException,
						ClassNotFoundException {
			pl.name = in.readUTF();
			File f = (File) in.readObject();
			pl.loadCoreAssetProject(f);
			int n = in.readInt();
			for (int i = 0; i < n; i++) {
				f = (File) in.readObject();
				try {
					pl.loadProduct(f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public Object readResolve() {
			return pl;
		}
	}
}
