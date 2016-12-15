/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author chengt
 */
public class AttributeList implements Serializable{

    private ArrayList<Attribute> list;
    public AttributeList(){ list = new ArrayList(); }
    public AttributeList(int s){ list = new ArrayList<Attribute>(s);}
    public void addAttribute(Attribute attr){
      int i=0;
      for(; i<list.size(); i++){
        if (list.get(i).compareTo(attr) >= 0){
          break;
        }
      }
      list.add(i, attr);
    }

    public void append(Attribute attr){ list.add(attr); }

    public void remove(Attribute attr){
      for(int i=0; i<list.size(); i++){
        if (list.get(i).compareTo(attr) == 0){
          list.remove(i);
          break;
        }
      }
    }

    public void remove(String name){
      for(int i=0; i<list.size(); i++){
        if (list.get(i).getName().equals(name)){
          list.remove(i);
          break;
        }
      }
    }

    public AttributeList clone(){
      AttributeList l = new AttributeList();
      l.list =  (ArrayList<Attribute>) list.clone(); 
      return l;
    }

    public int size(){ return list.size(); }

    public String getName(int i){ return list.get(i).getName(); }

    public String getValue(int i){ return list.get(i).getValue(); }

    public Attribute get(int i){ return list.get(i);}

    public int indexOf(String name){
      for(int i=0; i<list.size(); i++){
        if (name.equals(list.get(i).getName())) return i;
      }
      return -1;
    }

    public int indexOf(Attribute attr){
      for(int i=0; i<list.size(); i++){
        if (attr.getName().equals(list.get(i).getName())) return i;
      }
      return -1;
    }

    public String getValue(String name){
      for(int i=0; i<list.size(); i++){
        Attribute attr = list.get(i);
        if (name.equals(attr.getName())) return attr.getValue();
      }
      return null;
    }

	@Override
    public String toString(){
      return list.toString();
    }

    public int hashCode(){
      return list.hashCode();
    }

    public boolean equals(AttributeList l){
      return list.equals(l.list);
    }

    public static void main(String[] args){
     AttributeList list  = new AttributeList();
     list.addAttribute(new Attribute("name", "Cheng Thao"));
     list.addAttribute(new Attribute("address", "3355 N. Oakland Ave, Apt 205"));
     list.addAttribute(new Attribute("city", "Milwaukee"));
     list.addAttribute(new Attribute("state", "WI"));
     list.addAttribute(new Attribute("zip", "53211"));
     list.addAttribute(new Attribute("country", "USA"));
     System.out.println(list);

     AttributeList list2  = new AttributeList();
     list2.addAttribute(new Attribute("name", "Cheng P. Thao"));
     list2.addAttribute(new Attribute("address", "3355 N. Oakland Ave, Apt 205"));
     list2.addAttribute(new Attribute("city", "Milwaukee"));
     list2.addAttribute(new Attribute("state", "WI"));
     list2.addAttribute(new Attribute("zip", "53211"));
     list2.addAttribute(new Attribute("country", "USA"));
     System.out.println(list2);
     System.out.println(list.equals(list2));

     long t0 = System.currentTimeMillis();
     for(int i=0; i<1000000; i++){
       boolean f = list.equals(list2);
     }
     long t1 = System.currentTimeMillis();
     for(int i=0; i<1000000; i++){
       boolean f = list.hashCode() ==  list2.hashCode();
     }
     long t2 = System.currentTimeMillis();
     for(int i=0; i<1000000; i++){
       boolean f = list.equals(list2);
     }
     long t3 = System.currentTimeMillis();
     for(int i=0; i<1000000; i++){
       boolean f = list.hashCode() ==  list2.hashCode();
     }
     long t4 = System.currentTimeMillis();
      System.out.println("first: " + (t1-t0));
      System.out.println("second:" + (t2-t1));
      System.out.println("third:" + (t3-t2));
      System.out.println("forth:" + (t4-t3));
    }

}
