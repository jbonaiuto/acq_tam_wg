package acq_tam_wg.Delay1.v1_1_1.src;

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

public class Delay1 extends NslJavaModule{

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Delay1
//versionName: 1_1_1
//floatSubModules: true


//variables 
public  NslDinDouble1 in; // 
public  NslDoutDouble1 out; // 
private  NslDouble2 delay_line; // 
private double delay; // 
private int delay_time_steps; // 

//methods 
public void initRun()
{
	init(system.getRunDelta());
}

public void initTrain()
{
	init(system.getTrainDelta());
}

protected void init(double dt)
{
	delay=minDelay+NslRandom.eval()*(maxDelay-minDelay);
	delay_time_steps=(int)(delay/dt);
	delay_line.nslMemAlloc(delay_time_steps, size);
	out.set(0);
}

public void simRun()
{
	processInput();
}

public void simTrain()
{
	processInput();
}

protected void processInput()
{
	for(int i=delay_time_steps-1; i>0; i--)
	{
		delay_line.set(i, __tempacq_tam_wg_Delay1_v1_1_1_src_Delay10.setReference(delay_line.get(i-1)));
	}
	delay_line.set(0, in);
	if(system.getCurrentTime()>delay)
		out.set(__tempacq_tam_wg_Delay1_v1_1_1_src_Delay11.setReference(delay_line.get(delay_time_steps-1)));
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
	int size;
	double minDelay;
	double maxDelay;

	/* Temporary variables */
		NslDouble1 __tempacq_tam_wg_Delay1_v1_1_1_src_Delay10 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_Delay1_v1_1_1_src_Delay11 = new NslDouble1(1);

	/* GENERIC CONSTRUCTOR: */
	public Delay1(String nslName, NslModule nslParent, int size, double minDelay, double maxDelay)
{
		super(nslName, nslParent);
		this.size=size;
		this.minDelay=minDelay;
		this.maxDelay=maxDelay;
		initSys();
		makeInstDelay1(nslName, nslParent, size, minDelay, maxDelay);
	}

	public void makeInstDelay1(String nslName, NslModule nslParent, int size, double minDelay, double maxDelay)
{ 
		Object[] nslArgs=new Object[]{size, minDelay, maxDelay};
		callFromConstructorTop(nslArgs);
		in = new NslDinDouble1("in", this, size);
		out = new NslDoutDouble1("out", this, size);
		delay_line = new NslDouble2("delay_line", this, 0, size);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end Delay1

