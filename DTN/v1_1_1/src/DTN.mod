package acq_tam_wg.DTN.v1_1_1.src;

nslJavaModule DTN(int size, double sigma){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  DTN
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDinDouble0 currentOrientation(); // 
public NslDoutDouble1 orientationOut(size); // 

//methods 
public void initModule()
{
	nslAddSpatialCanvas("output", "head orientation", orientationOut, 0, 1);
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
		double dirPrefAngle=0+i*(2*Math.PI/(size-1));
		double dist=getDist(dirPrefAngle, currentOrientation.get(),0,2*Math.PI);
		orientationOut.set(i, nslExp(-nslPow(dist,2)/(2*sigma*sigma)));
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
public void makeConn(){
}
}//end DTN

