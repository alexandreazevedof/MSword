package edu.uwm.cs.molhado.delta;

import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Contains all the revisions made to an XML file
 *
 * @author chengt
 */
public class RevisionHistory {


	public final HashMap<UUID, Revision> revisionMap = new HashMap<UUID, Revision>();
	public final HashMap<String, Revision> nameMap = new HashMap<String, Revision>();

	private int indent = 2;
	private RevisionList revisions = new RevisionList();
	private Revision root;
	private UniqueList<UUID> curParentIds = new UniqueList<UUID>();
	//current revision user
	private String curRevisionUser;
	//current revision id
	private UUID curRevisionId;
	private SimpleXmlParser3 parser;



	public Revision getRoot(){
		if (revisions.isEmpty()) return null;
		return revisions.get(0);
	}

	public void addCurParentIds(UUID p){
		curParentIds.add(p);
	}

	public void clearParentIds(){
		curParentIds.clear();
	}

	public int getMaxNodeId(){
		return parser.getMouid();
	}

	public void setMaxNodeId(int id){
		parser.setMouid(id);
	}
	public UniqueList<UUID> getCurParents(){
		return curParentIds;
	}

	public void setCurRevisionUser(String user){
		curRevisionUser = user;
	}

	public String getCurRevisionUser(){
		return curRevisionUser;
	}

	public void setCurRevisionId(UUID id){
		curRevisionId = id;
	}

	public UUID getCurRevisionId(){
		return curRevisionId;
	}

	public Revision newRevision(String name){
		Revision r = new Revision(name);
		revisions.add(r);
		revisionMap.put(r.getId(), r);
		nameMap.put(name, r);
		return r;
	}

	public Revision newRevision(Revision parent, String name){
		Revision r = new Revision(name);
		r.addParent(parent);
		revisions.add(r);
		revisionMap.put(r.getId(), r);
		nameMap.put(name, r);
		return r;
	}

	public Revision newRevision(UUID id, String name){
		Revision r = new Revision(id, name);
		revisions.add(r);
		revisionMap.put(id, r);
		nameMap.put(name, r);
		return r;
	}

	public Revision newRevision(UUID id, ArrayList<UUID> parentIds, String name){
		Revision r = new Revision(id, name, parentIds, this);
		revisions.add(r);
		revisionMap.put(id, r);
		nameMap.put(name, r);
		return r;
	}

	public Revision get(UUID id){
		return revisionMap.get(id);
	}

	public Revision get(String name){
		return nameMap.get(name);
	}

	public RevisionList getRevisions(){
		return revisions;
	}

	public Revision getLast(){
		if (revisions.isEmpty()) return null;
		return revisions.get(revisions.size()-1);
	}

	public int count(){
		return revisions.size();
	}

	public void setIndent(int indent){
		this.indent = indent;
	}

	@Override
	public String toString(){
		System.out.println("--Revision count:" + revisions.size());
		StringBuilder sb = new StringBuilder();
		sb.append("  <molhado:revision-history id='revision-history' ");
		sb.append(" max-id='");
		sb.append(getMaxNodeId());
		sb.append("' ");
		sb.append("cur-rev-id='"+getCurRevisionId() + "' cur-user='" + getCurRevisionUser() + "' ");
		if (!curParentIds.isEmpty()) {
		   sb.append("cur-parents='");
			for (int i = 0; i < curParentIds.size() - 1; i++) {
				sb.append(curParentIds.get(i));
				sb.append(",");
			}
			sb.append(curParentIds.get(curParentIds.size()-1));
			sb.append("' ");
		}
		sb.append(">\n");

		toString(sb, getRoot());
		
//		for(Revision r:revisions){
//			sb.append(r.toString()).append("\n");
//		}

		sb.append("  </molhado:revision-history>\n");
		return sb.toString();
	}



	private void toString(StringBuilder sb, Revision rev){
		rev.visit();
		System.out.println("Writing " + rev.getId());
		if (rev.writeReady()){
			rev.write(sb);
			System.out.println(rev.getId() + " has " + rev.getChildren().size() + " children");
			for(Revision c:rev.getChildren()){
				toString(sb, c);
			}
		}
	}

	public void setParser(SimpleXmlParser3 aThis) {
		parser = aThis;
	}
}
