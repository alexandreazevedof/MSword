package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.version.Era;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChunk;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.cmu.cs.fluid.version.VersionedRegionDelta;
import edu.uwm.cs.molhado.version.VersionSupport;

/**
 *
 * @author chengt
 */
public abstract class FluidRegistryLoading {

	public static void ensureStaticLoaded(){}

	static {
		/* this code must be called to ensure that the persistent
		 * objects (VersionedRegion, VersionChunk, ...) are registered.
		 * Else we will get a not registered PersistentKind exception*/
		VersionedRegion.ensureLoaded();
		VersionedChunk.ensureLoaded();
		Version.ensureLoaded();
		Era.ensureLoaded();
		Bundle.ensureLoaded();
		VersionSupport.ensureLoaded();
		VersionedRegionDelta.ensureLoaded();
		Component.ensureClassLoaded();
	}
}
