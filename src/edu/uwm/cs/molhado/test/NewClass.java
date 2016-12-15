/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author chengt
 */
public class NewClass {

	public static void main(String[] args) throws IOException{
		System.out.println( new File("/a/b/", "../c").getCanonicalPath());
		System.out.println(new File("a/b/c/d").getParent());

		System.out.println(new File(new File("a/b/c/d").getParent(), "x/y/z"));

		}

}
