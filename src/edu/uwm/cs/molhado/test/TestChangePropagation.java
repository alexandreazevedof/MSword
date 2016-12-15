package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.SharedComponent;
import edu.uwm.cs.molhado.component.SharedXmlComponent;
import edu.uwm.cs.molhado.component.XmlComponent;
import edu.uwm.cs.molhado.spl.CoreAssetProject;
import edu.uwm.cs.molhado.spl.ProductProject;
import edu.uwm.cs.molhado.version.VersionGraphViewForm;
import java.io.File;

/**
 *
 * @author chengt
 */
public class TestChangePropagation {

	private static File pathX = new File("/tmp/irdata/pX");
	private static File pathA = new File("/tmp/irdata/pA");
	private static File pathB = new File("/tmp/irdata/pB");
	private static File pathC = new File("/tmp/irdata/pC");
	static CoreAssetProject X;
	static ProductProject A, B, C;

	/**
	 * Forward changes from core asset to product's copy which has not changed.
	 * core  | prod         core  |  prod
	 * ------------------------------------
	 * a'    | a       =>   a'    |  a'
	 * @throws Exception
	 */
	public static void forwardChange() throws Exception {
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");

		Version.setVersion(X.getInitialVersion());
		XmlComponent comp = new XmlComponent(X, X.getRootComponent(), "test.xml");
		comp.updateContent("<a></a>");
		Version Xv1 = X.commit("", "");

		Version.setVersion(A.getInitialVersion());
		SharedComponent a_sc = new SharedXmlComponent(A, A.getRootComponent(), comp, Xv1);
		Version Av1 = A.commit("", "");

		Version.setVersion(Xv1);
		comp.updateContent("<a molhado:id='0'><b/></a>");
		Version Xv2 = X.commit("", "");

		Version.setVersion(Av1);
		a_sc.forwardChange();
		Version Av2 = A.commit("", "");

		A.printProjectTree();
	}

	/**
	 * Forward changes from core asset to product's copy which has changed.
	 * The result is a merge of the two copies.
	 * core  | prod         core  |  prod
	 * ------------------------------------
	 * a'    | a*      =>   a'    |  a*'
	 * @throws Exception
	 */
	public static void forwardChangeWithMerge() throws Exception {
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");

		Version.setVersion(X.getInitialVersion());
		XmlComponent comp = new XmlComponent(X, X.getRootComponent(), "test.xml");
		comp.updateContent("<a><b/></a>");
		Version Xv1 = X.commit("X1", "X1");

		comp.updateContent("<a molhado:id='0'><b molhado:id='1'/><d><e/></d></a>");
		Version Xv2 = X.commit("X2", "X2");
		comp.dumpContent();
		System.out.println("=======================");

		Version.setVersion(A.getInitialVersion());
		SharedXmlComponent a_sc = new SharedXmlComponent(A, A.getRootComponent(), comp, Xv1);
		Version Av1 = A.commit("A1", "A1");

		Version.setVersion(Av1);
		a_sc.updateContent("<a molhado:id='0'><b molhado:id='1'><c/></b></a>");
		Version Av2 = A.commit("A2", "A2");

		Version.setVersion(Av2);
		a_sc.forwardChange();
		Version Av3 = A.commit("A3", "A3");
		//A.printProjectTree();
		X.printProjectTree();

		//	a_sc.dumpContent();
//		Version.saveVersion(VersionSupport.getDeepestDescendentInTrunk(X.getInitialVersion()));
//		comp.dumpContent();

		//	A.store();
		//	X.store();
		//	X.printProjectTree();

		System.out.println("========================================");
		A.printProjectTree();

//		VersionGraphViewForm f = new VersionGraphViewForm(X.getInitialVersion());
//    f.setTitle(X.getName());
//		f.setVisible(true);
//		VersionGraphViewForm f2 = new VersionGraphViewForm(A.getInitialVersion());
//    f2.setTitle(A.getName());
//		f2.setVisible(true);
		VersionGraphViewForm f3 = new VersionGraphViewForm(Version.getInitialVersion());
		f3.setTitle(A.getName());
		f3.setVisible(true);
	}

	/**
	 * Shared a new core asset with a product
	 * core  | prod         core  |  prod
	 * ------------------------------------
	 * a     |         =>   a     |  a
	 * @throws Exception
	 */
	public static void CoreProj() throws Exception {
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");

		Version.setVersion(X.getInitialVersion());
		XmlComponent comp = new XmlComponent(X, X.getRootComponent(), "test.xml");
		comp.updateContent("<a></a>");
		Version Xv1 = X.commit("", "");

		Version.setVersion(A.getInitialVersion());
		X.share( //core asset
						comp, //core asset's component
						Xv1,  //core asset's latest version
						A, //product project
						A.getRootComponent()); //path in product
		Version Av1 = A.commit("A1", "A1");

		A.printProjectTree();

	}

	public static void forwardChangeOverriding() throws Exception{
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");
		Version.setVersion(X.getInitialVersion());
		XmlComponent comp = new XmlComponent(X, X.getRootComponent(), "test.xml");
		comp.updateContent("<a></a>");
		Version Xv1 = X.commit("", "");
		comp.updateContent("<a molhado:id='0'><b/></a>");
		Version Xv2 = X.commit("", "");

		Version.setVersion(A.getInitialVersion());
		SharedXmlComponent a_sc = new SharedXmlComponent(A, A.getRootComponent(), comp, Xv1);
		Version Av1 = A.commit("", "");
		a_sc.updateContent("<a molhado:id='0'><c/></a>");
		Version Av2 = A.commit("","");

		a_sc.forwardChangeWithOverride();
		A.commit("", "");

	  A.printProjectTree();
		System.out.println("================================");
		X.printProjectTree();
	}

	public static void productRevertVersion() throws Exception {
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");
		Version.setVersion(X.getInitialVersion());
		XmlComponent comp = new XmlComponent(X, X.getRootComponent(), "test.xml");
		comp.updateContent("<a></a>");
		Version Xv1 = X.commit("", "");
		comp.updateContent("<a molhado:id='0'><b/></a>");
		Version Xv2 = X.commit("", "");

		Version.setVersion(A.getInitialVersion());
		SharedXmlComponent a_sc = new SharedXmlComponent(A, A.getRootComponent(), comp, Xv1);
		Version Av1 = A.commit("", "");
		a_sc.updateContent("<a molhado:id='0'><c/></a>");
		Version Av2 = A.commit("","");
		a_sc.updateContent("<a molhado:id='0'><c molhado:id='1'/><d/></a>");
		A.commit("","");

//		a_sc.revertVersionInProduct();
//		A.commit("", "");

	  A.printProjectTree();
		System.out.println("================================");
		//X.printProjectTree();
	}

	public static void backwardPropagationCase2() throws Exception {
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");

		Version.setVersion(X.getInitialVersion());
		XmlComponent comp = new XmlComponent(X, X.getRootComponent(), "test.xml");
		comp.updateContent("<a><b/></a>");
		Version Xv1 = X.commit("X1", "X1");

		comp.updateContent("<a molhado:id='0'><b molhado:id='1'/><d><e/></d></a>");
		Version Xv2 = X.commit("X2", "X2");
		comp.dumpContent();
		System.out.println("=======================");

		Version.setVersion(A.getInitialVersion());
		SharedXmlComponent a_sc = new SharedXmlComponent(A, A.getRootComponent(), comp, Xv1);
		Version Av1 = A.commit("A1", "A1");

		Version.setVersion(Av1);
		a_sc.updateContent("<a molhado:id='0'><b molhado:id='1'><c/></b></a>");
		Version Av2 = A.commit("A2", "A2");

		Version.setVersion(Av2);
		a_sc.backwardChangePropagation();
		Version Av3 = A.commit("A3", "A3");
		//A.printProjectTree();
		X.printProjectTree();

		//	a_sc.dumpContent();
//		Version.saveVersion(VersionSupport.getDeepestDescendentInTrunk(X.getInitialVersion()));
//		comp.dumpContent();

		//	A.store();
		//	X.store();
		//	X.printProjectTree();

		System.out.println("========================================");
		A.printProjectTree();

//		VersionGraphViewForm f = new VersionGraphViewForm(X.getInitialVersion());
//    f.setTitle(X.getName());
//		f.setVisible(true);
//		VersionGraphViewForm f2 = new VersionGraphViewForm(A.getInitialVersion());
//    f2.setTitle(A.getName());
//		f2.setVisible(true);
		VersionGraphViewForm f3 = new VersionGraphViewForm(Version.getInitialVersion());
		f3.setTitle(A.getName());
		f3.setVisible(true);
	}

	public static void backwardPropagationCase1() throws Exception {
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");

		Version.setVersion(X.getInitialVersion());
		XmlComponent comp = new XmlComponent(X, X.getRootComponent(), "test.xml");
		comp.updateContent("<a></a>");
		Version Xv1 = X.commit("", "");

		Version.setVersion(A.getInitialVersion());
		SharedXmlComponent shared = new SharedXmlComponent(A, A.getRootComponent(), comp, Xv1);
		Version Av1 = A.commit("", "");

		Version.setVersion(Av1);
		//update content on a shared component will result in a branch
		shared.updateContent("<a molhado:id='0'><b/></a>");
		Version Av2 = A.commit("", "");

		//now we are going to do a back propagate
		Version.setVersion(Av2);
		shared.backwardChangePropagation();
		Version Av3 = A.commit("", "");

		X.printProjectTree();
		System.out.println("=======================================");
		A.printProjectTree();

	}

	public static void backwardPropagationCase3() throws Exception {
		X = new CoreAssetProject(pathX, "pX");
		A = new ProductProject(pathA, "pA");

		Version.setVersion(A.getInitialVersion());
		XmlComponent comp = new XmlComponent(A, A.getRootComponent(), "test.xml");
		comp.updateContent("<a></a>");
		Version Av1 = A.commit("", "");

		//should really set version to latest trunk version
		//Version.setVersion(X.getInitialVersion());
		//Version.setVersion(Av1);
		A.moveToCoreAsset(comp, X, X.getRootComponent());
		A.commit("", "");
		//Version Xv1 = X.commit("", "");

		X.printProjectTree();
		System.out.println("===========================================");
		//A.printProjectTree();
	}


	public static void coreAssetRevertVersion(){

	}

	public static void main(String[] args) throws Exception {
		//forwardChange();
		//forwardChangeWithMerge();
		//CoreProj();
		//forwardChangeOverriding();
		productRevertVersion();
		//backwardPropagationCase1();
		//backwardPropagationCase2();
		//backwardPropagationCase3();
	}
}
