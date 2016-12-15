/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.delta;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author chengt
 */
public class RevisionList extends ArrayList<Revision>{

	@Override
	public boolean add(Revision e) {
		if (contains(e)) return false;
		return super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends Revision> clctn) {
		throw new RuntimeException("Unsupported");
	}

	@Override
	public boolean addAll(int i, Collection<? extends Revision> clctn) {
		throw new RuntimeException("Unsupported");
	}

	@Override
	public void add(int i, Revision e) {
		if (contains(e)) return;
		super.add(i, e);
	}
	
}
