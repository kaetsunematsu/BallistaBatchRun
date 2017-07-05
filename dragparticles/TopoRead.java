/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dragparticles;

import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import static dragparticles.utility.ConstParam.*;

/**
 *
 * @author Kae Tsunematsu
 */
class TopoRead {
    private String FileNameDEMAscii;
    private double [][]data;
    private int headerlinenum;
    


    public TopoRead(String FileNameDEMAscii) {
        this.FileNameDEMAscii = FileNameDEMAscii;//"dem/hakone_part_5m_DEM.asc";//TODO: Read file name from parameter file
        headerlinenum = 0;
        
    }
    public void readCenter(String FileNameCenterPosition){
        try
        {
            //Read Geotiff header part
            FileReader fr2 = new FileReader(FileNameCenterPosition);
            BufferedReader br2 = new BufferedReader(fr2);
 
            String line;
           

            while((line = br2.readLine()) != null) {
                /*--- Reading header lines ---*/
                if(line.length() > 2){
                    String left = line.substring(0,12); String right = line.substring(13,line.length());
                    //System.out.println("left="+left+",  right="+right);
                    if(left.contains("CenterX")){
                        CenterX = Double.parseDouble(right);
                    }else if(left.contains("CenterY")){
                        CenterY = Double.parseDouble(right);
                    }else{
                        System.out.println("Catch the end of file");
                        break;
                    }
                }
            }
          
            br2.close();
            fr2.close();
//            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error of reading center position");
            System.exit(0);   
        }
        //this.data= new double[DATAHEIGHT][DATAWIDTH];
                 
            
        
    }
    
    public void readheader(){
        try
        {
            //Read Geotiff header part
            FileReader fr1 = new FileReader(FileNameDEMAscii);
            BufferedReader br1 = new BufferedReader(fr1);
 
            String line;
            int linenum = 0;
            

            while((line = br1.readLine()) != null) {
                
                /*--- Reading header lines ---*/
                String left = line.substring(0,12); String right = line.substring(13,line.length());
                //System.out.println("left="+left+",  right="+right);
                if(left.contains("ncols")){
                    DATAWIDTH = Integer.valueOf(right);
                    System.out.println("DATAWIDTH = "+DATAWIDTH);
                    linenum++;
                }else if(left.contains("nrows")){
                    DATAHEIGHT = Integer.valueOf(right);
                    System.out.println("DATAHEIGHT = "+DATAHEIGHT);
                    linenum++;
                }else if(left.contains("xllcorner")){
                    XllCorner = Double.valueOf(right);
                    System.out.println("XllCorner = "+XllCorner);
                    linenum++;                        
                }else if(left.contains("yllcorner")){
                    YllCorner = Double.valueOf(right);
                    System.out.println("YllCorner = "+YllCorner);
                    linenum++;
                }else if(left.contains("cellsize")){
                    GridSize = Double.valueOf(right);
                    System.out.println("GridSize = "+GridSize);
//                    NX = (int)Math.round(calcAX/GridSize);
//                    NY = (int)Math.round(calcAY/GridSize);
                    NZ = (int)Math.round(calcAZ/GridSize);
                    linenum++;
                }else if(left.contains("dx")){
                    grid_x = Double.valueOf(right);
                    System.out.println("grid size X = "+grid_x);
                    linenum++;
                }else if(left.contains("dy")){
                    grid_y = Double.valueOf(right);
                    System.out.println("grid size Y = "+grid_y);
                    linenum++;
                }else if(left.contains("NODATA_value")){
//                    NODATA_VALUE = Integer.valueOf(right);
//                    System.out.println("NODATA_VALUE = "+NODATA_VALUE);
                    linenum++;
                }else{
                    break;
                }
            }
            headerlinenum = linenum;
            if(GridSize==0 ){
                System.out.println("GridSize=grid_x="+grid_x);
                GridSize = grid_x;
//                NX = (int)Math.round(calcAX/GridSize);
//                NY = (int)Math.round(calcAY/GridSize);
                NZ = (int)Math.round(calcAZ/GridSize);
            }
            System.out.println("headerlinenum = "+headerlinenum);
//            System.out.println("How many times were looped "+linenum);
            br1.close();
            fr1.close();
//            
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("Error of reading header");
            System.exit(0);   
        }
        //this.data= new double[DATAHEIGHT][DATAWIDTH];
                 
            
        
    }
    
    
    public double[][] altitude()
    {
//        System.out.println("DATAHEIGHT = "+DATAHEIGHT);
        data = new double[DATAHEIGHT][DATAWIDTH];

        String p = "";
        String line;
        int gridlinenum = 0;
        int linenum = 0;
        try{
            
            //Read Geotiff header part
            FileReader fr1 = new FileReader(FileNameDEMAscii);
            BufferedReader br1 = new BufferedReader(fr1);
//            System.out.println("file is here!"+DATAHEIGHT);
                    //System.out.println("DATAWIDTH="+DATAWIDTH+", DATAHEIGHT="+ DATAHEIGHT);
            while((line = br1.readLine()) != null) { 
                //System.out.println("linenum = "+line);
                if(linenum >= headerlinenum){//If it is after the header lines
                    
                    String [] readtext;
                    readtext = line.split(" ");
//                    System.out.println("num of texts= "+readtext.length);
                    for(int i=1;i<readtext.length; i++){ //Be careful! Ascii file includes one space at the head of line
                        //System.out.print("readtext ="+readtext[i]);
                        //data[gridlinenum][i-1] = Double.parseDouble(readtext[i]);
                        data[DATAHEIGHT-gridlinenum-1][i-1] = Double.parseDouble(readtext[i]);
//                        System.out.println("i= "+i+" data ="+data[gridlinenum][i-1]);
                    }

//                    System.out.println("linenum = "+gridlinenum);
                    gridlinenum++;
                    
                }else{
//                    System.out.println("koko");
                }
                    
                linenum++;
            }
         
            br1.close();
            fr1.close();
 
        }catch(Exception e){
            
        }
        
        return data;
    }
    
    public double getcenterXYaltitude(){
        int nX =  (int)(Math.floor((CenterX- XllCorner)/GridSize));
        int nY =  (int)(Math.floor((CenterY- YllCorner)/GridSize));
        System.out.println("at Center("+CenterX+", "+CenterY+")");
        System.out.print ("nX = "+nX+", nY ="+nY);
        System.out.print( ":  altitude is "+data[nY][nX]+"\n");
        return data[nY][nX];
    }
    
//    public double getAltitudeAt(double xcoord, double ycoord){
//        int nX =  (int)(Math.floor((xcoord-XllCorner)/GridSize));
//        int nY =  (int)(Math.floor((ycoord-YllCorner)/GridSize));
//        return data[nY][nX];
//    }
    

    public int getDATAWIDTH() {
        return DATAWIDTH;
    }

    public int getDATAHEIGHT() {
        return DATAHEIGHT;
    }

    public double getXllCorner() {
        return XllCorner;
    }

    public double getYllCorner() {
        return YllCorner;
    }

    public double getGridSize() {
        return GridSize;
    }

    public int getNODATA_VALUE() {
        return NODATA_VALUE;
    }
    
//    public static void main(String[] args){
//        String fInName = "DEM/DEM_clipped_utm.asc";
//        TopoRead tprd = new TopoRead(fInName);
//        //tprd.readheader();
//        tprd.altitude();
//        CenterX = 723588.0;//Ontake case
//        CenterY = 3974463.0;//Ontake case
//        //tprd.getcenterXYaltitude();
//    
//        
//   
//    }

   
    
}
