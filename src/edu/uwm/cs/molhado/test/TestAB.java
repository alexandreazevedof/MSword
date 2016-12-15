/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

/**
 *
 * @author chengt
 */
public class TestAB {
	
	public static void main(String[] args){
		//B.ensureLoaded();
		for (String string : B.list) {
			System.out.println(string);
		}
		for (String string : A.list) {
			System.out.println(string);
		}
	}

}
