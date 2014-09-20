package acq_tam_wg.Actor.v1_1_1.src;
nslImport acq_tam_wg.DNF.v1_1_1.src.*;
nslImport acq_tam_wg.Pop1dDecoder.v1_1_1.src.*;
nslImport acq_tam_wg.MotorController.v1_1_1.src.*;

nslJavaModule Actor(int size, int numDrives, double signalThreshold, double actionThreshold){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Actor
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDinDouble1 executability(size); // 
public NslDinDouble1 topDownDesirability(size); // 
public NslDinDouble1 currentPosition(2); // 
public NslDoutDouble1 goalPosition(2); // 
public acq_tam_wg.DNF.v1_1_1.src.DNF dnf(size); // 
private double noiseRange; // 
private NslDouble1 desirabilityNoise(size); // 
private double input_w; // 
private NslDouble1 priority(size); // 
public acq_tam_wg.Pop1dDecoder.v1_1_1.src.Pop1dDecoder directionDecoder(size, 0, 2*Math.PI, signalThreshold, true); // 
public NslDinDouble1 bottomUpDesirability(size); // 
public NslDinDouble1 motivationalState(numDrives); // 
public NslDoutDouble0 goSignal(); // 
public NslDinDouble1 distances(size); // 
public NslDoutDouble1 efferenceCopyOut(size); // 
public NslDoutDouble2 currentLocalDesirability(numDrives, size); // 
public NslDinDouble1 tamReinforcement(numDrives); // 
private NslDouble2 localDesirability(numDrives, size); // 
private double alpha; // 
private NslDouble1 localDesirabilityBias(size); // 
public NslDinDouble1 efferenceCopyIn(size); // 
private NslDouble1 efferenceCopy(size); // 
private double delta; // 
public acq_tam_wg.MotorController.v1_1_1.src.MotorController motorController(size, actionThreshold); // 

//methods 
public void initModule()
{
	alpha=.1;
	noiseRange=.05;
	input_w=20;
	delta=0.95;
	nslAddSpatialCanvas("output", "local desirability", localDesirabilityBias, -0.5, 0.5);
	nslAddSpatialCanvas("output", "priority", priority, -1.5, 1.5);
	nslAddSpatialCanvas("output", "actor", dnf.output, 0.0, 1.0);
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
	motorController.distance.setReference(new NslDouble0());
	motorController.goSignal.setReference(new NslDouble0());
	dnf.input.setReference(new NslDouble1(size));
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
	updateEfferenceCopy();

	if(nslMaxValue(nslAbs(tamReinforcement))>0)
		updateLocalDesirability(tamReinforcement.get());

	updateLocalDesirabilityBias();
	// Set dnf input to by priority with gain by motivational state
	double[] noise=new double[size];
	for(int i=0; i<size; i++)
	{
		noise[i]=nslRandom();
	}
	priority.set(nslElemMult(executability.get(),nslAdd(localDesirabilityBias.get(),nslAdd(nslAdd(topDownDesirability.get(),bottomUpDesirability.get()), noise))));
	dnf.input.set(priority*input_w*nslSum(nslAbs(motivationalState.get())));

	// Compute mean and std of DNF output
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

	// Go signal is max value of DNF output, only if std dev of DNF output is small enough
	double go=0;
	if(std<5)
		go=nslMaxValue(dnf.output.get());
	
	
	motorController.goSignal.set(go);
	goSignal.set(go);

	// Set the distance to go
	motorController.distance.set(distances.get(nslMaxElem(dnf.output.get())));	
}

public void updateEfferenceCopy()
{
	if(system.getCurrentTime()<system.getDelta())
		efferenceCopy=0;
	for(int j=0; j<size; j++)
	{
		if(efferenceCopyIn.get(j)>0)
			efferenceCopy.set(j, efferenceCopyIn.get(j));
		else
			efferenceCopy.set(j, delta*efferenceCopy.get(j));
	}
}

public void updateLocalDesirability(double[] reinforcement)
{
	for(int i=0; i<numDrives; i++)
	{
		for(int j=0; j<size; j++)
		{
			localDesirability.set(i, j, nslMax(-.5, nslMin(0.5, localDesirability.get(i, j)+alpha*reinforcement[i]*efferenceCopy.get(j))));
		}
	}
}

protected void updateLocalDesirabilityBias()
{
	localDesirabilityBias=0;
	for(int i=0; i<numDrives; i++)
	{
		for(int j=0; j<size; j++)
		{
			localDesirabilityBias.set(j, localDesirabilityBias.get(j)+motivationalState.get(i)*localDesirability.get(i,j));
		}
	}
}
public void makeConn(){
    nslRelabel(currentPosition,motorController.currentPosition);
    nslConnect(directionDecoder.output,motorController.goalOrientation);
    nslConnect(dnf.output,directionDecoder.input);
    nslRelabel(dnf.output,efferenceCopyOut);
    nslRelabel(motorController.goalPosition,goalPosition);
}
}//end Actor

