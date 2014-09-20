package acq_tam_wg.Critic.v1_1_1.src;

nslJavaModule Critic(int size, int numDrives, int mapSize, double sigma){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Critic
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDinDouble1 rewards(numDrives); // 
public NslDoutDouble1 reductions(numDrives); // 
public NslDinDouble1 motivations(numDrives); // 
public NslDinDouble1 currentPosition(2); // 
private NslDouble1 lastPosition(2); // 
public NslDoutDouble1 desirability(size); // 
private double gamma; // 
private double alpha; // 
private NslDouble3 desirability_map(numDrives, mapSize+1, mapSize+1); // 
private NslDouble1 rewards_wm(numDrives); // 
private NslDouble1 motivation_wm(numDrives); // 
private double initDesirabilityScale; // 
private NslDouble0 currentOrientation; // 
private NslDouble2 thirst_desirability(mapSize+1, mapSize+1); // 
private NslDouble2 hunger_desirability(mapSize+1, mapSize+1); // 
private NslDouble2 fear_desirability(mapSize+1, mapSize+1); // 

//methods 
public void initModule()
{
	alpha=0.1;
	gamma=0.99;
	initDesirabilityScale=.3;
	currentOrientation=Math.PI;
	for(int i=0; i<numDrives; i++)
	{
		for(int x=0; x<mapSize+1; x++)
		{
			for(int y=0; y<mapSize+1; y++)
			{
				desirability_map.set(i,x,y,initDesirabilityScale*nslRandom());
			}
		}
	}
	nslAddSpatialCanvas("output", "local desirability", desirability, 0, 1);
	nslSetColumns(1,"output");
	nslAddThermalCanvas("desirability", "thirst desirability", thirst_desirability, 0, 1);
	nslAddThermalCanvas("desirability", "hunger desirability", hunger_desirability, 0, 1);
	//nslAddThermalCanvas("desirability", "fear", fear_desirability, 0, 1);
	nslSetColumns(1,"desirability");
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
	//if(!(((int)currentPosition.get(0))==((int)lastPosition.get(0)) && ((int)currentPosition.get(1))==((int)lastPosition.get(1))))
		learnDesirability();
	
	updateDesirabilityOutput();

	// Update variables
	rewards_wm.set(rewards.get());
	motivation_wm.set(motivations.get());
	reductions.set(rewards.get());
	lastPosition.set(currentPosition.get());
	thirst_desirability.set(desirability_map.get(0));
	hunger_desirability.set(desirability_map.get(1));
	//fear_desirability.set(desirability_map.get(2));
}

protected void learnDesirability()
{
	double[] curr_pos_desirability=new double[numDrives];
	double[] last_pos_desirability=new double[numDrives];
	double[] td_errors=new double[numDrives];
	for(int i=0; i<numDrives; i++)
	{
		// Get current and last position desirability
		curr_pos_desirability[i]=desirability_map.get(i,(int)currentPosition.get(0),(int)currentPosition.get(1));
		last_pos_desirability[i]=desirability_map.get(i,(int)lastPosition.get(0),(int)lastPosition.get(1));

		// Calculate TD error
		td_errors[i]=rewards_wm.get(i)/**motivation_wm.get(i)*/+gamma*curr_pos_desirability[i]-last_pos_desirability[i];

		// Update last position desirability
		desirability_map.set(i, (int)lastPosition.get(0), (int)lastPosition.get(1), last_pos_desirability[i]+alpha*td_errors[i]);
	}
}

protected void updateDesirabilityOutput()
{
	desirability=0;
	// Output desirability of adjacent locations
	for(int i=0; i<size; i++)
	{
		double prefAngle=-Math.PI+i*(2*Math.PI/(size-1));
		//right
		double d=computeDesirability((int)lastPosition.get(0), (int)lastPosition.get(1)-1, Math.PI, prefAngle);
		//left
		d=d+computeDesirability((int)lastPosition.get(0), (int)lastPosition.get(1)+1, 0, prefAngle);
		//up
		d=d+computeDesirability((int)lastPosition.get(0)-1, (int)lastPosition.get(1), 3*Math.PI/2, prefAngle);
		//upper right
		d=d+computeDesirability((int)lastPosition.get(0)-1, (int)lastPosition.get(1)-1, 5*Math.PI/4, prefAngle);
		//upper left
		d=d+computeDesirability((int)lastPosition.get(0)-1, (int)lastPosition.get(1)+1, 7*Math.PI/4, prefAngle);
		//down
		d=d+computeDesirability((int)lastPosition.get(0)+1, (int)lastPosition.get(1), Math.PI/2, prefAngle);
		//lower left
		d=d+computeDesirability((int)lastPosition.get(0)+1, (int)lastPosition.get(1)+1, Math.PI/4, prefAngle);
		//lower right
		d=d+computeDesirability((int)lastPosition.get(0)+1, (int)lastPosition.get(1)-1, 3*Math.PI/4, prefAngle);

		desirability.set(i, desirability.get(i)+d);
	}
}

protected double computeDesirability(int x, int y, double angle, double prefAngle)
{
	double d=0;
	if(x>=0 && x<=mapSize && y>=0 && y<=mapSize)
	{
		for(int j=0; j<numDrives; j++)
		{
			double dist=getDist(prefAngle, getRelativeAngle(angle));
			d=d+motivations.get(j)*desirability_map.get(j, x, y)*nslExp(-nslPow(dist, 2)/(2*sigma*sigma));
		}
	}
	return d;
}

protected double getDist(double ang1, double ang2)
{
	double dist=(ang1-ang2)%(2*Math.PI+0.001);
	double altDist=(ang2-ang1)%(2*Math.PI+0.001);
	if(nslAbs(altDist)<nslAbs(dist))
		dist=altDist;
	return dist;
}

protected double getRelativeAngle(double angle)
{
	double relativeAngle=0.0;
	if(currentOrientation.get()>angle)
	{
		double relAngleRight=(angle-currentOrientation.get());
		double relAngleLeft=2*Math.PI+relAngleRight;
		if(nslAbs(relAngleRight)<nslAbs(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	else
	{
		double relAngleLeft=angle-currentOrientation.get();
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
}//end Critic

