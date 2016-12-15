/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

/**
 *
 * @author chengt
 */
public class Property {
  private String name;
  private String value;
  public Property(String name, String value){
    this.name = name;
    this.value = value;
  }
  public String getName(){
    return name;
  }
  public String getValue(){
    return value;
  }

  public boolean equals(Property p){
    return name.equals(p.name) && value.equals(p.value);
  }

  public boolean nameEquals(Property p){
    return name.equals(p.name);
  }

  public boolean valueEquals(Property p){
    return value.equals(p);
  }

  public void setName(String name){
    this.name = name;
  }

  public void  setValue(String val){
    this.value = val;
  }
}
