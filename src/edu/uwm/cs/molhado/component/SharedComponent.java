package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.version.Version;

/**
 * A symbolic link to a component in another project at a particular version.
 *
 * @author chengt
 */
public interface SharedComponent  {

	public boolean isSharedRoot();

	public void updateToLatest();

	public void updateToVersion(Version v);

	public void forwardChange();

	public void backwardChangePropagation();

}
