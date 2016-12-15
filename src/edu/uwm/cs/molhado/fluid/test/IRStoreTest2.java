package edu.uwm.cs.molhado.fluid.test;

import de.schlichtherle.io.File;
import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRPersistent;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.Era;
import edu.cmu.cs.fluid.version.OverlappingEraException;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChunk;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.util.IRObjectInputStream;
import edu.uwm.cs.molhado.util.IRObjectOutputStream;
import edu.uwm.cs.molhado.util.TrueZipFileLocator;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.IOException;

public class IRStoreTest2 extends FluidRegistryLoading{

	public static TrueZipFileLocator floc = new TrueZipFileLocator("irdata/store");
	public static IRNode node;
	public static Version version;
	public static Era era;
	public static VersionedRegion region;
	public static final Bundle bundle = SimpleXmlParser3.bundle;
	public static final SimpleXmlParser3 p = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);

        public static final String xmlText = "<a x='0' y='1'><b /></a>";
        
	public static void createStructure() throws OverlappingEraException, Exception{
		era = new Era(Version.getVersion());
		era.setName("era");
		Version.setDefaultEra(era);
		region = new VersionedRegion();
		region.setName("region");
		PlainIRNode.setCurrentRegion(region);
		node = p.parse(new File("test.xml"));
		version = Version.getVersion();
		era.complete();
	}

	public static void storeStructure() throws IOException, OverlappingEraException, Exception{
		createStructure();
		IRPersistent.setDebugIO(true);
		IRPersistent.setTraceIO(true);
		era.store(floc);
		region.store(floc);
		VersionedChunk.get(region, bundle).getDelta(era).store(floc);
		IRObjectOutputStream out = floc.getObjectOutputStream("info.data");
		out.writeNode(node);
		out.writeVersion(version);
		out.close();
		System.out.println(SimpleXmlParser3.toStringWithID(node));
	}

	public static void loadStructure() throws IOException{
		IRPersistent.setDebugIO(true);
		IRPersistent.setTraceIO(true);
		era = Era.loadEra(UniqueID.parseUniqueID("era"), floc);
		region = VersionedRegion.loadVersionedRegion( UniqueID.parseUniqueID("region"), floc);
		VersionedChunk.get(region, bundle).getDelta(era).load(floc);
		IRObjectInputStream in = floc.getObjectInputStream("info.data");
		node = in.readNode();
		version = in.readVersion();
		in.close();

		Version.saveVersion(version);
		System.out.println(SimpleXmlParser3.toStringWithID(node));
		Version.restoreVersion();
	}


	public static void main(String[] args) throws IOException, OverlappingEraException, Exception{
		if (args[0].equals("store")){
			storeStructure();
		} else {
			loadStructure();
		}
	}
}
