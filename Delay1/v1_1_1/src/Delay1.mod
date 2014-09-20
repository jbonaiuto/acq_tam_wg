package acq_tam_wg.Delay1.v1_1_1.src;

nslJavaModule Delay1(int size, double minDelay, double maxDelay){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  Delay1
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDinDouble1 in(size); // 
public NslDoutDouble1 out(size); // 
private NslDouble2 delay_line(0, size); // 
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
	delay=minDelay+nslRandom()*(maxDelay-minDelay);
	delay_time_steps=(int)(delay/dt);
	delay_line.nslMemAlloc(delay_time_steps, size);
	out=0;
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
		delay_line[i]=delay_line[i-1];
	}
	delay_line[0]=in;
	if(system.getCurrentTime()>delay)
		out=delay_line[delay_time_steps-1];
}
public void makeConn(){
}
}//end Delay1

