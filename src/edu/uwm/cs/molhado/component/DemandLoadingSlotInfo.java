package edu.uwm.cs.molhado.component;

import edu.uwm.cs.molhado.util.IRLoadingUtil;
import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.SlotImmutableException;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.ir.SlotUndefinedException;
import edu.cmu.cs.fluid.version.VersionedRegion;

/**
 *
 * @param <T>
 * @author chengt
 */
public class DemandLoadingSlotInfo<T> extends SlotInfo<T>{

	public static boolean demandLoading = true;
	private final SlotInfo<T> si;
	private final Bundle bundle;

	public DemandLoadingSlotInfo(SlotInfo<T> si){
		this.si= si;
		this.bundle = si.getBundle();
	}

	public SlotInfo<T> getShadowSlotInfo(){
		return si;
	}

	public static void setDemandLoading(boolean dl){
		demandLoading = dl;
	}

	protected boolean valueExists(IRNode node) {
		if (demandLoading){
			VersionedRegion r = VersionedRegion.getVersionedRegion(node);
			IRLoadingUtil.load(r, bundle, r.getFileLocator());
		}
		return node.valueExists(si);
	}

	@Override
	protected T getSlotValue(IRNode node) throws SlotUndefinedException {
		if (demandLoading){
			VersionedRegion r = VersionedRegion.getVersionedRegion(node);
			IRLoadingUtil.load(r, bundle, r.getFileLocator());
		}
		return node.getSlotValue(si);
	}

	@Override
	protected void setSlotValue(IRNode node, T newValue) throws SlotImmutableException {
		node.setSlotValue(si, newValue);
	}

}
