package acq_tam_wg.PPC.v1_1_2.src;

/*********************************/
/*                               */
/*   Importing all Nsl classes   */
/*                               */
/*********************************/

import nslj.src.system.*;
import nslj.src.cmd.*;
import nslj.src.lang.*;
import nslj.src.math.*;
import nslj.src.display.*;
import nslj.src.display.j3d.*;

/*********************************/

public class PPC extends NslJavaModule{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  PPC
//versionName: 1_1_2


//variables 
private double baseOrientation; // 
public  NslDinDouble2 adjacentNodeCenters; // 
public  NslDinDouble1 currentPosition; // 
public  NslDinDouble0 currentOrientation; // 
public  NslDoutDouble1 affordanceDirOut; // 
public  NslDoutDouble1 affordanceDistOut; // 
private double maxDist; // 

//methods 
/**
 * Executed at the start of module constructor
 */
public void callFromConstructorTop(Object[] args)
{
	maxDist=300;
}


public void initModule()
{
	nslAddSpatialCanvas("output", "affordances", affordanceDirOut, 0.0, 1.0);
	nslSetColumns(1,"output");
}

public void simRun()
{
	process();
}

public void simTrain()
{
	process();
}

protected void process()
{
	for(int i=0; i<size; i++)
	{
		double affPrefAngle=-Math.PI+i*(2*Math.PI/(size-1));
		affordanceDirOut.set(i,0.0);
		affordanceDistOut.set(i,0.0);
		for(int j=0; j<maxNodes; j++)
		{
			if(!Double.isNaN(adjacentNodeCenters.get(j,0)))
			{
				// Compute angle between adjacent node position and this node position
				double[] baseVec= new  double[]{1,0};
				double[] directionVec= new  double[]{-adjacentNodeCenters.get(j,1)+currentPosition.get(1),-adjacentNodeCenters.get(j,0)+currentPosition.get(0)};
				double angle=(Math.PI+NslOperator.atan2.eval(directionVec[1],directionVec[0])-NslOperator.atan2.eval(baseVec[1],baseVec[0]))%(Math.PI*2.0);
				if(angle<0)
					angle=Math.PI*2.0+angle;

				// Compute relative angle between affordance and current orientation
				double relativeAngle=getRelativeAngle(currentOrientation.get(), angle);

				// Encode affordance direction in population code
				double angleDist=getDist(relativeAngle, affPrefAngle, -Math.PI, Math.PI);
				double distance=NslOperator.distance.eval(adjacentNodeCenters.get(j),currentPosition.get());
				double distanceBias=1-distance/maxDist;
				//double directionBias=nslExp(-nslPow((i-size/2.0), 2)/(2*500000*sigma*sigma));
				//double directionBias=nslExp(-nslPow((i-size/2.0), 2)/(2*250000*sigma*sigma));
				affordanceDirOut.set(i,NslMin.eval(1.0,affordanceDirOut.get(i)+distanceBias*NslOperator.exp.eval(-NslOperator.pow.eval(angleDist,2)/(2*sigma*sigma))));

				// Set affordance distance if this is above noise levels population code peak
				if(NslOperator.exp.eval(-NslOperator.pow.eval(angleDist,2)/(2*NslOperator.pow.eval(wgDesirabilitySigma*.75,2)))>.0000001)
					affordanceDistOut.set(i, distance);
			}
		}
	}
}

/**
 * Get distance between two angles
*/
protected double getDist(double ang1, double ang2, double min, double max)
{
	double dist=(ang1-ang2)%(2*Math.PI+0.001);
	double altDist=dist;
	if(ang1<ang2)
		altDist=max-ang2+ang1-min;
	else if(ang2<ang1)
		altDist=max-ang1+ang2-min;
	if(NslOperator.abs.eval(altDist)<NslOperator.abs.eval(dist))
		dist=altDist;
	return dist;
}

/**
 * Get relative angle between two angles
*/
protected double getRelativeAngle(double ang1, double ang2)
{
	double relativeAngle=0.0;
	if(ang1>ang2)
	{
		double relAngleRight=(ang2-ang1);
		double relAngleLeft=2*Math.PI+relAngleRight;
		if(NslOperator.abs.eval(relAngleRight)<NslOperator.abs.eval(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	else
	{
		double relAngleLeft=ang2-ang1;
		double relAngleRight=relAngleLeft-2*Math.PI;
		if(NslOperator.abs.eval(relAngleRight)<NslOperator.abs.eval(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	return relativeAngle;
}
public void makeConn(){
}

	/******************************************************/
	/*                                                    */
	/* Generated by nslc.src.NslCompiler. Do not edit these lines! */
	/*                                                    */
	/******************************************************/

	/* Constructor and related methods */
	/* makeinst() declared variables */

	/* Formal parameters */
	int size;
	int maxNodes;
	double sigma;
	double wgDesirabilitySigma;

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public PPC(String nslName, NslModule nslParent, int size, int maxNodes, double sigma, double wgDesirabilitySigma)
{
		super(nslName, nslParent);
		this.size=size;
		this.maxNodes=maxNodes;
		this.sigma=sigma;
		this.wgDesirabilitySigma=wgDesirabilitySigma;
		initSys();
		makeInstPPC(nslName, nslParent, size, maxNodes, sigma, wgDesirabilitySigma);
	}

	public void makeInstPPC(String nslName, NslModule nslParent, int size, int maxNodes, double sigma, double wgDesirabilitySigma)
{ 
		Object[] nslArgs=new Object[]{size, maxNodes, sigma, wgDesirabilitySigma};
		callFromConstructorTop(nslArgs);
		adjacentNodeCenters = new NslDinDouble2("adjacentNodeCenters", this, maxNodes, 2);
		currentPosition = new NslDinDouble1("currentPosition", this, 2);
		currentOrientation = new NslDinDouble0("currentOrientation", this);
		affordanceDirOut = new NslDoutDouble1("affordanceDirOut", this, size);
		affordanceDistOut = new NslDoutDouble1("affordanceDistOut", this, size);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end PPC

