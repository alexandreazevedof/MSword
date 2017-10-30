/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uwm.cs.molhado.merge;

import static edu.uwm.cs.molhado.xml.simple.SimpleXmlParser.createVIDs;
import static edu.uwm.cs.molhado.xml.simple.SimpleXmlParser.mergeVIDs;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;
import java.util.Enumeration;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.FileDialog;
import java.io.FilenameFilter;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
//import tdm.tool.TreeDiffMerge;
import java.util.UUID;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.lang.ClassNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author agaze
 */
public class WordMerge {

    static JFrame frame;
    static File baseXml, branch1Xml, branch2Xml, documentXml;
    static long startTime, endTime;
        
    // EXIT CODES
    final static int EXIT_SUCCESS = 0;
    final static int EXIT_GENERALERROR = 1;
    final static int EXIT_MISSINGREQUIREDINPUT = 2;
    final static int EXIT_IOFILESYSTEMERROR = 3;
    final static int EXIT_XMLERROR = 4;

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("System look and feel not found or identified.");
        }
        frame = new JFrame();
        String base;
        
        if(args[0].equals("True")){
//        if(false){
            if (args.length > 1 && new File(args[1]).isFile()) {
                base = getDocx("base");
                try {
                    baseXml = extractXml(base, "test", "word/document.xml");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                base = args[2]; //looks for base as second argument if it exists
            }else{
                base = getDocx("base"); //get base docx
            }
            
            try { //extract base xml file from base docx
                baseXml = extractXml(base, "base", "word/document.xml");
            } catch (IOException e) {
                System.err.println("Error reading document.xml in file " + base);
                e.printStackTrace();
                System.exit(EXIT_IOFILESYSTEMERROR);
            }
            
            String outputName = JOptionPane.showInputDialog("Enter the filename for the document with VIDs"); //get input for merged document's filename
            if (outputName.trim().equals("") || outputName == null) {
                JOptionPane.showMessageDialog(null, "Please enter a filename for the merged document.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(EXIT_MISSINGREQUIREDINPUT);
            }
            if (!outputName.endsWith(".docx")) {
                outputName += ".docx";
            }

            String basePath="";
            for(int i=base.length()-1; i>=0; i--){
                if(base.charAt(i) == '\\' ){
                    basePath = base.substring(0, i+1);
                    break;
                }
            }
        
            outputName = basePath + outputName;

            try{
                            documentXml = File.createTempFile("document", ".xml");
                    }
                    catch(IOException e){
                            e.printStackTrace();
                            System.exit(EXIT_IOFILESYSTEMERROR);
                    }
            startTime = System.currentTimeMillis();
            createVIDs(baseXml, documentXml);
            createDocx(base, "", "", outputName);
            
        }else{
        
        
            base = getDocx("base"); //get base docx

            if (base == null) {
                JOptionPane.showMessageDialog(null, "Please select a docx file", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(EXIT_MISSINGREQUIREDINPUT);
            }

            String branch1;

            if (args.length > 1 && new File(args[1]).isFile()) {
                branch1 = args[1]; //looks for branch1 as second argument if it exists
            } else {
                branch1 = getDocx("branch1"); //get branch1
            }
            if (branch1 == null) {
                JOptionPane.showMessageDialog(null, "Please select a docx file", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(EXIT_MISSINGREQUIREDINPUT);
            }

            String branch2 = getDocx("branch2"); //get branch2
            if (branch2 == null) {
                JOptionPane.showMessageDialog(null, "Please select a docx file", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(EXIT_MISSINGREQUIREDINPUT);
            }

            String basePath="";
            for(int i=base.length()-1; i>=0; i--){
                if(base.charAt(i) == '\\' ){
                    basePath = base.substring(0, i+1);
                    break;
                }
            }
            startTime = System.currentTimeMillis();

            try { //extract base xml file from base docx
                baseXml = extractXml(base, "base", "word/document.xml");
            } catch (IOException e) {
                System.err.println("Error reading document.xml in file " + base);
                e.printStackTrace();
                System.exit(EXIT_IOFILESYSTEMERROR);
            }

            try { //extract branch1 xml file from branch1 docx
                branch1Xml = extractXml(branch1, "branch1", "word/document.xml");
            } catch (IOException e) {
                System.err.println("Error reading document.xml in file " + branch1);
                e.printStackTrace();
                System.exit(EXIT_IOFILESYSTEMERROR);
            }

            try { //extract branch2 xml file from branch2 docx
                branch2Xml = extractXml(branch2, "branch2", "word/document.xml");
            } catch (IOException e) {
                System.err.println("Error reading document.xml in file " + branch2);
                e.printStackTrace();
                System.exit(EXIT_IOFILESYSTEMERROR);
            }

            String outputName = JOptionPane.showInputDialog("Enter the filename for the merged document"); //get input for merged document's filename
            if (outputName.trim().equals("") || outputName == null) {
                JOptionPane.showMessageDialog(null, "Please enter a filename for the merged document.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(EXIT_MISSINGREQUIREDINPUT);
            }
            if (!outputName.endsWith(".docx")) {
                outputName += ".docx";
            }

            outputName = basePath + outputName;

            try{
                            documentXml = File.createTempFile("document", ".xml");
                    }
                    catch(IOException e){
                            e.printStackTrace();
                            System.exit(EXIT_IOFILESYSTEMERROR);
                    }

            //call merge algorithm
            mergeVIDs(baseXml, branch1Xml, branch2Xml, documentXml);

            createDocx(base, branch1, branch2, outputName);

            
        }
        endTime = System.currentTimeMillis();
                    System.out.println("Completion time: " + ((endTime-startTime)/1000.0) + " seconds.");

                    System.exit(EXIT_SUCCESS); //success!
    }

    static String getDocx(String type) {
        //Presents file browser that prompts user for a .docx file

        FileDialog browser = new FileDialog(frame, "Select the " + type + " .docx file");
        browser.setFilenameFilter(new DocxFilter());
        browser.setVisible(true);

        if (browser.getFile() == null) {
            return null;
        } else {
            return (browser.getDirectory() + browser.getFile());
        }

    }

    static File extractXml(String name, String outputName, String entryName) throws IOException {
        //extracts an XML file entryName in the docx file name and writes it to XML file outputName

        ZipFile docx = new ZipFile(name);
        ZipEntry document = docx.getEntry(entryName);
        //File file = new File(outputName);
        File file = File.createTempFile(outputName, ".xml");

        InputStream is = docx.getInputStream(document);
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) >= 0) {
            fos.write(buffer, 0, length);
        }
        is.close();
        fos.close();

        return file;
    }

    static void createDocx(String base, String branch1, String branch2, String outputFile){
		//creates the new docx file 
		try{
			ZipFile docxFile = new ZipFile(base);
//			ZipFile branch1Docx = new ZipFile(branch1);
//			ZipFile branch2Docx = new ZipFile(branch2);
			Enumeration<?> enu = docxFile.entries();
//			Enumeration<?> branch1enu = branch1Docx.entries();
//			Enumeration<?> branch2enu = branch2Docx.entries();
			InputStream is;
			ZipEntry entry;
			ZipEntry oldEntry;
			
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));
			
			while(enu.hasMoreElements()){ 
				
				oldEntry = (ZipEntry)enu.nextElement();
				String entryName = oldEntry.getName();
									
				entry = new ZipEntry(entryName);
				
//				if(!entry.getName().equals("history/revision-history.xml") && !entry.getName().equals("history/_rels/revision-history.xml.rels") && !entry.getName().equals("[Content_Types].xml")){
//				
//					
					if(entry.getName().equals("word/document.xml")){ 
						zos.putNextEntry(new ZipEntry(entry.getName()));
						is = new FileInputStream(documentXml); 
						writeEntry(entry, is, zos);
					}
					else{ //writes non-history and non-special files to the new docx
						zos.putNextEntry(entry);
						is = docxFile.getInputStream(oldEntry);
						writeEntry(entry, is, zos);
					}
	
					
//				}
			}
			
			zos.closeEntry();
			
			zos.close();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			System.exit(EXIT_IOFILESYSTEMERROR);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(EXIT_IOFILESYSTEMERROR);
		}
				
	}
    static void writeEntry(ZipEntry entry, InputStream is, ZipOutputStream zos) throws FileNotFoundException, IOException{
		byte[] buffer = new byte[1024];
		int length;
		System.out.println(entry);
				
		while((length = is.read(buffer)) >= 0){
			zos.write(buffer, 0, length);
		}
		is.close();
		zos.closeEntry();
	}
    
}

class DocxFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        if (name.matches(".+docx")) {
            return true;
        } else {
            return false;
        }
    }
}
