package acq_tam_wg.MotorController.v1_1_2.src;

nslJavaModule MotorController(int size, double threshold){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  MotorController
//versionName: 1_1_2


//variables 
public NslDoutDouble1 goalPosition(2); // 
public NslDinDouble1 currentPosition(2); // 
public NslDinDouble0 currentOrientation(); // 
public NslDinDouble0 goSignal(); // 
public NslDinDouble0 orientationDelta(); // 
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
	if(goSignal.get()>threshold && !Double.isNaN(orientationDelta.get()))
	{
		double[] directionVec=new double[]{nslSin(orientationDelta.get()+currentOrientation.get()),nslCos(orientationDelta.get()+currentOrientation.get())};
		goalPosition.set(0, currentPosition.get(0)+distance.get()*directionVec[0]);
		goalPosition.set(1, currentPosition.get(1)+distance.get()*directionVec[1]);
	}
}
public void makeConn(){
}
}//end MotorController

