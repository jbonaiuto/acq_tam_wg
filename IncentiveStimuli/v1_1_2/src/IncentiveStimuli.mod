package acq_tam_wg.IncentiveStimuli.v1_1_2.src;

nslJavaModule IncentiveStimuli(int size, int numDrives, double sigma){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  IncentiveStimuli
//versionName: 1_1_2


//variables 
private double maxIncentive; // 
public NslDinDouble2 incentivePosition(numDrives, 2); // 
public NslDinDouble1 motivationalState(numDrives); // 
public NslDinDouble1 currentPosition(2); // 
public NslDinDouble0 currentOrientation(); // 
public NslDoutDouble1 incentiveDirection(size); // 

//methods 
public void initModule()
{
	maxIncentive=1.0;
	nslAddSpatialCanvas("output", "incentive", incentiveDirection, 0.0, 1.0);
}

public void simTrain()
{
	process();
}

public void simRun()
{
	process();
}

protected void process()
{
	incentiveDirection=0;
	for(int i=0; i<numDrives; i++)
	{
		if(!Double.isNaN(incentivePosition.get(i,0)))
		{
			// Compute angle between current position and incentive position
			double[] baseVec=new double[]{1,0};
			double[] directionVec=new double[]{-incentivePosition.get(i,1)+currentPosition.get(1),-incentivePosition.get(i,0)+currentPosition.get(0)};
			double angle=(Math.PI+nslArcTan2(directionVec[1],directionVec[0])-nslArcTan2(baseVec[1],baseVec[0]))%(Math.PI*2.0);
			if(angle<0)
				angle=Math.PI*2.0+angle;

			// Get relative angle between current orientation and incentive direction
			double relAngle=getRelativeAngle(currentOrientation.get(), angle);

			for(int j=0; j<size; j++)
			{
				double prefAngle=-Math.PI+j*(2*Math.PI/(size-1));
				//double prefAngle=0+j*(2*Math.PI/(size-1));
				double dist=getDist(relAngle, prefAngle, -Math.PI, Math.PI);
				incentiveDirection.set(j, incentiveDirection.get(j)+maxIncentive*motivationalState.get(i)*nslExp(-nslPow(dist,2)/(2*sigma*sigma)));
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
	if(nslAbs(altDist)<nslAbs(dist))
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
		if(nslAbs(relAngleRight)<nslAbs(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	else
	{
		double relAngleLeft=ang2-ang1;
		double relAngleRight=relAngleLeft-2*Math.PI;
		if(nslAbs(relAngleRight)<nslAbs(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	return relativeAngle;
}
public void makeConn(){
}
}//end IncentiveStimuli

