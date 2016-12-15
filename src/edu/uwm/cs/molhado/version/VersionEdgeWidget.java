/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.version;

import org.netbeans.api.visual.widget.ConnectionWidget;

/**
 *
 * @author chengt
 */
public class VersionEdgeWidget extends ConnectionWidget{

    private VersionEdge edge;

    public VersionEdgeWidget(VersionGraphScene scene, VersionEdge edge){
        super(scene);
        this.edge = edge;
    }

    @Override
    public String toString(){
        return edge.toString();
    }
}