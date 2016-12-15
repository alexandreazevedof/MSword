package edu.uwm.cs.molhado.version;

import edu.cmu.cs.fluid.util.*;
import edu.cmu.cs.fluid.version.*;
import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.*;

/**
 *
 * @author chengt
 */
public class TestVersionTree extends FluidRegistryLoading{

  static FileLocator floc = new DirectoryFileLocator("/tmp/ir/");
  static String projInfo = "proj.info";
  static String eraName = "era1";
  static Era era;
  static Version root;

	public static void testConvertBranchToTrunk(){

		Version.bumpVersion();
		Version first = root = VersionSupport.commit("first", "");
		Version.bumpVersion();
		Version second = VersionSupport.commit("second", "");
		Version.bumpVersion();
		Version third = VersionSupport.commit("third", "");
		Version.bumpVersion();
		Version forth = VersionSupport.commitAsBranch("forth", "forth");
		Version.bumpVersion();
		Version fith = VersionSupport.commit("fith", "fith");
		Version.bumpVersion();
		Version sixth= VersionSupport.commit("sixth", "sixth");
		Version.bumpVersion();
		Version sixth_b = VersionSupport.commitAsBranch("sixthb", "sixthb");
		Version.setVersion(sixth);
		Version.bumpVersion();
		Version seventh= VersionSupport.commit("seventh", "seventh");
		//Version.bumpVersion();

		VersionSupport.putPathInTrunk(third, seventh);

	}

	public static void testDeepestCommonAncestor(){

		Version.bumpVersion();
		Version first = VersionSupport.commit("first", "");
		Version.bumpVersion();
		Version second = VersionSupport.commit("second", "");
		Version.bumpVersion();
		Version third = VersionSupport.commit("third", "");
		Version.bumpVersion();
		Version forth = VersionSupport.commitAsBranch("forth", "forth");
		Version.bumpVersion();
		Version fith = VersionSupport.commitAsBranch("fith", "fith");

		Version.setVersion(second);
		Version.bumpVersion();
		Version secondb = VersionSupport.commitAsBranch("seond-branch","");

		Version v = Version.latestCommonAncestor(third, secondb);
		root = first;

		Version t = VersionSupport.getParentVersion(third);
		if (t == second){
			System.out.println("parent version si correct);");
		}
		if (v == second) {
			System.out.println("Correct");
		} else if (v == first){
			System.out.println("Wrong");
		}

		v = Version.latestCommonAncestor(second, third);
		if (second == v){
			System.out.println("Correct");
		} else if (v == third){
			System.out.println("Wrong");
		}

		Version deepest = VersionSupport.getDeepestDescendentInTrunk(root);
		if (deepest == third){
			System.out.println("Correct");
		}

		System.out.println(VersionSupport.getVersionNumber(deepest));

	}



  public static void createStructure() throws OverlappingEraException {
    Version init = Version.getInitialVersion();

    era = new Era(init);
    Version.setDefaultEra(era);

    Version.bumpVersion();
    Version v11 = VersionSupport.commit("initial", "intial version");
    root = v11;

    Version.bumpVersion();
    Version v12 = VersionSupport.commit("v2", "minor fix");

    Version.bumpVersion();
    Version v13 = VersionSupport.commit("v3", "some files deleted.");

    Version.bumpVersion();
    Version v21 = VersionSupport.commit(2, "rc1", "release candidate");

    Version.setVersion(v11);
    Version.bumpVersion();
    Version v1111 = VersionSupport.commitAsBranch("b1", "experimental code");

    Version.bumpVersion();
    Version v1112 = VersionSupport.commit("b2", "x is added.");

    Version.bumpVersion();
    Version v1213 = VersionSupport.commit("b2.1", "minor changes");

    Version.setVersion(v11);
    Version.bumpVersion();
    Version v1121 = VersionSupport.commitAsBranch("b3", "for XP");

    Version v22 = VersionSupport.merge(v1112, v21, "release 2.0", "merge of feature X");

    Version.setVersion(v22);
    Version.bumpVersion();
    Version v31 = VersionSupport.commit(3, "release 2", "Release 2");

    Version.setVersion(v13);
    Version.bumpVersion();
    Version v32 = VersionSupport.commit("tag", "log");

	  Version.bumpVersion();
	  Version v321 = VersionSupport.commitAsBranch("test", "test");

	  VersionSupport.putInTrunk(v321);



  }

  public static void storeStructure() throws OverlappingEraException, IOException {
    createStructure();
    era.setName(eraName);
    era.store(floc);
    OutputStream os = floc.openFileWrite(projInfo);
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(root);
    oos.flush();
    oos.close();
  }

  public static void loadStructure() throws IOException, ClassNotFoundException {
    UniqueID id = UniqueID.parseUniqueID(eraName);
    era = Era.loadEra(id, floc);
    InputStream os = floc.openFileRead(projInfo);
    ObjectInputStream ois = new ObjectInputStream(os);
    root = (Version) ois.readObject();
    ois.close();
  }

  public static void main(String[] args) throws OverlappingEraException, 
          IOException, ClassNotFoundException {

		testConvertBranchToTrunk();
//		testDeepestCommonAncestor();
//		createStructure();
//    String title = args[0];
//    if (args[0].equals("create")) {
//      createStructure();
//    } else if (args[0].equals("load")) {
//      loadStructure();
//    } else if (args[0].equals("store")) {
//      storeStructure();
//    } else{
//      return;
//    }

    printVersionTree(2, root);

    VersionGraphViewForm f = new VersionGraphViewForm(root);
//    f.setTitle(title);
    f.setVisible(true);

  }

  public static String spaces(int sp) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sp; i++) sb.append(" ");
    return sb.toString();
  }

  public static void printVersionTree(int indent, Version v) {
    System.out.println(spaces(indent) + VersionSupport.getVersionNumber(v) + ":" +
            VersionSupport.getTimeStamp(v) + ":" + VersionSupport.getLog(v));
    for (Version version : VersionSupport.getChildren(v)) {
      printVersionTree(indent + 2, version);
    }
  }

}
