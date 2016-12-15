package edu.uwm.cs.molhado.component;

import edu.cmu.cs.fluid.ir.*;

/**
 * A derived slot info or attribute that will return a component represent
 * a IR node in a project tree.  This slot info can not be used outside the
 * context of a project.  Each project has a slot info and components created
 * by its slot info will always has a reference to the project.  A component
 * is only created once and is held by compHolderAttr.
 *
 * @author chengt
 */
public class ComponentSlotInfo extends DerivedSlotInfo<Component>{

	/* attribute that temporary holds the component */
	private final static SlotInfo<Component> compHolderAttr;

	/* project this attribute belongs to */
	private final Project project;

	protected ComponentSlotInfo(Project project){
		this.project = project;
	}

	static{
		SlotInfo<Component> compHolderAttrX = null;
		try {
			compHolderAttrX = SimpleSlotFactory.prototype.newAttribute("proj.comp.com",
					                  new IRObjectType<Component>());
		} catch (SlotAlreadyRegisteredException ex) {
			ex.printStackTrace();
		}
		compHolderAttr = compHolderAttrX;
	}

	@Override
	protected Component getSlotValue(IRNode node) {
		//System.out.println("Node=" + node);
//		System.out.println(node.getSlotValue(Component.nameAttr));
		if (node.valueExists(compHolderAttr)){
			return  node.getSlotValue(compHolderAttr);
		}
		return Component.getComponent(project, node);
	}

	@Override
	protected boolean valueExists(IRNode node) {
		//if we have no type at the current version, we have no component
		return node.valueExists(Component.typeAttr);
	}

	public static void setComponent(IRNode node, Component comp){
     node.setSlotValue(compHolderAttr, comp);
	}

}
