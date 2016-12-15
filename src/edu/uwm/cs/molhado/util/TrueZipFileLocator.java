package edu.uwm.cs.molhado.util;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;
import edu.cmu.cs.fluid.util.FileLocator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author chengt
 */
public class TrueZipFileLocator implements FileLocator {

  private File trueZipFile;

  public TrueZipFileLocator(java.io.File file) {
    this.trueZipFile = new File(file);
    file.mkdir();
  }

  public TrueZipFileLocator(String fname) {
    this.trueZipFile = new File(fname);
    trueZipFile.mkdir();
  }

  public OutputStream openFileWriteOrNull(String name) {
    File file = this.locateFile(name, false);
    if (file == null) {
      return null;
    }
    try {
      return new BufferedOutputStream(new FileOutputStream(file));
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public OutputStream openFileWrite(String name) throws IOException {
    OutputStream os = openFileWriteOrNull(name);
    if (os == null) {
      throw new FileNotFoundException("Could not open " + name + " for writing");
    }
    return os;
  }

  public InputStream openFileReadOrNull(String name) {
    File file = this.locateFile(name, true);
    if (file == null) {
      return null;
    }
    try {
      return new BufferedInputStream(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public InputStream openFileRead(String name) throws IOException {
    InputStream is = openFileReadOrNull(name);
    if (is == null) {
      throw new FileNotFoundException("Could not open " + name + " for reading");
    }
    return is;
  }

  public IRObjectInputStream getObjectInputStream(String name) throws IOException{
	  return new IRObjectInputStream(openFileRead(name));
  }

  public IRObjectOutputStream getObjectOutputStream(String name) throws IOException{
	  return new IRObjectOutputStream(openFileWrite(name));
  }

  public static boolean canRead(File f, boolean dir) {
    return f.exists() && (dir == f.isDirectory());
  }

  public static boolean canCreateOrWrite(File f, boolean dir) {
    if (f.exists()) {
      return (dir == f.isDirectory()) && f.canWrite();
    }
    String p = new File(f.getAbsolutePath()).getParent();
    if (p == null) {
      return false;
    }
    // System.out.println("Looking at " + p);
    File pdir = new File(p);
    boolean possible = canCreateOrWrite(pdir, true);
    if (possible) {
      if (!pdir.exists()) {
        pdir.mkdir();
      }
    }
    return possible;
  }

  public File locateFile(String name, boolean mustExist) {
    File test = new File(trueZipFile, name);
    if (mustExist && canRead(test, false)) {
      return test;
    } else if (!mustExist && canCreateOrWrite(test, false)) {
      return test;
    }
    return null;
  }

  public File[] lisFiles(){
	  return (File[]) trueZipFile.listFiles();
  }

  public File[] listFiles(final String ext){
	  FilenameFilter ff = new FilenameFilter() {
		  public boolean accept(java.io.File dir, String name) {
			  return name.endsWith(ext);
		  }
	  };
	  return  (File[]) trueZipFile.listFiles(ff);
  }

  public void commit() throws IOException {
  }
}
