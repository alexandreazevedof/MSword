/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.project;

import edu.uwm.cs.molhado.component.FluidRegistryLoading;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class TestProjectRepo extends FluidRegistryLoading {

  public static void store(File f) throws IOException {
    ProjectRepository pr = new ProjectRepository(f, "test");
    pr.store();
  }

  public static void load(File f) throws IOException{
    ProjectRepository pr = ProjectRepository.load(f);
  }

  public static void main(String[] args) throws IOException {
    File f1 = new File("/tmp/ir/");
    File f2 = new File("/tmp/ir/test");
    store(f1);
    load(f2);
    
  }
}
