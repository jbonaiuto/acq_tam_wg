package acq_tam_wg.TAMCritic.v1_1_1.src;

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

public class TAMCritic extends NslJavaModule{

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  TAMCritic
//versionName: 1_1_1
//floatSubModules: true


//variables 
public  NslDinDouble1 rewards; // 
public  NslDinDouble1 motivations; // 
private double gamma; // 
private double[] rewards_wm; // 
private double[] motivation_wm; // 
private  NslDouble1 predictorOutput; // 
private  NslDouble1 lastPredictorOutput; // 
public  NslDinInt0 currentNodeId; // 
private int lastNodeId; // 
public  NslDinDouble2 currentDesirability; // 
public  NslDoutDouble1 reinforcement; // 

//methods 
public void initModule()
{
	gamma=0.9;
}

public void reset()
{
	lastNodeId=-1;
	rewards_wm= new  double[numDrives];
	motivation_wm= new  double[numDrives];
}

public void simTrain()
{
	if(system.getCurrentTime()<system.getDelta())
		reset();

	updateReinforcement();

	lastNodeId=currentNodeId.get();
}

protected void updateReinforcement()
{
	for(int i=0; i<numDrives; i++)
	{
		predictorOutput.set(i, NslSum.eval(currentDesirability.get(i)));
		reinforcement.set(i, 0);
		if(currentNodeId.get()!=lastNodeId&&lastNodeId>-1)
		{
			reinforcement.set(i, rewards_wm[i]+(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*(gamma*predictorOutput.get(i)-lastPredictorOutput.get(i)));
			rewards_wm[i]=rewards.get(i)*motivation_wm[i];
		}
		else
			rewards_wm[i]=rewards_wm[i]+rewards.get(i)*motivation_wm[i];

		motivation_wm[i]=motivations.get(i);

		lastPredictorOutput.set(i,predictorOutput.get(i));
	}
}

public double[] getFinalReinforcement()
{
	return rewards_wm;
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
	double[] d_min;
	double[] d_max;

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public TAMCritic(String nslName, NslModule nslParent, int size, int numDrives, double[] d_min, double[] d_max)
{
		super(nslName, nslParent);
		this.size=size;
		this.numDrives=numDrives;
		this.d_min=d_min;
		this.d_max=d_max;
		initSys();
		makeInstTAMCritic(nslName, nslParent, size, numDrives, d_min, d_max);
	}

	public void makeInstTAMCritic(String nslName, NslModule nslParent, int size, int numDrives, double[] d_min, double[] d_max)
{ 
		Object[] nslArgs=new Object[]{size, numDrives, d_min, d_max};
		callFromConstructorTop(nslArgs);
		rewards = new NslDinDouble1("rewards", this, numDrives);
		motivations = new NslDinDouble1("motivations", this, numDrives);
		predictorOutput = new NslDouble1("predictorOutput", this, numDrives);
		lastPredictorOutput = new NslDouble1("lastPredictorOutput", this, numDrives);
		currentNodeId = new NslDinInt0("currentNodeId", this);
		currentDesirability = new NslDinDouble2("currentDesirability", this, numDrives, size);
		reinforcement = new NslDoutDouble1("reinforcement", this, numDrives);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end TAMCritic

