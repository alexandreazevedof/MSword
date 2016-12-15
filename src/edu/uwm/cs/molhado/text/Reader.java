/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.text;

import com.qarks.util.files.diff.Diff;
import com.qarks.util.files.diff.MergeResult;
import edu.cmu.cs.fluid.ir.Bundle;
import edu.cmu.cs.fluid.ir.ConstantSlotFactory;
import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.ir.IRNodeType;
import edu.cmu.cs.fluid.ir.IRSequence;
import edu.cmu.cs.fluid.ir.IRSequenceType;
import edu.cmu.cs.fluid.ir.IRStringType;
import edu.cmu.cs.fluid.ir.PlainIRNode;
import edu.cmu.cs.fluid.ir.SimpleSlotFactory;
import edu.cmu.cs.fluid.ir.SlotInfo;
import edu.cmu.cs.fluid.util.UniqueID;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.cmu.cs.fluid.version.VersionedSlotFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class Reader {

	public final static SlotInfo<String> textLineAttr;
	public final static SlotInfo<IRSequence<IRNode>> lineSeqAttr;
	public final static Bundle bundle = new Bundle();
	public final static VersionedSlotFactory vsf = VersionedSlotFactory.prototype;
	public final static ConstantSlotFactory csf = ConstantSlotFactory.prototype;
	public final static SimpleSlotFactory ssf = SimpleSlotFactory.prototype;

	static{
		SlotInfo<String> textLineAttrX = null;
		SlotInfo<IRSequence<IRNode>> lineSeqAttrX = null;
		try{
			textLineAttrX = vsf.newAttribute("doc.txt.line", IRStringType.prototype);
			lineSeqAttrX= vsf.newAttribute("doc.txt.seq.line", new IRSequenceType(IRNodeType.prototype));
		} catch (Exception e) {
			Exceptions.printStackTrace(e);
		}
		bundle.setName("ttbundle");
		bundle.saveAttribute(textLineAttr = textLineAttrX); 
		bundle.saveAttribute(lineSeqAttr = lineSeqAttrX);
	}


	public static IRNode createNode(String line){
		IRNode node = new PlainIRNode();
		node.setSlotValue(textLineAttr, line);
		return node;
	}
	
	public static IRNode readFile(String file) throws FileNotFoundException{
		IRNode docNode = new PlainIRNode();
		IRSequence<IRNode> sq = VersionedSlotFactory.prototype.newSequence(-1);
		docNode.setSlotValue(lineSeqAttr, sq);
		Scanner scanner = new Scanner(new File(file));
		while(scanner.hasNextLine()){
			IRNode n = createNode(scanner.nextLine()+"\n");
			sq.appendElement(n);
		}
		scanner.close();
		return docNode;
	}

	public static IRNode readString(String s) {
		IRNode docNode = new PlainIRNode();
		IRSequence<IRNode> sq = VersionedSlotFactory.prototype.newSequence(-1);
		docNode.setSlotValue(lineSeqAttr, sq);
		Scanner scanner = new Scanner(s);
		while(scanner.hasNextLine()){
			IRNode n = createNode(scanner.nextLine()+"\n");
			sq.appendElement(n);
		}
		scanner.close();
		return docNode;
	}

	public static void update(IRNode root, String s){
		HashMap<String, IRNode> map = new HashMap<String, IRNode>();
		IRSequence<IRNode>  seq = root.getSlotValue(lineSeqAttr);
		IRSequence<IRNode> seq2 = VersionedSlotFactory.prototype.newSequence(-1);
		root.setSlotValue(lineSeqAttr, seq2);
		int n = seq.size();
		for(int i=0; i<n; i++){
			IRNode node = seq.elementAt(i);
			IRNode x = map.put(node.getSlotValue(textLineAttr), node);
		}
		Scanner scanner = new Scanner(s);
		while(scanner.hasNextLine()){
			String line = scanner.nextLine()+ "\n";
			IRNode node = map.get(line);
			if (node == null){
				node = createNode(line);
			}
			seq2.appendElement(node);
		}
	}


	public static String getText(Version v, IRNode doc){
		Version.saveVersion(v);
		String txt = getText(doc);
		Version.restoreVersion();
	   return txt;
	}

	public static String getText(IRNode doc){
		IRSequence<IRNode>  seq = doc.getSlotValue(lineSeqAttr);
		StringBuilder sb = new StringBuilder();
		int n = seq.size();
		for(int i=0; i<n; i++){
			IRNode node = seq.elementAt(i);
			sb.append(node.getSlotValue(textLineAttr));
		}
		return sb.toString();
	}

	public static void writeToFile(File f, IRNode doc){
		
	}
	
	public static String merge3(String v0, String v1, String v2){
		MergeResult result = Diff.quickMerge(v0, v1, v2, true);
		return result.getDefaultMergedResult();
	}
	

	public static void main(String[] args){
		String s0 = "a\nb\nc\nd\ne";
		String s1 = "a\nc\nd\ne";
		String s2 = "a\nb\nc\nd";

		IRNode doc = readString(s0);
		Version v0 = Version.getVersion();

		update(doc, s1);
		Version v1 = Version.getVersion();

		Version.setVersion(v0);
		update(doc, s2);
		Version v2 = Version.getVersion();

		Version.setVersion(v2);
		String s3 = merge3(s0, s1, s2);
		update(doc, s3);
		Version v3 = Version.getVersion();
		
		Version.saveVersion(v0);
		System.out.println("==v0===");
		System.out.println(getText(doc));

		Version.saveVersion(v1);
		System.out.println("==v1===");
		System.out.println(getText(doc));

		Version.saveVersion(v2);
		System.out.println("==v2===");
		System.out.println(getText(doc));

		Version.saveVersion(v3);
		System.out.println("==v3===");
		System.out.println(getText(doc));
	}
	

//	public static void main(String[] args){
//		String s = "Hello\nworld";
//		Version.setVersion(Version.getInitialVersion());
//		IRNode doc = readString(s);
//		Version v1 = Version.getVersion();
//		update(doc, "Cow\nSays\nHello\nworld\nand Good Bye!\nHello");
//		Version v2 = Version.getVersion();
//
//		System.out.println("=v2===============");
//		System.out.print(getText(doc));
//		System.out.println("=v1===============");
//		Version.saveVersion(v1);
//		System.out.print(getText(doc));
//	}
	
}
