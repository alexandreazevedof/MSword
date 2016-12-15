/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import edu.uwm.cs.molhado.version.document.VersionedXmlDocument;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class TestVersionedDocument extends FluidRegistryLoading{

	public static VersionedXmlDocument doc;

	public static void createDoc() throws IOException{
	}



	public static void loadDoc() throws IOException{
		doc = VersionedXmlDocument.open("irdata/test.zip");
	}



	public static void main(String[] args) throws IOException{

		System.out.println(Version.getInitialVersion());
		boolean create = false;
		if (create){
			createDoc();
		} else {
			loadDoc();
		}

		System.out.println("========================");
		System.out.println(doc.getContent());
		System.out.println("========================");

	}

}
