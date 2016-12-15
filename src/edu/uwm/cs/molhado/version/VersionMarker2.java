package edu.uwm.cs.molhado.version;

import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;

/**
 * This class does everything the VersionMarker class does.
 * Problem with versionMarker class is that the version name is messed
 * up during its evolution.
 *
 * @author chengt
 */
public class VersionMarker2 extends VersionMarker{

	private String versionName;

	public VersionMarker2(){
		super();
		versionName = VersionSupport.getVersionNumber(getVersion());
	}

	public VersionMarker2(Version v){
		super(v);
		versionName = VersionSupport.getVersionNumber(v);
	}

	public synchronized void setVersionStartPoint(Version v) {
		super.setVersion(v);
		versionName = VersionSupport.getVersionNumber(v);
	}

	public String toString(){
		return versionName;
	}

	public String getVersionName(){
		return versionName;
	}
}
