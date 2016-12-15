package edu.uwm.cs.molhado.project;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.cmu.cs.fluid.version.VersionTracker;
import java.util.Hashtable;
import java.util.Observable;

/**
 *
 * @author chengt
 */
public class Project extends Observable{

  ProjectRepository repository;
  private Version version;
  private VersionTracker tracker;
  private Tree tree;

  private Project(ProjectRepository repository, Version version){
    this.repository = repository;
    this.tree = repository.getTree();
    this.version = version;
    tracker = new VersionMarker(version);
  }

  public Version getVersion(){
    return version;
  }
  
  public IRNode createNode(String name){
    IRNode n = repository.createNode(name);
    return n;
  }

  public IRNode createNode(IRNode p, String name){
    IRNode n = repository.createNode(p, name);
    return n;
  }
  
  public IRNode getRootNode(){
    return repository.getRootNode();
  }

  public IRNode[] getChildren(IRNode node){

    Version.saveVersion(version);

    int n = tree.numChildren(node);
    IRNode[] children = new IRNode[n];
    for (int i = 0; i < n; i++) {
      children[i] =  tree.getChild(node, i);
    }
    Version.restoreVersion();

    return children;
  }

  public Object getNodeAttr(IRNode node, SlotInfo si){
    Version.saveVersion(version);
    Object val = null;
    if (node.valueExists(si)) {
      val = node.getSlotValue(si);
    }
    Version.restoreVersion();
    return val;
  }


  private static transient Hashtable<Version, Project> openedProjects = new Hashtable<Version, Project>();

  public static Project getProject(ProjectRepository repository, Version v){
    if (openedProjects.contains(v)) return openedProjects.get(v);
    Project p = new Project(repository, v);
    openedProjects.put(v, p);
    return p;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append("project..\n");
    sb.append("version=" + version+"\n");
    return sb.toString();
  }

  private String spaces(int n){
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<n; i++) sb.append(" ");
    return sb.toString();

  }
  private void printTree(int ind, IRNode p){
    System.out.println(spaces(ind) + p.getSlotValue(ProjectRepository.compNameAttr));
    int n = tree.numChildren(p);
    for(int i=0; i<n; i++){
      printTree(ind+2, tree.getChild(p, i));
    }
  }
  void printTree() {
    printTree(2, repository.getRootNode());
  }
}
