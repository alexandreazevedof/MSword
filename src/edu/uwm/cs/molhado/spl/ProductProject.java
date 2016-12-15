/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.spl;

import edu.cmu.cs.fluid.version.Version;
import edu.cmu.cs.fluid.version.VersionTracker;
import edu.uwm.cs.molhado.component.Component;
import edu.uwm.cs.molhado.component.DirectoryComponent;
import edu.uwm.cs.molhado.component.Project;
import edu.uwm.cs.molhado.component.SharedXmlComponent;
import edu.uwm.cs.molhado.component.XmlComponent;
import edu.uwm.cs.molhado.test.B;
import edu.uwm.cs.molhado.version.VersionSupport;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author chengt
 */
public class ProductProject extends Project {

	public ProductProject() {
		super();
	}

	public ProductProject(File path, String name) {
		super(path, name);
	}

	protected ProductProject(File path) throws IOException {
		super(path);
	}

	public Component moveToCoreAsset(final Component comp, final CoreAssetProject core, DirectoryComponent toDir, VersionTracker coreTracker) {
		final String name = comp.getName();
		Project A = comp.getProject();
		DirectoryComponent parent = (DirectoryComponent) comp.getParentComponent();

		if (comp instanceof XmlComponent) {
			XmlComponent xmlComp = (XmlComponent) comp;
			final String content = xmlComp.getContent();
			A.removeComponent(comp);

			final XmlComponent[]  c = new XmlComponent[1];
			coreTracker.executeIn(new Runnable() {

				public void run() {
					XmlComponent comp_copy = new XmlComponent(core, core.getRootComponent(), name);
					try {
						comp_copy.updateContent(content);
						c[0] = comp_copy;
						//			((XmlComponent)comp).copyTo(version, comp_copy);
						//remove original component from product
						//create a share component in product that points to the component in the core asset
						//			return comp_copy;
					} catch (Exception ex) {
						Exceptions.printStackTrace(ex);
					}
				}
			});

			//Version.setVersion(coreTracker.getVersion());
			Version.saveVersion(coreTracker.getVersion());
			Version Xv = VersionSupport.commit("", "");
			Version.restoreVersion();
			SharedXmlComponent shared = new SharedXmlComponent(A, parent, c[0], Xv);
			coreTracker.setVersion(Xv);
		} else if (comp instanceof DirectoryComponent) {
		}
		return null;
	}

	public Component moveToCoreAsset(Component comp, CoreAssetProject core, DirectoryComponent toDir) {
		String name = comp.getName();
		Project A = comp.getProject();
		DirectoryComponent parent = (DirectoryComponent) comp.getParentComponent();
		if (parent == null) {
			System.out.println("Parent == null");
			System.exit(0);
		}
		System.out.println("parent=" + parent.getName());

		if (comp instanceof XmlComponent) {
			XmlComponent xmlComp = (XmlComponent) comp;
			String content = xmlComp.getContent();
			A.removeComponent(comp);

			//we need version tracker instead of hard code like this
			Version toIniVersion = toDir.getProject().getInitialVersion();
			Version latest = VersionSupport.getDeepestDescendentInTrunk(toIniVersion);
			Version.saveVersion(latest);
//			XmlComponent comp_copy = new XmlComponent(core, core.getRootComponent(), name);
			XmlComponent comp_copy = new XmlComponent(core, toDir, name);
			try {
				comp_copy.updateContent(content);
				//			((XmlComponent)comp).copyTo(version, comp_copy);
				//remove original component from product
				//create a share component in product that points to the component in the core asset
				//			return comp_copy;
			} catch (Exception ex) {
				Exceptions.printStackTrace(ex);
			}

			//should not commit..  manual commit.
			Version Xv = VersionSupport.commit("", "");
			Version.restoreVersion();

			SharedXmlComponent shared = new SharedXmlComponent(A, parent, comp_copy, Xv);
		} else if (comp instanceof DirectoryComponent) {
		}
		return null;
	}

	public static ProductProject load(File path) throws IOException {
		ProductProject p = (ProductProject) pathProjectMapping.get(path);
		if (p == null) {
			p = new ProductProject(path);
		}
		return p;
	}
}
