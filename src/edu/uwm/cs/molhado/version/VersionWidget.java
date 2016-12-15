/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uwm.cs.molhado.version;

import edu.cmu.cs.fluid.version.Version;
import edu.uwm.cs.molhado.version.VersionSupport;
import org.netbeans.api.visual.widget.LabelWidget;

/**
 *
 * @author chengt
 */
public class VersionWidget extends LabelWidget{

    private Version version;
    public VersionWidget(VersionGraphScene scene, Version version){
        super(scene);
        this.version = version;
        this.setLabel(toString());
        this.setOrientation(Orientation.ROTATE_90);
    }

    public Version getVersion(){
        return version;
    }


    @Override
    public String toString(){
       return VersionSupport.getVersionNumber(version);
      //return version.toString();
    }




}
