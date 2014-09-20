package acq_tam_wg.SimWorld.v1_1_1.src;
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

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  SimWorld
//versionName: 1_1_1
//floatSubModules: true


//variables 
public Nsl3dCanvas ndc; // 
private BackgroundSound sound; // 
public  NslDinDouble1 avatarMapPosition; // 
public  NslDoutDouble1 incentives; // 
public  NslDoutDouble1 reductions; // 
private double reductionDelta; // 
private double incentiveDelta; // 
public  NslDoutDouble1 currentNodeCenter; // 
private int nodeCount; // 
private double[][] nodeCenters; // 
public  NslDoutDouble2 incentivePosition; // 
public  NslDoutDouble2 adjacentNodeCenters; // 
private int currentNodeIdx; // 
public  NslDoutDouble2 affordances; // 
private int numAffordances; // 
private double fearReductionDelta; // 
public  NslDinDouble1 avatarWorldPosition; // 
private NslString0 protocol; // 

//methods 
public void initSys()
{
	fearReductionDelta=0.001;
	reductionDelta=0.1;
	incentiveDelta=0.05;
	currentNodeIdx=-1;
}

public void initNodes()
{
	nodeCount=0;
	nodeCenters= new  double[maxNodes][2];
	for(int x=0; x<mapSize; x++)
	{
		for(int y=0; y<mapSize; y++)
		{
			int coordColor=ndc.getMapCoordColor(x, y);
			if(coordColor==Nsl3dCanvas.colorFood||coordColor==Nsl3dCanvas.colorWater||coordColor==Nsl3dCanvas.colorNode)
			{
				//Vector3d nodeWorldCoord=ndc.convertToWorldCoordinate(new Point2d(x,y));
				nodeCenters[nodeCount]= new  double[]{x, y};
				nodeCount=nodeCount+1;
			}
		}
	}
}

public void initView()
{
	//set the initial position for the Viewer
	ViewingPlatform vp=ndc.getVp();
	TransformGroup steerTG = vp.getViewPlatformTransform();
	Transform3D t3d =   new   Transform3D();
	steerTG.getTransform(t3d);
	t3d.lookAt(   new   Point3d(0,100.0,0),   new   Point3d(0,0,0),   new   Vector3d(0,1,-1));
	t3d.invert();
	steerTG.setTransform(t3d);
}

protected double getScale()
{
	return 0.05;
}

public void simRun()
{
	process();
}

public void simTrain()
{
	process();
}

protected void process()
{
	setCurrentNodeCenter();
	//setAdjacentNodeCenters();
	//setAffordances();
	//setReductions();
	//setIncentives();
	adjacentNodeCenters.set(Double.NaN);
	numAffordances=0;
	affordances.set(Double.NaN);
	reductions.set(0);
	incentives.set(0);
	incentivePosition.set(Double.NaN);
			
	for(int i=0; i<nodeCount; i++)
	{
		int coordColor=ndc.getMapCoordColor(nodeCenters[i][0], nodeCenters[i][1]);
		Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[i][0], nodeCenters[i][1]));
		double[] nodeWorldCoord= new  double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z};
		double worldDist=NslOperator.distance.eval(nodeWorldCoord,avatarWorldPosition.get());
	
		setAdjacentNodeCenters(i);
		setAffordances(i, coordColor, worldDist);
		setReductions(i, coordColor, worldDist);
		setIncentives(i, coordColor, worldDist);
	}
	if(numAffordances>=3)
		reductions.set(2,fearReductionDelta);
	if(protocol.equals("linearMaze"))
	{
		if(system.getCurrentEpoch()>5&&reductions.get(1)>0)
			reductions.set(2,10*fearReductionDelta);
	}
}

public void tMazeProtocol()
{
	protocol.set("tMaze");
}

public void eightArmMazeProtocol()
{
	protocol.set("eightArmMaze");
}

public void linearMazeProtocol()
{
	protocol.set("linearMaze");
}

protected void setCurrentNodeCenter()
{
	currentNodeIdx=getNodeId(avatarMapPosition.get());
	currentNodeCenter.set(0, nodeCenters[currentNodeIdx][0]);
	currentNodeCenter.set(1, nodeCenters[currentNodeIdx][1]);
}

protected int getNodeId(double[] position)
{
	double nearestDist=Double.POSITIVE_INFINITY;
	int currentNodeIdx=-1;
	for(int i=0; i<nodeCount; i++)
	{
		double dist=NslOperator.distance.eval(nodeCenters[i],position);
		if(dist<nearestDist)
		{
			nearestDist=dist;
			currentNodeIdx=i;
		}
	}
	return currentNodeIdx;
}

protected void setAdjacentNodeCenters(int nodeIdx)
{
	//for(int i=0; i<maxNodes; i++)
	//{
		//adjacentNodeCenters.set(nodeIdx, new double[]{Double.NaN, Double.NaN});
		// If this is a node and not the node where the model is currently located and there is
		// a clear path to it
		if(nodeIdx<nodeCount&&nodeIdx!=currentNodeIdx&&!Double.isNaN(nodeCenters[nodeIdx][0])&&isReachable(nodeCenters[currentNodeIdx],nodeCenters[nodeIdx]))
		{
			// Add to list
			adjacentNodeCenters.set(nodeIdx,  new  double[]{nodeCenters[nodeIdx][0],nodeCenters[nodeIdx][1]});
		}
	//}
}

protected boolean isReachable(double[] start, double[] end)
{
	int startNodeId=getNodeId(start);
	int endNodeId=getNodeId(end);
	if(startNodeId==endNodeId)
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
		//for(int xOffset=-1; xOffset<2; xOffset++)
		//{
			// Get color of map coordinate
			int color=ndc.getMapCoordColor(x/*+xOffset*/, y);
			// If collision with wall or a node other than current one
			int nearestNodeId=getNodeId(  new   double[]{x/*+xOffset*/,y});
			if(color==Nsl3dCanvas.colorWall||(nearestNodeId!=startNodeId&&nearestNodeId!=endNodeId))
			{
				return false;
			}
		//}
	}
	return true;
}

protected void setAffordances(int nodeIdx, int coordColor, double dist)
{
	//numAffordances=0;
	//for(int i=0; i<maxNodes; i++)
	//{
		//affordances.set(nodeIdx, new double[]{Double.NaN, Double.NaN});

		//int coordColor=ndc.getMapCoordColor(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]);

		// If this is the node the model is currently in but the model is not in the node center and the node contains food or water
		boolean affordanceWithinNode=(nodeIdx==currentNodeIdx&&dist>5&&(coordColor==Nsl3dCanvas.colorFood||coordColor==Nsl3dCanvas.colorWater));
		// If this is a node and not the node where the model is currently located and there is a straight clear path to it
		boolean affordanceOutsideNode=(nodeIdx!=currentNodeIdx&&nodeIdx<nodeCount&&!Double.isNaN(nodeCenters[nodeIdx][0])&&isReachable(nodeCenters[currentNodeIdx],nodeCenters[nodeIdx]));
		if(affordanceWithinNode||affordanceOutsideNode )
		{
			Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]));
			affordances.set(nodeIdx,  new  double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z});
			numAffordances=numAffordances+1;
		}
	//}
}

protected void setReductions(int nodeIdx, int coordColor, double dist)
{
	//reductions=0;
	//for(int i=0; i<maxNodes; i++)
	//{
		//int coordColor=ndc.getMapCoordColor(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]);

		if(dist<=2)
		{ 
			if(coordColor==Nsl3dCanvas.colorWater)
				reductions.set(0,reductionDelta);
			else if(coordColor==Nsl3dCanvas.colorFood)
				reductions.set(1,reductionDelta);
		}
	//}
	//if(numAffordances>=3)
	//	reductions.set(2,fearReductionDelta);
}

protected void setIncentives(int nodeIdx, int coordColor, double dist)
{
	//incentives=0;
	//incentivePosition.set(0, 0, Double.NaN);
	//incentivePosition.set(0, 1, Double.NaN);
	//incentivePosition.set(1,0,Double.NaN);
	//incentivePosition.set(1,1,Double.NaN);
	//incentivePosition.set(2,0,Double.NaN);
	//incentivePosition.set(2,1,Double.NaN);
	
	//for(int i=0; i<maxNodes; i++)
	//{
		//int coordColor=ndc.getMapCoordColor(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]);
		if(dist<=10&&dist>2)
		{
			if(coordColor==Nsl3dCanvas.colorWater)
			{
				incentives.set(0, incentiveDelta);
				Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]));
				incentivePosition.set(0,  new  double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z});
			}
			else if(coordColor==Nsl3dCanvas.colorFood)
			{
				incentives.set(1, incentiveDelta);
				Vector3d nodeWorldCoordVec=ndc.convertToWorldCoordinate( new  Point2d(nodeCenters[nodeIdx][0], nodeCenters[nodeIdx][1]));
				incentivePosition.set(1,  new  double[]{nodeWorldCoordVec.x, nodeWorldCoordVec.z});
			}
		}
	//}
}

/*
public double[] checkCircle(double x, double y, double radius, int color)
{
	for(int i=0; i<=2*radius; i++)
	{
		if(x-radius<mapSize && x-radius>-1 && y-radius+i<mapSize && y-radius+i>-1 &&
			ndc.getMapCoordColor(x-radius, y-radius+i)==color)
			return new double[]{x-radius, y-radius+i};;
	}
	for(int i=1; i<2*radius; i++)
	{
		if(x-radius+i<mapSize && x-radius+1>-1 && y+radius<mapSize && y+radius>-1 &&
			ndc.getMapCoordColor(x-radius+i, y+radius)==color)
			return new double[]{x-radius+i, y+radius};
	}
	for(int i=0; i<=2*radius; i++)
	{
		if(x+radius<mapSize && x+radius>-1 && y+radius-i<mapSize && y+radius-i>-1 &&
			ndc.getMapCoordColor(x+radius, y+radius-i)==color)
			return new double[]{x+radius, y+radius-i};
	}
	for(int i=1; i<2*radius; i++)
	{
		if(x-radius+i<mapSize && x-radius+i>-1 && y-radius<mapSize && y-radius>-1 && 
			ndc.getMapCoordColor(x-radius+i, y-radius)==color)
			return new double[]{x-radius+i, y-radius};
	}
	return new double[]{Double.NaN,Double.NaN};
}
*/
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
		adjacentNodeCenters = new NslDoutDouble2("adjacentNodeCenters", this, maxNodes, 2);
		affordances = new NslDoutDouble2("affordances", this, maxNodes, 2);
		avatarWorldPosition = new NslDinDouble1("avatarWorldPosition", this, 2);
		protocol = new NslString0("protocol", this);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end SimWorld

