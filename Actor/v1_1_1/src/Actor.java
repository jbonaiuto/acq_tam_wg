package acq_tam_wg.Actor.v1_1_1.src;
import acq_tam_wg.DNF.v1_1_1.src.*;
import acq_tam_wg.Pop1dDecoder.v1_1_1.src.*;
import acq_tam_wg.MotorController.v1_1_1.src.*;

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

public class Actor extends NslJavaModule{

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Actor
//versionName: 1_1_1
//floatSubModules: true


//variables 
public  NslDinDouble1 executability; // 
public  NslDinDouble1 topDownDesirability; // 
public  NslDinDouble1 currentPosition; // 
public  NslDoutDouble1 goalPosition; // 
public  acq_tam_wg.DNF.v1_1_1.src.DNF dnf; // 
private double noiseRange; // 
private  NslDouble1 desirabilityNoise; // 
private double input_w; // 
private  NslDouble1 priority; // 
public  acq_tam_wg.Pop1dDecoder.v1_1_1.src.Pop1dDecoder directionDecoder; // 
public  NslDinDouble1 bottomUpDesirability; // 
public  NslDinDouble1 motivationalState; // 
public  NslDoutDouble0 goSignal; // 
public  NslDinDouble1 distances; // 
public  NslDoutDouble1 efferenceCopyOut; // 
public  NslDoutDouble2 currentLocalDesirability; // 
public  NslDinDouble1 tamReinforcement; // 
private  NslDouble2 localDesirability; // 
private double alpha; // 
private  NslDouble1 localDesirabilityBias; // 
public  NslDinDouble1 efferenceCopyIn; // 
private  NslDouble1 efferenceCopy; // 
private double delta; // 
public  acq_tam_wg.MotorController.v1_1_1.src.MotorController motorController; // 

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
	motorController.distance.setReference( new  NslDouble0());
	motorController.goSignal.setReference( new  NslDouble0());
	dnf.input.setReference( new  NslDouble1(size));
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

	if(NslMaxValue.eval(NslOperator.abs.eval(tamReinforcement))>0)
		updateLocalDesirability(tamReinforcement.get());

	updateLocalDesirabilityBias();
	// Set dnf input to by priority with gain by motivational state
	double[] noise= new  double[size];
	for(int i=0; i<size; i++)
	{
		noise[i]=NslRandom.eval();
	}
	priority.set(NslElemMult.eval(executability.get(),NslAdd.eval(localDesirabilityBias.get(),NslAdd.eval(NslAdd.eval(topDownDesirability.get(),bottomUpDesirability.get()), noise))));
	dnf.input.set(__tempacq_tam_wg_Actor_v1_1_1_src_Actor1.setReference(NslElemMult.eval(__tempacq_tam_wg_Actor_v1_1_1_src_Actor1.get(), __tempacq_tam_wg_Actor_v1_1_1_src_Actor0.setReference(NslElemMult.eval(__tempacq_tam_wg_Actor_v1_1_1_src_Actor0.get(), priority, input_w)), NslSum.eval(NslOperator.abs.eval(motivationalState.get())))));

	// Compute mean and std of DNF output
	double mean=0, std=0;
	int s=0;
	if(dnf.output.get(0)>.1&&dnf.output.get(size-1)>.1)
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
				if(i<size/2&&rightSide>leftSide)
					mean=mean+size+i;
				else if(i>=size/2&&leftSide>rightSide)
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
				if(i<size/2&&rightSide>leftSide)
					std=std+NslOperator.pow.eval(size+i-mean,2);
				else if(i>=size/2&&leftSide>rightSide)
					std=std+NslOperator.pow.eval(i-size-mean,2);
				else
					std=std+NslOperator.pow.eval(i-mean,2);
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
				std=std+NslOperator.pow.eval(i-mean,2);
		}
		std=std/s;
	}

	// Go signal is max value of DNF output, only if std dev of DNF output is small enough
	double go=0;
	if(std<5)
		go=NslMaxValue.eval(dnf.output.get());
	
	
	motorController.goSignal.set(go);
	goSignal.set(go);

	// Set the distance to go
	motorController.distance.set(distances.get(NslMaxElem.eval(dnf.output.get())));	
}

public void updateEfferenceCopy()
{
	if(system.getCurrentTime()<system.getDelta())
		efferenceCopy.set(0);
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
			localDesirability.set(i, j, NslMax.eval(-.5, NslMin.eval(0.5, localDesirability.get(i,j)+alpha*reinforcement[i]*efferenceCopy.get(j))));
		}
	}
}

protected void updateLocalDesirabilityBias()
{
	localDesirabilityBias.set(0);
	for(int i=0; i<numDrives; i++)
	{
		for(int j=0; j<size; j++)
		{
			localDesirabilityBias.set(j, localDesirabilityBias.get(j)+motivationalState.get(i)*localDesirability.get(i,j));
		}
	}
}
public void makeConn(){
    nslConnect(currentPosition,motorController.currentPosition);
    nslConnect(directionDecoder.output,motorController.goalOrientation);
    nslConnect(dnf.output,directionDecoder.input);
    nslConnect(dnf.output,efferenceCopyOut);
    nslConnect(motorController.goalPosition,goalPosition);
}

	/******************************************************/
	/*                                                    */
	/* Generated by nslc.src.NslCompiler. Do not edit these lines! */
	/*                                                    */
	/******************************************************/

	/* Constructor and related methods */
	/* makeinst() declared variables */

	/* Formal parameters */
	int size;
	int numDrives;
	double signalThreshold;
	double actionThreshold;

	/* Temporary variables */
		NslDouble1 __tempacq_tam_wg_Actor_v1_1_1_src_Actor0 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_Actor_v1_1_1_src_Actor1 = new NslDouble1(1);

	/* GENERIC CONSTRUCTOR: */
	public Actor(String nslName, NslModule nslParent, int size, int numDrives, double signalThreshold, double actionThreshold)
{
		super(nslName, nslParent);
		this.size=size;
		this.numDrives=numDrives;
		this.signalThreshold=signalThreshold;
		this.actionThreshold=actionThreshold;
		initSys();
		makeInstActor(nslName, nslParent, size, numDrives, signalThreshold, actionThreshold);
	}

	public void makeInstActor(String nslName, NslModule nslParent, int size, int numDrives, double signalThreshold, double actionThreshold)
{ 
		Object[] nslArgs=new Object[]{size, numDrives, signalThreshold, actionThreshold};
		callFromConstructorTop(nslArgs);
		executability = new NslDinDouble1("executability", this, size);
		topDownDesirability = new NslDinDouble1("topDownDesirability", this, size);
		currentPosition = new NslDinDouble1("currentPosition", this, 2);
		goalPosition = new NslDoutDouble1("goalPosition", this, 2);
		dnf = new acq_tam_wg.DNF.v1_1_1.src.DNF("dnf", this, size);
		desirabilityNoise = new NslDouble1("desirabilityNoise", this, size);
		priority = new NslDouble1("priority", this, size);
		directionDecoder = new acq_tam_wg.Pop1dDecoder.v1_1_1.src.Pop1dDecoder("directionDecoder", this, size, 0, 2*Math.PI, signalThreshold, true);
		bottomUpDesirability = new NslDinDouble1("bottomUpDesirability", this, size);
		motivationalState = new NslDinDouble1("motivationalState", this, numDrives);
		goSignal = new NslDoutDouble0("goSignal", this);
		distances = new NslDinDouble1("distances", this, size);
		efferenceCopyOut = new NslDoutDouble1("efferenceCopyOut", this, size);
		currentLocalDesirability = new NslDoutDouble2("currentLocalDesirability", this, numDrives, size);
		tamReinforcement = new NslDinDouble1("tamReinforcement", this, numDrives);
		localDesirability = new NslDouble2("localDesirability", this, numDrives, size);
		localDesirabilityBias = new NslDouble1("localDesirabilityBias", this, size);
		efferenceCopyIn = new NslDinDouble1("efferenceCopyIn", this, size);
		efferenceCopy = new NslDouble1("efferenceCopy", this, size);
		motorController = new acq_tam_wg.MotorController.v1_1_1.src.MotorController("motorController", this, size, actionThreshold);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end Actor

