/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.relation;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.netbeans.api.visual.vmd.VMDGraphScene;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;

/**
 *
 * @author chengt
 */
public class VisualLibTest extends JFrame{
	
	public VisualLibTest(){
		setTitle("Test");
		setSize(400, 400);
		JScrollPane scenePane = new JScrollPane();
		add(scenePane);
		VMDGraphScene scene = new VMDGraphScene();
		scenePane.setViewportView(scene.createView());
		VMDNodeWidget node1 = (VMDNodeWidget)scene.addNode("Node 1");
		node1.setNodeName("Node 1");
		VMDNodeWidget node2 = (VMDNodeWidget)scene.addNode("Node 2");
		node2.setNodeName("Node 2");
		VMDNodeWidget node3 = (VMDNodeWidget)scene.addNode("Node 3");
		node3.setNodeName("Node 3");
		VMDPinWidget p1 =(VMDPinWidget)scene.addPin("Node 1", "Pin 1");
		p1.setPinName("Pin 1");
		VMDPinWidget p2 =(VMDPinWidget)scene.addPin("Node 2", "Pin 2");
		p2.setPinName("Pin 2");
		VMDPinWidget p3 =(VMDPinWidget)scene.addPin("Node 2", "Pin 3");
		//p3.setPinName("Pin 3");
		VMDPinWidget p4 =(VMDPinWidget)scene.addPin("Node 3", "Pin 4");
		//p4.setPinName("Pin 4");
		scene.addEdge("Edge 1");

		scene.setEdgeSource("Edge 1", "Pin 1");
		scene.setEdgeTarget("Edge 1", "Pin 2");
		scene.addEdge("Edge 2");
		scene.setEdgeSource("Edge 2", "Pin 3");
		scene.setEdgeTarget("Edge 2", "Pin 4");
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
			
		});
	}
	
	public static void main(String[] args){
		new VisualLibTest().setVisible(true);
	}
	
}
