package acq_tam_wg.Critic.v1_1_2.src;

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

public class Critic extends NslJavaModule{

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Critic
//versionName: 1_1_2
//floatSubModules: true


//variables 
public  NslDinDouble1 rewards; // 
public  NslDinDouble1 motivations; // 
private int lastNodeId; // 
private double gamma; // 
private double rewardsWm[/**/]; // 
private double motivationWm[/**/]; // 
public  NslDinDouble1 currentNodeDesirability; // 
public  NslDoutDouble0 effectiveReinforcement; // 
public  NslDinInt0 currentNodeId; // 
private double desirabilityWm[/**/]; // 
public  NslDinDouble0 lastNodeDist; // 

//methods 
public void initModule()
{
	lastNodeId=-1;
	gamma=0.99;
	desirabilityWm= new  double[numDrives];
	rewardsWm= new  double[numDrives];
	motivationWm= new  double[numDrives];
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
			effectiveReinforcement.set(effectiveReinforcement.get()+motivationWm[i]*(rewardsWm[i]+NslOperator.pow.eval(gamma,lastNodeDist.get())*currentNodeDesirability.get(i)-desirabilityWm[i]));
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

	/******************************************************/
	/*                                                    */
	/* Generated by nslc.src.NslCompiler. Do not edit these lines! */
	/*                                                    */
	/******************************************************/

	/* Constructor and related methods */
	/* makeinst() declared variables */

	/* Formal parameters */
	int size;
	int numDrives;
	int mapSize;
	double sigma;

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public Critic(String nslName, NslModule nslParent, int size, int numDrives, int mapSize, double sigma)
{
		super(nslName, nslParent);
		this.size=size;
		this.numDrives=numDrives;
		this.mapSize=mapSize;
		this.sigma=sigma;
		initSys();
		makeInstCritic(nslName, nslParent, size, numDrives, mapSize, sigma);
	}

	public void makeInstCritic(String nslName, NslModule nslParent, int size, int numDrives, int mapSize, double sigma)
{ 
		Object[] nslArgs=new Object[]{size, numDrives, mapSize, sigma};
		callFromConstructorTop(nslArgs);
		rewards = new NslDinDouble1("rewards", this, numDrives);
		motivations = new NslDinDouble1("motivations", this, numDrives);
		currentNodeDesirability = new NslDinDouble1("currentNodeDesirability", this, numDrives);
		effectiveReinforcement = new NslDoutDouble0("effectiveReinforcement", this);
		currentNodeId = new NslDinInt0("currentNodeId", this);
		lastNodeDist = new NslDinDouble0("lastNodeDist", this);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end Critic

