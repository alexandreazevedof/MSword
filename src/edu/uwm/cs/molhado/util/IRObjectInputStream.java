/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.version.Era;
import edu.uwm.cs.molhado.component.*;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.util.FileLocator;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedRegion;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class IRObjectInputStream extends ObjectInputStream {
	public IRObjectInputStream() throws IOException{
	  super();	
	}

	public IRObjectInputStream(InputStream in) throws IOException{
		super(in);
	}

	public Era readEra(FileLocator floc) throws IOException{
		UniqueID id = readUniqueId();
		return Era.loadEra(id, floc);
	}

	public VersionedRegion readVersionedRegion(FileLocator floc) throws IOException{
		UniqueID id = readUniqueId();
		return VersionedRegion.loadVersionedRegion(id, floc);
	}

	public Version readVersion() throws IOException{
		Version v = null;
		try {
			v = (Version) readObject();
		} catch (ClassNotFoundException ex) {
			Exceptions.printStackTrace(ex);
		}
		return v;
	}

	public IRNode readNode() throws IOException{
     IRNode n = null;
	  try{
		  n = (IRNode) readObject();
	  }catch(ClassNotFoundException ex){
		  Exceptions.printStackTrace(ex);
	  }
	  return n;
	}

	public UniqueID readUniqueId() throws IOException {
     UniqueID id = null;
	  try{
		  id = (UniqueID) readObject();
	  }catch(ClassNotFoundException ex){
		  Exceptions.printStackTrace(ex);
	  }
	  return id;
	}

	public Component readComponent() throws IOException{
		Component c = null;
		try{
			c = (Component) readObject();
		}catch(ClassNotFoundException ex){
			Exceptions.printStackTrace(ex);
		}
		return c;
	}

	public Bundle readBundle(FileLocator floc) throws IOException{
		UniqueID id = readUniqueId();
		return Bundle.loadBundle(id, floc);
	}
}
