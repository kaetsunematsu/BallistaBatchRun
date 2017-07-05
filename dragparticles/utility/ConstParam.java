/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dragparticles.utility;

/**
 *
 * @author tsunemat
 */
public class ConstParam {
    public static final int D = 3;
    public static final double Timebuff = 600;
    public static final double calcAX = 5000; //m x=[-250, 250]
    public static final double calcAY = 5000; //m y=[-250, 250]
    public static final double calcAZ = 1000; //m
    
    /*---- Parameters for reading DEM ----*/
    public static int DATAWIDTH;
    public static int DATAHEIGHT;
    public static double XllCorner;
    public static double YllCorner;
    public static double GridSize;
    public static double grid_x;
    public static double grid_y;
    public static int NODATA_VALUE;
    
    public static final double  Dt = 0.0001;
    public static final double  G  = 9.81; 
    public static final double OutDt = 0.05; //delta t(time) for output trajectory
    
//    public static int NX ;
//    public static int NY ;
    public static int NZ ;
    
    public static final double  Ux = 0.0;
    public static final double  Uy = 0.0;
    public static final double  Uz = 0.0;
    
    public static final double WindX = 0.0;//WindVelocity is from observation
    public static final double WindY = 0.0;//WindVelocity 
    
    public static final double rhoa = 0.9;
    //public static final double Cd = 0.6;//Mean of Alatorre-Ibargüengoitia and Delgado-Granados(2006)
    public static double Cd ;//Mean of Alatorre-Ibargüengoitia and Delgado-Granados(2006)

    public static final double Elast = 1.0;
    

    
    // TODO: It is better to set these parameters from the file
    public static double CenterX ;//= 723588.0;//Ontake case
    public static double CenterY ;//= 3974463.0;//Ontake case
    public static double CenterZ;//Read from DEM file
    public static double [][] Altitude;
    	
}
