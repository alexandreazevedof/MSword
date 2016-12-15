/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

import de.schlichtherle.io.File;
import edu.uwm.cs.molhado.component.ProjectManager;
import java.util.Scanner;

/**
 *
 * @author chengt
 */
public class MoApp {

	private ProjectManager manager = ProjectManager.instance;

	private File repoPath;


	public MoApp(){
		File userdir = new File(System.getProperty("user.dir"));
	//	repoPath = new File(userdir, ".molhado")
	}
	
   public static void main(String[] args){
		System.out.println(System.getProperty("user.dir"));
		String a = "hello";
		String b = "hello";
		if (a == b) {
			System.out.println("equal@");
		}

   Scanner scanner = new Scanner(System.in);
		String first = scanner.next();
		String second = scanner.next();
		if (first == second){
			System.out.println("args[0] == args[1]");
		}

	}

}
