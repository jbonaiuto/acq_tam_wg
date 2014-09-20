package acq_tam_wg.TAMActor.v1_1_1.src;

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

public class TAMActor extends NslJavaModule{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  TAMActor
//versionName: 1_1_1


//variables 
private  NslDouble3 policy; // 
private  NslDouble1 effectivePolicy; // 
private int currentNodeId; // 
private int lastNodeId; // 
private double alpha; // 
public  NslDinDouble1 eligibility; // 
public  NslDinInt0 currentNodeIdIn; // 
public  NslDinDouble1 motivations; // 
public  NslDinDouble1 reinforcement; // 
public  NslDoutDouble1 desirability; // 

//methods 
/**
 * Executed when NSL starts
 */
public void initSys()
{
	alpha=.01;
}

/**
 * Executed when the module is initialized
 */
public void initModule()
{
	nslAddSpatialCanvas("output", "tamPolicy", desirability, 0.0, maxDesirability);
}

public void initTrain()
{
	init();
}

public void initRun()
{
	init();
}

protected void init()
{
	// Reset node IDs
	lastNodeId=-1;
	currentNodeId=-1;
}

/**
 * Executed during each run cycle
 */
public void simRun()
{
	// Update node IDs
	updateNodeIds();

	// Update desirability for current state
	updateDesirability();
}

/**
 * Executed during each training cycle
 */
public void simTrain()
{
	// Update node IDs
	updateNodeIds();

	// Update policy for last state
	updatePolicy();

	// Update desirability for current state
	updateDesirability();
}

/**
 * Update current and last node ID
 */
protected void updateNodeIds()
{
	if(currentNodeIdIn.get()>-1)
	{
		if(currentNodeIdIn.get()!=currentNodeId&&currentNodeId>-1)
			lastNodeId=currentNodeId;
		currentNodeId=currentNodeIdIn.get();
	}
}

/**
 * Update the desirability for the current state
 */
protected void updateDesirability()
{
	// Compute the effective policy - policy weighted by motivation levels
	updateEffectivePolicy();

	// Desirability is just effective policy
	desirability.set(effectivePolicy.get());
}

/**
 * Update the policy using the output of the critic
 */
public void updatePolicy()
{
	// If this is not the first state we've encountered
	if(lastNodeId>-1&&lastNodeId!=currentNodeIdIn.get())
	{
		for(int i=0; i<numDrives; i++)
		{
			// update policy using learning rate (alpha), reinforcement from critic and eligibility signal
			for(int j=0; j<size; j++)
				policy.set(lastNodeId, i, j, policy.get(lastNodeId,i,j)+(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*alpha*reinforcement.get(i)*eligibility.get(j));
		}

		// Normalize policy
		normalizePolicy();
	}
}

/**
 * Normalizes the policy so that each drive's policy is less than d_max
 */
protected void normalizePolicy()
{
	for(int i=0; i<numDrives; i++)
	{
		double maxVal=NslMaxValue.eval(policy.get(lastNodeId, i));
		if(maxVal>0.0)
		{
			for(int j=0; j<size; j++)
				policy.set(lastNodeId, i, j, (policy.get(lastNodeId,i,j)/maxVal)*NslMin.eval(d_max[i],maxVal));
		}
	}
}

/**
 * Update the effective policy based on motivation levels
 */
public void updateEffectivePolicy()
{
	// If we are currently in a state
	if(currentNodeId>-1)
	{
		effectivePolicy.set(0);

		for(int i=0; i<numDrives; i++)
		{
			// Compute the center of mass for this policy
			double centerOfMass=0.0, total=0.0, maxVal=0.0;
			for(int j=0; j<size; j++)
			{
				centerOfMass=centerOfMass+policy.get(currentNodeId,i,j)*(j+1);
				total=total+policy.get(currentNodeId,i,j);
				maxVal=NslMax.eval(maxVal, policy.get(currentNodeId, i, j));
			}

			// If there is some policy
			if(total>0.0)
			{
				// Compute center-of-mass
				centerOfMass=centerOfMass/total-1.0;

				// drive policies are a Gaussian on the center of mass, weighted by drive level
				for(int j=0; j<size; j++)
					effectivePolicy.set(j, effectivePolicy.get(j)+(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*maxVal*NslOperator.exp.eval(-NslOperator.pow.eval(j-centerOfMass,2)/(2*sigma*sigma)));
			}
		}
	}
}

/**
 * Save policy to file
 */
public void saveWeights(String filename)
{
	NslTextFile file= new  NslTextFile(filename);
	file.open('W');
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<numDrives; j++)
		{
			for(int k=0; k<size; k++)
			{
				if(Double.isNaN(policy.get(i,j,k)))
					file.puts("NaN");
				else
					file.puts(policy.get(i,j,k));
			}
		}
	}
	file.flush();
	file.close();
}

/**
 * Load policy from file
 */
public void loadWeights(String filename)
{
	NslTextFile file= new  NslTextFile(filename);
	file.open('R');
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<numDrives; j++)
		{
			for(int k=0; k<size; k++)
			{
				String ln=file.gets();
				if(ln.equals("NaN"))
					policy.set(i,j,k,Double.NaN);
				else
					policy.set(i,j,k,Double.parseDouble(ln));
			}
		}
	}
	file.close();
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
	int maxNodes;
	double sigma;
	double maxDesirability;
	double[] d_min;
	double[] d_max;

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public TAMActor(String nslName, NslModule nslParent, int size, int numDrives, int maxNodes, double sigma, double maxDesirability, double[] d_min, double[] d_max)
{
		super(nslName, nslParent);
		this.size=size;
		this.numDrives=numDrives;
		this.maxNodes=maxNodes;
		this.sigma=sigma;
		this.maxDesirability=maxDesirability;
		this.d_min=d_min;
		this.d_max=d_max;
		initSys();
		makeInstTAMActor(nslName, nslParent, size, numDrives, maxNodes, sigma, maxDesirability, d_min, d_max);
	}

	public void makeInstTAMActor(String nslName, NslModule nslParent, int size, int numDrives, int maxNodes, double sigma, double maxDesirability, double[] d_min, double[] d_max)
{ 
		Object[] nslArgs=new Object[]{size, numDrives, maxNodes, sigma, maxDesirability, d_min, d_max};
		callFromConstructorTop(nslArgs);
		policy = new NslDouble3("policy", this, maxNodes, numDrives, size);
		effectivePolicy = new NslDouble1("effectivePolicy", this, size);
		eligibility = new NslDinDouble1("eligibility", this, size);
		currentNodeIdIn = new NslDinInt0("currentNodeIdIn", this);
		motivations = new NslDinDouble1("motivations", this, numDrives);
		reinforcement = new NslDinDouble1("reinforcement", this, numDrives);
		desirability = new NslDoutDouble1("desirability", this, size);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end TAMActor

