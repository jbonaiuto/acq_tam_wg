package acq_tam_wg.RatBrain.v1_1_1.src;
import javax.vecmath.*;
import acq_tam_wg.MotivationalSchema.v1_1_2.src.*;
import acq_tam_wg.IncentiveStimuli.v1_1_2.src.*;
import acq_tam_wg.PPC.v1_1_2.src.*;
import acq_tam_wg.WorldGraph.v1_1_2.src.*;
import acq_tam_wg.Actor.v1_1_2.src.*;

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

public class RatBrain extends NslJavaModule{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  RatBrain
//versionName: 1_1_1


//variables 
public  acq_tam_wg.MotivationalSchema.v1_1_2.src.MotivationalSchema motivationalSchema; // 
public  acq_tam_wg.IncentiveStimuli.v1_1_2.src.IncentiveStimuli incentives; // 
public  acq_tam_wg.PPC.v1_1_2.src.PPC ppc; // 
public  acq_tam_wg.WorldGraph.v1_1_2.src.WorldGraph wg; // 
public  acq_tam_wg.Actor.v1_1_2.src.Actor actor; // 
private double incentiveSigma; // 
private double affordanceSigma; // 
private double wgDesirabilitySigma; // 
private double tamDesirabilitySigma; // 
private double signalThreshold; // 
private double[] d_min; // 
private double[] d_max; // 
private double maxDesirability; // 
public  NslDinDouble2 incentivesPosition; // 
public  NslDinDouble1 reductions; // 
public  NslDinDouble1 incentiveStrength; // 
public  NslDinDouble1 currentMapPosition; // 
public  NslDinDouble0 currentOrientation; // 
public  NslDoutDouble1 goalPosition; // 
public  NslDoutDouble0 goSignal; // 
public  NslDinDouble1 currentNodeCenter; // 
public  NslDinDouble2 affordances; // 
public  NslDinDouble2 adjacentNodeCenters; // 
public  NslDinDouble1 currentWorldPosition; // 
public  NslDinDouble1 currentNodeWorldPosition; // 
private  NslDouble1 nodeWorldPosition; // 

//methods 
/**
 * Executed when NSL starts
 */
public void initSys()
{
	incentiveSigma=.25;
	affordanceSigma=.1;
	wgDesirabilitySigma=.25;
	tamDesirabilitySigma=5;
	signalThreshold=.1;
	d_min= new  double[]{0.0, 0.0, 0.0};
	d_max= new  double[]{2.0, 2.0, 5.0};
	maxDesirability=1.0;
}

/**
 * Executed at the end of module constructor
 */
public void callFromConstructorBottom()
{
	actor.movingAwayFromNode.setReference( new  NslInt0());
}


/**
 * Executed before each training epoch
 */
public void initTrain()
{
	init();
}

/**
 * Executed at the start of each run epoch
 */
public void initRun()
{
	init();
}

protected void init()
{
	actor.movingAwayFromNode.set(0);
}

/**
 * Executed during each training cycle
 */
public void simTrain()
{
	process();
}

/**
 * Executed during each run cycle
 */
public void simRun()
{
	process();
}

protected void process()
{
	if(!Double.isNaN(currentNodeWorldPosition.get(0)))
	{
		nodeWorldPosition.set(0, currentNodeWorldPosition.get(0));
		nodeWorldPosition.set(1, currentNodeWorldPosition.get(1));
	}
	// Compute orientation vector from current orientation angle		
	double[] orientationVec= new  double[]{NslOperator.cos.eval(currentOrientation.get()-Math.PI), NslOperator.sin.eval(currentOrientation.get()-Math.PI)};
	// Compute goal position orientation vector from current position and goal position
	double[] goalVec= new  double[]{-nodeWorldPosition.get(1)+currentWorldPosition.get(1), -nodeWorldPosition.get(0)+currentWorldPosition.get(0)};
	// Angle between current orientation and direction of the current goal
	double angle=NslOperator.atan2.eval(goalVec[1],goalVec[0])-NslOperator.atan2.eval(orientationVec[1],orientationVec[0]);
	// Set the goal orientation
	//double nodeOrientation=(currentOrientation.get()+angle)%(Math.PI*2.0);
	if(angle<0)
		angle=Math.PI*2+angle;
	if((angle<=6&&angle>=.28))
		actor.movingAwayFromNode.set(1);
	else
		actor.movingAwayFromNode.set(0);
}
public void makeConn(){
    nslConnect(incentiveStrength,motivationalSchema.incentives);
    nslConnect(currentMapPosition,incentives.currentPosition);
    nslConnect(currentMapPosition,actor.currentPosition);
    nslConnect(currentMapPosition,ppc.currentPosition);
    nslConnect(currentMapPosition,wg.currentPosition);
    nslConnect(currentNodeCenter,wg.currentNodeCenter);
    nslConnect(currentOrientation,incentives.currentOrientation);
    nslConnect(currentOrientation,ppc.currentOrientation);
    nslConnect(currentOrientation,actor.currentOrientation);
    nslConnect(currentOrientation,wg.currentOrientation);
    nslConnect(affordances,ppc.adjacentNodeCenters);
    nslConnect(adjacentNodeCenters,wg.adjacentNodeCenters);
    nslConnect(incentivesPosition,incentives.incentivePosition);
    nslConnect(reductions,motivationalSchema.reductions);
    nslConnect(incentives.incentiveDirection,actor.incentives);
    nslConnect(ppc.affordanceDirOut,actor.executability);
    nslConnect(ppc.affordanceDistOut,actor.distances);
    nslConnect(motivationalSchema.motivationalState,incentives.motivationalState);
    nslConnect(motivationalSchema.motivationalState,actor.motivations);
    nslConnect(motivationalSchema.wgReinforcement,actor.wgReinforcement);
    nslConnect(motivationalSchema.wgReinforcement,wg.currentNodeDesirability);
    nslConnect(motivationalSchema.tamReinforcement,actor.tamReinforcement);
    nslConnect(wg.currentNodeId,motivationalSchema.currentNodeId);
    nslConnect(wg.currentNodeId,actor.currentNodeIdIn);
    nslConnect(wg.lastNodeDist,motivationalSchema.lastDist);
    nslConnect(wg.eligibility,actor.wgEligibility);
    nslConnect(wg.novelNodeBias,actor.novelNodeBias);
    nslConnect(actor.goalPosition,goalPosition);
    nslConnect(actor.goSignal,goSignal);
}

	/******************************************************/
	/*                                                    */
	/* Generated by nslc.src.NslCompiler. Do not edit these lines! */
	/*                                                    */
	/******************************************************/

	/* Constructor and related methods */
	/* makeinst() declared variables */

	/* Formal parameters */
	int angleRepSize;
	int numDrives;
	int maxNodes;
	double actionThreshold;

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public RatBrain(String nslName, NslModule nslParent, int angleRepSize, int numDrives, int maxNodes, double actionThreshold)
{
		super(nslName, nslParent);
		this.angleRepSize=angleRepSize;
		this.numDrives=numDrives;
		this.maxNodes=maxNodes;
		this.actionThreshold=actionThreshold;
		initSys();
		makeInstRatBrain(nslName, nslParent, angleRepSize, numDrives, maxNodes, actionThreshold);
	}

	public void makeInstRatBrain(String nslName, NslModule nslParent, int angleRepSize, int numDrives, int maxNodes, double actionThreshold)
{ 
		Object[] nslArgs=new Object[]{angleRepSize, numDrives, maxNodes, actionThreshold};
		callFromConstructorTop(nslArgs);
		motivationalSchema = new acq_tam_wg.MotivationalSchema.v1_1_2.src.MotivationalSchema("motivationalSchema", this, maxNodes, numDrives, angleRepSize, d_min, d_max, maxDesirability);
		incentives = new acq_tam_wg.IncentiveStimuli.v1_1_2.src.IncentiveStimuli("incentives", this, angleRepSize, numDrives, incentiveSigma);
		ppc = new acq_tam_wg.PPC.v1_1_2.src.PPC("ppc", this, angleRepSize, maxNodes, affordanceSigma, wgDesirabilitySigma);
		wg = new acq_tam_wg.WorldGraph.v1_1_2.src.WorldGraph("wg", this, angleRepSize, numDrives, d_min, d_max, maxNodes, wgDesirabilitySigma, maxDesirability);
		actor = new acq_tam_wg.Actor.v1_1_2.src.Actor("actor", this, angleRepSize, numDrives, maxNodes, signalThreshold, actionThreshold, tamDesirabilitySigma, wgDesirabilitySigma, maxDesirability, d_min, d_max);
		incentivesPosition = new NslDinDouble2("incentivesPosition", this, numDrives, 2);
		reductions = new NslDinDouble1("reductions", this, numDrives);
		incentiveStrength = new NslDinDouble1("incentiveStrength", this, numDrives);
		currentMapPosition = new NslDinDouble1("currentMapPosition", this, 2);
		currentOrientation = new NslDinDouble0("currentOrientation", this);
		goalPosition = new NslDoutDouble1("goalPosition", this, 2);
		goSignal = new NslDoutDouble0("goSignal", this);
		currentNodeCenter = new NslDinDouble1("currentNodeCenter", this, 2);
		affordances = new NslDinDouble2("affordances", this, maxNodes, 2);
		adjacentNodeCenters = new NslDinDouble2("adjacentNodeCenters", this, maxNodes, 2);
		currentWorldPosition = new NslDinDouble1("currentWorldPosition", this, 2);
		currentNodeWorldPosition = new NslDinDouble1("currentNodeWorldPosition", this, 2);
		nodeWorldPosition = new NslDouble1("nodeWorldPosition", this, 2);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end RatBrain

