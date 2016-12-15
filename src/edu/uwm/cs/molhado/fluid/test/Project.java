/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.fluid.test;

import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.ConstantSlotFactory;
import edu.cmu.cs.fluid.ir.IRBooleanType;
import edu.cmu.cs.fluid.ir.IRIntegerType;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRNodeType;
import edu.cmu.cs.fluid.ir.IRPersistentReferenceType;
import edu.cmu.cs.fluid.ir.IRStringType;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.IROperatorType;
import edu.cmu.cs.fluid.tree.Operator;
import edu.cmu.cs.fluid.tree.Tree;
import edu.cmu.cs.fluid.version.Era;
import edu.cmu.cs.fluid.version.IRVersionType;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChunk;
import edu.cmu.cs.fluid.version.VersionedRegion;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import java.io.Serializable;

/**
 *
 * @author chengt
 */
@SuppressWarnings("unchecked")
public class Project implements Serializable {

  static {
    /* this code must be called to ensure that the persistent
     * objects (VersionedRegion, VersionChunk, ...) are registered.
     * Else we will get a not registered PersistentKind exception*/
    VersionedRegion.ensureLoaded();
    VersionedChunk.ensureLoaded();
    Era.ensureLoaded();
    Bundle.ensureLoaded();
  }
  private String name;
  private String path;
  private IRNode rootNode;
  private Version rootVersion;
  private VersionedRegion region;
  private final static Bundle bundle = new Bundle();
  private final static Tree tree;
  /* component name */
  private final static SlotInfo<String> componentNameAttr;

  /* root node of component representing root of document */
  private final static SlotInfo<IRNode> componentRootNodeAttr;

  /* region that stores the nodes of a component */
  private final static SlotInfo<VersionedRegion> regionAttr;

  /* date created of component */
  private final static SlotInfo<String> componentCreationDateAttr;
  private final static SlotInfo<String> componentModifiedDateAttr;
  /* author of component */
  private final static SlotInfo<String> componentAuthorAttr;
  private final static SlotInfo<Operator> componentOperatorAttr;
  private final static SlotInfo<String> componentInfoAttr;
  private final static VersionedSlotFactory VSF = VersionedSlotFactory.prototype;
  private final static ConstantSlotFactory CSF = ConstantSlotFactory.prototype;
  private final static IRStringType ST = IRStringType.prototype;
  private final static IRBooleanType BT = IRBooleanType.prototype;
  private final static IRIntegerType IT = IRIntegerType.prototype;
  private final static IRVersionType VT = IRVersionType.prototype;
  

  static {
    Tree t = null;
    SlotInfo<String> componentNameAttrX = null;
    SlotInfo<IRNode> componentRootNodeAttrX = null;
    SlotInfo<VersionedRegion> regionAttrX = null;
    SlotInfo<String> componentCreationDateAttrX = null;
    SlotInfo<String> componentModifiedDateAttrX = null;
    SlotInfo<String> componentAuthorAttrX = null;
    SlotInfo<Operator> componentOperatorAttrX = null;
    SlotInfo<String> componentInfoAttrX = null;
    try {
      t = new Tree("fluid.project.tree", VSF);
      componentNameAttrX = VSF.newAttribute("comp.name", ST);
      componentRootNodeAttrX = VSF.newAttribute("comp.root",
				  IRNodeType.prototype);
      regionAttrX = VSF.newAttribute("comp.region",
				  IRPersistentReferenceType.prototype);
      componentCreationDateAttrX = CSF.newAttribute("comp.creation", ST);
      componentModifiedDateAttrX = VSF.newAttribute("comp.modified", ST);
      componentOperatorAttrX = CSF.newAttribute("comp.op",
				  IROperatorType.prototype);
      componentInfoAttrX = CSF.newAttribute("comp.info", ST, "");
      componentAuthorAttrX = VSF.newAttribute("comp.author", ST);
    } catch (Exception e) {
      e.printStackTrace();
    }
    (tree = t).saveAttributes(bundle);
    bundle.saveAttribute(componentNameAttr = componentNameAttrX);
    bundle.saveAttribute(componentRootNodeAttr = componentRootNodeAttrX);
    bundle.saveAttribute(regionAttr = regionAttrX);
    bundle.saveAttribute(componentCreationDateAttr = componentCreationDateAttrX);
    bundle.saveAttribute(componentModifiedDateAttr = componentModifiedDateAttrX);
    bundle.saveAttribute(componentOperatorAttr = componentOperatorAttrX);
    bundle.saveAttribute(componentInfoAttr = componentInfoAttrX);
    bundle.saveAttribute(componentAuthorAttr = componentAuthorAttrX);
  }

  public Project(String name, String path) {
    this.name = name;
    this.path = path;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  public void load() {
  }

  public void unload() {
  }
}
