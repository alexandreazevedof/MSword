package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.*;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.util.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class IRStoreTest extends FluidRegistryLoading {

	public static TrueZipFileLocator floc = new TrueZipFileLocator("/home/chengt/irdata/store");

	public static ArrayList<Version> versions = new ArrayList<Version>();
	public static ArrayList<IRNode> nodes = new ArrayList<IRNode>();

	public static Era era;
	public static VersionedRegion region;
	public static Bundle bundle = new Bundle();
	public static final SlotInfo<String> attr;
	static{
		SlotInfo<String> si = null;
		try {
			si = VersionedSlotFactory.prototype.newAttribute("ir.name", IRStringType.prototype);
		} catch (SlotAlreadyRegisteredException ex) {
			Exceptions.printStackTrace(ex);
		}
		attr = si;
		bundle.saveAttribute(attr);
		bundle.setName("bundle");
	}

	public static void update(IRNode n, String name){
		n.setSlotValue(attr, name);
	}

	public static IRNode createNode(String name){
		IRNode n = new PlainIRNode(region);
		n.setSlotValue(attr, name);
		return n;
	}

	public static String getName(IRNode n){
		if (n.valueExists(attr)) return n.toString() + ":" + n.getSlotValue(attr);
		return n.toString() + ":"  + null;
	}

	public static void createStructure() throws OverlappingEraException{

		era = new Era(Version.getInitialVersion());
		era.setName("era");
		Version.setDefaultEra(era);

		region = new VersionedRegion();
		region.setName("region");

		IRNode a = createNode("a");
		IRNode b = createNode("b");
		nodes.add(a);
		nodes.add(b);
		Version v1 = Version.getVersion();
		versions.add(v1);

		update(a, "x");
    Version v2 = Version.getVersion();
		versions.add(v2);

		update(b, "y");
		Version v3 = Version.getVersion();
		versions.add(v3);
		//era = new Era(Version.getInitialVersion(), new Version[]{v1, v2, v3});
		//era.setName("era");
		era.complete();
		System.out.println("Num Nodes=" + region.getNumNodes());
		System.out.println(VersionedRegion.getOwner(b).getNumNodes());
	}

	public static void storeStructure() throws IOException, OverlappingEraException{
		createStructure();

		IRPersistent.setDebugIO(true);
		era.store(floc);
		region.store(floc);

		VersionedChunk vc = VersionedChunk.get(region, bundle);
		VersionedDelta vd = vc.getDelta(era);
		vd.store(floc);

		IRObjectOutputStream out = floc.getObjectOutputStream("info.data");
		out.writeNode(nodes.get(0));
		out.writeNode(nodes.get(1));
		out.writeVersion(versions.get(0));
		out.writeVersion(versions.get(1));
		out.writeVersion(versions.get(2));
		out.close();
		System.out.println("Nnum Nodes=" + region.getNumNodes());
	}

	public static void storeStructure2() throws IOException, OverlappingEraException{
		createStructure();

		IRPersistent.setDebugIO(true);
		era.store(floc);
		region.store(floc);

		VersionedChunk vc = VersionedChunk.get(region, bundle);
		VersionedDelta vd = vc.getDelta(era);
		vd.store(floc);

		ObjectOutputStream oos = new ObjectOutputStream(floc.openFileWrite("info.data"));
		oos.writeObject(nodes.get(0));
		oos.writeObject(nodes.get(1));
		oos.writeObject(versions.get(0));
		oos.writeObject(versions.get(1));
		oos.writeObject(versions.get(2));
		oos.close();
		System.out.println("Nnum Nodes=" + region.getNumNodes());
	}

	public static void loadStructure() throws IOException{
		IRPersistent.setDebugIO(true);
		era = Era.loadEra(UniqueID.parseUniqueID("era"), floc);
		region = VersionedRegion.loadVersionedRegion(UniqueID.parseUniqueID("region"), floc);

		VersionedChunk vc = VersionedChunk.get(region, bundle);
		VersionedDelta vd = vc.getDelta(era);
		vd.load(floc);

		IRObjectInputStream in = floc.getObjectInputStream("info.data");
		nodes.add(in.readNode());
		nodes.add(in.readNode());
		versions.add(in.readVersion());
		versions.add(in.readVersion());
		versions.add(in.readVersion());
		in.close();

		System.out.println("Nnum Nodes=" + region.getNumNodes());
	}


	public static void loadStructure2() throws IOException, ClassNotFoundException{
		IRPersistent.setDebugIO(true);
		era = Era.loadEra(UniqueID.parseUniqueID("era"), floc);
		region = VersionedRegion.loadVersionedRegion(UniqueID.parseUniqueID("region"), floc);

		VersionedChunk vc = VersionedChunk.get(region, bundle);
		VersionedDelta vd = vc.getDelta(era);
		vd.load(floc);

		ObjectInputStream ois = new ObjectInputStream(floc.openFileRead("info.data"));
		nodes.add((IRNode) ois.readObject());
		nodes.add((IRNode) ois.readObject());
		versions.add((Version) ois.readObject());
		versions.add((Version) ois.readObject());
		versions.add((Version) ois.readObject());
		ois.close();


		System.out.println("Nnum Nodes=" + region.getNumNodes());
	}

	public static void printStructure(){
		Version.saveVersion(versions.get(0));
		System.out.println(getName(nodes.get(0)));
		System.out.println(getName(nodes.get(1)));
		Version.restoreVersion();

		Version.saveVersion(versions.get(1));
		System.out.println(getName(nodes.get(0)));
		System.out.println(getName(nodes.get(1)));
		Version.restoreVersion();

		Version.saveVersion(versions.get(2));
		System.out.println(getName(nodes.get(0)));
		System.out.println(getName(nodes.get(1)));
		Version.restoreVersion();
	}

	public static void main(String[] args) throws OverlappingEraException, IOException, ClassNotFoundException{

		//storeStructure2();
		loadStructure2();
		printStructure();
	}
}
