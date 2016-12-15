package edu.uwm.cs.molhado.delta;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author chengt
 */
public class Revision {

	//public final static HashMap<UUID, Revision> revisionMap = new HashMap<UUID, Revision>();
	//public final static HashMap<String, Revision> nameMap = new HashMap<String, Revision>();
	public final static TimeBasedGenerator uuid_gen = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

	private int visited = 0;

	private UUID 		id;
	private String 	user;
	private Date   	date;
	private String    name;

	private ArrayList<Edit> edits = new ArrayList<Edit>();
	private RevisionList parents = new RevisionList();
	private RevisionList children = new RevisionList();
	
	private int indentSpaces = 4;

	public static UUID createId(){
	  return uuid_gen.generate();	
	}

	public boolean writeReady(){
		return visited >= parents.size();
	}

	public void resetVisited(){
		visited = 0;
	}
	
	public void relabelIds(HashMap<Integer, Integer> idMap){
		for(Edit e:edits){
			e.relabelIds(idMap);
		}
		for(Revision c:children){
			c.relabelIds(idMap);
		}
	}
	
	public void visit(){
		++visited;
	}
	
	protected Revision(String name){
		this.id = createId();
		this.name = name;
//		revisionMap.put(id, this);
//		nameMap.put(name, this);
	}

	//root node of version graph
	protected Revision(UUID id, String name){
		this.id = id;
		this.name = name;
//		revisionMap.put(id, this);
//		nameMap.put(name, this);
	}

	//non root nodes in version graph
	protected Revision(UUID id, String name, UUID parentId, RevisionHistory rh){
		this(id, name);
		Revision parent = rh.get(parentId);
		//this may happen when we have merge
		if (parent == null) throw new RuntimeException("Can't find parent " + parentId);
		parent.addChild(this);
		parents.add(parent);
	}

	protected Revision(UUID id, String name, ArrayList<UUID> parentIds, RevisionHistory rh){
		this(id, name);
		for(UUID pId : parentIds){
			Revision parent = rh.get(pId);
			//this may happen when we have merge
			if (parent == null) throw new RuntimeException("Can't find parent " + pId);
			parent.addChild(this);
			parents.add(parent);
		}
	}

	public void removeParents(){
		for(Revision p:parents) p.children.remove(this);
		parents.clear();
	}

	public RevisionList getParents(){ 
		return (RevisionList) parents.clone();
	}

	public RevisionList getChildren(){;
		return (RevisionList) children.clone();
	}
	
	public void write(StringBuilder sb){
		sb.append(toString());
		visited = 0;
	}

	public void addParent(Revision parent){ 
		if (parents.contains(parent)) return;
		this.parents.add(parent); 
		parent.addChild(this);
	 }

	
	private void addChild(Revision c){ 
		if (children.contains(c)) return;
		children.add(c); 
		c.addParent(this);
	}

	//todo: remove this method
	//replace with a factory method .createEdit(tagName, attrs)
	public void addEdit(Edit e){ getEdits().add(e); }

	public String getName(){ return name; }

	public void setName(String name){ this.name = name; }

	public Date getDate(){ return date; }

	public void setDate(Date date){ this.date = date; }

	public String getUser(){ return user; }

	public void setUser(String user){ this.user = user; }

	//todo: remove this method
	public void setEdits(ArrayList<Edit> edits){ this.edits = edits; }
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(indent(indentSpaces));
		sb.append("<molhado:revision ");
		if (user != null){ sb.append("user='").append(user).append("' "); }
		if (name != null){ sb.append("name='").append(name).append("' "); }
		sb.append("id='").append(getId()).append("' ");
		sb.append("parents='");
		if (!parents.isEmpty()) {
			for (int i = 0; i < parents.size() - 1; i++) {
				sb.append(parents.get(i).getId()).append(", ");
			}
			sb.append(parents.get(parents.size() - 1).getId());
		}
		sb.append("' >\n");
		for(Edit e:getEdits()){
			sb.append(e).append("\n");
		}
		sb.append(indent(indentSpaces));
		sb.append("</molhado:revision>\n");
		return sb.toString();
	}

	/**
	 * @return the id
	 */
	public UUID getId() { return id; }

	@Override
	public boolean equals(Object o) {
		if (o instanceof Revision){
			Revision other = (Revision) o;
			if (id.equals(other.id)) return true;
		}
		return false;
	}

	
	/**
	 * @return the edits
	 */
	public ArrayList<Edit> getEdits() { return edits; }

	public void patch(IRNode root){
		if (!children.isEmpty()){
			//we will only use the first child
			children.get(0).patch(root);
		}
		HashMap<Integer, IRNode> map = new HashMap<Integer, IRNode>();
		buildTable2(map, root);
		for(Edit e:edits){
			e.patch(map);
		}
	}


	private static void buildTable2(HashMap<Integer, IRNode> map, IRNode n){
		Iteratable<IRNode> it = SimpleXmlParser3.tree.topDown(n);
		while(it.hasNext()){
			IRNode node = it.next();
			String type = n.getSlotValue(SimpleXmlParser3.nodeTypeAttr);
			if (type.equals("char")){
				//skip
				//txtNodes.put(n.getSlotValue(SimpleXmlParser3.textAttr), tree.getParent(n), n);
			} else if(type.equals("element")) {
				map.put(node.getSlotValue(SimpleXmlParser3.mouidAttr), node);
				//System.out.println(node.getIntSlotValue(SimpleXmlParser3.mouidAttr) + ": " + node);
			}
		}
	}
	
	private static void buildTable(HashMap<Integer, IRNode> map, IRNode n) {
		Tree tree = SimpleXmlParser3.tree;
			String nodeType = n.getSlotValue(SimpleXmlParser3.tagNameAttr);
			if (nodeType.equals("char")) {
				//txtNodes.put(n.getSlotValue(SimpleXmlParser3.textAttr), tree.getParent(n), n);
			} else {

				map.put(n.getIntSlotValue(SimpleXmlParser3.mouidAttr), n);
				for (int i = 0; i < tree.numChildren(n); i++) {
					IRNode c = tree.getChild(n, i);
					map.put(c.getIntSlotValue(SimpleXmlParser3.mouidAttr), c);
					buildTable(map, c);
				}
			}
		}

	protected String indent(int spaces) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < spaces; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

}
