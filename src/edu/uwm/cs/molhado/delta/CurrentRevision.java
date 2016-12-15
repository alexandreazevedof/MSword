package edu.uwm.cs.molhado.delta;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author chengt
 */
public class CurrentRevision {

	private String user;
	private Set<UUID> parents = new HashSet<UUID>();
	private String content;
	
	public CurrentRevision(Set<UUID> parents, String user){
		this.user = user;
		this.parents = parents;
	}

	public String getUser(){
		return user;
	}

	public Set<UUID> getParents(){
		return parents;
	}

	public String getContent(){
		return content;
	}
}
