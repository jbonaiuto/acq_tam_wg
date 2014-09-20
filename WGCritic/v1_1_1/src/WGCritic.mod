package acq_tam_wg.WGCritic.v1_1_1.src;

nslJavaModule WGCritic(int numDrives, double[] d_min, double[] d_max){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  WGCritic
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDinDouble1 rewards(numDrives); // 
public NslDinDouble1 motivations(numDrives); // 
private int lastNodeId; // 
private double gamma; // 
private double[] rewardsWm; // 
private double[] motivationWm; // 
public NslDinInt0 currentNodeId(); // 
public NslDinDouble0 lastNodeDist(); // 
public NslDinDouble1 currentNodeDesirability(numDrives); // 
public NslDoutDouble1 reinforcement(numDrives); // 
public NslDinDouble1 lastNodeDesirability(numDrives); // 

//methods 
public void initModule()
{
	gamma=0.9;
}

public void reset()
{
	lastNodeId=-1;
	rewardsWm=new double[numDrives];
	motivationWm=new double[numDrives];
}


public void simTrain()
{
	if(system.getCurrentTime()<system.getDelta())
		reset();
	updateNodeReinforcement();
	lastNodeId=currentNodeId.get();
}

protected void updateNodeReinforcement()
{
	for(int i=0; i<numDrives; i++)
	{
		reinforcement.set(i,0);
		if(lastNodeId!=currentNodeId.get() && lastNodeId>-1)
		{
			reinforcement.set(i,rewardsWm[i]+(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*(gamma*currentNodeDesirability.get(i)-lastNodeDesirability.get(i)));
			rewardsWm[i]=rewards.get(i)*motivationWm[i];
		}
		else
			rewardsWm[i]=rewardsWm[i]+rewards.get(i)*motivationWm[i];
		
		motivationWm[i]=motivations.get(i);
		
	}
}

public double[] getFinalReinforcement()
{
	return rewardsWm;
}
public void makeConn(){
}
}//end WGCritic

