package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.ir.IRNode;
import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionMarker;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.cmu.cs.fluid.version.VersionedChangeRecord;
import edu.uwm.cs.molhado.component.Component;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.XmlComponent;
import edu.uwm.cs.molhado.merge.FileTreeMerge;
import edu.uwm.cs.molhado.merge.FileTreeMerge.ConflictInfo;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author chengt
 */
public class TestFileStructureChange {


	public static void deleteChange() throws Exception{

		Project p = new Project(new File("/tmp/test"), "root");
		Version v0 = p.getInitialVersion();

		Version.setVersion(v0);
		DirectoryComponent root = p.getRootComponent();
		DirectoryComponent a = new DirectoryComponent(p, root, "a");
		DirectoryComponent b = new DirectoryComponent(p, a, "b");
		DirectoryComponent s = new DirectoryComponent(p, b, "s");
		DirectoryComponent t = new DirectoryComponent(p, s, "t");
		DirectoryComponent u = new DirectoryComponent(p, t, "u");
		DirectoryComponent c = new DirectoryComponent(p, a, "c");
		DirectoryComponent d = new DirectoryComponent(p, a, "d");
		Version v1 = VersionSupport.commit("", "");

		Version.setVersion(v1);
		a.removeChild(b);
		Version v1_1 = VersionSupport.commitAsBranch("", "");

		Version.setVersion(v1);
		u.setName("hello");
		Version v1_2 = VersionSupport.commitAsBranch("", "");

		p.printProjectTree();

		VersionedChangeRecord rc = Component.treeChangeRecord;
		Iterator<IRNode> it = rc.iterator(Component.projectTree, root.getShadowNode(), v1_1, v1);
		while(it.hasNext()){
			IRNode n = it.next();
			Component x = Component.getComponent(p, n);
			System.out.println(x.getName() + " : " + n.toString());
		}

		//p.printProjectTree();
		VersionTracker t1, t0, t2;
		t1 = new VersionMarker(v1_1);
		t0 = new VersionMarker(v1);
		t2 = new VersionMarker(v1_2);

		FileTreeMerge merge = new FileTreeMerge(Component.projectTree,
						 p.getRootComponent().getShadowNode(), t1, t0, t2);
		Version v = merge.merge();
		if (v == null){
			Vector<ConflictInfo> conflicts = merge.getConflicts();
			for(ConflictInfo i:conflicts){
				System.out.println(i.description);
			}
		} else {
			System.out.println("Merge is successful");
		}
	}

	public static void deleteMove() throws Exception{

		Project p = new Project(new File("/tmp/test"), "root");
		Version v0 = p.getInitialVersion();

		Version.setVersion(v0);
		DirectoryComponent root = p.getRootComponent();
		DirectoryComponent a = new DirectoryComponent(p, root, "a");
		DirectoryComponent b = new DirectoryComponent(p, a, "b");
		DirectoryComponent c = new DirectoryComponent(p, a, "c");
		DirectoryComponent d = new DirectoryComponent(p, a, "d");
		Version v1 = VersionSupport.commit("", "");

		Version.setVersion(v1);
		a.removeChild(b);
		d.addChild(b);
		Version v1_1 = VersionSupport.commitAsBranch("", "");

		Version.setVersion(v1);
		a.removeChild(b);
		Version v1_2 = VersionSupport.commitAsBranch("", "");

		VersionedChangeRecord rc = Component.treeChangeRecord;
		Iterator<IRNode> it = rc.iterator(Component.projectTree, root.getShadowNode(), v1_1, v1);
		while(it.hasNext()){
			IRNode n = it.next();
			Component x = Component.getComponent(p, n);
			System.out.println(x.getName() + " : " + n.toString());
		}

		//p.printProjectTree();
		VersionTracker t1, t0, t2;
		t1 = new VersionMarker(v1_1);
		t0 = new VersionMarker(v1);
		t2 = new VersionMarker(v1_2);

		FileTreeMerge merge = new FileTreeMerge(Component.projectTree,
						 p.getRootComponent().getShadowNode(), t1, t0, t2);
		Version v = merge.merge();
		if (v == null){
			Vector<ConflictInfo> conflicts = merge.getConflicts();
			for(ConflictInfo i:conflicts){
				System.out.println(i.description);
			}
		} else {
			System.out.println("Merge is successful");
		}
	}

	public static void moveDifferentParent() throws Exception{
		Project p = new Project(new File("/tmp/test"), "root");
		Version v0 = p.getInitialVersion();

		Version.setVersion(v0);
		DirectoryComponent root = p.getRootComponent();
		DirectoryComponent a = new DirectoryComponent(p, root, "a");
		DirectoryComponent b = new DirectoryComponent(p, a, "b");
		DirectoryComponent c = new DirectoryComponent(p, a, "c");
		DirectoryComponent d = new DirectoryComponent(p, a, "d");
		Version v1 = VersionSupport.commit("", "");

		Version.setVersion(v1);
		a.removeChild(b);
		c.addChild(b);
		Version v1_1 = VersionSupport.commitAsBranch("", "");

		Version.setVersion(v1);
		a.removeChild(b);
		d.addChild(b);
		Version v1_2 = VersionSupport.commitAsBranch("", "");

		VersionedChangeRecord rc = Component.treeChangeRecord;
		Iterator<IRNode> it = rc.iterator(Component.projectTree, root.getShadowNode(), v1_1, v1);
		while(it.hasNext()){
			IRNode n = it.next();
			Component x = Component.getComponent(p, n);
			System.out.println(x.getName() + " : " + n.toString());
		}

		//p.printProjectTree();
		VersionTracker t1, t0, t2;
		t1 = new VersionMarker(v1_1);
		t0 = new VersionMarker(v1);
		t2 = new VersionMarker(v1_2);

		FileTreeMerge merge = new FileTreeMerge(Component.projectTree,
						 p.getRootComponent().getShadowNode(), t1, t0, t2);
		Version v = merge.merge();
		if (v == null){
			Vector<ConflictInfo> conflicts = merge.getConflicts();
			for(ConflictInfo i:conflicts){
				System.out.println(i.description);
			}
		} else {
			System.out.println("Merge is successful");
		}

	}

	public static void structureChange() throws Exception{
		Project A = new Project(new File("/tmp/test"), "root");

		Version v0 = A.getInitialVersion();

		Version.setVersion(v0);
		DirectoryComponent root = A.getRootComponent();
		DirectoryComponent a = new DirectoryComponent(A, root, "a");
		DirectoryComponent b = new DirectoryComponent(A, a, "b");
		Version v1 = VersionSupport.commit("", "");

		DirectoryComponent c = new DirectoryComponent(A, a, "c");
		Version v2 = VersionSupport.commit("", "");

		DirectoryComponent d = new DirectoryComponent(A, b, "d");
		Version v3 = VersionSupport.commit("", "");

		DirectoryComponent e = new DirectoryComponent(A, b, "e");
		Version v4 = VersionSupport.commit("", "");

		DirectoryComponent f = new DirectoryComponent(A, c, "f");
		Version v5 = VersionSupport.commit("", "");

		f.setName("hello");
		Version v6 = VersionSupport.commit("", "");

		Version.saveVersion(v5);
		f.setName("world");
		Version v6b = VersionSupport.commit("", "");

		XmlComponent doc = new XmlComponent(A, f, "doc.xml");
		doc.updateContent("<c/>");
		Version v7 = VersionSupport.commit("", "");

		doc.updateContent("<c molhado:id='0'><d/></c>");
		Version v8 = VersionSupport.commit("", "");

		VersionTracker t1, t0, t2;
		t1 = new VersionMarker(v6);
		t0 = new VersionMarker(v5);
		t2 = new VersionMarker(v6b);

		FileTreeMerge merge = new FileTreeMerge(Component.projectTree,
						 A.getRootComponent().getShadowNode(), t1, t0, t2);
		Version v = merge.merge();
		if (v == null){
			Vector<ConflictInfo> conflicts = merge.getConflicts();
			for(ConflictInfo i:conflicts){
				System.out.println(i.description);
			}
		}
	}

	public static void main(String[] args) throws Exception{
//		structureChange();
//		moveDifferentParent();
//		deleteMove();
		deleteChange();

//		Project A = new Project(new File("/tmp/test"), "test");
//		DirectoryComponent a = A.getRootComponent();
//
//		Version.setVersion(A.getInitialVersion());
//
//		DirectoryComponent b = new DirectoryComponent(A, a, "b");
//		Version v1 = VersionSupport.commit("", "");
//
//		XmlComponent c = new XmlComponent(A, a, "c.xml");
//		c.updateContent("<c/>");
//		Version v2 = VersionSupport.commit("", "");
//
//		b.setName("X");
//		Version v3 = VersionSupport.commit("", "");
//
//		c.updateContent("<c molhado:id='0'><d/></c>");
//		Version v4 = VersionSupport.commit("", "");
//
//		DirectoryComponent d = new DirectoryComponent(A, a, "d");
//		Version v5 = VersionSupport.commit("", "");
////		A.printProjectTree();
//
//		VersionedChangeRecord rc = Component.treeChangeRecord;
//		Iterator<IRNode> it = rc.iterator(Component.projectTree, a.getShadowNode(), v2, v3);
//		while(it.hasNext()){
//			IRNode n = it.next();
//			Component x = Component.getComponent(A, n);
//			System.out.println(x.getName());
//		}
	}
}
