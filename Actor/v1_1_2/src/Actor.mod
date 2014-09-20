package acq_tam_wg.Actor.v1_1_2.src;
nslImport acq_tam_wg.DNF.v1_1_1.src.*;
nslImport acq_tam_wg.Pop1dDecoder.v1_1_1.src.*;
nslImport acq_tam_wg.MotorController.v1_1_2.src.*;
nslImport acq_tam_wg.TAMActor.v1_1_1.src.*;
nslImport acq_tam_wg.WGActor.v1_1_1.src.*;

nslJavaModule Actor(int size, int numDrives, int maxNodes, double signalThreshold, double actionThreshold, double tamSigma, double wgSigma, double maxDesirability, double[] d_min, double[] d_max){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Actor
//versionName: 1_1_2


//variables 
public acq_tam_wg.DNF.v1_1_1.src.DNF dnf(size); // 
public acq_tam_wg.Pop1dDecoder.v1_1_1.src.Pop1dDecoder directionDecoder(size, -Math.PI, Math.PI, signalThreshold, true); // 
public acq_tam_wg.MotorController.v1_1_2.src.MotorController motorController(size, actionThreshold); // 
public acq_tam_wg.TAMActor.v1_1_1.src.TAMActor tamActor(size, numDrives, maxNodes, tamSigma, maxDesirability, d_min, d_max); // 
public acq_tam_wg.WGActor.v1_1_1.src.WGActor wgActor(size, numDrives, maxNodes, wgSigma, maxDesirability, d_min, d_max); // 
private int currentNodeId; // 
private int lastNodeId; // 
private double eligibilityDelta; // 
private double noiseRange; // 
private double input_w; // 
private NslDouble1 priority(size); // 
private NslDouble1 tamEligibility(size); // 
private NslDouble1 tamEligibilityWm(size); // 
public NslDinDouble1 wgEligibility(size); // 
public NslDinInt0 currentNodeIdIn(); // 
public NslDinDouble1 incentives(size); // 
public NslDinDouble1 motivations(numDrives); // 
public NslDinDouble1 wgReinforcement(numDrives); // 
public NslDinDouble1 currentPosition(2); // 
public NslDinDouble0 currentOrientation(); // 
public NslDinDouble1 executability(size); // 
public NslDinDouble1 distances(size); // 
public NslDinDouble1 tamReinforcement(numDrives); // 
public NslDoutDouble1 goalPosition(2); // 
public NslDoutDouble0 goSignal(); // 
private double arrivalTime; // 
public NslDinDouble1 novelNodeBias(size); // 
private NslDouble1 kernel(size*2); // 
private NslDouble1 ior(size); // 
private double iorDelta; // 
private double iorMax; // 
public NslDinInt0 movingAwayFromNode(); // 
private int realLastNode; // 

//methods 
/**
 * Executed when NSL starts
 */
public void initSys()
{
	noiseRange=.075;
	input_w=20;
	eligibilityDelta=0.95;
	iorDelta=0.95;
	iorMax=.1;
}

/**
 * Executed when the module is initialized
 */
public void initModule()
{
	nslAddSpatialCanvas("output", "priority", priority, 0.0, 1.5);
	nslAddSpatialCanvas("output", "actor", dnf.output, 0.0, 1.0);
	nslAddSpatialCanvas("output", "ior", ior, 0, iorMax);

	for(int i=0; i<size*2; i++)
	{
		kernel[i]=nslExp((double)(-(i-size)*(i-size))/(2.0*10*10));
	}
}

/**
 * Executed at the end of module constructor
 */
public void callFromConstructorBottom()
{
	motorController.distance.setReference(new NslDouble0());
	motorController.goSignal.setReference(new NslDouble0());
	dnf.input.setReference(new NslDouble1(size));
	tamActor.eligibility.setReference(new NslDouble1(size));
}

public void initTrain()
{
	init();
}

public void initRun()
{
	init();
}

protected void init()
{
	tamEligibility=0;
	tamEligibilityWm=0;
	lastNodeId=-1;
	currentNodeId=-1;
	ior=0;
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
	// Handle TAM eligibility signal
	if((currentNodeIdIn.get()>0 && /*currentNodeId>0 && currentNodeId*/realLastNode!=currentNodeIdIn.get()) || nslMaxValue(dnf.output.get())<actionThreshold)
	{
		tamEligibility.set(tamEligibilityWm.get());
		tamEligibilityWm=0;
		if(currentNodeIdIn.get()>0 && /*currentNodeId>0 && currentNodeId*/realLastNode!=currentNodeIdIn.get())
		{
			for(int i=0; i<size; i++)
			{
				if(i<10 || i>size-10)
					ior.set(i, iorMax);
			}
			arrivalTime=system.getCurrentTime();
			dnf.init();
		}
	}

	updateNodeIds();

	double[] noise=new double[size];
	for(int i=0; i<size; i++)
		noise[i]=nslRandom();

	// Set dnf input to by priority with gain by motivational state
	priority.set(nslElemMult(executability.get(),novelNodeBias.get()+tamActor.desirability.get()+wgActor.desirability.get()+incentives.get()-ior.get()+noise));
	dnf.input.set(priority*input_w);

	// Go signal is max value of DNF output, only if std dev of DNF output is small enough
	double go=0;
	if(computeDNFStd()<5)
		go=nslMaxValue(dnf.output.get());
	
	motorController.goSignal.set(go);
	goSignal.set(go);

	// Set the distance to go
	if(nslMaxElem(dnf.output.get())>-1)
		motorController.distance.set(distances.get(nslMaxElem(dnf.output.get())));	

	if(goSignal.get()>=actionThreshold && nslMaxValue(tamEligibilityWm.get())<.1 && system.getCurrentTime()>=(arrivalTime+.05))
	{
		//tamEligibilityWm.set(nslConv(kernel.get(),dnf.output.get()));
		tamEligibilityWm.set(dnf.output.get());
		//tamEligibilityWm.set((1.0/nslMaxValue(tamEligibilityWm.get()))*tamEligibilityWm.get());
	}
	tamEligibility.set(eligibilityDelta*tamEligibility.get());
	tamActor.eligibility.set(tamEligibility.get());
	ior.set(iorDelta*ior.get());
}

protected void updateNodeIds()
{
	if(currentNodeIdIn.get()>-1)
	{
		if(currentNodeIdIn.get()!=currentNodeId)
			lastNodeId=currentNodeId;
		currentNodeId=currentNodeIdIn.get();
	}
	realLastNode=currentNodeIdIn.get();
}

protected double computeDNFStd()
{
	double mean=0, std=0;
	int s=0;
	if(dnf.output.get(0)>.1 && dnf.output.get(size-1)>.1)
	{
		double leftSide=0, rightSide=0;
		for(int i=0; i<size; i++)
		{
			if(i<size/2)
				leftSide=leftSide+dnf.output.get(i);
			else
				rightSide=rightSide+dnf.output.get(i);
		}
		for(int i=0; i<size; i++)
		{
			if(dnf.output.get(i)>.1)
			{
				if(i<size/2 && rightSide>leftSide)
					mean=mean+size+i;
				else if(i>=size/2 && leftSide>rightSide)
					mean=mean+(i-size);
				else
					mean=mean+i;
				s++;
			}
		}
		mean=mean/s;
		for(int i=0; i<size; i++)
		{
			if(dnf.output.get(i)>.1)
			{
				if(i<size/2 && rightSide>leftSide)
					std=std+nslPow(size+i-mean,2);
				else if(i>=size/2 && leftSide>rightSide)
					std=std+nslPow(i-size-mean,2);
				else
					std=std+nslPow(i-mean,2);
			}
		}
		std=std/s;
	}
	else
	{
		for(int i=0; i<size; i++)
		{
			if(dnf.output.get(i)>.1)
			{
				mean=mean+i;
				s++;
			}
		}
		mean=mean/s;
		for(int i=0; i<size; i++)
		{
			if(dnf.output.get(i)>.1)
				std=std+nslPow(i-mean,2);
		}
		std=std/s;
	}
	return std;
}
public void makeConn(){
    nslRelabel(currentPosition,motorController.currentPosition);
    nslRelabel(currentOrientation,motorController.currentOrientation);
    nslRelabel(currentOrientation,wgActor.currentOrientation);
    nslRelabel(wgReinforcement,wgActor.reinforcement);
    nslRelabel(wgEligibility,wgActor.eligibility);
    nslRelabel(currentNodeIdIn,wgActor.currentNodeIdIn);
    nslRelabel(currentNodeIdIn,tamActor.currentNodeIdIn);
    nslRelabel(motivations,tamActor.motivations);
    nslRelabel(motivations,wgActor.motivations);
    nslRelabel(tamReinforcement,tamActor.reinforcement);
    nslRelabel(distances,wgActor.edgeLengths);
    nslRelabel(movingAwayFromNode,wgActor.movingAwayFromNode);
    nslConnect(directionDecoder.output,motorController.orientationDelta);
    nslConnect(dnf.output,directionDecoder.input);
    nslRelabel(motorController.goalPosition,goalPosition);
}
}//end Actor

