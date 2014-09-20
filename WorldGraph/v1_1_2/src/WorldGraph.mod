package acq_tam_wg.WorldGraph.v1_1_2.src;
nslImport java.awt.*;

nslJavaModule WorldGraph(int angleRepSize, int numDrives, double[] d_min, double[] d_max, int maxNodes, double sigma, double maxDesirability){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  WorldGraph
//versionName: 1_1_2


//variables 
private NslGraphCanvas canvas; // 
private double[][] nodes; // 
private double[][] edgeAngles; // 
private double[][] edgeLengths; // 
private int lastNodeId; // 
public int lastDiffNodeId; // 
private double[] lastNodeCenter; // 
private double eligibilityDecayDelta; // 
public NslDinDouble1 currentNodeCenter(2); // 
public NslDinDouble1 currentNodeDesirability(numDrives); // 
public NslDinDouble1 currentPosition(2); // 
public NslDoutInt0 currentNodeId(); // 
public NslDoutDouble0 lastNodeDist(); // 
public NslDoutDouble1 eligibility(angleRepSize); // 
public NslDinDouble2 adjacentNodeCenters(maxNodes, 2); // 
private double novelNodeDesirability; // 
public NslDoutDouble1 novelNodeBias(angleRepSize); // 
public NslDinDouble0 currentOrientation(); // 

//methods 
/**
 * Executed when NSL starts
 */
public void initSys()
{
	eligibilityDecayDelta=.95;
	novelNodeDesirability=0.5;
}

/**
 * Executed when the module is initialized
 */
public void initModule()
{
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
	canvas=nslAddGraphCanvas("desirability", "world graph", 32, -nslMaxValue(d_max), 2);
	nslSetColumns(2,"desirability");
	NslDisplaySystem outDisplay=nslFindDisplaySystem("desirability");
	outDisplay.frame.setBounds(0, 0, outDisplay.frame.getWidth()*2, outDisplay.frame.getHeight());

}

public void reset()
{
	lastNodeId=-1;
	lastDiffNodeId=-1;
	lastNodeCenter=new double[2];
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
	currentNodeId.set(getNode(currentNodeCenter.get(),system.getCurrentTime()>=2*system.getDelta()));
	canvas.unhighlightAll();

	if(currentNodeId.get()>-1)
	{
		canvas.highlightNode(""+currentNodeId.get());
		// Moved to a new node
		if(system.getCurrentTime()>=10*system.getDelta() && currentNodeId.get()!=lastNodeId && lastNodeId>-1)
		{
			addEdge();
			lastNodeDist.set(nslSqrt(nslPow(currentNodeCenter.get(0)-lastNodeCenter[0],2)+nslPow(currentNodeCenter.get(1)-lastNodeCenter[1],2)));
			lastDiffNodeId=lastNodeId;	

			computeEligibility();
		}
		else
		{
			lastNodeDist.set(0.0);
			eligibility=eligibilityDecayDelta*eligibility.get();
			updateGraph();
		}

		updateNovelNodeBias();
	
		lastNodeId=currentNodeId.get();
		lastNodeCenter[0]=currentNodeCenter.get(0);
		lastNodeCenter[1]=currentNodeCenter.get(1);
	}
}

protected void updateNovelNodeBias()
{
	// Compute desirability bias
	novelNodeBias=0;
	// For each desirability bias unit
	for(int j=0; j<angleRepSize; j++)
	{
		double prefAngle=-Math.PI+j*(2*Math.PI/(angleRepSize-1));
		for(int i=0; i<maxNodes; i++)
		{
			// If there is a node next to this one
			if(!Double.isNaN(adjacentNodeCenters.get(i,0)))
			{
				boolean novel=false;
				// Get the adjacent node index
				int adjIdx=getNode(adjacentNodeCenters.get(i),false);

				// If the index is -1 (its not in the graph) or there is no edge between this node and the adjacent one
				if(adjIdx<0 || (adjIdx>=0 && currentNodeId.get()>=0 && Double.isNaN(edgeAngles[currentNodeId.get()][adjIdx])))
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
					novelNodeBias.set(j,novelNodeBias.get(j)+novelNodeDesirability*nslExp(-nslPow(ang_dist, 2)/(2*sigma*sigma)));
				}
			}
		}
	}
}

public void updateGraph()
{
	if(currentNodeId.get()>-1)
	{
		double[] colors= new  double[numDrives];
		for(int i=0; i<numDrives; i++)
		{
			colors[i]=(double)getColor(currentNodeDesirability.get(i));
		}
		canvas.updateNode(""+currentNodeId.get(), colors);
	}
}

protected void computeEligibility()
{
	double[] baseVec=new double[]{1,0};
	double[] directionVec=new double[]{-currentNodeCenter.get(1)+lastNodeCenter[1],-currentNodeCenter.get(0)+lastNodeCenter[0]};
	double angle=(Math.PI+nslArcTan2(directionVec[1],directionVec[0])-nslArcTan2(baseVec[1],baseVec[0]))%(Math.PI*2.0);
	if(angle<0)
		angle=Math.PI*2.0+angle;
	for(int j=0; j<angleRepSize; j++)
	{
		double prefAngle=0+j*(2*Math.PI/(angleRepSize-1));
		double ang_dist=getDist(angle, prefAngle, 0, 2*Math.PI);
		eligibility.set(j,eligibility.get(j)+nslExp(-nslPow(ang_dist, 2)/(2*sigma*sigma)));
	}
}
		
protected int getNode(double[] position, boolean add)
{
	if(!Double.isNaN(position[0]) && !Double.isNaN(position[1]))
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
					colors[j]=(double)getColor(currentNodeDesirability.get(j));
				}
				canvas.addNode(""+i, new double[]{nodes[i][0], nodes[i][1]}, colors, false);
				
				return i;
			}
			else
				break;
		}
	}
	return -1;
}

protected void addEdge()
{
	// Add new edge
	if(Double.isNaN(edgeAngles[lastNodeId][currentNodeId.get()]))
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
	//float degreesOfHue = 240.0f-((float)((val-nslMin(0,nslMinValue(d_min))*.5)/(nslMax(1,nslMaxValue(d_max))*.5 - nslMin(0,nslMinValue(d_min))*.5))*240.0f);
	float degreesOfHue = 240.0f-((float)((val+nslMaxValue(d_max))/(2+nslMaxValue(d_max)))*240.0f);
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

public void saveGraph(String filename)
{
	NslTextFile file(filename);
	file.open('W');
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<2; j++)
		{
			if(!Double.isNaN(nodes[i][j]))
				file.puts(nodes[i][j]);
			else
				file.puts("NaN");
		}
	}
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<maxNodes; j++)
		{
			if(!Double.isNaN(edgeAngles[i][j]))
				file.puts(edgeAngles[i][j]);
			else
				file.puts("NaN");
		}
	}
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<maxNodes; j++)
		{
			if(!Double.isNaN(edgeLengths[i][j]))
				file.puts(edgeLengths[i][j]);
			else
				file.puts("NaN");
		}
	}
	file.flush();
	file.close();
}

public void loadGraph(String filename)
{
	NslTextFile file(filename);
	file.open('R');
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<2; j++)
		{
			String ln=file.gets();
			if(ln.equals("NaN"))
				nodes[i][j]=Double.NaN;
			else
				nodes[i][j]=Double.parseDouble(ln);
		}
		if(!Double.isNaN(nodes[i][0]) && !Double.isNaN(nodes[i][1]))
		{
			double[] colors=new double[numDrives];
			for(int j=0; j<numDrives; j++)
			{
				colors[j]=(double)getColor(0);
			}
			canvas.addNode(""+i, new double[]{nodes[i][0], nodes[i][1]}, colors, false);
		}
	}
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<maxNodes; j++)
		{
			String ln=file.gets();
			if(ln.equals("NaN"))
				edgeAngles[i][j]=Double.NaN;
			else
				edgeAngles[i][j]=Double.parseDouble(ln);
		}
	}
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<maxNodes; j++)
		{
			String ln=file.gets();
			if(ln.equals("NaN"))
				edgeLengths[i][j]=Double.NaN;
			else
			{
				edgeLengths[i][j]=Double.parseDouble(ln);
				canvas.addEdge(""+i, ""+j);
			}
		}
	}
}
public void makeConn(){
}
}//end WorldGraph

