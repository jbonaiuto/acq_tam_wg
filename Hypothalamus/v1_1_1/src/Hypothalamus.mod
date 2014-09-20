package acq_tam_wg.Hypothalamus.v1_1_1.src;

nslJavaModule Hypothalamus(int numDrives, double[] d_min, double[] d_max){

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
private NslDouble1 incentives_wm(numDrives); // 
public NslDinDouble1 incentives(numDrives); // 
public NslDinDouble1 reductions(numDrives); // 
public NslDoutDouble1 drives(numDrives); // 
private double aversiveAlpha; // 
private double[] tonicInput; // 

//methods 
public void initModule()
{
	appetitiveAlpha=0.000025;
	aversiveAlpha=0.1;
	tonicInput=new double[]{0,0,-1.25};
	NslCanvas c=null;
	c=nslAddTemporalCanvas("output", "drives", thirst, nslMin(0,nslMinValue(d_min)), nslMax(1,nslMaxValue(d_max)), NslColor.getColor("BLUE"));
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
	incentives_wm=0;
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
			drives.set(i, nslMin(d_max[i], nslMax(d_min[i], drives.get(i)+appetitiveAlpha*(d_max[i]-drives.get(i)+tonicInput[i])-reductions.get(i)*nslAbs(drives.get(i)-d_min[i])+(incentives.get(i)-incentives_wm.get(i))*nslAbs(d_max[i]-drives.get(i)))));
		if(numDrives>2)
			drives.set(2, nslMin(d_max[2], nslMax(d_min[2], drives.get(2)-aversiveAlpha*(drives.get(2)-d_min[2]+tonicInput[2])-reductions.get(2)*nslAbs(d_max[2]-drives.get(2))+(incentives.get(2)-incentives_wm.get(2))*nslAbs(d_max[2]-drives.get(2)))));
	}
	thirst.set(drives.get(0));
	hunger.set(drives.get(1));
	if(numDrives>2)
		fear.set(drives.get(2));
	incentives_wm.set(incentives.get());
}
public void makeConn(){
}
}//end Hypothalamus

