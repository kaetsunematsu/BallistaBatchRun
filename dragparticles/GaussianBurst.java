/*
 * This is for the lagrangian transport of ballistics
 * 
 */
package dragparticles;

import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static dragparticles.utility.Coordinate.*;
import static dragparticles.utility.ConstParam.*;
import java.util.Locale;

//TODO: velocity angle should be rotated around Y and Z axis 
// rotation angle gamma should be "gamma(around Y) and zeta (around Z)"???
/**
 *
 * @author tsunemat
 */

class GaussianBurst {
    private final Random RNG = new Random();
    private final double time;
    private final ReadInit params;
    //private final List<Particle> partList;
    private final double avgNum;
    private final double sdNum;
    private final double avgDensity;
    private final double sdDensity;
    private final double avgDiam;
    private final double sdDiam;
    private final double minDiam;
    private final double maxDiam;
    private final double avgDisp;
    private final double sdDisp;
    private final double maxDisp;
    private final double avgV;
    private final double sdV;
    private final double rotDeg;
    private final double direcDeg;
    private final double vFNorm;
    private int id = 0;
    
    public GaussianBurst(double time, ReadInit params) {
        this.time = time;
        this.params = params;
        this.avgNum = params.getDouble("numOfParticles");
        this.sdNum = 0.0;
        this.avgDensity = params.getDouble("density.avg");
        this.sdDensity = params.getDouble("density.sd");
        this.avgDiam = params.getDouble("diameter.avg");
        this.sdDiam = params.getDouble("diameter.sd");
        this.minDiam = params.getDouble("diameter.min");
        this.maxDiam = params.getDouble("diameter.max");
        this.avgDisp = params.getDouble("displacement.avg");
        this.sdDisp = params.getDouble("displacement.sd");
        this.maxDisp = params.getDouble("displacement.max");
        this.avgV = params.getDouble("velocity.norm.avg");
        this.sdV = params.getDouble("velocity.norm.sd");
        this.rotDeg = params.getDouble("axis.eject.deg");
        //Read direct.bearing.deg is an angle from north to east
        //-> direcDeg is an angle from east to north
        this.direcDeg = 90.0 - params.getDouble("direct.bearing.deg");
        this.vFNorm = params.getDouble("initFVelocity.cnst");
        Cd = params.getDouble("dragCoefficient.cnst");
        
    }
    
    private double drawNormal( double average, double sd ) {
        double res = RNG.nextGaussian()*sd + average;
        while( res < 0 || res > average*2 ) {
            res = RNG.nextGaussian()*sd + average;
        }
        return res;  
    }
    
    public List<Particle> drawPartList() {
        final int n = drawNumberOfParticles();
        List<Particle> particles = new ArrayList<Particle>(n);
        System.out.println();
        for (int i = 0; i < n; i++) {
            particles.add(drawParticle());
            id++;
        }
        //System.out.println("numP in DrawParticleList="+particles.size());
        return particles;
    }
    
    public double time() {
        return time;
    }

    public boolean involves(Particle p) {
        return false;
    }
    
    private int drawNumberOfParticles() {
        final int n = (int) Math.ceil(RNG.nextGaussian() * sdNum + avgNum);
        if (n < 0) {
            return 1;
        } else {
            return n;
        }
    }
    
    private Particle drawParticle() {
        
        double density = -0.0;
        double diam = -0.0;
        double x[] = new double[3];//position
        double v[] = new double[3];//particle velocity
        double v2[] = new double[3];//particle velocity
        double vF[] = new double[3];//flow velocity
        double vF2[] = new double[3];//flow velocity
        double vNorm = -0.0;
        double theta = -0.0;
        double phi = -0.0;
        double gamma = -0.0;
        double gamma2 = -0.0;
        double vx = -0.0;
        double vy = -0.0;
        double vz = -0.0;
        double vfx = 0.0;
        double vfy = 0.0;
        double vfz = 0.0;
     
        while(v2[Z] <= 0.0){
            density = drawNormal(avgDensity, sdDensity);
             /*---- Particle diameter ----*/
             /*--- Gaussian ---*/
            diam = drawNormal(avgDiam,sdDiam);
            while(diam < minDiam){// minimum diameter is 25cm 
                diam = drawNormal(avgDiam,sdDiam);
            }

            
            /*---- Ejection Position ----*/
         
            double distance= maxDisp + 1;
            while(distance > maxDisp){
                //distance = maxDisp + 1;
                for (int i = 0; i < 2; i++) {
                    double random = RNG.nextDouble();
                    boolean flag = RNG.nextBoolean();
                    int sign;
                    if (flag ==true)
                        sign = 1;
                    else{
                        sign = -1;
                    }
                    x[i] = sign * random*maxDisp;
                }
                distance = sqrt(x[X]* x[X] + x[Y]+x[Y]);
                //System.out.println("x = "+x[X]+", y="+x[Y]+", distance ="+distance);

            }
            
            
            //If it is only vent center, it will be only one position
            int nX =  (int)(Math.floor((x[X]+CenterX-XllCorner)/GridSize));
            int nY =  (int)(Math.floor((x[Y]+CenterY-YllCorner)/GridSize));
            
//            System.out.println("x ="+x[X]+", y="+x[Y]);
//            System.out.println("nx ="+nX+", ny="+nY);
            x[Z] = Altitude[nY][nX];
            
         
            /*---  Velocity ---*/
            vNorm = drawNormal(avgV,sdV);

            theta = drawTheta();
            phi = RNG.nextDouble() * 2 * PI;
            gamma = rotDeg/180 * PI;

            /***  Calculate Particle Velocity   ***/
            vx = vNorm * sin(theta) * cos(phi);
            vy = vNorm * sin(theta) * sin(phi);
            vz = vNorm * cos(theta);

            //Rotation around Y axis : inclined to  eastward (particle)
            v[X] =  cos(gamma) * vx + sin(gamma) * vz;
            v[Y] =  vy;
            v[Z] = -sin(gamma) * vx + cos(gamma) * vz;
            
            //Rotation of around Z axis: directed to northward (particle)
            gamma2 = direcDeg/180 * PI;
            v2[X] =  cos(gamma2) * v[X] - sin(gamma2) * v[Y];
            v2[Y] =  sin(gamma2) * v[X] + cos(gamma2) * v[Y];
            v2[Z] =  v[Z];
            
            /***  Calculate Flow Velocity   ***/
            vfx = vFNorm * sin(theta) * cos(phi);
            vfy = vFNorm * sin(theta) * sin(phi);
            vfz = vFNorm * cos(theta);
            
            //Rotation around Y axis : inclined to  eastward (flow)
            vF[X] = cos(gamma) * vfx + sin(gamma) * vfz;
            vF[Y] =  vfy;
            vF[Z] = -sin(gamma) * vfx + cos(gamma) * vfz;  
            
            //Rotation of around Z axis: directed to northward (flow)
            vF2[X] =  cos(gamma2) * vF[X] - sin(gamma2) * vF[Y] ;
            vF2[Y] =  sin(gamma2) * vF[X] + cos(gamma2) * vF[Y];
            vF2[Z] =  vF[Z];


            //System.out.println("vFNorm is:"+vFNorm);
          
        }
        

        return new Particle(id, diam, density, x, v2, vF2, time);
        
    }
    


    private double drawTheta() {
        double avgVAngle = 0.0;
        double sdVAngle = 15.0;
        double theta = PI * (RNG.nextGaussian() * sdVAngle + avgVAngle) / 180;
        while( theta < -PI/2 || theta > PI/2 ) {
           theta = PI * (RNG.nextGaussian() * sdVAngle + avgVAngle) / 180;
        }
        return theta;
    }
    /**
     * 
     * @param avg: mean of Gaussian Profile
     * @param std: standard deviation of Gaussian Profile
     * @param min
     * @param max
     * @return 
     */
    private double gaussianGS(double avg, double std, double min, double max){
        double ramdomDiam =0.0;
        while (ramdomDiam <minDiam || ramdomDiam >maxDiam){
            ramdomDiam = drawNormal(avgDiam,sdDiam);
        }
        return ramdomDiam;
    }
    
    
    
}

    
    
    

    
    
    
