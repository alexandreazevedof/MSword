/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.util;

/**
 *
 * @author chengt
 */
public class StopWatch{
  private long startTime;
  private long endTime;
  public void start(){ startTime = System.nanoTime(); }
  public void stop(){ endTime = System.nanoTime(); }
  public long nanoElapse(){ return endTime - startTime; }
  public long elapsed(){ return nanoElapse()/1000000; }
}