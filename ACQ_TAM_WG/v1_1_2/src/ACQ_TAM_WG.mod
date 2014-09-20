package acq_tam_wg.ACQ_TAM_WG.v1_1_2.src;
nslImport javax.media.j3d.*;
nslImport org.openmali.vecmath2.*;
nslImport com.sun.j3d.utils.universe.*;
nslImport javax.vecmath.*;
nslImport acq_tam_wg.WGCritic.v1_1_1.src.*;
nslImport acq_tam_wg.TAMCritic.v1_1_1.src.*;
nslImport acq_tam_wg.RatBrain.v1_1_1.src.*;
nslImport acq_tam_wg.SimWorld.v1_1_2.src.*;
nslImport acq_tam_wg.ModelAvatar.v1_1_1.src.*;

nslModel ACQ_TAM_WG(){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  ACQ_TAM_WG
//versionName: 1_1_2


//variables 
public acq_tam_wg.RatBrain.v1_1_1.src.RatBrain ratBrain(angleRepSize, numDrives, maxNodes, actionThreshold); // 
public acq_tam_wg.SimWorld.v1_1_2.src.SimWorld world(numDrives, mapSize, maxNodes); // 
private BackgroundSound sound; // 
public acq_tam_wg.ModelAvatar.v1_1_1.src.ModelAvatar avatar(angleRepSize, actionThreshold); // 
private int angleRepSize; // 
private int numDrives; // 
private int mapSize; // 
private int maxNodes; // 
private double wgDesirabilitySigma; // 
private double actionThreshold; // 
private boolean randInitPosition; // 
private boolean randInitDrives; // 

//methods 
public void initSys()
{
	system.setRunDelta(0.001);
	system.setTrainDelta(0.001);
	system.setRunEndTime(4);
	system.setTrainEndTime(2);
	system.setNumTrainEpochs(75);
	system.setNumRunEpochs(75);

	numDrives=3;
	angleRepSize=100;
	mapSize=32;
	maxNodes=50;
	actionThreshold=.65;

	randInitPosition=false;
	randInitDrives=false;
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
	nslDeclareProtocol("detourMaze1", "Detour Maze - 1");
	system.addProtocolToAll("detourMaze1");
	nslDeclareProtocol("detourMaze2", "Detour Maze - 2");
	system.addProtocolToAll("detourMaze2");
	nslDeclareProtocol("detourMaze3", "Detour Maze - 3");
	system.addProtocolToAll("detourMaze3");
}

public void manualProtocol()
{
	initSound();
}

public void tMazeProtocol()
{
	avatar.setStartPosition(1.0, 22.0);
	randInitDrives=true;
	initWorld("maps/t_map.gif");
	initModelView();
}

public void eightArmMazeProtocol()
{
	avatar.setStartPosition(1.0, 22.0);
	randInitDrives=true;
	initWorld("maps/eight_arm_map.gif");
	initModelView();	
}

public void linearMazeProtocol()
{
	avatar.setStartPosition(0.0, 27.0);
	initWorld("maps/linear_map.gif");
	initModelView();
}

public void largeMazeProtocol()
{
	avatar.setStartPosition(1.0, 1.0);
	initWorld("maps/small_map.gif");
	initModelView();
}

public void detourMaze1Protocol()
{
	randInitPosition=true;
	avatar.setStartPosition(1.0, 1.0);
	initWorld("maps/detour_map1.gif");
	initModelView();
}

public void detourMaze2Protocol()
{
	randInitPosition=true;
	avatar.setStartPosition(1.0, 1.0);
	initWorld("maps/detour_map2.gif");
	initModelView();
}

public void detourMaze3Protocol()
{
	randInitPosition=true;
	avatar.setStartPosition(1.0, 1.0);
	initWorld("maps/detour_map3.gif");
	initModelView();
}

protected void initWorld(String mapFile)
{
	world.ndc=world.nslAdd3dCanvas("desirability","WORLD",0f);
	world.ndc.setGravityMagnitude(0.0f);
	world.ndc.createMap(mapFile);
	world.initView();
	world.initNodes();
	show("desirability");
	world.ndc.nslDisplayFrame.refresh();
}

protected void initModelView()
{
	avatar.init(world.ndc);
	ViewingPlatform vp=avatar.getVp();
	KeyCustomBehavior keyBehavior=new KeyCustomBehavior(vp.getViewPlatformTransform(), avatar);
	keyBehavior.setSchedulingBounds(world.ndc.getCanvasBounds());
	keyBehavior.setMovementRate(0.7);
	keyBehavior.setEnable(true);
	vp.addChild( keyBehavior );
	show("modelView");
	avatar.modelPerspectiveCanvas.nslDisplayFrame.refresh();
}

protected void initSound()
{
	sound = world.ndc.addBackgroundSound( "sounds/background.wav" );
	sound.setEnable(true);
}

/**
 * Executed at the start of each run epoch
 */
public void initRun()
{
	ratBrain.motivationalSchema.hypothalamus.drives.set(0.0);
	ratBrain.motivationalSchema.hypothalamus.drives.set(1, 1);
}


/**
 * Executed before each training epoch
 */
public void initTrain()
{
	if(randInitDrives)
	{
		ratBrain.motivationalSchema.hypothalamus.drives.set(0.0);
		if(nslRandom()>=0.5)
			ratBrain.motivationalSchema.hypothalamus.drives.set(0, .75);
		else
			ratBrain.motivationalSchema.hypothalamus.drives.set(1, .75);
	}

	if(randInitPosition)
	{
		int startIdx=(int)(nslRandom()*(world.nodeCount-1));
		Vector3d worldCoord=world.ndc.convertToWorldCoordinate(new Point2d(world.nodeCenters[startIdx][0], world.nodeCenters[startIdx][1]));
		avatar.setStartPosition(worldCoord.x, worldCoord.z);
	}	
}

public void saveWeights(String filePrefix)
{
	ratBrain.motivationalSchema.wgCritic.saveWeights(filePrefix+"_wgCritic.txt");
	ratBrain.motivationalSchema.tamCritic.saveWeights(filePrefix+"_tamCritic.txt");
	ratBrain.wg.saveGraph(filePrefix+"_wg.txt");
	ratBrain.actor.wgActor.saveWeights(filePrefix+"_wgActor.txt");
	ratBrain.actor.tamActor.saveWeights(filePrefix+"_tamActor.txt");
}

public void loadWeights(String filePrefix)
{
	ratBrain.motivationalSchema.wgCritic.loadWeights(filePrefix+"_wgCritic.txt");
	ratBrain.motivationalSchema.tamCritic.loadWeights(filePrefix+"_tamCritic.txt");
	ratBrain.wg.loadGraph(filePrefix+"_wg.txt");
	ratBrain.actor.wgActor.loadWeights(filePrefix+"_wgActor.txt");
	ratBrain.actor.tamActor.loadWeights(filePrefix+"_tamActor.txt");
}
public void makeConn(){
    nslConnect(avatar.currentMapPosition,world.avatarMapPosition);
    nslConnect(avatar.currentMapPosition,ratBrain.currentMapPosition);
    nslConnect(avatar.currentWorldPosition,world.avatarWorldPosition);
    nslConnect(avatar.currentWorldPosition,ratBrain.currentWorldPosition);
    nslConnect(avatar.currentOrientation,ratBrain.currentOrientation);
    nslConnect(world.incentives,ratBrain.incentiveStrength);
    nslConnect(world.reductions,ratBrain.reductions);
    nslConnect(world.currentNodeCenter,ratBrain.currentNodeCenter);
    nslConnect(world.incentivePosition,ratBrain.incentivesPosition);
    nslConnect(world.affordances,ratBrain.affordances);
    nslConnect(world.adjacentNodeCenters,ratBrain.adjacentNodeCenters);
    nslConnect(world.currentNodeWorldPosition,ratBrain.currentNodeWorldPosition);
    nslConnect(ratBrain.goalPosition,avatar.targetPosition);
    nslConnect(ratBrain.goSignal,avatar.goSignal);
}
}//end ACQ_TAM_WG

