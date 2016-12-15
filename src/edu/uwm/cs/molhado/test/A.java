/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

import java.util.Date;
import java.util.Vector;

/**
 *
 * @author chengt
 */
public class A {
	public static Vector<String> list = new Vector<String>();
	static{
		list.add("1");
		list.add("2");
		list.add("3");
	}
  public static void main(String[] args){
		System.out.println(new Date());
		System.out.println(System.getProperty("user.name"));
   
 }
}
