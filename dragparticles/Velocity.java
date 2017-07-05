/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dragparticles;

import static dragparticles.utility.ConstParam.*;
import static dragparticles.utility.Coordinate.*;
import static java.lang.Math.*;
import java.util.Random;

/**
 *
 * @author tsunemat
 */
public class Velocity {
    private final Random RNG = new Random();
    private Particle currentp;
    
    private ReadInit ri;
    private final double fRange;
    private int numEq = 3;

    public Velocity(ReadInit ri) {
        this.ri = ri;
//        double avgDisp = ri.getDouble("displacement.avg");
//        double sdDisp = ri.getDouble("displacement.sd");
//        double craterX = RNG.nextGaussian() * sdDisp + avgDisp;
//        double craterY = RNG.nextGaussian() * sdDisp + avgDisp;
        this.fRange = ri.getDouble("flowRange.cnst");
        //TODO: if you want to include vertical velocity field, turn on this line
        //this.fvf = new FountainVelocityField(craterX, craterY); 

    }
    
    
    
    public double[] getVelocityWind(Particle pp, double t, double dist){
        this.currentp = pp;
        double vFNorm = ri.getDouble("initFVelocity.cnst");
//        double vFAngle = ri.getDouble("flowAngle.deg");
//        double vFRadian = vFAngle / 180 * PI;
        double ufx;
        double ufy;
        double ufz;
        
        if (dist <= fRange){

            ufx = pp.getFVelocity()[X];
            ufy = pp.getFVelocity()[Y];
            ufz = pp.getFVelocity()[Z];
            
        }else{
            ufx = 0.0;
            ufy = 0.0;
            ufz = 0.0;
        }
        ufx = ufx + WindX;
        ufy = ufy + WindY;

//        System.out.println("velocity\t"+"ufx="+ufx+", ufz ="+ufz);
//        System.out.println("cd=\t"+Cd);
        //double[] velocity = vExp(ufx, ufy, ufz);
        double[] velocity = vRunge(t,ufx, ufy, ufz);
//        if(ufx >0)
//            System.out.println(ufx+"\t"+ufz+";\t"+velocity[0]+"\t"+velocity[1]+"\t"+velocity[2]);
        return velocity;
    }
    

    

    /*
     * Explicit way of calculation
     */
    public double[] vExp(double ufx, double ufy, double ufz){
        double[] ve = new double[3];
        double vx0 = currentp.getVelocity()[0];
        double vy0 = currentp.getVelocity()[1];
        double vz0 = currentp.getVelocity()[2];
        double absV = sqrt(vx0*vx0 + vy0*vy0 + vz0*vz0);//Particle Velocity
        //double absU = sqrt(ufx*ufx + ufz*ufz);
        double absU = sqrt(ufx*ufx + ufy*ufy + ufz*ufz);//Flow Velocity
        
        final double A = PI*pow((currentp.getDiameter()/2.0),2);

        
        double beta = A*rhoa*Cd/currentp.getMass()/2;
        
        ve[0] = vx0 - (beta *(vx0-ufx)*abs(absV-absU)  )*Dt;
        ve[1] = vy0 - (beta *(vy0-ufy)*abs(absV-absU)  )*Dt;
        ve[2] = vz0 - (beta *(vz0-ufz)*abs(absV-absU))*Dt- G*Dt; 
        //ve[2] = vz0 - (beta *(vz0)*abs(absV-absU))*Dt - G*Dt; 


//       System.out.println("vx0="+vx0+", vy0="+vy0+", vz0="+vz0);
//       System.out.println(ve[0]+"\t"+ve[1]+"\t"+ve[2]);
        return ve;
    }
    
    public double[] vRunge(double t, double ufx, double ufy, double ufz){
        int numPara = 3; //ufx, ufy, ufz(Flow Velocities)
        
        /**-- x is unknown parameter. Here it is velocity--**/
        double[] v = new double[numEq];
        double[] gradV = new double[numEq];
        double[] uPara = new double[numPara];
        
        double[] k0 = new double[numEq];
        double[] k1 = new double[numEq];
        double[] k2 = new double[numEq];
        double[] k3 = new double[numEq];

        double[] v1 = new double[numEq];
        double[] v2 = new double[numEq];
        double[] v3 = new double[numEq];
        
        v[X] = currentp.getVelocity()[0];
        v[Y] = currentp.getVelocity()[1];
        v[Z]= currentp.getVelocity()[2];
        uPara[X] = ufx;
        uPara[Y] = ufy;
        uPara[Z] = ufz;
        
        /***** Runge Kutta *****/
        /*k1 = h * defineFunction(x, y);
        k2 = h * defineFunction(x + h/2, y + k1/2);
        k3 = h * defineFunction(x + h/2, y + k2/2);
        k4 = h * defineFunction(x + h, y + k3);*/
//		0
        gradV = functions(t, v, uPara);
        for(int i=0; i<numEq; i++){
                k0[i] = Dt*gradV[i];
                v1[i]=v[i] + k0[i]/2.0;
        }

//		1
        gradV = functions((t+0.5*Dt), v1, uPara);
        for(int i=0; i<numEq; i++){
                k1[i]=Dt*gradV[i];
                v2[i]=v[i] + k1[i]/2.0;
        }


//		2
        gradV = functions((t+0.5*Dt), v2, uPara);
        for(int i=0; i<numEq; i++){
                k2[i]=Dt*gradV[i];
                v3[i]=v[i]+k2[i];
        }	

//		3
        gradV = functions((t+Dt), v3, uPara);		
        for(int i=0; i<numEq;i++){
                k3[i]=Dt*gradV[i];
                v[i]=v[i]+(k0[i] + 2.0*k1[i] + 2.0*k2[i] + k3[i])/6.0;
        }
                /*y += (k1 + 2*k2 + 2*k3 + k4) / 6;  // 加重平均*/
        
        //System.out.println("(Vx ,Vy, Vz)="+v[X]+"\t"+v[Y]+"\t"+v[Z]);
        return v;
    }
    
    
    public double[] functions(double t, double[] v, double[] uParam){
        double[] dv = new double [numEq];
        
        final double A = PI*pow((currentp.getDiameter()/2.0),2);//Particle surface area
        double beta = A*rhoa*Cd/currentp.getMass()/2;
        
        double ufx = uParam[0];
        double ufy = uParam[1];
        double ufz = uParam[2];
        
        double absV = sqrt(v[X]*v[X] + v[Y]*v[Y] + v[Z]*v[Z]);//Particle Velocity
        double absU = sqrt(ufx*ufx + ufy*ufy + ufz*ufz);//Flow Velocity
        
        dv[X] = -( beta *(v[X]-ufx)*abs(absV-absU)  );
        dv[Y] = -( beta *(v[Y]-ufy)*abs(absV-absU) );
        dv[Z] = -( beta *(v[Z]-ufz)*abs(absV-absU) )-G;
        //System.out.println("t ="+t+", DVx="+dv[X]+", DVy="+dv[Y]+", DVz="+dv[Z]);
       
        return dv;
    }
    
    
    
}
