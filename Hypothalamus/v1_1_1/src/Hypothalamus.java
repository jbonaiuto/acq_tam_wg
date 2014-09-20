package acq_tam_wg.Hypothalamus.v1_1_1.src;

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

public class Hypothalamus extends NslJavaModule{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Hypothalamus
//versionName: 1_1_1


//variables 
private double appetitiveAlpha; // 
private NslDouble0 thirst; // 
private NslDouble0 hunger; // 
private NslDouble0 fear; // 
private  NslDouble1 incentives_wm; // 
public  NslDinDouble1 incentives; // 
public  NslDinDouble1 reductions; // 
public  NslDoutDouble1 drives; // 
private double aversiveAlpha; // 
private double[] tonicInput; // 

//methods 
public void initModule()
{
	appetitiveAlpha=0.000025;
	aversiveAlpha=0.1;
	tonicInput= new  double[]{0,0,-1.25};
	NslCanvas c=null;
	c=nslAddTemporalCanvas("output", "drives", thirst, NslMin.eval(0,NslMinValue.eval(d_min)), NslMax.eval(1,NslMaxValue.eval(d_max)), NslColor.getColor("BLUE"));
	c.addVariable(hunger, NslColor.getColor("RED"));
	if(numDrives>2)
		c.addVariable(fear, NslColor.getColor("GREEN"));
	drives.nslSetAccess('W');
}

public void initRun()
{
	init();
}

public void initTrain()
{
	init();
}

protected void init()
{
	incentives_wm.set(0);
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
	if(system.getCurrentTime()>10*system.getDelta())
	{
		for(int i=0; i<2; i++)
			drives.set(i, NslMin.eval(d_max[i], NslMax.eval(d_min[i], drives.get(i)+appetitiveAlpha*(d_max[i]-drives.get(i)+tonicInput[i])-reductions.get(i)*NslOperator.abs.eval(drives.get(i)-d_min[i])+(incentives.get(i)-incentives_wm.get(i))*NslOperator.abs.eval(d_max[i]-drives.get(i)))));
		if(numDrives>2)
			drives.set(2, NslMin.eval(d_max[2], NslMax.eval(d_min[2], drives.get(2)-aversiveAlpha*(drives.get(2)-d_min[2]+tonicInput[2])-reductions.get(2)*NslOperator.abs.eval(d_max[2]-drives.get(2))+(incentives.get(2)-incentives_wm.get(2))*NslOperator.abs.eval(d_max[2]-drives.get(2)))));
	}
	thirst.set(drives.get(0));
	hunger.set(drives.get(1));
	if(numDrives>2)
		fear.set(drives.get(2));
	incentives_wm.set(incentives.get());
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
	double[] d_min;
	double[] d_max;

	/* Temporary variables */

	/* GENERIC CONSTRUCTOR: */
	public Hypothalamus(String nslName, NslModule nslParent, int numDrives, double[] d_min, double[] d_max)
{
		super(nslName, nslParent);
		this.numDrives=numDrives;
		this.d_min=d_min;
		this.d_max=d_max;
		initSys();
		makeInstHypothalamus(nslName, nslParent, numDrives, d_min, d_max);
	}

	public void makeInstHypothalamus(String nslName, NslModule nslParent, int numDrives, double[] d_min, double[] d_max)
{ 
		Object[] nslArgs=new Object[]{numDrives, d_min, d_max};
		callFromConstructorTop(nslArgs);
		thirst = new NslDouble0("thirst", this);
		hunger = new NslDouble0("hunger", this);
		fear = new NslDouble0("fear", this);
		incentives_wm = new NslDouble1("incentives_wm", this, numDrives);
		incentives = new NslDinDouble1("incentives", this, numDrives);
		reductions = new NslDinDouble1("reductions", this, numDrives);
		drives = new NslDoutDouble1("drives", this, numDrives);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end Hypothalamus

