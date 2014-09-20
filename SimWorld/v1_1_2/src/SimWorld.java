package acq_tam_wg.SimWorld.v1_1_2.src;
import javax.media.j3d.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.vecmath.*;
import java.net.*;
import java.io.*;

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

public class SimWorld extends NslJavaModule{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  SimWorld
//versionName: 1_1_2


//variables 
public Nsl3dCanvas ndc; // 
private BackgroundSound sound; // 
public  NslDinDouble1 avatarMapPosition; // 
public  NslDoutDouble1 incentives; // 
public  NslDoutDouble1 reductions; // 
private double appetitiveDriveReductionDelta; // 
private double appetitiveIncentiveDelta; // 
public  NslDoutDouble1 currentNodeCenter; // 
public int nodeCount; // 
public double[][] nodeCenters; // 
public  NslDoutDouble2 incentivePosition; // 
private int currentNodeIdx; // 
public  NslDoutDouble2 affordances; // 
private int numAffordances; // 
public  NslDinDouble1 avatarWorldPosition; // 
private NslString0 protocol; // 
public  NslDoutDouble2 adjacentNodeCenters; // 
private double aversiveDriveReductionDelta; // 
public NslBoolean0 shock; // 
public  NslDoutDouble1 currentNodeWorldPosition; // 

//methods 
public void initSys()
{
	// Magnitude of drive reduction for aversive drives
	aversiveDriveReductionDelta=-5.0;
	// Magnitude of drive reduction for appetitive drives
	appetitiveDriveReductionDelta=0.2;
	// Magnitude of incentives
	appetitiveIncentiveDelta=0.1;
}

/**
 * Executed when the module is initialized
 */
public void initModule()
{
	shock.set(false);
	shock.nslSetAccess('W');
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
	// Currently not in any node
	currentNodeIdx=-1;
}

/**
 * Initialize nodes from the current map
*/
public void initNodes()
{
	// Initialize number of nodes and node centers
	nodeCount=0;
	nodeCenters= new  double[maxNodes][2];

	// Iterate through map coordinates
	for(int x=0; x<mapSize; x++)
	{
		for(int y=0; y<mapSize; y++)
		{
			// Get coordinate color
			int coordColor=ndc.getMapCoordColor(x, y);

			// If coordinate corresponds to food, water, or a marked node, create a node for it
			if(coordColor==Nsl3dCanvas.colorFood||coordColor==Nsl3dCanvas.colorWater||coordColor==Nsl3dCanvas.colorNode)
			{
				nodeCenters[nodeCount]= new  double[]{x, y};
				nodeCount=nodeCount+1;
			}
		}
	}
}

/**
 * Set the position and view for the third-person view of the maze
*/
public void initView()
{
	// Get viewing platform from Nsl3dCanvas
	ViewingPlatform vp=ndc.getVp();
	// Modify location and orientation of viewing platform to be looking down on maze from above
	TransformGroup steerTG = vp.getViewPlatformTransform();
	Transform3D t3d =   new   Transform3D();
	steerTG.getTransform(t3d);
	t3d.lookAt(   new   Point3d(0,100.0,0),   new   Point3d(0,0,0),   new   Vector3d(0,1,-1));
	t3d.invert();
	steerTG.setTransform(t3d);
}

public void simRun()
{
	process();
}

public void simTrain()
{
	process();
}

/**
 * Process world for a simulation time step
*/
protected void process()
{
	// Compute the center of the node the avatar is currently in
	setCurrentNodeCenter();

	// Initialize adjacent nodes
	adjacentNodeCenters.set(Double.NaN);
	// Initialize number of affordances in current node
	numAffordances=0;
	// Initialize affordances
	affordances.set(Double.NaN);
	// Initialize number of drive reductions in current node
	reductions.set(0);
	// Initialize number of incentives in current node
	incentives.set(0);
	// Initialize incentive positions
	incentivePosition.set(Double.NaN);
	
	// Go through each node in the current map
	for(int i=0; i<nodeCount; i++)
	{
		// Get the color of the coordinate of the this node
		int coordColor=ndc.getMapCoordColor(nodeCenters[i][0], nodeCenters[i][1]);

		// Convert node centers from map coordinates to world coordinates
		Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[i][0], nodeCenters[i][1]));
		double[] nodeWorldCoord= new  double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z};

		// Compute distance from avatar position to node position in world coordinates
		double worldDist=NslOperator.distance.eval(nodeWorldCoord,avatarWorldPosition.get());

		// Check if node is adjacent to current node
		setAdjacentNodeCenter(i);
		// Check for affordances
		addAffordances(i, coordColor, worldDist);
		// Check for drive reductions
		addDriveReductions(i, coordColor, worldDist);
		// Check for incentives
		addIncentives(i, coordColor, worldDist);
	}
}

/**
 * Compute the center of the node the avatar is currently in
*/
protected void setCurrentNodeCenter()
{
	currentNodeIdx=getNodeIndex(avatarWorldPosition.get());
	if(currentNodeIdx>-1)
	{
		currentNodeCenter.set(0, nodeCenters[currentNodeIdx][0]);
		currentNodeCenter.set(1, nodeCenters[currentNodeIdx][1]);
		Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[currentNodeIdx][0], nodeCenters[currentNodeIdx][1]));
		currentNodeWorldPosition.set(0, nodeWorldCoordVec.x);
		currentNodeWorldPosition.set(1, nodeWorldCoordVec.z);
	}
	else
	{
		currentNodeCenter.set(0, Double.NaN);
		currentNodeCenter.set(1, Double.NaN);
		currentNodeWorldPosition.set(0, Double.NaN);
		currentNodeWorldPosition.set(1, Double.NaN);
	}
}

/**
 * Get the index of the node closest to the given position
*/
protected int getNodeIndex(double[] position)
{
	// Initialize nearest node distance and index
	double nearestDist=Double.POSITIVE_INFINITY;
	int idx=-1;

	// Loop through each node
	for(int i=0; i<nodeCount; i++)
	{
		// Compute distance to given position
		Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[i][0], nodeCenters[i][1]));
		double[] nodeWorldCoord= new  double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z};
		double dist=NslOperator.distance.eval(nodeWorldCoord,position);

		// Update nearest distance and node index
		if(dist<nearestDist)
		{
			nearestDist=dist;
			idx=i;
		}
	}
	if(nearestDist<=3)
		return idx;
	else
		return -1;
}

/**
 * Check if the node at the given index is adjacent to the current node
*/
protected void setAdjacentNodeCenter(int nodeIdx)
{
	// If this is a node and not the node where the model is currently located and there is
	// a clear path to it
	if(/*currentNodeIdx>-1 && */nodeIdx<nodeCount&&nodeIdx!=currentNodeIdx&&!Double.isNaN(nodeCenters[nodeIdx][0])&&isReachable(avatarMapPosition.get(),nodeCenters[nodeIdx])
		/*isReachable(nodeCenters[currentNodeIdx], nodeCenters[nodeIdx])*/)
	{
		// Add to list
		adjacentNodeCenters.set(nodeIdx,  new  double[]{nodeCenters[nodeIdx][0],nodeCenters[nodeIdx][1]});
	}
}

/**
 * Check if there is a clear path from the start position to the end position
*/
protected boolean isReachable(double[] start, double[] end)
{
	// Return true if both positions are in the same node
	Vector3d startWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(start[0], start[1]));
	double[] startWorldCoord= new  double[]{startWorldCoordVec.x, startWorldCoordVec.z};
	Vector3d endWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(end[0], end[1]));
	double[] endWorldCoord= new  double[]{endWorldCoordVec.x, endWorldCoordVec.z};
	int startNodeId=getNodeIndex(startWorldCoord);
	int endNodeId=getNodeIndex(endWorldCoord);
	if(startNodeId>-1&&endNodeId>-1&&startNodeId==endNodeId)
		return true;

	// Check for a path between current node position and adjacent node position
	double slope=Double.POSITIVE_INFINITY;
	if((int)(end[0]-start[0])!=0)
		slope=(end[1]-start[1])/(end[0]-start[0]);
	double yDelta=0, xDelta=0;

	// Slope is horizontal or vertical
	if(NslOperator.abs.eval(slope)<0.001||Double.isInfinite(slope))
	{
		// Slope is horizontal
		xDelta=(end[0]-start[0])/10;

		// Slope is vertical
		yDelta=(end[1]-start[1])/10;
	}
	//Slope is diagonal
	else
	{
		xDelta=(end[0]-start[0])/10;
		yDelta=xDelta*slope;
	}
	// Length of a single step
	double stepLength=NslOperator.sqrt.eval(NslOperator.pow.eval(xDelta,2)+NslOperator.pow.eval(yDelta,2));
	// Number of steps to take
	int steps=(int)(NslOperator.distance.eval(end,start)/stepLength)-1;

	// Check for collisions step-by-step between position and node center
	for(int i=0; i<steps; i++)
	{
		// Calculate position
		double x=start[0]+(i+1)*xDelta;
		double y=start[1]+(i+1)*yDelta;
		// Get color of map coordinate
		int color=ndc.getMapCoordColor(x/*+xOffset*/, y);
		// If collision with wall or a node other than current one
		// Convert node centers from map coordinates to world coordinates
		Vector3d worldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(x, y));
		double[] worldCoord= new  double[]{worldCoordVec.x, worldCoordVec.z};
		int nearestNodeId=getNodeIndex(worldCoord);
		if(color==Nsl3dCanvas.colorWall||(nearestNodeId>-1&&nearestNodeId!=startNodeId&&nearestNodeId!=endNodeId))
			return false;
	}
	return true;
}

/**
 * Checks for an affordance in the given node
*/
protected void addAffordances(int nodeIdx, int coordColor, double dist)
{
	// If this is the node the model is currently in but the model is not in the node center and the node contains food or water
	boolean affordanceWithinNode=(nodeIdx==currentNodeIdx&&dist>3&&(coordColor==Nsl3dCanvas.colorFood||coordColor==Nsl3dCanvas.colorWater));
	// If this is a node and not the node where the model is currently located and there is a straight clear path to it
	boolean affordanceOutsideNode=(nodeIdx!=currentNodeIdx&&nodeIdx<nodeCount&&!Double.isNaN(nodeCenters[nodeIdx][0])&&isReachable(avatarMapPosition.get(),nodeCenters[nodeIdx])
							/*isReachable(nodeCenters[currentNodeIdx],nodeCenters[nodeIdx])*/);
	//boolean affordanceOutsideNode=(nodeIdx!=currentNodeIdx && nodeIdx<nodeCount && !Double.isNaN(nodeCenters[nodeIdx][0]) && 
	//						isReachable(avatarMapPosition.get(),nodeCenters[nodeIdx]));
	if(affordanceWithinNode||affordanceOutsideNode )
	{
		// Add center of node in world coordinates as an affordance
		//Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate(new Point2d(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]));
		//affordances.set(nodeIdx, new double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z});
		affordances.set(nodeIdx, nodeCenters[nodeIdx]);
		// Update number of affordances
		numAffordances=numAffordances+1;
	}
}

/**
 * Checks for a drive reduction in the given node
*/
protected void addDriveReductions(int nodeIdx, int coordColor, double dist)
{
	// If the avatar is close enough to the center of the node
	if(dist<=3)
	//if(dist<=2)
	{ 
		// Set drive reductions if node contains food or water
		if(coordColor==Nsl3dCanvas.colorWater)
			reductions.set(0,appetitiveDriveReductionDelta);
		else if(coordColor==Nsl3dCanvas.colorFood)
		{
			reductions.set(1,appetitiveDriveReductionDelta);			
			// Shock in linear maze
			if(protocol.get().equals("linearMaze")&&numDrives>2&&shock.get())
			{
				reductions.set(2,aversiveDriveReductionDelta);
			}
		}
	}
}

/**
 * Checks for incentives in the given node
*/
protected void addIncentives(int nodeIdx, int coordColor, double dist)
{
	// If the avatar is close enough to the center of the node, but not too close
	if(dist<=10)
	{
		// Set incentive if node contains food or water
		if(coordColor==Nsl3dCanvas.colorWater)
		{
			incentives.set(0, appetitiveIncentiveDelta);
			Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]));
			//incentivePosition.set(0, new double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z});
			incentivePosition.set(0, nodeCenters[nodeIdx]);
		}
		else if(coordColor==Nsl3dCanvas.colorFood)
		{
			incentives.set(1, appetitiveIncentiveDelta);
			Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]));
			incentivePosition.set(1, nodeCenters[nodeIdx]); 
			//incentivePosition.set(1, new double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z});
		}
	}	
}

/**
 * Set protocol to t-maze
*/
public void tMazeProtocol()
{
	protocol.set("tMaze");
}

/**
 * Set protocol to 8 arm maze
*/
public void eightArmMazeProtocol()
{
	protocol.set("eightArmMaze");
}

/**
 * Set protocol to linear maze
*/
public void linearMazeProtocol()
{
	protocol.set("linearMaze");
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
	int numDrives;
	int mapSize;
	int maxNodes;

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public SimWorld(String nslName, NslModule nslParent, int numDrives, int mapSize, int maxNodes)
{
		super(nslName, nslParent);
		this.numDrives=numDrives;
		this.mapSize=mapSize;
		this.maxNodes=maxNodes;
		initSys();
		makeInstSimWorld(nslName, nslParent, numDrives, mapSize, maxNodes);
	}

	public void makeInstSimWorld(String nslName, NslModule nslParent, int numDrives, int mapSize, int maxNodes)
{ 
		Object[] nslArgs=new Object[]{numDrives, mapSize, maxNodes};
		callFromConstructorTop(nslArgs);
		avatarMapPosition = new NslDinDouble1("avatarMapPosition", this, 2);
		incentives = new NslDoutDouble1("incentives", this, numDrives);
		reductions = new NslDoutDouble1("reductions", this, numDrives);
		currentNodeCenter = new NslDoutDouble1("currentNodeCenter", this, 2);
		incentivePosition = new NslDoutDouble2("incentivePosition", this, numDrives, 2);
		affordances = new NslDoutDouble2("affordances", this, maxNodes, 2);
		avatarWorldPosition = new NslDinDouble1("avatarWorldPosition", this, 2);
		protocol = new NslString0("protocol", this);
		adjacentNodeCenters = new NslDoutDouble2("adjacentNodeCenters", this, maxNodes, 2);
		shock = new NslBoolean0("shock", this);
		currentNodeWorldPosition = new NslDoutDouble1("currentNodeWorldPosition", this, 2);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end SimWorld

