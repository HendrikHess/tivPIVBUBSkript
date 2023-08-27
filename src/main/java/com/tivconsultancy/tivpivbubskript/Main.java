/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tivconsultancy.tivpivbubskript;

import com.tivconsultancy.opentiv.highlevel.protocols.UnableToRunException;
import com.tivconsultancy.tivGUI.StaticReferences;
import com.tivconsultancy.tivpiv.PIVController;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVCalcDisplacement;
import com.tivconsultancy.tivpivbub.PIVBUBController;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

/**
 *
 * @author Nutzer
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, UnableToRunException {
 
//String mainFolder ="C:\\NoAdmin\\CompleteCaseTest\\";
        String mainFolder =args[0];
        System.out.println("Starting at "+mainFolder);
        PIVSkriptMethod PSM = new PIVSkriptMethod(mainFolder,args[1],args[2]);
        
        
//        PIVSkriptMethod PSM = new PIVSkriptMethod(mainFolder,"jpeg","set2");
         System.out.println("");
        System.out.println("Finished!");
        System.exit(0);

    }

}
