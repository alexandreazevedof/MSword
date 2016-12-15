/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.version;

import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.version.VersionSupport;

/**
 *
 * @author chengt
 */
public class VersionEdge{
    private Version parent;
    private Version child;

    public VersionEdge(Version parent, Version child){
        this.parent = parent;
        this.child = child;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof VersionEdge)) return false;
        VersionEdge edge = (VersionEdge) obj;
        return parent == edge.parent && child == edge.child;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        hash = 59 * hash + (this.child != null ? this.child.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString(){
        return VersionSupport.getVersionNumber(parent) + '-' + VersionSupport.getVersionNumber(child);
    }
}