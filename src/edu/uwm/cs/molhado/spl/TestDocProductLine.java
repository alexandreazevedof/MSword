package edu.uwm.cs.molhado.spl;

import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.component.XmlComponent;
import java.io.File;
import java.io.IOException;

/**
 *
 * 
 * @author chengt
 */
public class TestDocProductLine {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		String path = "/home/chengt/demo/dita/docpl";

		if (args[0].equals("store")) {

			// creating a product line called MyProductLine and place it in /tmp
			ProductLine productLine = new ProductLine("/home/chengt/demo/dita", "docpl");

			//populate core assets
			CoreAssetProject core = productLine.getCoreAsset();
			Version.setVersion(core.getInitialVersion());
			core.importFiles(new File("ditasample").listFiles());
			Version coreV1 = core.commit("c1", "initial import");

			//deriving a product using a ditamap
			XmlComponent seqditamap = (XmlComponent) core.getComponent(coreV1, "/sequence.ditamap");
			XmlComponent seq_ant = (XmlComponent) core.getComponent(coreV1, "/sequence_pdf.xml");
			ProductProject seqProject = productLine.createDitaProduct(coreV1, "sequence", seqditamap);

			//sharing it with the product
			Version.setVersion(seqProject.getLatestVersion());
			core.share(seq_ant, coreV1, seqProject, seqProject.getRootComponent());
			seqProject.importFile(new File("test.xml"));
			Version vs = seqProject.commit("sequence - ant file", "share ant file");

			//deriving a product using a ditamap
			XmlComponent hiditamap = (XmlComponent) core.getComponent(coreV1, "/hierarchy.ditamap");
			XmlComponent hi_ant = (XmlComponent) core.getComponent(coreV1, "/hierarchy_pdf.xml");
			ProductProject hiProject = productLine.createDitaProduct(coreV1, "hierarchy", hiditamap);

			//sharing the ant build script with product
			Version.setVersion(hiProject.getLatestVersion());
			core.share(hi_ant, coreV1, hiProject, hiProject.getRootComponent());

			hiProject.importFile(new File("document.xml"));
			Version hs = hiProject.commit("hierarachy - ant file", "share ant file");

			//storing the entire product line
			productLine.store();

		} else if (args[0].equals("load")) {

			//loading a product line
			ProductLine productLine = ProductLine.load(new File(path));

			//retrieving products
			ProductProject p1 = productLine.getProduct("sequence");
			ProductProject p2 = productLine.getProduct("hierarchy");

			//exporting files to disk to be built?
			p1.exportToFile(new File(path + "/export"), p1.getLatestVersion());
			p2.exportToFile(new File(path + "/export"), p2.getLatestVersion());

			//print the project tree of product 1
			//p1.printProjectTree();
			p2.printProjectTree();;
		}

	}
}
