package acq_tam_wg.ACQ_TAM_WG.v1_1_1.src;
import javax.media.j3d.*;
import org.openmali.vecmath2.*;
import com.sun.j3d.utils.universe.*;
import javax.vecmath.*;
import acq_tam_wg.WGCritic.v1_1_1.src.*;
import acq_tam_wg.TAMCritic.v1_1_1.src.*;
import acq_tam_wg.MotivationalSchema.v1_1_1.src.*;
import acq_tam_wg.IncentiveStimuli.v1_1_1.src.*;
import acq_tam_wg.Actor.v1_1_1.src.*;
import acq_tam_wg.SimWorld.v1_1_1.src.*;
import acq_tam_wg.ModelPerspectiveView.v1_1_1.src.*;
import acq_tam_wg.WorldGraph.v1_1_1.src.*;
import acq_tam_wg.PPC.v1_1_2.src.*;

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

public class ACQ_TAM_WG extends NslModel{

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  ACQ_TAM_WG
//versionName: 1_1_1
//floatSubModules: true


//variables 
public  acq_tam_wg.MotivationalSchema.v1_1_1.src.MotivationalSchema motivationalSchema; // 
public  acq_tam_wg.IncentiveStimuli.v1_1_1.src.IncentiveStimuli incentives; // 
public  acq_tam_wg.Actor.v1_1_1.src.Actor actor; // 
public  acq_tam_wg.SimWorld.v1_1_1.src.SimWorld world; // 
public  acq_tam_wg.ModelPerspectiveView.v1_1_1.src.ModelPerspectiveView modelView; // 
public  acq_tam_wg.WorldGraph.v1_1_1.src.WorldGraph wg; // 
private double[] d_min; // 
private double[] d_max; // 
private int angleRepSize; // 
private double affordanceSigma; // 
private int numDrives; // 
private int maxTaxons; // 
private int mapSize; // 
private int maxNodes; // 
private double wgDesirabilitySigma; // 
private BackgroundSound sound; // 
private double headDirectionSigma; // 
private double signalThreshold; // 
private double incentiveSigma; // 
private double tamDesirabilitySigma; // 
private double actionThreshold; // 
public  acq_tam_wg.PPC.v1_1_2.src.PPC ppc; // 
private double rewardTime; // 

//methods 
public void initSys()
{
	system.setRunDelta(0.001);
	system.setTrainDelta(0.001);
	system.setRunEndTime(5.0);
	system.setTrainEndTime(5.0);
	system.setNumTrainEpochs(50);
	system.setNumRunEpochs(50);

	numDrives=3;
	angleRepSize=100;
	maxTaxons=8;
	affordanceSigma=.1;
	incentiveSigma=.25;
	wgDesirabilitySigma=.25;
	tamDesirabilitySigma=.25;
	headDirectionSigma=.25;
	mapSize=32;
	//maxNodes=10;
	maxNodes=50;
	signalThreshold=.1;
	//actionThreshold=.75;
	actionThreshold=.5;
	d_min= new  double[]{0.0, 0.0, -1.0};
	d_max= new  double[]{1.0, 1.0, 0.0};
}

public void initModule()
{
	nslSetRunDelta(0.001);
	nslSetTrainDelta(0.001);
	nslDeclareProtocol("tMaze","T-Maze");
	system.addProtocolToAll("tMaze");
	nslDeclareProtocol("eightArmMaze","8-Arm Radial Maze");
	system.addProtocolToAll("eightArmMaze");
	nslDeclareProtocol("linearMaze", "Linear Maze");
	system.addProtocolToAll("linearMaze");
	nslDeclareProtocol("largeMaze", "Large Maze");
	system.addProtocolToAll("largeMaze");
}

public void manualProtocol()
{
	initSound();
}

public void tMazeProtocol()
{
	initWorld("maps/t_map.gif");
	initModelView();
}

public void eightArmMazeProtocol()
{
	initWorld("maps/eight_arm_map.gif");
	initModelView();	
}

public void linearMazeProtocol()
{
	initWorld("maps/linear_map.gif");
	initModelView();
}

public void largeMazeProtocol()
{
	initWorld("maps/small_map.gif");
	initModelView();
}

protected void initWorld(String mapFile)
{
	world.ndc=world.nslAdd3dCanvas("world","WORLD");
	world.ndc.setGravityMagnitude(0.0f);
	world.ndc.createMap(mapFile);
	world.initView();
	world.initNodes();
	show("world");
	world.ndc.nslDisplayFrame.refresh();
}

protected void initModelView()
{
	modelView.init(world.ndc);
	ViewingPlatform vp=modelView.getVp();
	KeyCustomBehavior keyBehavior= new  KeyCustomBehavior(vp.getViewPlatformTransform(), modelView);
	keyBehavior.setSchedulingBounds(world.ndc.getCanvasBounds());
	keyBehavior.setMovementRate(0.7);
	keyBehavior.setEnable(true);
	vp.addChild( keyBehavior );
	show("modelView");
	modelView.modelPerspectiveCanvas.nslDisplayFrame.refresh();
}

protected void initSound()
{
	sound = world.ndc.addBackgroundSound( "sounds/background.wav" );
	sound.setEnable(true);
}

public void simRun()
{
	/*if(system.getCurrentTime()<system.getRunDelta())
	{
		rewardTime=-1;
		nslPrintln("reset");
	}
	if(rewardTime<0 && system.getCurrentTime()>system.getRunDelta() && (world.reductions.get(0)>0 || world.reductions.get(1)>0))
	{
		rewardTime=system.getCurrentTime();
		nslPrintln(rewardTime);
	}*/
	//if(rewardTime>0 && nslAbs(system.getCurrentTime()-(rewardTime+.25))<=system.getRunDelta())
	//{
		/*TAMCritic tc=motivationalSchema.tamCritic;
		double[] tamReinforcement=tc.getFinalReinforcement();
		actor.updateLocalDesirability(tamReinforcement);
		WGCritic wc=motivationalSchema.wgCritic;
		double[] nodeReinforcement=wc.getFinalReinforcement();
		wg.lastDiffNodeId=wg.currentNodeId.get();
		wg.updateNodeDesirability(nodeReinforcement);*/
	//	system.breakCycles();
	//}
}
public void makeConn(){
    nslConnect(ppc.affordanceDirOut,actor.executability);
    nslConnect(ppc.affordanceDistOut,actor.distances);
    nslConnect(incentives.incentiveDirection,actor.bottomUpDesirability);
    nslConnect(world.incentives,motivationalSchema.incentives);
    nslConnect(world.reductions,motivationalSchema.reductions);
    nslConnect(world.currentNodeCenter,wg.currentNodeCenter);
    nslConnect(world.incentivePosition,incentives.incentivePosition);
    nslConnect(world.adjacentNodeCenters,wg.adjacentNodeCenters);
    nslConnect(world.affordances,ppc.adjacentNodeCenters);
    nslConnect(wg.currentNodeId,motivationalSchema.currentNodeId);
    nslConnect(wg.lastNodeDist,motivationalSchema.lastNodeDist);
    nslConnect(wg.currentNodeDesirability,motivationalSchema.currentNodeDesirability);
    nslConnect(wg.desirabilityBias,actor.topDownDesirability);
    nslConnect(wg.lastNodeDesirability,motivationalSchema.lastNodeDesirability);
    nslConnect(actor.goalPosition,modelView.targetPosition);
    nslConnect(actor.goSignal,modelView.goSignal);
    nslConnect(actor.efferenceCopyOut,modelView.efferenceCopyIn);
    nslConnect(actor.currentLocalDesirability,motivationalSchema.currentLocalDesirability);
    nslConnect(motivationalSchema.motivationalState,incentives.motivationalState);
    nslConnect(motivationalSchema.motivationalState,actor.motivationalState);
    nslConnect(motivationalSchema.motivationalState,wg.motivations);
    nslConnect(motivationalSchema.nodeReinforcement,wg.reinforcement);
    nslConnect(motivationalSchema.tamReinforcement,actor.tamReinforcement);
    nslConnect(modelView.currentMapPosition,world.avatarMapPosition);
    nslConnect(modelView.efferenceCopyOut,actor.efferenceCopyIn);
    nslConnect(modelView.currentWorldPosition,world.avatarWorldPosition);
    nslConnect(modelView.currentWorldPosition,incentives.currentPosition);
    nslConnect(modelView.currentWorldPosition,ppc.currentPosition);
    nslConnect(modelView.currentWorldPosition,actor.currentPosition);
    nslConnect(modelView.currentWorldPosition,wg.currentPosition);
}

	/******************************************************/
	/*                                                    */
	/* Generated by nslc.src.NslCompiler. Do not edit these lines! */
	/*                                                    */
	/******************************************************/

	/* Constructor and related methods */
	/* makeinst() declared variables */

	/* EMPTY CONSTRUCTOR: Called only for the top level module */
	public ACQ_TAM_WG() {
		super("aCQ_TAM_WG",(NslModel)null);
		initSys();
		makeInstACQ_TAM_WG("aCQ_TAM_WG",null);
	}

	/* Formal parameters */

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public ACQ_TAM_WG(String nslName, NslModule nslParent)
{
		super(nslName, nslParent);
		initSys();
		makeInstACQ_TAM_WG(nslName, nslParent);
	}

	public void makeInstACQ_TAM_WG(String nslName, NslModule nslParent)
{ 
		Object[] nslArgs=new Object[]{};
		callFromConstructorTop(nslArgs);
		motivationalSchema = new acq_tam_wg.MotivationalSchema.v1_1_1.src.MotivationalSchema("motivationalSchema", this, numDrives, angleRepSize, mapSize, d_min, d_max);
		incentives = new acq_tam_wg.IncentiveStimuli.v1_1_1.src.IncentiveStimuli("incentives", this, angleRepSize, numDrives, incentiveSigma);
		actor = new acq_tam_wg.Actor.v1_1_1.src.Actor("actor", this, angleRepSize, numDrives, signalThreshold, actionThreshold);
		world = new acq_tam_wg.SimWorld.v1_1_1.src.SimWorld("world", this, numDrives, mapSize, maxNodes);
		modelView = new acq_tam_wg.ModelPerspectiveView.v1_1_1.src.ModelPerspectiveView("modelView", this, angleRepSize, actionThreshold);
		wg = new acq_tam_wg.WorldGraph.v1_1_1.src.WorldGraph("wg", this, angleRepSize, numDrives, d_min, d_max, maxNodes, wgDesirabilitySigma);
		ppc = new acq_tam_wg.PPC.v1_1_2.src.PPC("ppc", this, angleRepSize, maxNodes, affordanceSigma);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end ACQ_TAM_WG

