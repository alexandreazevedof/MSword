package edu.uwm.cs.molhado.fluid.test;

import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.version.*;
import edu.uwm.cs.molhado.util.*;
import java.io.IOException;

public class TestEra extends FluidRegistryLoading {

	public static IRNode[] nodes = new IRNode[3];
	public static IRNode outNode;
	public static Era[] eras = new Era[3];
	public static Version[] versions = new Version[3];
	public static TrueZipFileLocator floc = new TrueZipFileLocator("/tmp/testera");
	public static VersionedRegion region = new VersionedRegion();
	public static VersionedRegion outRegion = new VersionedRegion();
	public static SlotInfo<String> nameAttr;
	public static SlotInfo<VersionedRegion> outRegAttr;
	public static SlotInfo<IRNode> outNodeAttr;
	public static Bundle bundle = new Bundle();
	static{
		bundle.setName("bundle");
		try {
			nameAttr = VersionedSlotFactory.prototype.newAttribute("test.era.name",
					  IRStringType.prototype);
			outRegAttr = VersionedSlotFactory.prototype.newAttribute("test.era.reg",
					  IRPersistentReferenceType.prototype);

			outNodeAttr = VersionedSlotFactory.prototype.newAttribute("test.era.outnode",
					  IRNodeType.prototype);
		} catch (SlotAlreadyRegisteredException ex) {
			ex.printStackTrace();
		}
		bundle.saveAttribute(nameAttr);
		bundle.saveAttribute(outRegAttr);
		bundle.saveAttribute(outNodeAttr);
	}

	public static Era makeEra(Version v){
		Era e = null;
		try {
			e = new Era(v.parent(), new Version[]{v});
		} catch (OverlappingEraException ex) {
			ex.printStackTrace();
		} catch (DisconnectedEraException ex) {
			ex.printStackTrace();
		}
		return e;
	}

	public static void create(){
		nodes[0] = new PlainIRNode(region);
		nodes[0].setSlotValue(nameAttr, "a");
		versions[0] = Version.getVersion();
		eras[0] = makeEra(versions[0]);

		nodes[1] = new PlainIRNode(region);
		nodes[1].setSlotValue(nameAttr, "b");
		versions[1] = Version.getVersion();
		eras[1] = makeEra(versions[1]);

		nodes[2] = new PlainIRNode(region);
		nodes[2].setSlotValue(nameAttr, "c");
		IRNode outNode = new PlainIRNode(outRegion);
		nodes[2].setSlotValue(outRegAttr, outRegion);
		nodes[2].setSlotValue(outNodeAttr, outNode);
		outNode.setSlotValue(nameAttr, "out");
		versions[2]= Version.getVersion();
		eras[2] = makeEra(versions[2]);
	}


	public static void print(Version v){
		System.out.println("==version " + v.toString());
		Version.saveVersion(v);
		for(int i=0; i<3; i++){
			System.out.println("n" + i + ":");
			if (nodes[i].valueExists(nameAttr)){
			  System.out.println(">name: " + nodes[i].getSlotValue(nameAttr));
			} else {
				System.out.println(">name: no value");
			}
			if (nodes[i].valueExists(outRegAttr)){
				System.out.println(">outreg: " + nodes[i].getSlotValue(outRegAttr));
				try {
					outRegion = nodes[i].getSlotValue(outRegAttr);
					if(!outRegion.isDefined()){
						System.out.println("outregion not defined--loading");
					outRegion.load(floc);
					}
					if (outRegion.isDefined()){
						System.out.println("Now outregion is defined.");
					} else {
						System.out.println("Now outregion is not defined.");
					}
					VersionedChunk.get(outRegion, bundle).getDelta(v.getEra()).load(floc);
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}else {
            System.out.println(">outreg: no value");
			}
			if (nodes[i].valueExists(outNodeAttr)){
				System.out.println(">outnode: " + nodes[i].getSlotValue(outNodeAttr));
				IRNode o = nodes[i].getSlotValue(outNodeAttr);
				if (o.valueExists(nameAttr)){
					System.out.println(">outnode name: " + o.getSlotValue(nameAttr));
				} else {
					System.out.println(">outnode name: no value");
				}
			}else {
            System.out.println(">outnode: no value");
			}
		}
		Version.restoreVersion();
	}

	public static void loadVersion(Version v){
		if (v == Version.getInitialVersion()) {
			return;
		}
		VersionedChunk ch = VersionedChunk.get(region, bundle);
		VersionedDelta d = ch.getDelta(v.getEra());
		if (!d.isDefined(v)){
			try {
				d.load(floc);
				loadVersion(v.parent());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	public static void printAll(){
		for(Version v:versions){
			print(v);
		}
	}
	public static void store() throws IOException{
		create();

		IRObjectOutputStream out = floc.getObjectOutputStream("proj.info");

		out.writeVersionedRegion(region, floc);
		//out.writeVersionedRegion(outRegion, floc);
		outRegion.store(floc);

		for(Era e:eras) {
			//out.writeEra(e, floc);
			VersionedChunk.get(region, bundle).getDelta(e).store(floc);
			VersionedChunk.get(outRegion, bundle).getDelta(e).store(floc);
		}
		for(Version v: versions){
			out.writeVersion(v);
		}

		for(IRNode n:nodes){
			out.writeNode(n);
		}
		out.writeNode(outNode);
		out.close();
	}

	public static void load() throws IOException{
		IRObjectInputStream in = floc.getObjectInputStream("proj.info");
		region = in.readVersionedRegion(floc);
		//outRegion = in.readVersionedRegion(floc);

		for(int i=0; i<3; i++){
			eras[i] = in.readEra(floc);
			//VersionedChunk.get(region, bundle).getDelta(eras[i]).load(floc);
			//VersionedChunk.get(outRegion, bundle).getDelta(eras[i]).load(floc);
		}
		for(int i=0; i<3; i++){
			versions[i] = in.readVersion();
		}
		for(int i=0; i<3; i++){
			nodes[i] =  in.readNode();
		}
		outNode = in.readNode();
		in.close();
	}

	public static void main(String[] args) throws IOException{
		//store();
		//load();
		loadVersion(versions[2]);
		printAll();


	}
}
