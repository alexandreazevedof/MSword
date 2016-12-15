package edu.uwm.cs.molhado.version;

import edu.cmu.cs.fluid.ir.*;
import edu.cmu.cs.fluid.version.IRVersionType;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.tree.TreeInterface;
import edu.cmu.cs.fluid.util.FileLocator;
import edu.cmu.cs.fluid.version.Era;
import edu.cmu.cs.fluid.version.Version;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chengt
 */
public abstract class VersionSupport {

	private static final Tree shadowTree = (Tree) Version.getShadowTree();

	/* hold version node attributes */
	public final static Bundle versionAttrBundle;

	/* major number of the version */
	private final static SlotInfo<String> majorAttribute;

	/* minor number of the version */
	private final static SlotInfo<Integer> minorAttribute;

	/* release number of version */
	private final static SlotInfo<String> releaseAttribute;

	/* tag or label developer assign to a version */
	private final static SlotInfo<String> tagAttribute;

	private final static SlotInfo<String> userAttribute;

	/* time stamp of checkin */
	private final static SlotInfo<String> timeStampAttribute;

	/* log provided when checkin */
	private final static SlotInfo<String> logAttribute;

	/* whether version is in main trunk */
	private final static SlotInfo<Boolean> inTrunkAttribute;

	/* the version that is merged into the main trunk */
	private final static SlotInfo<Version> mergeSourceAttribute;
	private static final SimpleSlotFactory SSF = SimpleSlotFactory.prototype;
	private static final ConstantSlotFactory CSF = ConstantSlotFactory.prototype;
	private static final IRStringType ST = IRStringType.prototype;
	private static final IRBooleanType BT = IRBooleanType.prototype;
	private static final IRIntegerType IT = IRIntegerType.prototype;
	private static final IRVersionType VT = IRVersionType.prototype;
	private static final Integer ONE = 1;
	private static final Boolean FALSE = false;
	private static final Boolean TRUE = true;
	private static final Date NOW = new Date();

	static {
		SlotInfo<String> majorAttrX = null;
		SlotInfo<Integer> minorAttrX = null;
		SlotInfo<String> releaseAttrX = null;
		SlotInfo<String> tagAttrX = null;
		SlotInfo<String> userAttrX = null;
		SlotInfo<String> timeStampAttrX = null;
		SlotInfo<String> logAttrX = null;
		SlotInfo<Version> mergeSourceAttrX = null;
		SlotInfo<Boolean> inTrunkAttrX = null;
		SlotInfo<VersionSupport> wrapper = null;
		try {
			majorAttrX = SSF.newAttribute("version.number.major", ST, "1");
			minorAttrX = SSF.newAttribute("version.number.minor", IT, 0);
			releaseAttrX = SSF.newAttribute("version.number.release", ST);
			tagAttrX = SSF.newAttribute("version.tag", ST, "");
			userAttrX = SSF.newAttribute("version.user", ST, "");
			timeStampAttrX = SSF.newAttribute("version.timestamp", ST, new Date().toString());
			logAttrX = SSF.newAttribute("version.log", ST, "");
			mergeSourceAttrX = SSF.newAttribute("version.merge.source", VT, null);
			inTrunkAttrX = SSF.newAttribute("version.intrunk", BT, FALSE);
			wrapper = SSF.newAttribute("version.wrapper", new IRObjectType<VersionSupport>(), null);
		} catch (Exception ex) {
			Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
		}

		majorAttribute = majorAttrX;
		minorAttribute = minorAttrX;
		releaseAttribute = releaseAttrX;
		tagAttribute = tagAttrX;
		userAttribute = userAttrX;
		timeStampAttribute = timeStampAttrX;
		logAttribute = logAttrX;
		mergeSourceAttribute = mergeSourceAttrX;
		inTrunkAttribute = inTrunkAttrX;

		//creating the bundle for storing and loadting of attributes
		versionAttrBundle = new Bundle();
		versionAttrBundle.saveAttribute(majorAttribute);
		versionAttrBundle.saveAttribute(minorAttribute);
		versionAttrBundle.saveAttribute(releaseAttribute);
		versionAttrBundle.saveAttribute(tagAttribute);
		versionAttrBundle.saveAttribute(userAttribute);
		versionAttrBundle.saveAttribute(timeStampAttribute);
		versionAttrBundle.saveAttribute(logAttribute);
		versionAttrBundle.saveAttribute(mergeSourceAttribute);
		versionAttrBundle.saveAttribute(inTrunkAttribute);
		versionAttrBundle.setName("version");
	}

	public static void loadEraShadowRegion(Era e, FileLocator floc) throws IOException {
		IRChunk c = IRChunk.get(e.getShadowRegion(), versionAttrBundle);
		c.load(floc);
	}

	public static void storeEraShadowRegion(Era e, FileLocator floc) throws IOException {
		IRChunk c = IRChunk.get(e.getShadowRegion(), versionAttrBundle);
		c.store(floc);
	}

	public static void ensureLoaded() {
	}

//  public static boolean comesFrom2(Version v, Version ancestor) {
//    if (v.equals(ancestor)) {
//      return true;
//    }
//    if (v.depth() < ancestor.depth()) {
//      return false;
//    }
//    Version m = getMergeSource(v);
//    Version p = v.parent();
//    if (m == null) {
//      return comesFrom2(p, ancestor);
//    } else {
//      return comesFrom2(p, ancestor) | comesFrom2(p, ancestor);
//    }
//  }
	public static String getTag(Version v) {
		return v.getShadowNode().getSlotValue(tagAttribute);
	}

	private static void setTag(Version v, String tag) {
		v.getShadowNode().setSlotValue(tagAttribute, tag);
	}

	public static String getTimeStamp(Version v) {
		return v.getShadowNode().getSlotValue(timeStampAttribute);
	}

	private static void setTimeStamp(Version v, Date d) {
		v.getShadowNode().setSlotValue(timeStampAttribute, d.toString());
	}

	public static String getLog(Version v) {
		return v.getShadowNode().getSlotValue(logAttribute);
	}

	private static void setLog(Version v, String log) {
		v.getShadowNode().setSlotValue(logAttribute, log);
	}

	public static Version getMergeSource(Version v) {
		return v.getShadowNode().getSlotValue(mergeSourceAttribute);
	}

	private static void setMergeSource(Version v, Version src) {
		v.getShadowNode().setSlotValue(mergeSourceAttribute, src);
	}

	public static boolean inTrunk(Version v) {
		return v.getShadowNode().getSlotValue(inTrunkAttribute).booleanValue();
	}

	public static boolean isMergedVersion(Version v){
		Version src = getMergeSource(v);
		if (src != null) return true;
		return false;
	}

	public static void putInTrunk(Version v) {
		//need to recompute the version number
		v.getShadowNode().setSlotValue(inTrunkAttribute, true);
		setVersionNumber(v);
	}

	/**
	 * Get all versions between early and late versions
	 * @param start
	 * @param end
	 * @return a list versions excluding early and late versions)
	 */
	public static ArrayList<Version> getVersionsBetween(Version early, Version late){
		ArrayList<Version> list = new ArrayList<Version>();
		for(Version v=getParentVersion(late); v != early; v = getParentVersion(v)){
			list.add(v);
		}
		Collections.reverse(list);
		return list;
	}

	/**
	 *
	 * @param start
	 * @param end
	 */
	public static void putPathInTrunk(Version start, Version end){
		ArrayList<Version> list = getVersionsBetween(start, end);
		if (!inTrunk(start)){
			putInTrunk(start);
		}
		for(Version v:list){
			putInTrunk(v);
		}
		putInTrunk(end);
	}

	public static String getVersionNumber(Version v) {
		IRNode shadowNode = v.getShadowNode();
		String major = shadowNode.getSlotValue(majorAttribute);
		Integer minor = shadowNode.getSlotValue(minorAttribute);
		String release = null;
		String versionNumber = major + "." + minor;
		if (shadowNode.valueExists(releaseAttribute)) {
			release = shadowNode.getSlotValue(releaseAttribute);
			versionNumber = versionNumber + "." + release;
		}
		return versionNumber;
	}

	public static Version[] getChildren(Version v) {
		IRNode shadowNode = v.getShadowNode();
		int nc = shadowTree.numChildren(shadowNode);
		Version[] array = new Version[nc];
		for (int i = 0; i < nc; i++) {
			IRNode child = shadowTree.getChild(shadowNode, i);
			array[i] = Version.getShadowVersion(child);
		}
		return array;
	}

	private static Version getChildInTrunk(Version v) {
		Version[] children = getChildren(v);
		for (Version version : children) {
			if (inTrunk(version)) {
				return version;
			}
		}
		return null;
	}

	public static Version getDeepestDescendentInTrunk(Version v) {
		if (getChildren(v).length == 0 || getChildInTrunk(v) == null) {
			return v;
		}
		return getDeepestDescendentInTrunk(getChildInTrunk(v));
	}

	public static Version getParentVersion(Version v){
		IRNode shadowNode = v.getShadowNode();
		IRNode parentNode = shadowTree.getParentOrNull(shadowNode);
		if (parentNode== null) return null;
		return Version.getShadowVersion(parentNode);
	}

	public static Version commit(String tag, String log) {
	//	Version.bumpVersion();
		Version source = Version.getVersion();
		if (source != Version.getInitialVersion()) {
			Version dt = getDeepestDescendentInTrunk(source.parent());
			setTimeStamp(source, new Date());
			setTag(source, tag);
			setLog(source, log);
			setVersionNumber(source);
			if (dt == source.parent()) {
				putInTrunk(source);
			} else {
				return merge(source, dt, tag, log);
			}
		}
		return source;
	}

	public static Version commit(int major, String tag, String log) {
		Version v = commit(tag, log);
		setVersionNumber(v, "" + major);
		return v;
	}

	public static Version commitAsBranch(String tag, String log) {
		Version v = Version.getVersion();

		setTimeStamp(v, new Date());
		setTag(v, tag);
		setLog(v, log);

		IRNode n = v.getShadowNode();
		Version p = v.parent();

		IRNode pShadowNode = p.getShadowNode();

		String release = pShadowNode.getSlotValue(majorAttribute);
		Integer revision = pShadowNode.getSlotValue(minorAttribute);
		TreeInterface t = Version.getShadowTree();

		int position = 0;
		int numChildren = t.numChildren(pShadowNode);
		for (int i = 0; i < numChildren; i++) {
			IRNode c = t.getChild(pShadowNode, i);
			Boolean isTrunk = c.getSlotValue(inTrunkAttribute);
			if (isTrunk.booleanValue()) {
				position = position - 1;
			}
			if (c == n) {
				position = position + i + 1;
			}
		}

		if (v.parent() != Version.getInitialVersion()) {
			release = release + "." + revision + "." + position;
			setVersionNumber(v, release);
		}
		return v;
	}

	public static Version merge(Version source, Version target, String tag, String log) {
		//Version.setVersion(target);
	//	Version.bumpVersion();
		Version v3 = Version.getVersion();
		setTimeStamp(v3, new Date());
		setLog(v3, log);
		setTag(v3, tag);
		setVersionNumber(v3);
		setMergeSource(v3, source);
		if (getChildInTrunk(target) == null) {
			putInTrunk(v3);
		}
		return v3;
	}

	private static void setVersionNumber(Version v, String release) {
		v.getShadowNode().setSlotValue(majorAttribute, release);
		v.getShadowNode().setSlotValue(minorAttribute, new Integer(1));
	}

	private static void setVersionNumber(Version source) {
		Version p = source.parent();
		IRNode n = source.getShadowNode();
		if (p == Version.getInitialVersion()) {
			n.setSlotValue(majorAttribute, "1");
			n.setSlotValue(minorAttribute, new Integer(1));
		} else {
			IRNode pShadowNode = p.getShadowNode();
			String major = pShadowNode.getSlotValue(majorAttribute);
			Integer minor = pShadowNode.getSlotValue(minorAttribute);
			n.setSlotValue(majorAttribute, major);
			n.setSlotValue(minorAttribute, new Integer(minor.intValue() + 1));
		}
	}
}
