package dragparticles;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.*;

import dragparticles.Particle;
import static dragparticles.utility.ConstParam.*;
import static dragparticles.utility.Output.*;
import static dragparticles.math.Vector.*;
import static dragparticles.utility.Coordinate.X;
import static dragparticles.utility.Coordinate.Y;



/**
 *
 * @author tsunemat
 */
public class Simulation {
    private List<Particle> particles;
    private List<Particle> bparticles;
    private List btimeary;
    private final double maxTime;
    private final ReadInit ri;
    private Velocity velocity;
    public List[][] depositParticles;
    public List<Particle> depoparticles;
    private double ctime;//current time
    public List<Trajectory> trajectories;
    

    public Simulation( List timeary, ReadInit ri) {
        this.btimeary = timeary;
        this.maxTime = (double) timeary.get((timeary.size()-1)) + Timebuff;
        this.ri = ri;
        particles=makeParticleList();
        bparticles = makeParticleList();
        depoparticles = makeParticleList();
        velocity = new Velocity(ri);
        this.depositParticles = makeParticleField2D();
        trajectories = makeTrajectoryList();
        
    }
    
    public void iterate() {
        double timeburst = 0.;
        while(ctime <maxTime){//Time loop
            
            /*---Check collision and change velocity ---*/
            //collision(particles);
            /*---transport all existing particles---*/
            transport(ctime, particles);
            
            /*---check there is any bursting or not?---*/
            /*---This loop continues until the btime is larger than ctime---*/
            while((double)(btimeary.size())>0.0){
                if((ctime+Dt) < (double)btimeary.get(0)){
                    break;
                }
                 timeburst = (double)btimeary.get(0);
                 btimeary.remove(0);
                 double dtb = ctime+Dt - timeburst;
                 //TODO: delete unapplied (supposed to...) bursting
                 //System.out.println("current time="+ctime+", timeburst="+timeburst+", dtb="+dtb);
                 burst(timeburst);

                 particles.addAll(bparticles); 
                 //String filename = "../../burst.txt";
                 //writeCoord1D(filename,bparticles);
                 
                 bparticles.clear();
            }
            
            ctime = ctime+Dt;
            
            //If particle has all removed and already bursting has all finished,
            //Simulation ends
            if(particles.size()<=0 && btimeary.size()<=0) 
                break;
            
        }//End of time loop 
        
        
        
    }
    
    public void burst(double btime){
        /*Particle draw*/
        GaussianBurst gb = new GaussianBurst(btime,ri);
        bparticles = gb.drawPartList();
        
        for(int i=0;i<bparticles.size();i++){
            Particle bp = (Particle) bparticles.get(i);
        }
    }
    
    public void transport(double t, List<Particle> parray){

                
        for(int ip =0; ip < parray.size(); ip++){
            
            Particle currentP = (Particle) parray.get(ip);
                       
            int ix = currentP.getIndexX();
            int iy = currentP.getIndexY();
            int iz = currentP.getIndexZ();
            
            
            double vx = currentP.getVelocity()[0];
            double vy = currentP.getVelocity()[1];
            double vz = currentP.getVelocity()[2];
            
            double px = currentP.getPx();
            double py = currentP.getPy();
            double pz = currentP.getPz();
            
            double storageX = currentP.getStorageX();
            double storageY = currentP.getStorageY();
            double storageZ = currentP.getStorageZ();
                        
            double nextStorageX = storageX + vx*Dt;
            double nextStorageY = storageY + vy*Dt;
            double nextStorageZ = storageZ + vz*Dt;
            
            /*If there is no movement*/
            int nextX = ix;
            int nextY = iy;
            int nextZ = iz;
            /*Let's see the movement of particles by the amount of storgae */
            //TODO: nextStorageX should be cleaned up
            if(abs(nextStorageX)>= GridSize && nextStorageX >= 0){// move to + direction
                nextX = ix + 1;
                nextStorageX = nextStorageX - GridSize;
                storageX = 0.0;
            }else if(abs(nextStorageX)> GridSize && nextStorageX < 0){// move to - direction
                nextX = ix - 1;
                nextStorageX = nextStorageX + GridSize;
                storageX = 0.0;
            }
            
            if(abs(nextStorageY)>= GridSize && nextStorageY >= 0){
                nextY = iy + 1;
                nextStorageY = nextStorageY - GridSize;
                storageY = 0.0;
            }else if(abs(nextStorageY)> GridSize && nextStorageY < 0){
                nextY = iy - 1;
                nextStorageY = nextStorageY + GridSize;
                storageY = 0.0;
            }
            
            if(abs(nextStorageZ)>= GridSize && nextStorageZ >= 0){
                nextZ = iz + 1;
                nextStorageZ = nextStorageZ - GridSize;
                storageZ = 0.0;
            }else if(abs(nextStorageZ)> GridSize && nextStorageZ < 0){
                nextZ = iz - 1;
                nextStorageZ = nextStorageZ + GridSize;
                storageZ = 0.0;
            }
            
            currentP.setIndexX(nextX);
            currentP.setIndexY(nextY);
            currentP.setIndexZ(nextZ);
            
            //System.out.println("(nextX , nextY , nextZ) ="+nextX+"\t"+nextY+"\t"+nextZ);
            px = nextX * GridSize + nextStorageX;
            py = nextY * GridSize + nextStorageY;
            pz = nextZ * GridSize + nextStorageZ;
            //System.out.println("(px , py , pz) ="+px+"\t"+py+"\t"+pz);

            currentP.setPx(px);
            currentP.setPy(py);
            currentP.setPz(pz);
                        
            currentP.setStorageX(nextStorageX);
            currentP.setStorageY(nextStorageY); 
            currentP.setStorageZ(nextStorageZ); 
            
            int nX =  (int)(Math.floor((px+CenterX-XllCorner)/GridSize));
            int nY =  (int)(Math.floor((py+CenterY-YllCorner)/GridSize));
            double groundZ = Altitude[nY][nX];
            
            /*--If particle's position is lower than ground position, then this particle is deposited --*/            
            if(groundZ > pz){
                deposit(nextX, nextY, currentP);//A particle is added to the deposit array
//                    System.out.println(px+"\t"+py+"\t"+pz+"\t"+currentP.getId());
                //trajectory data is stored into the trajectory array
                trajectories.add(new Trajectory(ctime, currentP.getId(), px, py,pz,currentP.collisioncounter));
                //the current particle is removed from "particles" array 
                particles.remove(currentP);
            
            /*--If the particle is in the air, its velocity is updated --*/
            }else{
                double dz = pz - CenterZ;
                double dist = sqrt(px *px + py*py + dz*dz);//TODO: This distance is horizontal direction only --> make it along flow line? 
                //Velocity is calculated with wind velocity
                double[] vel = velocity.getVelocityWind(currentP, t, dist);
                currentP.setVelocity(vel[0], vel[1], vel[2]);
                /** A particle is in the air **/
                if(abs(ctime-round(ctime/OutDt) * OutDt) < (1e-8)){
//                
//                    System.out.println(""+ctime+"\t"+px+"\t"+py+"\t"+pz+"\t "+currentP.getId());
                    trajectories.add(new Trajectory(ctime, currentP.getId(), px, py,pz,currentP.collisioncounter));
                }
            }

        }
    }//Finish transport
    
    private void collision(List<Particle> particles) {
        List alreadyCol = new ArrayList();
        /****/
//        for(int i = 0; i< alreadyCol.size(); i++){
//            System.out.print("\t"+alreadyCol.get(i));
//        }
//        System.out.println();
        /****/
        
        for(int ip = 0; ip < particles.size()-1; ip++){
            if (alreadyCol.contains(ip) ){
                //System.out.println("time =" +ctime+", particle "+ip+" is already collided so skip it!");
            }else{
                for(int jp = ip+1; jp<particles.size();jp++){
                    Particle p1 = particles.get(ip) ;
                    Particle p2 = particles.get(jp) ;

                    double p1x = p1.getPx(); 
                    double p1y = p1.getPy(); 
                    double p1z = p1.getPz();
                    
                    double p2x = p2.getPx();
                    double p2y = p2.getPy();
                    double p2z = p2.getPz();
      
                    double dist = sqrt(pow((p1x-p2x),2.) + pow((p1y-p2y),2.)+ pow((p1z-p2z),2.));
                    //System.out.println(p1x +", "+p1y+", "+p1z+"\t"+p2x+", "+p2y+", "+p2z+"\t"+dist);

                    double radi = (p1.getDiameter() + p2.getDiameter())*0.5;

                    if(p1z != 0.0 && p2z!=0.0 &&dist < radi ){
//                        System.out.println("collision = ("+ip+", "+jp+"): dist= "+dist);
//                        System.out.print("("+p1.getPx()+","+p1.getPy()+","+p1.getPz()+")");
//                        System.out.println("\t ("+p2.getPx()+","+p2.getPy()+","+p2.getPz()+")");
                        p1.incrCollisionCounter();
                        p2.incrCollisionCounter();

                        
                        //System.out.println("Before: energy of p1 ="+mv2_1+", energy of p2="+mv2_2 +"\t  sum ="+ (mv2_1+mv2_2));
                        
                        changeVelocity(p1, p2);
                        
 
                        //System.out.println("After: energy of p1 ="+mv2_1+", energy of p2="+mv2_2+"\t  sum ="+ (mv2_1+mv2_2));
                        
                        alreadyCol.add(jp);
                        

                    } //end if
                }//for of jp
            }// if + else
            
        }// for of ip
        
    }
    
    
    private void changeVelocity(Particle p1, Particle p2) {
        double[] x1 = new double[D];
        double[] x2 = new double[D];
        x1[0] = p1.getPx();
        x1[1] = p1.getPy();
        x1[2] = p1.getPz();
        x2[0] = p2.getPx();
        x2[1] = p2.getPy();
        x2[2] = p2.getPz();
        
        double[] v1 = p1.getVelocity();
        double[] v2 = p2.getVelocity();

        double[] e = subs( x2, x1 );

        double mag_e = norm( e );

        e = divide( e, mag_e );

        //Projection for collsion direction
        //scalar product
        double proj1 = dot( v1, e );
        double proj2 = dot( v2, e );

        double[] v1p = mult( e, proj1 );
        double[] v2p = mult( e, proj2 );
        // make real vector
        //Projection for orthogonal direction
        double[] v1o = subs( v1, v1p );
        double[] v2o = subs( v2, v2p );

        //Velocity change in parallel direction
        final double m1 = p1.getMass();
        final double m2 = p2.getMass();

        double newV1 = (-proj1 + proj2) *
                (1 + Elast) / (m1 / m2 + 1) + proj1;
        double newV2 = (-proj2 + proj1) *
                (1 + Elast) / (m2 / m1 + 1) + proj2;

        // vector addition
        double[] vnew1 = add( v1o, mult( e, newV1 ) );
        double[] vnew2 = add( v2o, mult( e, newV2 ) );

        p1.setVelocity(vnew1[0], vnew1[1], vnew1[2]);
        
        p2.setVelocity(vnew2[0], vnew2[1], vnew2[2]);
       

    }
    
    public void deposit(int nextX, int nextY, Particle currentP){
        int inx =(int)floor(nextX/GridSize)+DATAWIDTH/2;
        int iny =(int)floor(nextY/GridSize)+DATAHEIGHT/2;
        depositParticles[iny][inx].add(currentP);
        depoparticles.add(currentP);
        particles.remove(currentP);
        
    }
     
    private List makeParticleList(){
        List pList = new ArrayList();
        return pList;
    }
    
    private List[][] makeParticleField2D() {
        List[][] newField2D = new List[DATAHEIGHT][DATAWIDTH];
        for( int y=0; y<DATAHEIGHT; y++ ) {
            for( int x=0; x<DATAWIDTH; x++ ) {
                newField2D[y][x] = new ArrayList();
            }
        }
        return newField2D;
    }
    
    private List makeTrajectoryList(){
        List tList = new ArrayList();
        return tList;
    }

    
    

   
    
    
    
}
