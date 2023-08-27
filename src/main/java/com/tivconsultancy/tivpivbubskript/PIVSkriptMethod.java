/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tivconsultancy.tivpivbubskript;

import com.google.common.io.Files;
import com.tivconsultancy.opentiv.helpfunctions.io.Reader;
import com.tivconsultancy.opentiv.helpfunctions.settings.SettingObject;
import com.tivconsultancy.opentiv.highlevel.methods.Method;
import com.tivconsultancy.opentiv.highlevel.protocols.NameSpaceProtocolResults1D;
import com.tivconsultancy.opentiv.highlevel.protocols.Prot_SystemSettings;
import com.tivconsultancy.opentiv.highlevel.protocols.Protocol;
import com.tivconsultancy.opentiv.highlevel.protocols.UnableToRunException;
import com.tivconsultancy.opentiv.imageproc.img_io.IMG_Reader;
import com.tivconsultancy.opentiv.imageproc.primitives.ImageInt;
import com.tivconsultancy.opentiv.math.specials.LookUp;
import com.tivconsultancy.opentiv.math.specials.NameObject;
import com.tivconsultancy.tivGUI.StaticReferences;
import com.tivconsultancy.tivpiv.PIVController;
import com.tivconsultancy.tivpiv.PIVMethod;
import com.tivconsultancy.tivpiv.PIVStaticReferences;
import com.tivconsultancy.tivpiv.data.DataPIV;
import com.tivconsultancy.tivpiv.protocols.PIVProtocol;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVCalcDisplacement;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVDataHandling;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVDisplay;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVInterrAreas;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVObjectMasking;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVPreProcessor;
import com.tivconsultancy.tivpiv.protocols.Prot_PIVRead2IMGFiles;
import com.tivconsultancy.tivpivbub.PIVBUBController;
import com.tivconsultancy.tivpivbub.PIVBUBMethod;
import com.tivconsultancy.tivpivbub.protocols.Prot_tivPIVBUBBubbleFinder;
import com.tivconsultancy.tivpivbub.protocols.Prot_tivPIVBUBBubbleTracking;
import com.tivconsultancy.tivpivbub.protocols.Prot_tivPIVBUBDataHandling;
import com.tivconsultancy.tivpivbub.protocols.Prot_tivPIVBUBMergeShapeBoundTrack;
import delete.com.tivconsultancy.opentiv.devgui.main.ImagePath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.ujmp.core.collections.list.ArrayIndexList;

/**
 *
 * @author Nutzer
 */
public class PIVSkriptMethod extends PIVProtocol {
//    protected LookUp<Protocol> methods;

    protected File mainFolder;
    protected String Settings;
    protected List<File> ReadInFile;

    public PIVSkriptMethod(String sMainFolder, String sEnding, String sSettings) throws IOException, UnableToRunException {
//        initProtocols();
        mainFolder = new File(sMainFolder);
        ReadInFile = new ArrayList<>();
        Settings = sMainFolder + sSettings;
        initSettins();
        read_In_Setting();
        process_sequence(sEnding);

    }

    public void process_sequence(String sEnding) throws UnableToRunException, IOException {
        for (File f : mainFolder.listFiles()) {
            if (f.isDirectory() || !f.getAbsolutePath().contains(sEnding)) {
                continue;
            }
            ReadInFile.add(f);
        }
        Collections.sort(ReadInFile);

        for (int i : getBurstStarts()) {
            int iLength = getLeapLength() > 1 ? getBurstLength() : 2;
            for (int j = i; j < i + iLength - 1; j++) {
                long starttime= System.currentTimeMillis();
                int index = j;
                List<File> lFiles = new ArrayList<>();
                lFiles.add(ReadInFile.get(index));
                lFiles.add(ReadInFile.get(index + 1));
                process_pair(lFiles, index);
                long endtime= System.currentTimeMillis();
                progressBar(index, ReadInFile.size()-getBurstLength(),endtime-starttime);
            }
        }
//        progressBar(ReadInFile.size(), ReadInFile.size(),0);
        File sets = new File(Settings);
        Files.copy(sets, new File(mainFolder + System.getProperty("file.separator") + "Results" + System.getProperty("file.separator") + sets.getName()));
    }

    public void process_pair(List<File> lFiles, int index) throws IOException, UnableToRunException {
        PIVBUBController PIC = new PIVBUBController("new");
        Prot_PIVPreProcessor pivPreproc = new Prot_PIVPreProcessor(new File("new"));
        pivPreproc.loSettings.addAll(loSettings);
        Prot_PIVObjectMasking pivMask = new Prot_PIVObjectMasking("new");
        pivMask.loSettings.addAll(loSettings);
        Prot_PIVInterrAreas pivInterr = new Prot_PIVInterrAreas("new");
        pivInterr.loSettings.addAll(loSettings);
        Prot_PIVCalcDisplacement pivDisp = new Prot_PIVCalcDisplacement("new");
        pivDisp.loSettings.addAll(loSettings);
        Prot_tivPIVBUBDataHandling pivData = new Prot_tivPIVBUBDataHandling("new");
        pivData.loSettings.addAll(loSettings);
        Prot_tivPIVBUBBubbleFinder pivBub = new Prot_tivPIVBUBBubbleFinder("new");
        pivBub.loSettings.addAll(loSettings);
        Prot_tivPIVBUBBubbleTracking pivBubtrack = new Prot_tivPIVBUBBubbleTracking("new");
        pivBubtrack.loSettings.addAll(loSettings);
        Prot_tivPIVBUBMergeShapeBoundTrack pivMergetrack = new Prot_tivPIVBUBMergeShapeBoundTrack("new");
        pivMergetrack.loSettings.addAll(loSettings);

        Object[] input = new Object[3];
        input[0] = new ImageInt(IMG_Reader.readImageGrayScale(lFiles.get(0)));
        input[1] = new ImageInt(IMG_Reader.readImageGrayScale(lFiles.get(1)));
        input[2] = PIC;
        pivPreproc.runSkript(input);

        Object[] inputMasking = new Object[4];
        inputMasking[0] = mainFolder.getAbsolutePath();
        inputMasking[1] = lFiles.get(0).getName();
        inputMasking[2] = lFiles.get(1).getName();
        inputMasking[3] = PIC;

        ImageInt[] masks = pivMask.runSkript(inputMasking);

        pivInterr.runSkript(PIC);
        PIVStaticReferences.calcIntensityValues(PIC.getDataPIV());

        pivDisp.runSkript(PIC);

        pivBub.runSkript(PIC, lFiles, mainFolder.getAbsolutePath(), masks[0], masks[1]);
        if (getSettingsValue("Tracking").toString().contains("Disable_Tracking") ? false : true) {
            pivBubtrack.runSkript(PIC, (ImageInt) input[0]);
            pivMergetrack.runSkript(PIC);
        }
        pivData.runSkript(PIC.getDataBUB(), PIC.getDataPIV(), index, mainFolder.getAbsolutePath());
    }

    public List<Integer> getBurstStarts() {
        int iLeap = getLeapLength();
        int iBurst = getBurstLength();
        List<Integer> startingPoints = new ArrayIndexList<>();
        if (iBurst < 2) {
            for (int i = 0; i < ReadInFile.size() - iLeap; i++) {
                startingPoints.add(i);
            }
        } else {
            for (int i = 0; i <= ReadInFile.size() - iBurst; i = i + iBurst) {
                startingPoints.add(i);
            }
        }
        return startingPoints;
    }

    public int getLeapLength() {
        int leapLength = Integer.valueOf(getSettingsValue("tivPIVInternalLeap").toString());
        return Math.max(leapLength, 1);
    }

    public int getBurstLength() {
        int burst = Integer.valueOf(getSettingsValue("tivPIVBurstLength").toString());
        return burst;
    }

    public void read_In_Setting() throws IOException {
        Reader oRead = new Reader(Settings);
        oRead.setSeperator(";");
        List<String[]> ls = oRead.readFileStringa2();
        setFromFile(ls);
    }

//    public void process_timestep(int index,){
//        
//    }
    private void initSettins() {

        //Masking
        this.loSettings.add(new SettingObject("Bubble Mask", "Mask", "Default(Hessenkemper2018)", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Threshold", "thresh", "100", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Erosion setps", "ero", "3", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Dilation steps", "dila", "3", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Mask Path", "mask_Path", "Mask", SettingObject.SettingsType.String));
        // Pre Proc
        this.loSettings.add(new SettingObject("Cut Top", "BcutyTop", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Value", "cutyTop", 0, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Cut Bottom", "BcutyBottom", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Value", "cutyBottom", 600, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Cut Left", "BcutxLeft", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Value", "cutxLeft", 0, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Cut Right", "BcutxRight", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Value", "cutxRight", 10, SettingObject.SettingsType.Integer));

        this.loSettings.add(new SettingObject("Use Noise Reduction", "NRSimple1", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Threshold", "NRSimple1Threshold", 50, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Algorithm", "NRType", "Simple1", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Standard Gauss", "SFGauss", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Large Gauss", "SF5x5Gauss", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Box Filter", "SF3x3Box", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("SFType", "Gauss", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("HGType", "Brightness", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Brighntess Correction", "HGBrightness", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Value", "Brightness", 50, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("HGType", "Equalize", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Equalize", "HGEqualize", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Max White Value", "Equalize", 255, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("HGType", "Contrast", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Contrast Correction", "HGContrast", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Min Black Value", "BlackMin", 0, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Max White Value", "WhiteMax", 255, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("HGType", "BlackStretch", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("HGBlackStretch", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("BlackStretchFactor", 1.0, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("HGType", "WhiteStretch", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("HGWhiteStretch", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("WhiteStretchFactor", 1.0, SettingObject.SettingsType.Double));

        this.loSettings.add(new SettingObject("Curve Correction", "CurveCorrection", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Old Values", "GreyOldValues", "0, 75, 255", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("New Values", "GreyNewValues", "0, 150, 255", SettingObject.SettingsType.String));

        this.loSettings.add(new SettingObject("Sharpen", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("SharpenThresh", 50, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("HistMin", 50, SettingObject.SettingsType.Integer));

        //PIV Inter Areas
        this.loSettings.add(new SettingObject("Window Size", "PIV_WindowSize", 32, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Grid Type", "PIV_GridType", "50Overlap", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("PIV Interrogation", "PIV_Interrogation", true, SettingObject.SettingsType.Boolean));

        //Calc Disp
        this.loSettings.add(new SettingObject("Hart1998", "tivPIVHart1998", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Hart1998Divider", "tivPIVHart1998Divider", 2.0, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("Sub Pixel Type", "tivPIVSubPixelType", "Gaussian", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Smoothing", "tivPIVSmoothing", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Smooth Factor", "tivPIVSmoothFactor", 2.0, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("Multipass", "tivPIVMultipass", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Multipass BiLinear", "tivPIVMultipass_BiLin", true, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Multipass Count", "tivPIVMultipassCount", 3, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Refinement", "tivPIVInterrAreaRefine", "Disable", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Leap", "tivPIVInternalLeap", 1, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Burst Length", "tivPIVBurstLength", -1, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Validate", "tivPIVValidateVectors", true, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Type", "tivPIVValidationType", "MedianComp", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Threshold", "tivPIVValThreshold", 2.0, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("StampSize", "tivPIVValStampSize", 2, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Interpolate New Values", "tivPIVValidateInterpol", true, SettingObject.SettingsType.Boolean));

        //Bub Finder
        this.loSettings.add(new SettingObject("Execution Order", "ExecutionOrder", new ArrayList<>(), SettingObject.SettingsType.Object));

        //PreProc for Bubble Finder
        this.loSettings.add(new SettingObject("LinNormalization", "LinNormalization", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("NonLinNormalization", "NonLinNormalization", false, SettingObject.SettingsType.Boolean));
        //Edge Detectors
        this.loSettings.add(new SettingObject("Edge Detector", "OuterEdges", true, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Threshold", "OuterEdgesThreshold", 127, SettingObject.SettingsType.Integer));

        //Simple Edge Detection
        this.loSettings.add(new SettingObject("SimpleEdges", "SimpleEdges", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("SimpleEdgesThreshold", "SimpleEdgesThreshold", 127, SettingObject.SettingsType.Integer));

        //Edge Operations
        this.loSettings.add(new SettingObject("Filter Small Edges", "SortOutSmallEdges", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("MinSize", "MinSize", 30, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Filter Large Edges", "SortOutLargeEdges", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("MaxSize", "MaxSize", 1000, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("RemoveOpenContours", "RemoveOpenContours", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("RemoveClosedContours", "RemoveClosedContours", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("CloseOpenContours", "CloseOpenContours", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("DistanceCloseContours", "DistanceCloseContours", 10, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("ConnectOpenContours", "ConnectOpenContours", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("DistanceConnectContours", "DistanceConnectContours", 10, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("SplitByCurv", "SplitByCurv", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("OrderCurvature", "OrderCurvature", 10, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("ThresCurvSplitting", "ThresCurvSplitting", 0.9, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("RemoveWeakEdges", "RemoveWeakEdges", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("ThresWeakEdges", "ThresWeakEdges", 180, SettingObject.SettingsType.Integer));

        //Shape Fitting
        this.loSettings.add(new SettingObject("Method", "Reco", "Default(ReadMaskandPoints)", SettingObject.SettingsType.String));
        //this.loSettings.add(new SettingObject("Ellipse Fit", "EllipseFit_Ziegenhein2019", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Distance", "EllipseFit_Ziegenhein2019_Distance", 50, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("Leading Size", "EllipseFit_Ziegenhein2019_LeadingSize", 30, SettingObject.SettingsType.Double));
        //Shape Filter
        this.loSettings.add(new SettingObject("RatioFilter_Max", "RatioFilter_Max", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("RatioFilter_Max_Value", "RatioFilter_Max_Value", 1, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("RatioFilter_Min", "RatioFilter_Min", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("RatioFilter_Min_Value", "RatioFilter_Min_Value", 0, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Size_Max", "Size_Max", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Size_Max_Value", "Size_Max_Value", 10000, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Size_Min", "Size_Min", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Size_Min_Value", "Size_Min_Value", 1, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Major_Max", "Major_Max", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Major_Max_Value", "Major_Max_Value", 10000, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Major_Min", "Major_Min", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Major_Min_Value", "Major_Min_Value", 0, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Minor_Max", "Minor_Max", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Minor_Max_Value", "Minor_Max_Value", 10000, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Minor_Min", "Minor_Min", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Minor_Min_Value", "Minor_Min_Value", 1, SettingObject.SettingsType.Integer));

        //Tracking
        //Edge Detector
        this.loSettings.add(new SettingObject("Edge Detector", "OuterEdges", true, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Threshold First Pic", "OuterEdgesThreshold", 127, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Threshold Second Pic", "OuterEdgesThresholdSecond", 127, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Filter Small Edges", "SortOutSmallEdges", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("MinSize", "MinSize", 30, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Filter Large Edges", "SortOutLargeEdges", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("MaxSize", "MaxSize", 1000, SettingObject.SettingsType.Integer));

        //Curv processing
        this.loSettings.add(new SettingObject("Curvature Order", "iCurvOrder", 5, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Tang Order", "iTangOrder", 10, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Curvature Threshold", "dCurvThresh", 0.075, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("Colorbar", "tivBUBColBar", "ColdToWarmRainbow2", SettingObject.SettingsType.String));

        //Tracking
        this.loSettings.add(new SettingObject("Search Radius Y Max", "BUBSRadiusYPlus", 20, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Search Radius Y Min", "BUBSRadiusYMinus", 5, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Search Radius X Max", "BUBSRadiusXPlus", 20, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Search Radius X Min", "BUBSRadiusXMinus", -20, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Bubble Tracking", "Tracking", "Default(Disable_Tracking)", SettingObject.SettingsType.String));

        //Data Handling
        this.loSettings.add(new SettingObject("Export->SQL", "sql_activation", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Experiment", "sql_experimentident", "NabilColumnTergitol1p0", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("UPSERT", "sql_upsert", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Settings PIV", "sql_evalsettingspiv", "bestpractice", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Settings BUB", "sql_evalsettingsbub", "bestpractice", SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Reference Pos X [Px]", "data_refposPXX", 0, SettingObject.SettingsType.String));
        this.loSettings.add(new SettingObject("Reference Pos X [m]", "data_refposMX", 0.0, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("Reference Pos Y [Px]", "data_refposPXY", 0, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Reference Pos Y [m]", "data_refposMY", 0.0, SettingObject.SettingsType.Double));
//        this.loSettings.add(new SettingObject("Reference Pos Z [Px]", "data_refposPXZ", 0, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Reference Pos Z [m]", "data_refposMZ", 0.0, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("Resolution [micron/Px]", "data_Resolution", 10.0, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("FPS", "data_FPS", 500, SettingObject.SettingsType.Integer));
        this.loSettings.add(new SettingObject("Burst Frequency [Hz]", "data_BurstFreq", 0.5, SettingObject.SettingsType.Double));
        this.loSettings.add(new SettingObject("CSV", "data_csvExport", false, SettingObject.SettingsType.Boolean));
        this.loSettings.add(new SettingObject("Export Path", "data_csvExportPath", "Directory", SettingObject.SettingsType.String));

    }

    public static void progressBar(int index, int maxValue,long elapsed) {
        int max = 10;
        int remain = ((100 * index) / maxValue) / max;
        char defaultChar='-';
        String uncon="*";
        String bare = new String(new char[max]).replace('\0', defaultChar)+"]";
        StringBuilder bareDone = new StringBuilder();
        bareDone.append("[");
        for (int i = 0; i < remain; i++) {
            bareDone.append(uncon);
        }
        String bareRemain = bare.substring(remain,bare.length());
        System.out.print("\r"+bareDone+bareRemain+" "+remain*10+"%. Processing time of last step: "+elapsed+" ms");
//        if (remain==maxValue){
//            System.out.print("\n");
//        }
    }

    @Override
    public NameSpaceProtocolResults1D[] get1DResultsNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getIdentForViews() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setImage(BufferedImage bi) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getOverTimesResult(NameSpaceProtocolResults1D ident) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run(Object... input) throws UnableToRunException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object[] getResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildClusters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
