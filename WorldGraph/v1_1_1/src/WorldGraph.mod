package acq_tam_wg.WorldGraph.v1_1_1.src;
nslImport java.awt.*;

nslJavaModule WorldGraph(int angleRepSize, int numDrives, double[] d_min, double[] d_max, int maxNodes, double sigma){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  WorldGraph
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDinDouble1 currentNodeCenter(2); // 
private int lastNodeId; // 
private double[][] edgeAngles; // 
public NslDoutInt0 currentNodeId(); // 
private double[][] nodes; // 
private double[] lastNodeCenter; // 
private double[][] edgeLengths; // 
public NslDoutDouble0 lastNodeDist(); // 
private NslGraphCanvas canvas; // 
public NslDinDouble1 reinforcement(numDrives); // 
private NslDouble2 nodeDesirability(maxNodes, numDrives); // 
public NslDoutDouble1 currentNodeDesirability(numDrives); // 
private double alpha; // 
public NslDoutDouble1 desirabilityBias(angleRepSize); // 
public int lastDiffNodeId; // 
public NslDinDouble1 motivations(numDrives); // 
public NslDoutDouble1 lastNodeDesirability(numDrives); // 
public NslDinDouble2 adjacentNodeCenters(maxNodes, 2); // 
private double novelNodeDesirability; // 
public NslDinDouble1 currentPosition(2); // 
public NslDinDouble0 currentOrientation(); // 

//methods 
public void initModule()
{
	alpha=.1;
	novelNodeDesirability=0.5;
	nodes=new double[maxNodes][2];
	edgeAngles=new double[maxNodes][maxNodes];
	edgeLengths=new double[maxNodes][maxNodes];
	for(int i=0; i<maxNodes; i++)
	{
		nodes[i][0]=Double.NaN;
		nodes[i][1]=Double.NaN;
		for(int j=0; j<maxNodes; j++)
		{
			edgeAngles[i][j]=Double.NaN;
			edgeLengths[i][j]=Double.NaN;
		}
	}
	canvas=nslAddGraphCanvas("desirability", "world graph", 32, nslMin(0, nslMinValue(d_min))*.1, nslMax(1, nslMaxValue(d_max))*.5);
	nslAddSpatialCanvas("output", "node desirability", desirabilityBias, (double)-0.1, (double)0.5);
	nslSetColumns(1,"output");
	nslSetColumns(2,"desirability");
}

public void reset()
{
	lastNodeId=-1;
	lastDiffNodeId=-1;
	lastNodeCenter=new double[2];
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
	if(system.getCurrentTime()<system.getDelta())
		reset();
	currentNodeId.set(getNode(currentNodeCenter.get(),true));
	canvas.unhighlightAll();
	canvas.highlightNode(""+currentNodeId.get());

	for(int i=0; i<numDrives; i++)
	{
		currentNodeDesirability.set(i, nodeDesirability.get(currentNodeId.get(),i));
	}

	// Moved to a new node
	if(currentNodeId.get()!=lastNodeId && lastNodeId>-1)
	{
		addEdge();
		lastNodeDist.set(nslSqrt(nslPow(currentNodeCenter.get(0)-lastNodeCenter[0],2)+nslPow(currentNodeCenter.get(1)-lastNodeCenter[1],2)));
		lastDiffNodeId=lastNodeId;
	}
	else
		lastNodeDist.set(0.0);

	if(lastDiffNodeId>-1)
	{
		for(int i=0; i<numDrives; i++)
			lastNodeDesirability.set(i, nodeDesirability.get(lastDiffNodeId,i));
		updateNodeDesirability(reinforcement.get());
	}

	updateDesirabilityBias();
	
	lastNodeId=currentNodeId.get();
	lastNodeCenter[0]=currentNodeCenter.get(0);
	lastNodeCenter[1]=currentNodeCenter.get(1);
}

public void updateNodeDesirability(double[] reinforcement)
{
	double[] colors= new  double[numDrives];
	for(int i=0; i<numDrives; i++)
	{
		nodeDesirability.set(lastDiffNodeId,i,nslMax(-.5,nslMin(0.5, nodeDesirability.get(lastDiffNodeId,i)+alpha*reinforcement[i])));
		colors[i]=(double)getColor(nodeDesirability.get(lastDiffNodeId,i));
	}
	if(nslMaxValue(nslAbs(reinforcement))>0)
		canvas.updateNode(""+lastDiffNodeId, colors);
}

protected void updateDesirabilityBias()
{
	// Compute desirability bias
	desirabilityBias=0;
	//if(!Double.isNaN(orientationDecoder.output.get()))
	//{
		// For each desirability bias unit
		for(int j=0; j<angleRepSize; j++)
		{
			// Set desirability to desirability values of node weighted by motivational state
			double prefAngle=-Math.PI+j*(2*Math.PI/(angleRepSize-1));
			//double prefAngle=0+j*(2*Math.PI/(angleRepSize-1));
			for(int i=0; i<maxNodes; i++)
			{
				// If node is adjacent to current node
				if(!Double.isNaN(edgeAngles[currentNodeId.get()][i]))
				{
					// Get relative angle between current orientation and edge orientation
					double ang=getRelativeAngle(currentOrientation.get(), edgeAngles[currentNodeId.get()][i]);
					double ang_dist=getDist(ang, prefAngle, -Math.PI, Math.PI);
					//double ang_dist=getDist(edgeAngles[currentNodeId.get()][i], prefAngle, 0, 2*Math.PI);
					double node_dist=nslDistance(currentPosition.get(), nodes[i]);
					for(int k=0; k<numDrives; k++)
					{
						desirabilityBias.set(j,desirabilityBias.get(j)+(nslAbs(motivations.get(k))*nodeDesirability.get(i,k)*nslExp(-nslPow(ang_dist, 2)/(2*sigma*sigma)))/(node_dist+1));
					}
				}

				if(!Double.isNaN(adjacentNodeCenters.get(i,0)) && getNode(adjacentNodeCenters.get(i),false)==-1)
				{
					// Compute angle between adjacent node position and this node position
					double[] baseVec=new double[]{1,0};
					double[] directionVec=new double[]{-adjacentNodeCenters.get(i,1)+currentNodeCenter.get(1),-adjacentNodeCenters.get(i,0)+currentNodeCenter.get(0)};
					double angle=(Math.PI+nslArcTan2(directionVec[1],directionVec[0])-nslArcTan2(baseVec[1],baseVec[0]))%(Math.PI*2.0);
					if(angle<0)
						angle=Math.PI*2.0+angle;
					double rel_ang=getRelativeAngle(currentOrientation.get(), angle);
					//double ang_dist=getDist(angle, prefAngle, 0, 2*Math.PI);
					double ang_dist=getDist(rel_ang, prefAngle, -Math.PI, Math.PI);
					desirabilityBias.set(j,desirabilityBias.get(j)+novelNodeDesirability*nslExp(-nslPow(ang_dist, 2)/(2*sigma*sigma)));
				}
			}
		}
	//}
}

protected int getNode(double[] position, boolean add)
{
	for(int i=0; i<maxNodes; i++)
	{
		if(!Double.isNaN(nodes[i][0]) && !Double.isNaN(nodes[i][1]))
		{
			if(nslDistance(nodes[i],position)<.1)
			{
				// Found current node id
				return i;
			}
		}
		else if(add)
		{
			// Add new node
			nodes[i][0]=position[0];
			nodes[i][1]=position[1];
			double[] colors=new double[numDrives];
			for(int j=0; j<numDrives; j++)
			{
				colors[j]=(double)getColor(nodeDesirability.get(i,j));
			}
			canvas.addNode(""+i, new double[]{nodes[i][0], nodes[i][1]}, colors, false);
			
			return i;
		}
		else
			break;
	}
	return -1;
}

protected void addEdge()
{
	// Add new edge
	if(Double.isNaN(edgeAngles[currentNodeId.get()][lastNodeId]) || Double.isNaN(edgeAngles[lastNodeId][currentNodeId.get()]))
	{
		// Compute angle between last node position and this node position
		double[] baseVec=new double[]{1,0};
		double[] directionVec=new double[]{-currentNodeCenter.get(1)+lastNodeCenter[1],-currentNodeCenter.get(0)+lastNodeCenter[0]};
		double angle=(Math.PI+nslArcTan2(directionVec[1],directionVec[0])-nslArcTan2(baseVec[1],baseVec[0]))%(Math.PI*2.0);
		if(angle<0)
			angle=Math.PI*2.0+angle;

		// Add edge from last node to this node
		edgeAngles[lastNodeId][currentNodeId.get()]=angle;
		edgeLengths[lastNodeId][currentNodeId.get()]=lastNodeDist.get();

		canvas.addEdge(""+lastNodeId, ""+currentNodeId.get());
	}
}

protected float getColor(double val)
{
	float degreesOfHue = 240.0f-((float)((val-nslMin(0,nslMinValue(d_min))*.5)/(nslMax(1,nslMaxValue(d_max))*.5 - nslMin(0,nslMinValue(d_min))*.5))*240.0f);
	if(degreesOfHue<0.0)
		degreesOfHue=0.0f;
	else if(degreesOfHue>360.0)
		degreesOfHue=360.0f;
	return degreesOfHue/360.0f;
}

protected double getDist(double ang1, double ang2, double min, double max)
{
	double dist=(ang1-ang2)%(2*Math.PI+0.001);
	//double altDist=(ang2-ang1)%(2*Math.PI+0.001);
	double altDist=dist;
	if(ang1<ang2)
		altDist=max-ang2+ang1-min;
	else if(ang2<ang1)
		altDist=max-ang1+ang2-min;
	if(nslAbs(altDist)<nslAbs(dist))
		dist=altDist;
	return dist;
}

protected double getRelativeAngle(double ang1, double ang2)
{
	double relativeAngle=0.0;
	if(ang1>ang2)
	{
		double relAngleRight=(ang2-ang1);
		double relAngleLeft=2*Math.PI+relAngleRight;
		if(nslAbs(relAngleRight)<nslAbs(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	else
	{
		double relAngleLeft=ang2-ang1;
		double relAngleRight=relAngleLeft-2*Math.PI;
		if(nslAbs(relAngleRight)<nslAbs(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	return relativeAngle;
}
public void makeConn(){
}
}//end WorldGraph

