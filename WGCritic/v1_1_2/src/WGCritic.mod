package acq_tam_wg.WGCritic.v1_1_2.src;

nslJavaModule WGCritic(int maxNodes, int numDrives, double[] d_min, double[] d_max, double maxDesirability){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  WGCritic
//versionName: 1_1_2


//variables 
private NslDouble2 nodeDesirability(maxNodes, numDrives); // 
private int currentNodeId; // 
private int lastNodeId; // 
private double alpha; // 
private double appetitiveDriveGamma; // 
public NslDinDouble1 rewards(numDrives); // 
public NslDinDouble1 motivations(numDrives); // 
public NslDinInt0 currentNodeIdIn(); // 
public NslDinDouble0 lastNodeDist(); // 
public NslDoutDouble1 reinforcement(numDrives); // 
private double aversiveDriveGamma; // 

//methods 
/**
 * Executed when the module is initialized
 */
public void initModule()
{
	// learning rate
	alpha=0.3;
	// discount rate
	appetitiveDriveGamma=0.9;
	aversiveDriveGamma=0.01;
}

/**
 * Reset internal variables
*/
public void reset()
{
	// Reset node IDs
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

	// Update desirability
	updateNodeValues();

	// Update reinforcement
	updateReinforcement();
}

/**
 * Executed during each run cycle
 */
public void simRun()
{
	// Update node IDs
	updateNodeIds();

	// Update reinforcement
	updateReinforcement();
}

/**
 * Update currrent and last node ID
 */
protected void updateNodeIds()
{
	if(currentNodeIdIn.get()>-1)
	{
		if(currentNodeIdIn.get()!=currentNodeId && currentNodeId>-1)
			lastNodeId=currentNodeId;
		currentNodeId=currentNodeIdIn.get();
	}
}

/**
 * Update reinforcement output signal
 */
protected void updateReinforcement()
{
	if(currentNodeIdIn.get()>-1)
		reinforcement.set(nodeDesirability.get(currentNodeId));
}

/**
 * Update value of last node
 */
protected void updateNodeValues()
{
	// For each drive
	for(int i=0; i<numDrives; i++)
	{
		// Initialize TD error
		double tdError=0;

		// If we know what node we're in
		if(currentNodeIdIn.get()>-1)
		{
			// If we've moved to a new node, update the last node's desirability
			if(lastNodeId!=currentNodeIdIn.get() && lastNodeId>-1)
			{
				// compute TD error
				if(i<2)
					tdError=(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*(/*nslPow(*/appetitiveDriveGamma/*,lastNodeDist.get()/10.0)*/*nodeDesirability.get(currentNodeIdIn.get(),i)-nodeDesirability.get(lastNodeId,i));
				else
					tdError=(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i])*(/*nslPow(*/aversiveDriveGamma/*,lastNodeDist.get()/10.0)*/*nodeDesirability.get(currentNodeIdIn.get(),i)-nodeDesirability.get(lastNodeId,i));

				// update last node value
				nodeDesirability.set(lastNodeId, i,nslMax(-d_max[i], nslMin(d_max[i], nodeDesirability.get(lastNodeId, i)+alpha*tdError)));
			}

			// Compute TD error for current node
			tdError=rewards.get(i)*(motivations.get(i)-d_min[i])/(d_max[i]-d_min[i]);

			// Update current node with rewards
			nodeDesirability.set(currentNodeIdIn.get(), i, nslMax(-d_max[i], nslMin(d_max[i], nodeDesirability.get(currentNodeIdIn.get(), i)+alpha*tdError)));
		}
	}
}

/**
 * Save node values to files
 */
public void saveWeights(String filename)
{
	NslTextFile file(filename);
	file.open('W');
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<numDrives; j++)
		{
			file.puts(nodeDesirability.get(i, j));
		}
	}
	file.flush();
	file.close();
}

/**
 * Load node values from file
 */
public void loadWeights(String filename)
{
	NslTextFile file(filename);
	file.open('R');
	for(int i=0; i<maxNodes; i++)
	{
		for(int j=0; j<numDrives; j++)
		{
			nodeDesirability.set(i, j, Double.parseDouble(file.gets()));
		}
	}
	file.close();
}
public void makeConn(){
}
}//end WGCritic

