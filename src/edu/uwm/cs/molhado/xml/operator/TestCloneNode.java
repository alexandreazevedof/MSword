package edu.uwm.cs.molhado.xml.operator;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.uwm.cs.molhado.xml.XmlParser;

public class TestCloneNode {
	
	public static void main(String [] args){
	  
		DocumentOp docOp = DocumentOp.prototype;

		ElementOp eo = ElementOp.prototype;
		AttrOp ao = AttrOp.prototype;
		
		IRNode elementA = docOp.createElement("a");
		IRNode attrX = ao.createAttrNode("x", "100");
		IRNode attrY = ao.createAttrNode("y", "200");
		eo.addAttribute(elementA, attrX);
		eo.addAttribute(elementA, attrY);
		
		IRNode elementB = docOp.createElement("b");
		IRNode attrS = ao.createAttrNode("s", "10");
		IRNode attrT = ao.createAttrNode("t", "20");
		eo.addAttribute(elementB, attrS);
		eo.addAttribute(elementB, attrT);
		eo.addChild(elementA, elementB);
		
		IRNode commentNode = docOp.createComment("My comment");
		IRNode textNode = docOp.createTextNode("some text");
		IRNode textNode2 = docOp.createTextNode("more text");
		
		eo.addChild(elementB, commentNode);
		eo.addChild(elementB, textNode);
		eo.addChild(elementB, textNode2);
		
		NodeOp no = (NodeOp) ElementOp.tree.getOperator(elementA);
		IRNode cloneNode = no.cloneNode(true, elementA);
		System.out.println("Done");

		XmlParser.dumpXmlTree(0, eo.tree(), cloneNode);
		
	}

}
