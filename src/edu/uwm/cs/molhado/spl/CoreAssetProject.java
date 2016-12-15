/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.spl;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import java.io.File;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.Component;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.JavaComponent;
import edu.uwm.cs.molhado.component.SharedComponent;
import edu.uwm.cs.molhado.component.SharedJavaComponent;
import edu.uwm.cs.molhado.component.SharedXmlComponent;
import edu.uwm.cs.molhado.component.XmlComponent;
import edu.uwm.cs.molhado.fm.FMComponent;
import edu.uwm.cs.molhado.util.IRObjectInputStream;
import edu.uwm.cs.molhado.util.IRObjectOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class CoreAssetProject extends Project{

	private final static String FILE = "FM.info"; 
	private IRNode fmRootNode;

	//?? This is a problem when comonent name change over time.
	//which name should we display??

	//we need to keep track of components
	//even in version that component do not exist
	//we still want to be able to know such
	//component exist somewhere in the version tree

	private ArrayList<Component> assets;

	public CoreAssetProject(File path, String name){
		super(path, name);
	}

	public CoreAssetProject(File path) throws IOException{
		super(path);
		if (floc.locateFile(FILE, true) != null){
		IRObjectInputStream in = floc.getObjectInputStream(FILE);
		fmRootNode = in.readNode();
		in.close();
		}
	}

	public CoreAssetProject(String path, String name){
		super(new File(path), name);
	}

	public CoreAssetProject(String path) throws IOException{
		super(new File(path));
	}
	public void addAsset(Component c){
		assets.add(c);
	}


	public static CoreAssetProject load(File path) throws IOException {
		CoreAssetProject p = (CoreAssetProject)pathProjectMapping.get(path);
		if (p == null) {
			p = new CoreAssetProject(path);
		}
		return p;
	}

	/**
	 * Share a core   asset with a product
	 * @param comp    core asset component to share
	 * @param version version of core asset
	 * @param toProduct product which core is shared
	 * @param toDir new parent of shared component in product
	 */
	public SharedComponent share(Component comp, Version version, ProductProject toProduct,
					DirectoryComponent toDir){
		if (comp instanceof XmlComponent){
			return  new SharedXmlComponent(toProduct, toDir, comp, version);
		}  else if (comp instanceof JavaComponent){
			return new SharedJavaComponent(toProduct, toDir, comp, version);
		}

		return null;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName());
		sb.append("\n");
		sb.append(this.getPath().toString());
		sb.append("\n");
		sb.append("hello World");
		return sb.toString();
	}


	@Override
	protected void storeOptionalNewDelta(Version v) {
		try {
			FMComponent.store(projRegion, v, floc);
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	@Override
	protected void storeOptional() {
		IRObjectOutputStream out = null;
		try {
			out = floc.getObjectOutputStream(FILE);
		   out.writeNode(getFMRoot());
		   out.close();
		} catch (IOException ex) {
			try {
				out.close();
			} catch (IOException ex1) {
				Exceptions.printStackTrace(ex1);
			}
			Exceptions.printStackTrace(ex);
		}
	}

	public IRNode getFMRoot(){
		return fmRootNode;
	}
	
	public void setFMRoot(IRNode root){
		fmRootNode = root;
	}

}
