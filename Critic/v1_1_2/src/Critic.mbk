package acq_tam_wg.Critic.v1_1_2.src;

nslJavaModule Critic(int size, int numDrives, int mapSize, double sigma){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Critic
//versionName: 1_1_2
//floatSubModules: true


//variables 
public NslDinDouble1 rewards(numDrives); // 
public NslDinDouble1 motivations(numDrives); // 
private int lastNodeId; // 
private double gamma; // 
private double rewardsWm[/**/]; // 
private double motivationWm[/**/]; // 
public NslDinDouble1 currentNodeDesirability(numDrives); // 
public NslDoutDouble0 effectiveReinforcement(); // 
public NslDinInt0 currentNodeId(); // 
private double desirabilityWm[/**/]; // 
public NslDinDouble0 lastNodeDist(); // 

//methods 
public void initModule()
{
	lastNodeId=-1;
	gamma=0.99;
	desirabilityWm=new double[numDrives];
	rewardsWm=new double[numDrives];
	motivationWm=new double[numDrives];
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
	effectiveReinforcement.set(0.0);
	for(int i=0; i<numDrives; i++)
	{
		if(lastNodeId!=currentNodeId.get())
		{
			effectiveReinforcement.set(effectiveReinforcement.get()+motivationWm[i]*(rewardsWm[i]+nslPow(gamma,lastNodeDist.get())*currentNodeDesirability.get(i)-desirabilityWm[i]));
			rewardsWm[i]=rewards.get(i);
		}
		else
			rewardsWm[i]=rewardsWm[i]+rewards.get(i);

		motivationWm[i]=motivations.get(i);
		desirabilityWm[i]=currentNodeDesirability.get(i);
	}
	lastNodeId=currentNodeId.get();
	
}
public void makeConn(){
}
}//end Critic

