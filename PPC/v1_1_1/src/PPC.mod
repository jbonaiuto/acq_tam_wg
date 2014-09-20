package acq_tam_wg.PPC.v1_1_1.src;

nslJavaModule PPC(int size, int maxTaxons, double sigma){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  PPC
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDinDouble1 taxons(maxTaxons); // 
public NslDinDouble0 numTaxons(); // 
private double baseOrientation; // 
public NslDoutDouble1 affordancesOut(size); // 

//methods 
public void initModule()
{
	//baseOrientation=Math.PI;

	nslAddSpatialCanvas("output", "affordances", affordancesOut, 0, 1);
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
		//double affPrefAngle=-Math.PI+i*(2*Math.PI/(size-1));
		double affPrefAngle=0+i*(2*Math.PI/(size-1));
		affordancesOut.set(i,0.0);
		for(int j=0; j<numTaxons.get(); j++)
		{
			//double relativeAngle=getRelativeAngle(baseOrientation, taxons.get(j));
			//double dist1=getDist(affPrefAngle,relativeAngle,-Math.PI,Math.PI);
			double dist1=getDist(affPrefAngle,taxons.get(j),0,2*Math.PI);
			//double dist2=getDist(0,affPrefAngle,-Math.PI,Math.PI);
			affordancesOut.set(i,affordancesOut.get(i)+nslExp(-nslPow(dist1, 2)/(2*sigma*sigma))/**nslExp(-nslPow(dist2,2)/(2*400*sigma*sigma))*/);
		}
	}
}

protected double getDist(double ang1, double ang2, double min, double max)
{
	double dist=(ang1-ang2)%(2*Math.PI+0.001);
	//double altDist=(ang2-ang1)%(2*Math.PI+0.001);
	double altDist=dist;
	if(ang1<ang2)
		altDist=max-ang2+ang1-min;
	else if(ang2<ang1)
		altDist=max-ang1+ang2-min;
	if(nslAbs(altDist)<nslAbs(dist))
		dist=altDist;
	return dist;
}

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

