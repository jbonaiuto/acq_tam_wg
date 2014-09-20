package acq_tam_wg.Actor.v1_1_2.src;
import acq_tam_wg.DNF.v1_1_1.src.*;
import acq_tam_wg.Pop1dDecoder.v1_1_1.src.*;
import acq_tam_wg.MotorController.v1_1_2.src.*;
import acq_tam_wg.TAMActor.v1_1_1.src.*;
import acq_tam_wg.WGActor.v1_1_1.src.*;

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

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Actor
//versionName: 1_1_2


//variables 
public  acq_tam_wg.DNF.v1_1_1.src.DNF dnf; // 
public  acq_tam_wg.Pop1dDecoder.v1_1_1.src.Pop1dDecoder directionDecoder; // 
public  acq_tam_wg.MotorController.v1_1_2.src.MotorController motorController; // 
public  acq_tam_wg.TAMActor.v1_1_1.src.TAMActor tamActor; // 
public  acq_tam_wg.WGActor.v1_1_1.src.WGActor wgActor; // 
private int currentNodeId; // 
private int lastNodeId; // 
private double eligibilityDelta; // 
private double noiseRange; // 
private double input_w; // 
private  NslDouble1 priority; // 
private  NslDouble1 tamEligibility; // 
private  NslDouble1 tamEligibilityWm; // 
public  NslDinDouble1 wgEligibility; // 
public  NslDinInt0 currentNodeIdIn; // 
public  NslDinDouble1 incentives; // 
public  NslDinDouble1 motivations; // 
public  NslDinDouble1 wgReinforcement; // 
public  NslDinDouble1 currentPosition; // 
public  NslDinDouble0 currentOrientation; // 
public  NslDinDouble1 executability; // 
public  NslDinDouble1 distances; // 
public  NslDinDouble1 tamReinforcement; // 
public  NslDoutDouble1 goalPosition; // 
public  NslDoutDouble0 goSignal; // 
private double arrivalTime; // 
public  NslDinDouble1 novelNodeBias; // 
private  NslDouble1 kernel; // 
private  NslDouble1 ior; // 
private double iorDelta; // 
private double iorMax; // 
public  NslDinInt0 movingAwayFromNode; // 
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
		kernel.set(i, NslOperator.exp.eval((double)(-(i-size)*(i-size))/(2.0*10*10)));
	}
}

/**
 * Executed at the end of module constructor
 */
public void callFromConstructorBottom()
{
	motorController.distance.setReference( new  NslDouble0());
	motorController.goSignal.setReference( new  NslDouble0());
	dnf.input.setReference( new  NslDouble1(size));
	tamActor.eligibility.setReference( new  NslDouble1(size));
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
	tamEligibility.set(0);
	tamEligibilityWm.set(0);
	lastNodeId=-1;
	currentNodeId=-1;
	ior.set(0);
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
	if((currentNodeIdIn.get()>0&&realLastNode!=currentNodeIdIn.get())||NslMaxValue.eval(dnf.output.get())<actionThreshold)
	{
		tamEligibility.set(tamEligibilityWm.get());
		tamEligibilityWm.set(0);
		if(currentNodeIdIn.get()>0&&realLastNode!=currentNodeIdIn.get())
		{
			for(int i=0; i<size; i++)
			{
				if(i<10||i>size-10)
					ior.set(i, iorMax);
			}
			arrivalTime=system.getCurrentTime();
			dnf.init();
		}
	}

	updateNodeIds();

	double[] noise= new  double[size];
	for(int i=0; i<size; i++)
		noise[i]=NslRandom.eval();

	// Set dnf input to by priority with gain by motivational state
	priority.set(NslElemMult.eval(executability.get(),__tempacq_tam_wg_Actor_v1_1_2_src_Actor4 = (NslAdd.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor4, __tempacq_tam_wg_Actor_v1_1_2_src_Actor3 = (NslSub.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor3, __tempacq_tam_wg_Actor_v1_1_2_src_Actor2 = (NslAdd.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor2, __tempacq_tam_wg_Actor_v1_1_2_src_Actor1 = (NslAdd.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor1, __tempacq_tam_wg_Actor_v1_1_2_src_Actor0 = (NslAdd.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor0, novelNodeBias.get(), tamActor.desirability.get())), wgActor.desirability.get())), incentives.get())), ior.get())), noise))));
	dnf.input.set(__tempacq_tam_wg_Actor_v1_1_2_src_Actor5.setReference(NslElemMult.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor5.get(), priority, input_w)));

	// Go signal is max value of DNF output, only if std dev of DNF output is small enough
	double go=0;
	if(computeDNFStd()<5)
		go=NslMaxValue.eval(dnf.output.get());
	
	motorController.goSignal.set(go);
	goSignal.set(go);

	// Set the distance to go
	if(NslMaxElem.eval(dnf.output.get())>-1)
		motorController.distance.set(distances.get(NslMaxElem.eval(dnf.output.get())));	

	if(goSignal.get()>=actionThreshold&&NslMaxValue.eval(tamEligibilityWm.get())<.1&&system.getCurrentTime()>=(arrivalTime+.05))
	{
		//tamEligibilityWm.set(nslConv(kernel.get(),dnf.output.get()));
		tamEligibilityWm.set(dnf.output.get());
		//tamEligibilityWm.set((1.0/nslMaxValue(tamEligibilityWm.get()))*tamEligibilityWm.get());
	}
	tamEligibility.set(__tempacq_tam_wg_Actor_v1_1_2_src_Actor6 = (NslElemMult.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor6, eligibilityDelta, tamEligibility.get())));
	tamActor.eligibility.set(tamEligibility.get());
	ior.set(__tempacq_tam_wg_Actor_v1_1_2_src_Actor7 = (NslElemMult.eval(__tempacq_tam_wg_Actor_v1_1_2_src_Actor7, iorDelta, ior.get())));
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
	return std;
}
public void makeConn(){
    nslConnect(currentPosition,motorController.currentPosition);
    nslConnect(currentOrientation,motorController.currentOrientation);
    nslConnect(currentOrientation,wgActor.currentOrientation);
    nslConnect(wgReinforcement,wgActor.reinforcement);
    nslConnect(wgEligibility,wgActor.eligibility);
    nslConnect(currentNodeIdIn,wgActor.currentNodeIdIn);
    nslConnect(currentNodeIdIn,tamActor.currentNodeIdIn);
    nslConnect(motivations,tamActor.motivations);
    nslConnect(motivations,wgActor.motivations);
    nslConnect(tamReinforcement,tamActor.reinforcement);
    nslConnect(distances,wgActor.edgeLengths);
    nslConnect(movingAwayFromNode,wgActor.movingAwayFromNode);
    nslConnect(directionDecoder.output,motorController.orientationDelta);
    nslConnect(dnf.output,directionDecoder.input);
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
	int maxNodes;
	double signalThreshold;
	double actionThreshold;
	double tamSigma;
	double wgSigma;
	double maxDesirability;
	double[] d_min;
	double[] d_max;

	/* Temporary variables */
		double[] __tempacq_tam_wg_Actor_v1_1_2_src_Actor0 = new double[1];
		double[] __tempacq_tam_wg_Actor_v1_1_2_src_Actor1 = new double[1];
		double[] __tempacq_tam_wg_Actor_v1_1_2_src_Actor2 = new double[1];
		double[] __tempacq_tam_wg_Actor_v1_1_2_src_Actor3 = new double[1];
		double[] __tempacq_tam_wg_Actor_v1_1_2_src_Actor4 = new double[1];
		NslDouble1 __tempacq_tam_wg_Actor_v1_1_2_src_Actor5 = new NslDouble1(1);
		double[] __tempacq_tam_wg_Actor_v1_1_2_src_Actor6 = new double[1];
		double[] __tempacq_tam_wg_Actor_v1_1_2_src_Actor7 = new double[1];

	/* GENERIC CONSTRUCTOR: */
	public Actor(String nslName, NslModule nslParent, int size, int numDrives, int maxNodes, double signalThreshold, double actionThreshold, double tamSigma, double wgSigma, double maxDesirability, double[] d_min, double[] d_max)
{
		super(nslName, nslParent);
		this.size=size;
		this.numDrives=numDrives;
		this.maxNodes=maxNodes;
		this.signalThreshold=signalThreshold;
		this.actionThreshold=actionThreshold;
		this.tamSigma=tamSigma;
		this.wgSigma=wgSigma;
		this.maxDesirability=maxDesirability;
		this.d_min=d_min;
		this.d_max=d_max;
		initSys();
		makeInstActor(nslName, nslParent, size, numDrives, maxNodes, signalThreshold, actionThreshold, tamSigma, wgSigma, maxDesirability, d_min, d_max);
	}

	public void makeInstActor(String nslName, NslModule nslParent, int size, int numDrives, int maxNodes, double signalThreshold, double actionThreshold, double tamSigma, double wgSigma, double maxDesirability, double[] d_min, double[] d_max)
{ 
		Object[] nslArgs=new Object[]{size, numDrives, maxNodes, signalThreshold, actionThreshold, tamSigma, wgSigma, maxDesirability, d_min, d_max};
		callFromConstructorTop(nslArgs);
		dnf = new acq_tam_wg.DNF.v1_1_1.src.DNF("dnf", this, size);
		directionDecoder = new acq_tam_wg.Pop1dDecoder.v1_1_1.src.Pop1dDecoder("directionDecoder", this, size, -Math.PI, Math.PI, signalThreshold, true);
		motorController = new acq_tam_wg.MotorController.v1_1_2.src.MotorController("motorController", this, size, actionThreshold);
		tamActor = new acq_tam_wg.TAMActor.v1_1_1.src.TAMActor("tamActor", this, size, numDrives, maxNodes, tamSigma, maxDesirability, d_min, d_max);
		wgActor = new acq_tam_wg.WGActor.v1_1_1.src.WGActor("wgActor", this, size, numDrives, maxNodes, wgSigma, maxDesirability, d_min, d_max);
		priority = new NslDouble1("priority", this, size);
		tamEligibility = new NslDouble1("tamEligibility", this, size);
		tamEligibilityWm = new NslDouble1("tamEligibilityWm", this, size);
		wgEligibility = new NslDinDouble1("wgEligibility", this, size);
		currentNodeIdIn = new NslDinInt0("currentNodeIdIn", this);
		incentives = new NslDinDouble1("incentives", this, size);
		motivations = new NslDinDouble1("motivations", this, numDrives);
		wgReinforcement = new NslDinDouble1("wgReinforcement", this, numDrives);
		currentPosition = new NslDinDouble1("currentPosition", this, 2);
		currentOrientation = new NslDinDouble0("currentOrientation", this);
		executability = new NslDinDouble1("executability", this, size);
		distances = new NslDinDouble1("distances", this, size);
		tamReinforcement = new NslDinDouble1("tamReinforcement", this, numDrives);
		goalPosition = new NslDoutDouble1("goalPosition", this, 2);
		goSignal = new NslDoutDouble0("goSignal", this);
		novelNodeBias = new NslDinDouble1("novelNodeBias", this, size);
		kernel = new NslDouble1("kernel", this, size*2);
		ior = new NslDouble1("ior", this, size);
		movingAwayFromNode = new NslDinInt0("movingAwayFromNode", this);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end Actor

