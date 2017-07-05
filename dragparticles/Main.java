package dragparticles;


import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static dragparticles.utility.Output.*;
import static dragparticles.utility.ConstParam.*;
import static dragparticles.utility.FileNameStorage.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.util.Arrays.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author volcano
 */
public class Main {
 
        
    
    public void trunk(String initFilePath){

                
        /*Read Properties from initial condition file*/
        Properties prop = new Properties();
        try{
            prop.load(new FileReader(initFilePath));
        }catch(IOException e){
            e.printStackTrace();
        }
        ReadInit ri = new ReadInit(prop);
                
        /*Find DEM filename*/
        String dirDEMName = ri.getString("directory.name");
        //String centerXYfname = ri.getString("centerXY.name");
        String ourputDirName = ri.getString("outputDir.name");
        System.out.println("DEM directory = "+ dirDEMName);
        String currentDir = System.getProperty("user.dir");
        System.out.println("My current directory is = "+currentDir);
        //Get Path of Topo files
        String dirDEMPath = "../../DEM/"+dirDEMName;
        System.out.println("DEM directory path is "+dirDEMPath);
        
        CenterX = ri.getDouble("centerX");
        CenterY = ri.getDouble("centerY");
        
//        System.out.println("Center file is " + centerXYfname);
        File demDir = new File(dirDEMPath); 
        String[] listFiles = demDir.list();
        String esriAsciiGridFileName = "";
        String centerPositionFileName = "";
        for(int i =0; i<listFiles.length;i++){
            System.out.println("list: "+listFiles[i]);
            if(listFiles[i].endsWith(".asc")){
                esriAsciiGridFileName = listFiles[i];
            }else if(listFiles[i].endsWith(".txt")){
                centerPositionFileName = listFiles[i];
//            }else if(listFiles[i].contains(centerXYfname)){
//                System.out.println("KOKO");
                
            }
        }
        //Ascii file
        System.out.println("Esri Ascii grid:      "+esriAsciiGridFileName);
        System.out.println("Center Position file: "+centerPositionFileName);
        
        
        /*--Read Topography---*/
        String demFilePath = dirDEMPath+"//"+esriAsciiGridFileName;
        TopoRead tprd = new TopoRead(demFilePath);// TODO: Activate this class when it is completed. 
        tprd.readheader();
        /*- Set altitude data to the memory-*/
        Altitude = tprd.altitude();
        
//        /*--Read Center Position from a file--*/
//        String centerFilePath = dirDEMPath+"//"+centerPositionFileName;
//        tprd.readCenter(centerFilePath);
        /*-- read the height of Center position and set to the memory --*/
        CenterZ = tprd.getcenterXYaltitude();
        System.out.println("Center coordinates = ("+ CenterX+ ","+CenterY+","+CenterZ+")");
               
        /*--Bursting!--*/ 
        double avgFq = 10;
        double sdFq  = 0;
        double maxTime = 10.001;
        
        Random rnd = new Random();
        double time = 0;
        //System.out.println("maxTime ="+maxTime);
        double nb = maxTime/avgFq -1 ; //Number of Burst
        List timeary = new ArrayList();

        if(avgFq == maxTime){
            timeary.add(0.0);
        }else{
            while(time < maxTime){
                time = time + abs(rnd.nextGaussian() * sdFq + avgFq);
                if(time<maxTime){
                    timeary.add(time);
                }
            }
        }
        
        /*--Sort array of tasks with time--*/
        Collections.sort(timeary);
        
        //Let's start Simulation
        Simulation sim = new Simulation(timeary, ri);
        sim.iterate();
        
        File inputfile = new File(initFilePath);
        String nameoffile = inputfile.getName();
        String dirOutput = nameoffile.replace(".txt", "//");
        outFilePath = "..//..//results//"+dirOutput;
        System.out.println("Output file path ="+ outFilePath);
        Path path = Paths.get(outFilePath);
        //if directory exists?
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
        
        String filefordeposition = outFilePath+"resdepo.txt";        
        String filefortrajectory = outFilePath+"restraje.txt";
        writeCoord1D(filefordeposition, sim.depoparticles);
        writeTrajectory3D(filefortrajectory, sim.trajectories); //Trun on this line and other trajectory gettin line in Simulation.java, if you want trajectory
        
    }
    
    /**
     * 
     * @param args 
     */
    /*public void simulation(String[] args){
        //TODO Write the simulation itself or functions for simulation
        
    }*/
    
    public static void main(String[] args) {
   
        Main mf = new Main();
        //args[0]: Full path of a file of initial conditions 
        //args[1]: Full path of a DEM DIRECTORY (deposition file)
        //args[2]: Full path of an output file name
        
        String initFileName = args[0]; 
        //String fileDEMPath = args[1];//Name of the directory for DEM
//        String fileCenterPath = args[2];//Name of the directory for Outputs
//        String fileOutPath = args[3];//Name of the directory for Outputs
        //double cd = Double.parseDouble(args[4]);//Read Drag Coefficient from the command line
        //mf.trunk(initFileName, fileDEMPath,fileCenterPath);
        mf.trunk(initFileName);
        
        
    } 
    
}
