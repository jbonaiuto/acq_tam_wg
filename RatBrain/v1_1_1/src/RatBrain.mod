package acq_tam_wg.RatBrain.v1_1_1.src;
nslImport javax.vecmath.*;
nslImport acq_tam_wg.MotivationalSchema.v1_1_2.src.*;
nslImport acq_tam_wg.IncentiveStimuli.v1_1_2.src.*;
nslImport acq_tam_wg.PPC.v1_1_2.src.*;
nslImport acq_tam_wg.WorldGraph.v1_1_2.src.*;
nslImport acq_tam_wg.Actor.v1_1_2.src.*;

nslJavaModule RatBrain(int angleRepSize, int numDrives, int maxNodes, double actionThreshold){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  RatBrain
//versionName: 1_1_1


//variables 
public acq_tam_wg.MotivationalSchema.v1_1_2.src.MotivationalSchema motivationalSchema(maxNodes, numDrives, angleRepSize, d_min, d_max, maxDesirability); // 
public acq_tam_wg.IncentiveStimuli.v1_1_2.src.IncentiveStimuli incentives(angleRepSize, numDrives, incentiveSigma); // 
public acq_tam_wg.PPC.v1_1_2.src.PPC ppc(angleRepSize, maxNodes, affordanceSigma, wgDesirabilitySigma); // 
public acq_tam_wg.WorldGraph.v1_1_2.src.WorldGraph wg(angleRepSize, numDrives, d_min, d_max, maxNodes, wgDesirabilitySigma, maxDesirability); // 
public acq_tam_wg.Actor.v1_1_2.src.Actor actor(angleRepSize, numDrives, maxNodes, signalThreshold, actionThreshold, tamDesirabilitySigma, wgDesirabilitySigma, maxDesirability, d_min, d_max); // 
private double incentiveSigma; // 
private double affordanceSigma; // 
private double wgDesirabilitySigma; // 
private double tamDesirabilitySigma; // 
private double signalThreshold; // 
private double[] d_min; // 
private double[] d_max; // 
private double maxDesirability; // 
public NslDinDouble2 incentivesPosition(numDrives, 2); // 
public NslDinDouble1 reductions(numDrives); // 
public NslDinDouble1 incentiveStrength(numDrives); // 
public NslDinDouble1 currentMapPosition(2); // 
public NslDinDouble0 currentOrientation(); // 
public NslDoutDouble1 goalPosition(2); // 
public NslDoutDouble0 goSignal(); // 
public NslDinDouble1 currentNodeCenter(2); // 
public NslDinDouble2 affordances(maxNodes, 2); // 
public NslDinDouble2 adjacentNodeCenters(maxNodes, 2); // 
public NslDinDouble1 currentWorldPosition(2); // 
public NslDinDouble1 currentNodeWorldPosition(2); // 
private NslDouble1 nodeWorldPosition(2); // 

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
	d_min=new double[]{0.0, 0.0, 0.0};
	d_max=new double[]{2.0, 2.0, 5.0};
	maxDesirability=1.0;
}

/**
 * Executed at the end of module constructor
 */
public void callFromConstructorBottom()
{
	actor.movingAwayFromNode.setReference(new NslInt0());
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
	double[] orientationVec=new double[]{nslCos(currentOrientation.get()-Math.PI), nslSin(currentOrientation.get()-Math.PI)};
	// Compute goal position orientation vector from current position and goal position
	double[] goalVec=new double[]{-nodeWorldPosition.get(1)+currentWorldPosition.get(1), -nodeWorldPosition.get(0)+currentWorldPosition.get(0)};
	// Angle between current orientation and direction of the current goal
	double angle=nslArcTan2(goalVec[1],goalVec[0])-nslArcTan2(orientationVec[1],orientationVec[0]);
	// Set the goal orientation
	//double nodeOrientation=(currentOrientation.get()+angle)%(Math.PI*2.0);
	if(angle<0)
		angle=Math.PI*2+angle;
	if((angle<=6 && angle>=.28))
		actor.movingAwayFromNode.set(1);
	else
		actor.movingAwayFromNode.set(0);
}
public void makeConn(){
    nslRelabel(incentiveStrength,motivationalSchema.incentives);
    nslRelabel(currentMapPosition,incentives.currentPosition);
    nslRelabel(currentMapPosition,actor.currentPosition);
    nslRelabel(currentMapPosition,ppc.currentPosition);
    nslRelabel(currentMapPosition,wg.currentPosition);
    nslRelabel(currentNodeCenter,wg.currentNodeCenter);
    nslRelabel(currentOrientation,incentives.currentOrientation);
    nslRelabel(currentOrientation,ppc.currentOrientation);
    nslRelabel(currentOrientation,actor.currentOrientation);
    nslRelabel(currentOrientation,wg.currentOrientation);
    nslRelabel(affordances,ppc.adjacentNodeCenters);
    nslRelabel(adjacentNodeCenters,wg.adjacentNodeCenters);
    nslRelabel(incentivesPosition,incentives.incentivePosition);
    nslRelabel(reductions,motivationalSchema.reductions);
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
    nslRelabel(actor.goalPosition,goalPosition);
    nslRelabel(actor.goSignal,goSignal);
}
}//end RatBrain

