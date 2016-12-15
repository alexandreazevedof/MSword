package edu.uwm.cs.molhado.merge;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import java.util.Vector;

/**
 *
 * @author chengt
 */
public class NodeChunk {
  private IRNode node;
  private Chunk chunk;
  /* versions are useful to determine some conflicts
     such as a node that has been moved in both v1 and v2.
   * This may seems like a delete from both tree (non conflict)
   * unless we look to see if the node is really moved and if so
   * are the parents the same, are they in the same position in
   * the new parent.
   */
  private Version v0;
  private Version v1;
  private Version v2;

  public NodeChunk(IRNode n, Chunk c, Version v1, Version v0, Version v2){
    node = n;
    chunk =c;
  }
  public IRNode getNode(){
    return node;
  }
  public Chunk getChunk(){
    return chunk;
  }
  public Vector<IRNode> getConflictingNodes(){
    return null;
  }
}
