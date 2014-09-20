package acq_tam_wg.WGActor.v1_1_1.src;
import java.awt.*;

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

public class WGActor extends NslJavaModule{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  WGActor
//versionName: 1_1_1


//variables 
private  NslDouble3 policy; // 
private  NslDouble2 effectivePolicy; // 
private int currentNodeId; // 
private int lastNodeId; // 
private double alpha; // 
public  NslDinDouble1 reinforcement; // 
public  NslDoutDouble1 desirability; // 
public  NslDinInt0 currentNodeIdIn; // 
public  NslDinDouble1 eligibility; // 
public  NslDinDouble1 motivations; // 
public  NslDinDouble0 currentOrientation; // 
public  NslDinDouble1 edgeLengths; // 
private double aversiveTau; // 
private double appetitiveTau; // 
private  NslDouble1 node0FearPolicy; // 
private  NslDouble1 node1FearPolicy; // 
private  NslDouble1 node2FearPolicy; // 
private  NslDouble1 node3FearPolicy; // 
private  NslDouble1 node0HungerPolicy; // 
private  NslDouble1 node1HungerPolicy; // 
private  NslDouble1 node2HungerPolicy; // 
private  NslDouble1 node3HungerPolicy; // 
public  NslDinInt0 movingAwayFromNode; // 

//methods 
/**
 * Executed when NSL starts
 */
public void initSys()
{
	alpha=.2;
	aversiveTau=4;
	appetitiveTau=100;
}

/**
 * Executed when the module is initialized
 */
public void initModule()
{
	nslAddSpatialCanvas("output", "wgPolicy", desirability, 0.0, maxDesirability);
	nslSetColumns(1,"output");
	nslAddSpatialCanvas("debug","node3Hunger", node3HungerPolicy, 0, d_max[1], NslColor.getColor("RED"));
	nslAddSpatialCanvas("debug","node3Fear", node3FearPolicy, -d_max[2], 0, NslColor.getColor("BLUE"));
	nslAddSpatialCanvas("debug","node2Hunger", node2HungerPolicy, 0, d_max[1], NslColor.getColor("RED"));
	nslAddSpatialCanvas("debug","node2Fear", node2FearPolicy, -d_max[2], 0, NslColor.getColor("BLUE"));
	nslAddSpatialCanvas("debug","node1Hunger", node1HungerPolicy, 0, d_max[1], NslColor.getColor("RED"));
	nslAddSpatialCanvas("debug","node1Fear", node1FearPolicy, -d_max[2], 0, NslColor.getColor("BLUE"));
	nslAddSpatialCanvas("debug","node0Hunger", node0HungerPolicy, 0, d_max[1], NslColor.getColor("RED"));
	nslAddSpatialCanvas("debug","node0Fear", node0FearPolicy, -d_max[2], 0, NslColor.getColor("BLUE"));
	nslSetColumns(1, "debug");
}

public void reset()
{
	// update node IDs
	lastNodeId=-1;
	currentNodeId=-1;
}

/**
 * Executed before each training epoch
 */
public void initTrain()
{
	reset();
}

/**
 * Executed at the start of each run epoch
 */
public void initRun()
{
	reset();
}

/**
 * Executed during each training cycle
 */
public void simTrain()
{
	// Update node IDs
	updateNodeIds();

	// Update policy for last node
	updatePolicy();

	// Compute desirability
	updateDesirability();
}

/**
 * Executed during each run cycle
 */
public void simRun()
{
	// Update node IDs
	updateNodeIds();

	// Compute desirability
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
 * Updates desirability
 */
protected void updateDesirability()
{
	// effective policy is weighted sum of policy for current node
	updateEffectivePolicy();

	// rotate policy so it is in egocentric reference frame
	rotatePolicy();
}

/**
 * Update the policy using output of the critic
 */
public void updatePolicy()
{
	// If this is not the first node we've been to
	if(lastNodeId>-1&&lastNodeId!=currentNodeIdIn.get())
	{
		for(int i=0; i<numDrives; i++)
		{
			// update policy using learning rate (alpha), reinforcement from critic and eligibility signal
			for(int j=0; j<size; j++)
				policy.set(lastNodeId, i, j, policy.get(lastNodeId,i,j)+(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*alpha*reinforcement.get(i)*eligibility.get(j));
		}

		// Normalize the policy
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
	// If we are currently at a node
	if(currentNodeId>-1)
	{
		effectivePolicy.set(0);
		
		for(int j=0; j<size ; j++)
		{
			// drive policies are weighted by drive level
			for(int i=0; i<numDrives; i++)
			{
				effectivePolicy.set(i, j, effectivePolicy.get(i,j)+(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*policy.get(currentNodeId,i,j));
			}
		}
	}
	node0FearPolicy.set(policy.get(0, 2));
	node0HungerPolicy.set(policy.get(0, 1));
	node1FearPolicy.set(policy.get(1, 2));
	node1HungerPolicy.set(policy.get(1, 1));
	node2FearPolicy.set(policy.get(2, 2));
	node2HungerPolicy.set(policy.get(2, 1));
	node3FearPolicy.set(policy.get(3, 2));
	node3HungerPolicy.set(policy.get(3, 1));
}

/**
 * Rotate the policy based on the current orientation
 */
protected void rotatePolicy()
{
	// Number of radians represented by each neuron
	double radiansPerUnit=(2.0*Math.PI)/size;
	// Number of neurons to shift by
	int unitsToShift=(int)((currentOrientation.get()-Math.PI)/radiansPerUnit);

	for(int j=0; j<size; j++)
	{
		desirability.set(j,0);
		for(int i=0; i<numDrives; i++)
		{
			// Compute rotated index, with wraparound
			int idx=j+unitsToShift;
			if(idx<0)
				idx=j+unitsToShift+size;
			else if(idx>=size)
				idx=idx-size;

			// Compute the distance bias
			double distBias=1.0;
			// in reverse direction desirability actually depends on the nodes before the last one
			if((j>=10&&j<=size-10)||movingAwayFromNode.get()<1)
			{
				// Compute the distance bias for appetitive drives
				if(i<2&&NslOperator.abs.eval(effectivePolicy.get(i,idx))>0&&edgeLengths.get(j)>0)
					distBias=NslOperator.exp.eval(-NslMax.eval(0.0,edgeLengths.get(j))/appetitiveTau);
				// Compute the distance bias for aversive drives
				else if(i>=2&&NslOperator.abs.eval(effectivePolicy.get(i,idx))>0&&edgeLengths.get(j)>0)
					distBias=NslOperator.exp.eval(-NslMax.eval(0.0,edgeLengths.get(j))/aversiveTau);
			}
			else
			{
				// Compute the distance bias for appetitive drives
				if(i<2&&NslOperator.abs.eval(effectivePolicy.get(i,idx))>0&&edgeLengths.get(j)>0)
					distBias=NslOperator.exp.eval(-NslMax.eval(0.0,edgeLengths.get(j)+15)/appetitiveTau);
				// Compute the distance bias for aversive drives
				else if(i>=2&&NslOperator.abs.eval(effectivePolicy.get(i,idx))>0&&edgeLengths.get(j)>0)
					distBias=NslOperator.exp.eval(-NslMax.eval(0.0,edgeLengths.get(j)+15)/aversiveTau);
			}
			// Desirability is rotated policy weighted by distance bias
			desirability.set(j, desirability.get(j)+distBias*effectivePolicy.get(i,idx));
		}
	}
}

/**
 * Save policy to file
 */
public void saveWeights(String filename)
{
	NslTextFile file = new NslTextFile("file", this, filename);
        
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
	NslTextFile file = new NslTextFile("file", this, filename);
        
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
	public WGActor(String nslName, NslModule nslParent, int size, int numDrives, int maxNodes, double sigma, double maxDesirability, double[] d_min, double[] d_max)
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
		makeInstWGActor(nslName, nslParent, size, numDrives, maxNodes, sigma, maxDesirability, d_min, d_max);
	}

	public void makeInstWGActor(String nslName, NslModule nslParent, int size, int numDrives, int maxNodes, double sigma, double maxDesirability, double[] d_min, double[] d_max)
{ 
		Object[] nslArgs=new Object[]{size, numDrives, maxNodes, sigma, maxDesirability, d_min, d_max};
		callFromConstructorTop(nslArgs);
		policy = new NslDouble3("policy", this, maxNodes, numDrives, size);
		effectivePolicy = new NslDouble2("effectivePolicy", this, numDrives, size);
		reinforcement = new NslDinDouble1("reinforcement", this, numDrives);
		desirability = new NslDoutDouble1("desirability", this, size);
		currentNodeIdIn = new NslDinInt0("currentNodeIdIn", this);
		eligibility = new NslDinDouble1("eligibility", this, size);
		motivations = new NslDinDouble1("motivations", this, numDrives);
		currentOrientation = new NslDinDouble0("currentOrientation", this);
		edgeLengths = new NslDinDouble1("edgeLengths", this, size);
		node0FearPolicy = new NslDouble1("node0FearPolicy", this, size);
		node1FearPolicy = new NslDouble1("node1FearPolicy", this, size);
		node2FearPolicy = new NslDouble1("node2FearPolicy", this, size);
		node3FearPolicy = new NslDouble1("node3FearPolicy", this, size);
		node0HungerPolicy = new NslDouble1("node0HungerPolicy", this, size);
		node1HungerPolicy = new NslDouble1("node1HungerPolicy", this, size);
		node2HungerPolicy = new NslDouble1("node2HungerPolicy", this, size);
		node3HungerPolicy = new NslDouble1("node3HungerPolicy", this, size);
		movingAwayFromNode = new NslDinInt0("movingAwayFromNode", this);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end WGActor

