/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.test;

import edu.cmu.cs.fluid.util.UniqueID;
import java.util.Scanner;

/**
 *
 * @author chengt
 */
public class TestUniqueID {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String input = "";
		while(!input.equals("exit")){
			input = scanner.next();
			try{
				System.out.println(UniqueID.parseUniqueID(input).toString());
			}catch(NumberFormatException e){
				e.printStackTrace();
			}
		}
	}
}
