package edu.uwm.cs.molhado.util;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.util.FileLocator;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChunk;
import edu.cmu.cs.fluid.version.VersionedDelta;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.IOException;

/**
 * A util class for loading and storing deltas of regions.  We need to load the
 * parent's delta before we can load the child's delta.
 *
 * @author chengt
 */
public class IRLoadingUtil {

	/**
	 * Load delta for current version.. mainly used for demand loading.
	 *
	 * @param r The region
	 * @param b The bundle
	 * @param f The location where project and deltas are stored.
	 */

	public static void load(VersionedRegion r, Bundle b, FileLocator f){
		//This is a problem when used in multithread program
		//as each thread will have thier own local version
		//avoid using this in multithreading
		Version current = Version.getVersionLocal();
		if (current == null) {
			System.out.println("localversion == null");
			return;
		}
	//	System.out.println("Loading version + " + current);
		try {
			load(r, b, current, f);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Load delta for given version.  To load delta for a version, we load
	 * the version's parent's deltas first if they had not been loaded.
	 *
	 * @param r The region
	 * @param b The bundle
	 * @param v The version to load delta for
	 * @param f The location where project and deltas are stored.
	 * @throws java.io.IOException
	 */
	public static void load(VersionedRegion r, Bundle b, Version v, FileLocator f)
			  throws IOException{

		if (r.isNew() || !r.isComplete()) return;
		if (v == Version.getInitialVersion()){
			return;
		}
		VersionedChunk vc = VersionedChunk.get(r, b);

		//new version created in current session
		//do not have an era yet.  we can't load
		//things we have not stored.
		while(v.getEra() == null){
			v = VersionSupport.getParentVersion(v);
		}

		if (!v.getEra().isStored()) {
			System.out.println("era is not stored");
			return;
		}
		VersionedDelta d = vc.getDelta(v.getEra());
		//	if (v.getEra().isLoaded(vc)) return;
		if (d.isDefined(v)) {
			return;
		}
		load(r, b, v.parent(), f);
		if (d.isDefined(v)) return;
		//	if (v.getEra().isLoaded(vc)) return;
		if(!d.isDefined(v) /*&& loaded.get(d)==null*/) {
			d.load(f);
		}
	}


	/**
	 * Store delta for version v.  Store the parent's delta if they had not been
	 * store.
	 *
	 * @param r The region
	 * @param b The bundle
	 * @param v The version
	 * @param f The location where the project and deltas are stored.
	 * @throws java.io.IOException
	 */
	public static void store(VersionedRegion r, Bundle b, Version v, FileLocator f)
			  throws IOException{

		if (v == Version.getInitialVersion()) {
			return;
		}

		VersionedChunk ch = VersionedChunk.get(r, b);
		VersionedDelta d = ch.getDelta(v.getEra());
		if (!d.isStored() && d.isDefined()){
			store(r, b, v.parent(), f);
			d.store(f);
		}
	}

}
