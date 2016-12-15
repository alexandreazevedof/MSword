package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.text.Reader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class JavaComponent extends FileComponent{

  protected JavaComponent(){

  }

	public JavaComponent(Project project, IRNode node) {
	  super(project, node);
	}

	public JavaComponent(Project project, DirectoryComponent parent, String name){
		super(project, parent, name);
	}

	public JavaComponent(Project project, DirectoryComponent parent, File file) throws IOException{
		super(project, parent, file);
	}

	@Override
	public void importFile(File file) throws IOException {

		if (!file.getName().endsWith(".java")){
			throw new IOException("Not a java file.");
		}

		PlainIRNode.pushCurrentRegion(region);
		try {
			IRNode root = Reader.readFile(file.getAbsolutePath());
			setRootNode(root);
		} catch (Exception ex) {
			System.out.println("Fail to parse file: " + file.getAbsolutePath());
			ex.printStackTrace();
		}
		PlainIRNode.popCurrentRegion();
	}

	@Override
	public void dumpContent() {
		System.out.println(getContent());
	}

	@Override
	protected Bundle getBundle() {
		return Reader.bundle;
	}


	@Override
	public String getContent() {
		super.dumpContent();
		return Reader.getText(getRootNode());
	}

	@Override
	public void exportToFile(File parent) {
		try {
			super.exportToFile(parent);
			File f = new File(parent, this.getName());
			FileWriter w = new FileWriter(f);
			w.write(Reader.getText(this.getRootNode()));
			w.close();
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}	
	}

	

	@Override
	protected SharedComponent makeSharedComponent(SharedComponent sharedRoot, Version v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
