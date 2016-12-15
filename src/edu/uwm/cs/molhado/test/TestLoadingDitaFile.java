/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.util.Iteratable;
import edu.uwm.cs.molhado.util.Property;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author chengt
 */
public class TestLoadingDitaFile {

	static SimpleXmlParser3 parser = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);
  static String f1 = "ditasample/hierarchy.ditamap";



	private static void collectLinks(IRNode node, ArrayList<String> links){
		String link = SimpleXmlParser3.getAttributeValue(node, "href");
		if (link !=null) links.add(link);
		int nc = SimpleXmlParser3.tree.numChildren(node);
		if (nc > 0) {
			Iteratable<IRNode> it = SimpleXmlParser3.tree.children(node);
			while(it.hasNext()){
				collectLinks(it.next(), links);
			}
		}
	}

	public static void main(String[] args) throws Exception{
		IRNode root = parser.parse(new File(f1));
		System.out.println(parser.toStringWithID(root));
		ArrayList<String> links = new ArrayList<String>();
		collectLinks(root, links);
		for(String link:links){
			System.out.println("ditasample/" + link);
		}
	}


}
