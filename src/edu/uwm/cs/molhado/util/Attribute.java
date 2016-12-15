/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

import java.io.Serializable;

/**
 *
 * @author chengt
 */
public class Attribute implements Serializable, Comparable<Attribute>{
    private String name;
    private String value;
    public Attribute(String name, String value){
      this.name = name;
      this.value = value;
    }

		public void setName(String name){
			this.name = name;
		}

		public void setValue(String value){
			this.value =value;
		}

    public int compareTo(Attribute o) {
      return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Attribute){
        Attribute mp = (Attribute) obj;
        return name.equals(mp.name) && value.equals(mp.value);
      }
      return false;
    }

    public boolean equals(String name, String value){
      return this.name.equals(name) && this.value.equals(value);
    }

    public Attribute clone(){
      return new Attribute(name, value);
    }

    public String getName(){return name;}
    public String getValue(){ return value;}


    @Override
    public int hashCode() {
      int hash = 7;
      hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
      hash = 83 * hash + (this.value != null ? this.value.hashCode() : 0);
      return hash;
    }

    public String toString(){
      return "("+name+"="+value+")";
    }

    public static void main(String[] args){
      Attribute a = new Attribute("name", "Cheng Thao");
      Attribute b = new Attribute("name", "Seng Thao");
      System.out.println("a.equals(b) "+ a.equals(b));
      System.out.println("a.compareTo(b) " + a.compareTo(b));
      System.out.println("a.hashCode() "+ a.hashCode());
      System.out.println("b.hashCode() "+ b.hashCode());


      long t0 = System.currentTimeMillis();
      for(int i=0; i<100000; i++){
        boolean f = a.equals(b);
        //boolean f  = a.hashCode() == b.hashCode();
      }
      long t1 = System.currentTimeMillis();
      for(int i=0; i<100000; i++){
        //boolean f = a.equals(b);
        boolean f  = a.hashCode() == b.hashCode();
      }
      long t2 = System.currentTimeMillis();

      System.out.println("first: " + (t1-t0));
      System.out.println("second:" + (t2-t1));
    }

  }