package acq_tam_wg.MotorController.v1_1_1.src;

nslJavaModule MotorController(int size, double threshold){

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  MotorController
//versionName: 1_1_1
//floatSubModules: true


//variables 
public NslDoutDouble1 goalPosition(2); // 
public NslDinDouble1 currentPosition(2); // 
public NslDinDouble0 goSignal(); // 
public NslDinDouble0 goalOrientation(); // 
public NslDinDouble0 distance(); // 

//methods 
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
	goalPosition.set(0,currentPosition.get(0));
	goalPosition.set(1,currentPosition.get(1));
	if(goSignal.get()>threshold && !Double.isNaN(goalOrientation.get()))
	{
		double[] directionVec=new double[]{nslSin(goalOrientation.get()),nslCos(goalOrientation.get())};
		goalPosition.set(0, currentPosition.get(0)+distance.get()*directionVec[0]);
		goalPosition.set(1, currentPosition.get(1)+distance.get()*directionVec[1]);
	}
}
public void makeConn(){
}
}//end MotorController

