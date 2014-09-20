package acq_tam_wg.PPC.v1_1_2.src;

nslJavaModule PPC(int size, int maxNodes, double sigma, double wgDesirabilitySigma){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  PPC
//versionName: 1_1_2


//variables 
private double baseOrientation; // 
public NslDinDouble2 adjacentNodeCenters(maxNodes, 2); // 
public NslDinDouble1 currentPosition(2); // 
public NslDinDouble0 currentOrientation(); // 
public NslDoutDouble1 affordanceDirOut(size); // 
public NslDoutDouble1 affordanceDistOut(size); // 
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
				double[] baseVec=new double[]{1,0};
				double[] directionVec=new double[]{-adjacentNodeCenters.get(j,1)+currentPosition.get(1),-adjacentNodeCenters.get(j,0)+currentPosition.get(0)};
				double angle=(Math.PI+nslArcTan2(directionVec[1],directionVec[0])-nslArcTan2(baseVec[1],baseVec[0]))%(Math.PI*2.0);
				if(angle<0)
					angle=Math.PI*2.0+angle;

				// Compute relative angle between affordance and current orientation
				double relativeAngle=getRelativeAngle(currentOrientation.get(), angle);

				// Encode affordance direction in population code
				double angleDist=getDist(relativeAngle, affPrefAngle, -Math.PI, Math.PI);
				double distance=nslDistance(adjacentNodeCenters.get(j),currentPosition.get());
				double distanceBias=1-distance/maxDist;
				//double directionBias=nslExp(-nslPow((i-size/2.0), 2)/(2*500000*sigma*sigma));
				//double directionBias=nslExp(-nslPow((i-size/2.0), 2)/(2*250000*sigma*sigma));
				affordanceDirOut.set(i,nslMin(1.0,affordanceDirOut.get(i)+distanceBias*nslExp(-nslPow(angleDist, 2)/(2*sigma*sigma))));

				// Set affordance distance if this is above noise levels population code peak
				if(nslExp(-nslPow(angleDist, 2)/(2*nslPow(wgDesirabilitySigma*.75,2)))>.0000001)
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
}//end PPC

