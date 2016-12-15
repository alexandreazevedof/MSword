/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;


/**
 *
 * @author chengt
 */
public class SubstringTest {


	public static void main(String[] args){
		String s = "[1,2,3,4]";
		String ss = s.substring(1, s.length()-1);
		System.out.println(ss);
		String[] result = ss.split(",");
		for(String e:result){
			System.out.println(e);
		}
	}

}
