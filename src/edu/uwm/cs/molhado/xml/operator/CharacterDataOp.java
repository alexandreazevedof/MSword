package edu.uwm.cs.molhado.xml.operator;

import java.util.HashSet;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.tree.Operator;
import edu.cmu.cs.fluid.tree.SyntaxTreeInterface;
import edu.uwm.cs.molhado.xml.XmlParser;

public class CharacterDataOp extends NodeOp {

	public static final CharacterDataOp prototype = new CharacterDataOp();
	private HashSet<SlotInfo> slotInfos;

	@Override
	public Operator superOperator() {
		return NodeOp.prototype;
	}

	protected static final SlotInfo<String> textValueAttr = XmlParser.textValueAttr;

	protected CharacterDataOp() {
	}

	public IRNode createCharacterDataNode(String data){
		return createCharacterDataNode(tree, data);
	}
	
	public IRNode createCharacterDataNode(SyntaxTreeInterface tree, String data) {
		IRNode node = createNode(tree);
		node.setSlotValue(textValueAttr, data);
		return node;
	}

	public String getData(IRNode node) {
		return getValue2(node, textValueAttr);
	}

	public void setData(IRNode node, String data) {
		setValue2(node, textValueAttr, data);
	}

	/* what's this code doing here?
	public Set<SlotInfo> getAttributes() {
		if (slotInfos == null) {
			HashSet<SlotInfo> slotInfos = new HashSet<SlotInfo>();
			slotInfos.add(textValueSlotInfo);
		}
		return slotInfos;
	}
	*/

	@Override
	public IRNode cloneNode(boolean deep, IRNode node){
		String data = prototype.getData(node);
		return prototype.createCharacterDataNode(data);			
	}
}
