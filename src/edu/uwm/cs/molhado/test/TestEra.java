package edu.uwm.cs.molhado.test;

import java.io.File;
import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.util.*;
import edu.cmu.cs.fluid.version.*;
import edu.cmu.cs.fluid.version.VersionedChunk;
import edu.cmu.cs.fluid.version.VersionedDelta;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import org.openide.util.Exceptions;

public class TestEra extends FluidRegistryLoading {

	static File path = new File("/tmp/abc/");
	static FileLocator floc = new DirectoryFileLocator(path);
	static VersionedRegion region;
	static SlotInfo<String> nameAttr;
	static Bundle bundle = new Bundle();
	static Vector<Era> eras = new Vector<Era>();
	static Vector<Version> versions = new Vector<Version>();
	static Vector<IRNode> nodes = new Vector<IRNode>();
	

	static {
		try {
			nameAttr = VersionedSlotFactory.prototype.newAttribute("test.era.name", IRStringType.prototype);
			bundle.saveAttribute(nameAttr);
			bundle.setName("bundle");
		} catch (SlotAlreadyRegisteredException ex) {
		}
	}

	public static void create() throws OverlappingEraException {

		region = new VersionedRegion();
		region.setName("reg");

		IRNode a = new PlainIRNode(region);
		nodes.add(a);
		a.setSlotValue(nameAttr, "a");
		Version v1 = Version.getVersion();
		versions.add(v1);
		eras.add(new Era(v1.parent(), new Version[]{v1}));

		IRNode b = new PlainIRNode(region);
		nodes.add(b);
		b.setSlotValue(nameAttr, "b");
		Version v2 = Version.getVersion();
		versions.add(v2);
		eras.add(new Era(v2.parent(), new Version[]{v2}));

		IRNode c = new PlainIRNode(region);
		nodes.add(c);
		c.setSlotValue(nameAttr, "c");
		Version v3 = Version.getVersion();
		versions.add(v3);
		eras.add(new Era(v3.parent(), new Version[]{v3}));
	}

	public static void store() throws IOException, OverlappingEraException {
		create();
		VersionedChunk vc = VersionedChunk.get(region, bundle);

		for(Era e:eras){
				e.store(floc);
				VersionedDelta vd = vc.getDelta(e);
				vd.store(floc);
		}
		region.store(floc);

		ObjectOutputStream oos = new ObjectOutputStream(floc.openFileWrite("proj.info"));

		oos.writeInt(versions.size());
		for (Version v : versions) {
			oos.writeObject(v);
		}

		oos.writeInt(nodes.size());
		for (IRNode n : nodes) {
			oos.writeObject(n);
		}

		oos.close();
	}

	public static void printNodes() {
		for(Version v:versions){
			Version.saveVersion(v);
			for (IRNode n : nodes) {
				System.out.print(n.toString());
				if (n.valueExists(nameAttr)){
					System.out.print("name="+n.getSlotValue(nameAttr));
				}
				System.out.println();
			}
			Version.restoreVersion();
		}
	}

	public static void load() throws IOException {
		region = VersionedRegion.loadVersionedRegion(UniqueID.parseUniqueID("reg"), floc);
		FilenameFilter ff = new FilenameFilter() {

			public boolean accept(java.io.File dir, String name) {
				return name.endsWith(".era");
			}
		};
		java.io.File[] listFiles = path.listFiles(ff);
		for (File file : listFiles) {
			String fileName = file.getName();
			String eraName = fileName.substring(0, fileName.lastIndexOf('.'));
			Era e = Era.loadEra(UniqueID.parseUniqueID(eraName), floc);
			VersionedChunk vc = VersionedChunk.get(region, bundle);
			VersionedDelta vd = vc.getDelta(e);
			vd.load(floc);
		}

		ObjectInputStream ois = new ObjectInputStream(floc.openFileRead("proj.info"));
		int n = ois.readInt();
		for (int i = 0; i < n; i++) {
			try {
				versions.add((Version) ois.readObject());
			} catch (ClassNotFoundException ex) {
				//Exceptions.printStackTrace(ex);
        ex.printStackTrace();
			}
		}
		n = ois.readInt();

		for (int i = 0; i < n; i++) {
			try {
				nodes.add((IRNode) ois.readObject());
			} catch (ClassNotFoundException ex) {
				//Exceptions.printStackTrace(ex);
        ex.printStackTrace();
			}
		}
		ois.close();
	}

	public static void main(String[] args) throws IOException, OverlappingEraException {
		//store();
		load();
		printNodes();
	}
}
