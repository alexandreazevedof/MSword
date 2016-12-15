package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRStringType;
import edu.cmu.cs.fluid.version.DisconnectedEraException;
import edu.cmu.cs.fluid.version.OverlappingEraException;
import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SlotAlreadyRegisteredException;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.version.Era;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import edu.uwm.cs.molhado.component.DemandLoadingSlotInfo;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.util.IRObjectInputStream;
import edu.uwm.cs.molhado.util.IRObjectOutputStream;
import edu.uwm.cs.molhado.util.TrueZipFileLocator;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 * Test demand loading at the slotInfo level..  We have a derived SlotInfo that
 * proxies a stored SlotInfo.  The dervied slot info make sure the deltas are
 * loaded before passing the getSlotValue() to the proxied SlotInfo.  This allow
 * us to support loading on demand.  Some changes that are needed to allow this
 * to happen:
 *   VesionedRegion now include a filelocator.
 *   Version.getVersionLocal() is made public.
 *   IRPersistent.setName(..) set id = name
 *
 * @author chengt
 */
public class TestDemandLoadingSlotInfo extends FluidRegistryLoading{

	static TrueZipFileLocator floc = new TrueZipFileLocator("/tmp/slot");

	static IRNode n11, n12, n21, n22;
	static Version v1, v2, v3, v4;
	static Era e1, e2, e3, e4;
	static SlotInfo<String> si1;
	static SlotInfo<String> si2;

	static VersionedRegion r1 ;
	static VersionedRegion r2 ;
	static Bundle b1 = new Bundle();
	static Bundle b2 = new Bundle();

	static int count = 0;

	static {
		b1.setName("b1");
		b2.setName("b2");
		try {

			SlotInfo<String> ts1 = VersionedSlotFactory.prototype.
					  newAttribute("test.b1.name", IRStringType.prototype);

			SlotInfo<String> ts2 = VersionedSlotFactory.prototype.
					  newAttribute("test.b2.name", IRStringType.prototype);

			b1.saveAttribute(ts1);
			b2.saveAttribute(ts2);

			si1 = new DemandLoadingSlotInfo<String>(ts1);
			si2 = new DemandLoadingSlotInfo<String>(ts2);

		} catch (SlotAlreadyRegisteredException ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	static Era makeEra(Version v){
		Era e = null;
		try {
			e = new Era(v.parent(), new Version[]{v});
			e.setName("e" + (++count));
		} catch (OverlappingEraException ex) {
			Exceptions.printStackTrace(ex);
		} catch (DisconnectedEraException ex) {
			Exceptions.printStackTrace(ex);
		}
		return e;

	}

	static void store() throws IOException{
		r1 = new VersionedRegion();
		r1.setName("r1");
		r2 = new VersionedRegion();
		r2.setName("r2");

		n11 = new PlainIRNode(r1);
		n11.setSlotValue(si1, "n11-b1");
		n11.setSlotValue(si2, "n11-b2");
		v1 = VersionSupport.commit("v1", "v1");
		e1 = makeEra(v1);

		n12 = new PlainIRNode(r1);
		n12.setSlotValue(si1, "n12-b1");
		n12.setSlotValue(si2, "n12-b2");
		v2 = VersionSupport.commit("v2", "v2");
		e2 = makeEra(v2);

		n21 = new PlainIRNode(r2);
		n21.setSlotValue(si1, "n21-b1");
		n21.setSlotValue(si2, "n21-b2");
      v3 = VersionSupport.commit("v3", "v3");
		e3 = makeEra(v3);

		n22 = new PlainIRNode(r2);
		n22.setSlotValue(si1, "n22-b1");
		n22.setSlotValue(si2, "n22-b2");
		v4 = VersionSupport.commit("v4", "v4");
		e4 = makeEra(v4);


		IRObjectOutputStream out = floc.getObjectOutputStream("proj.info");

		out.writeVersionedRegion(r1, floc);
		out.writeVersionedRegion(r2, floc);
		out.writeEra(e1, floc);
		out.writeEra(e2, floc);
		out.writeEra(e3, floc);
		out.writeEra(e4, floc);
		VersionSupport.storeEraShadowRegion(e1, floc);
		VersionSupport.storeEraShadowRegion(e2, floc);
		VersionSupport.storeEraShadowRegion(e3, floc);
		VersionSupport.storeEraShadowRegion(e4, floc);
		out.writeVersion(v1);
		out.writeVersion(v2);
		out.writeVersion(v3);
		out.writeVersion(v4);
		out.writeNode(n11);
		out.writeNode(n12);
		out.writeNode(n21);
		out.writeNode(n22);

		out.close();

		//IRUtil.saveState();
		IRLoadingUtil.store(r1, b1, v4, floc);
		IRLoadingUtil.store(r1, b2, v4, floc);
		IRLoadingUtil.store(r2, b1, v4, floc);
		IRLoadingUtil.store(r2, b2, v4, floc);

	}

	static void load() throws IOException{

		System.out.println("[[[[loading]]]]");
		IRObjectInputStream in = floc.getObjectInputStream("proj.info");
		r1 = in.readVersionedRegion(floc);
		r2 = in.readVersionedRegion(floc);
		e1 = in.readEra(floc);
		e2 = in.readEra(floc);
		e3 = in.readEra(floc);
		e4 = in.readEra(floc);
		VersionSupport.loadEraShadowRegion(e1, floc);
		VersionSupport.loadEraShadowRegion(e2, floc);
		VersionSupport.loadEraShadowRegion(e3, floc);
		VersionSupport.loadEraShadowRegion(e4, floc);
		v1 = in.readVersion();
		v2 = in.readVersion();
		v3 = in.readVersion();
		v4 = in.readVersion();
		n11 = in.readNode();
		n12 = in.readNode();
		n21 = in.readNode();
		n22 = in.readNode();
		in.close();
	}

	static void dump(Version v){
		Version.saveVersion(v);

		System.out.println("==Dumping state at version " + VersionSupport.getVersionNumber(v));

		if (n11.valueExists(si1)){
			System.out.println("n11.name-si1=" + n11.getSlotValue(si1));
		} else {
			System.out.println("n11.name-si1=no value" );
		}

		if (n11.valueExists(si2)){
			System.out.println("n11.name-si2=" + n11.getSlotValue(si2));
		} else {
			System.out.println("n11.name-si2=no value" );
		}

		if (n12.valueExists(si1)){
			System.out.println("n12.name-si1=" + n12.getSlotValue(si1));
		} else {
			System.out.println("n12.name-si1=no value" );
		}

		if (n12.valueExists(si2)){
			System.out.println("n12.name-si2=" + n12.getSlotValue(si2));
		} else {
			System.out.println("n12.name-si2=no value" );
		}

		if (n21.valueExists(si1)){
			System.out.println("n21.name-si1=" + n21.getSlotValue(si1));
		} else {
			System.out.println("n21.name-si1=no value" );
		}

		if (n21.valueExists(si2)){
			System.out.println("n21.name-si2=" + n21.getSlotValue(si2));
		} else {
			System.out.println("n21.name-si2=no value" );
		}

		if (n22.valueExists(si1)){
			System.out.println("n22.name-si1=" + n22.getSlotValue(si1));
		} else {
			System.out.println("n22.name-si1=no value" );
		}

		if (n22.valueExists(si2)){
			System.out.println("n22.name-si2=" + n22.getSlotValue(si2));
		} else {
			System.out.println("n22.name-si2=no value" );
		}

		Version.restoreVersion();
	}

	static void test(){
		System.out.println("Eras--------------------");
		System.out.println(r1);
		System.out.println(r2);
		System.out.println(e1);
		System.out.println(e2);
		System.out.println(e3);
		System.out.println(e4);
		System.out.println("Versions----------------");
		System.out.println(VersionSupport.getVersionNumber(v1));
		System.out.println(VersionSupport.getVersionNumber(v2));
		System.out.println(VersionSupport.getVersionNumber(v3));
		System.out.println(VersionSupport.getVersionNumber(v4));
		System.out.println("Nodes-------------------");
		System.out.println(n11);
		System.out.println(n12);
		System.out.println(n21);
		System.out.println(n22);

		dump(v1);
		dump(v2);
		dump(v3);
		dump(v4);


		System.out.println("=============");
		Version.setVersion(v1);
      System.out.println(n11.getSlotValue(si1));
		Version.restoreVersion();
		Version.setVersion(v2);
      System.out.println(n11.getSlotValue(si1));
		Version.restoreVersion();

	}


	public static void main(String[] args) throws IOException{
		DemandLoadingSlotInfo.setDemandLoading(true);
		//store();
		load();
		test();
	}

}
