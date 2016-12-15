/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.uwm.cs.molhado.xml.simple.SimpleXmlParser3;

/**
 *
 * @author chengt
 */
public class TestParser3 {


	public static void testWithIDs() throws Exception{
		String orignal = "<a molhado:id='0'><c molhado:id='2'/><b molhado:id='3'/><d molhado:id='1'/></a>";

		SimpleXmlParser3 p = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);
		IRNode root = p.parse(orignal);
		
		System.out.println(SimpleXmlParser3.toStringWithID(root));


	}


	public static void testSync() throws Exception{

		String orignal = "<a molhado:id='0'><b molhado:id='1'/></a>";

		SimpleXmlParser3 p = new SimpleXmlParser3(0, SimpleXmlParser3.NORMAL_PARSING);
		IRNode root = p.parse(orignal);
		
		System.out.println(SimpleXmlParser3.toStringWithID(root));
		System.out.println("=======================");

		String modified = "<X molhado:id='0'><x/><y/><b molhado:id='1'/></X>";
		p.parse(root, modified);

		System.out.println(SimpleXmlParser3.toStringWithID(root));
		System.out.println("=======================");
		
	}
	
	public static void main(String[] args) throws Exception{
//		testSync();
		testWithIDs();

		/*
		SimpleXmlParser3 p1 = new SimpleXmlParser3(0);
		SimpleXmlParser3 p2 = new SimpleXmlParser3(0);

		IRNode n = p1.parse("<a><b/></a>");
		String r = SimpleXmlParser3.toStringWithID(n);

		System.out.println(r);

		System.out.println("================");

		n = p2.parse(r);
		System.out.println(SimpleXmlParser3.toStringWithID(n));
		p2.parse(n, "<a molhado:id='0'><b molhado:id='1'/><c/></a>");
		System.out.println(SimpleXmlParser3.toStringWithID(n));
*/

	}

}
