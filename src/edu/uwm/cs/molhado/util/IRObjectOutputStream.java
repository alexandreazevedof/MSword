/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.uwm.cs.molhado.component.*;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.util.FileLocator;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.Era;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedRegion;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 *
 * @author chengt
 */
public class IRObjectOutputStream extends ObjectOutputStream{
	public IRObjectOutputStream() throws IOException{
	}

	public IRObjectOutputStream(OutputStream out) throws IOException{
		super(out);
	}

	public void writeVersion(Version version) throws IOException{
		writeObject(version);
	}

	public void writeNode(IRNode node) throws IOException{
		writeObject(node);
	}

	public void writeUniqueId(UniqueID id ) throws IOException {
      writeObject(id);
	}

	public void writeComponent(Component comp) throws IOException{
		writeObject(comp);
	}

	public void writeEra(Era era, FileLocator floc) throws IOException{
		writeUniqueId(era.getID());
		era.store(floc);
	}

	public void writeVersionedRegion(VersionedRegion region, FileLocator floc) throws IOException{
		writeUniqueId(region.getID());
		region.store(floc);
	}

	public void writeBundle(Bundle bundle, FileLocator floc) throws IOException{
		writeUniqueId(bundle.getID());
		bundle.store(floc);
	}
}
