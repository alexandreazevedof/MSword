/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.test;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.fasterxml.uuid.impl.UUIDUtil;
import java.util.UUID;

/**
 *
 * @author chengt
 */
public class TestUIDGen {

	public static void main(String[] args){

		EthernetAddress ethernet_address = EthernetAddress.fromInterface();
		TimeBasedGenerator uuid_gen = Generators.timeBasedGenerator(ethernet_address);

		for (int i = 0; i < 100; i++) {
			UUID uuid = uuid_gen.generate();
			System.out.println(uuid);
		}
	}
}
