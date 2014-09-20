package acq_tam_wg.ModelAvatar.v1_1_1.src;
import javax.media.j3d.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.vecmath.*;
import org.odejava.*;

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

public class ModelAvatar extends NslJavaModule implements KeyListener{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  ModelAvatar
//versionName: 1_1_1


//variables 
private Nsl3dCanvas mainCanvas; // 
public Nsl3dCanvas modelPerspectiveCanvas; // 
private TransformGroup viewTransformGroup; // 
private javax.media.j3d.Transform3D viewTransform; // 
private javax.vecmath.Vector3d viewPosition; // 
private ViewerAvatar viewerAvatar; // 
private nslj.src.display.j3d.Cone rat; // 
private double[] lastPosition; // 
private double lastOrientation; // 
private double lastDist; // 
private double arrivalTime; // 
private  NslDouble1 goalPosition; // 
private NslDouble0 goalOrientation; // 
private double rotateP; // 
private double rotateD; // 
private double translateP; // 
private double translateD; // 
private double translateDelta; // 
private double rotateDelta; // 
public  NslDinDouble1 targetPosition; // 
public  NslDinDouble0 goSignal; // 
public  NslDoutDouble1 currentMapPosition; // 
public  NslDoutDouble1 currentWorldPosition; // 
public  NslDoutDouble0 currentOrientation; // 
private  NslDouble1 startPosition; // 

//methods 
public void initSys()
{
	// Amount to translate when forward, or bacward buttons pushed
	translateDelta=0.25;
	// Amount to rotate when left or right buttons pushed
	rotateDelta=Math.PI/100;
	// gain parameter for rotation PD-controller
	rotateP=.0625;
	// damping parameter for rotation PD-controller
	rotateD=.1;
	// gain parameter for translation PD-controller
	translateP=.01;
	// damping parameter for translation PD-controller
	translateD=.05;
}

/**
 * Initialize ModelAvatar for the first time
*/
public void init(Nsl3dCanvas mc)
{
	// Set main canvas
	mainCanvas=mc;
	// Create a new Nsl3dCanvas representing the avatar's view of the world
	modelPerspectiveCanvas=nslAdd3dCanvas("modelView","MODEL_VIEW",mainCanvas.get3dLocale());
	// Initialize avatar view position
	initViewPosition();
	// Initialize the avatar
	initAvatar();
}

/**
 * Initialize avatar view position
*/
protected void initViewPosition()
{
	// Get viewing platform from the model perspective view
	ViewingPlatform vp=modelPerspectiveCanvas.getVp();
	// Change initial position of the model
	viewPosition= new  javax.vecmath.Vector3d(startPosition.get(0), 5, startPosition.get(1));
	Transform3D t3d =  new  Transform3D();
	t3d.setTranslation(viewPosition);
	vp.getViewPlatformTransform().setTransform( t3d );
}

/**
 * Initialize avatar
*/
public void initAvatar()
{
	ViewingPlatform vp=getVp();
	// Get view transform and transform group
	viewTransform=  new   javax.media.j3d.Transform3D();
	vp.getViewPlatformTransform().getTransform(viewTransform);
	viewTransformGroup=vp.getViewPlatformTransform();

	// Create avatar, get viewer, and set viewer avatar
	vp.setPlatformGeometry( new   PlatformGeometry());

	viewerAvatar=  new   ViewerAvatar();

	//create appearance and material for the Cone
	javax.vecmath.Color3f black=  new   javax.vecmath.Color3f(0f, 0f, 0f);
	javax.vecmath.Color3f white=  new   javax.vecmath.Color3f(1f, 1f, 1f);
	javax.vecmath.Color3f objColor=  new   javax.vecmath.Color3f(1f, 1f, 1f);

	// Create rat body
	Appearance bodyApp=  new   Appearance();
	bodyApp.setMaterial(  new   Material(objColor, black, objColor, white, 90.0f));
	rat=  new   nslj.src.display.j3d.Cone("avatar", bodyApp,   new   org.openmali.vecmath2.Vector3f(1.5f,6.0f,1.5f),
                                         new   org.openmali.vecmath2.Vector3f(0,-1.35f,-1.975f),
					 new   float[]{0,0,0});
	rat.getWorldTransform().mul(viewTransform, rat.getLocalTransform());
	rat.init(null, null);
	viewerAvatar.addChild(rat.getBranchGroup());

	// Create nose
	TransformGroup noseTg=   new    TransformGroup();
	Transform3D noseT3d=   new    Transform3D();
	noseT3d.set(   new    javax.vecmath.Vector3f(0,0,-3.0f));
	noseTg.setTransform(noseT3d);
	rat.getTransformGroup().addChild(noseTg);
	Appearance noseApp=   new    Appearance();
	noseApp.setMaterial(   new    Material(black, black, black, black, 90.0f));
	com.sun.j3d.utils.geometry.Sphere nose=   new    com.sun.j3d.utils.geometry.Sphere(.2f,noseApp);
	noseTg.addChild(nose);

	// Create whisker 1
	TransformGroup whisker1Tg=   new    TransformGroup();
	Transform3D whisker1T3d=   new    Transform3D();
	whisker1T3d.set(   new    javax.vecmath.Vector3f(0f,0f,-3.1f));
	Transform3D temp=   new    Transform3D();
	temp.setEuler(   new    javax.vecmath.Vector3d(Math.PI/8,0,Math.PI/2));
	whisker1T3d.mul(temp);
	whisker1Tg.setTransform(whisker1T3d);
	rat.getTransformGroup().addChild(whisker1Tg);
	com.sun.j3d.utils.geometry.Box whisker1=   new    com.sun.j3d.utils.geometry.Box(.01f,1f,.01f,noseApp);
	whisker1Tg.addChild(whisker1);

	// Create whisker 2
	TransformGroup whisker2Tg=   new    TransformGroup();
	Transform3D whisker2T3d=   new    Transform3D();
	whisker2T3d.set(   new    javax.vecmath.Vector3f(0f,0f,-3.1f));
	temp=   new    Transform3D();
	temp.setEuler(   new    javax.vecmath.Vector3d(-Math.PI/8,0,Math.PI/2));
	whisker2T3d.mul(temp);
	whisker2Tg.setTransform(whisker2T3d);
	rat.getTransformGroup().addChild(whisker2Tg);
	com.sun.j3d.utils.geometry.Box whisker2=   new    com.sun.j3d.utils.geometry.Box(.01f,1f,.01f,noseApp);
	whisker2Tg.addChild(whisker2);

	// Create whisker 3
	TransformGroup whisker3Tg=   new    TransformGroup();
	Transform3D whisker3T3d=   new    Transform3D();
	whisker3T3d.set(   new    javax.vecmath.Vector3f(0f,0f,-3.1f));
	temp=   new    Transform3D();
	temp.setEuler(   new    javax.vecmath.Vector3d(0,0,Math.PI/2));
	whisker3T3d.mul(temp);
	whisker3Tg.setTransform(whisker3T3d);
	rat.getTransformGroup().addChild(whisker3Tg);
	com.sun.j3d.utils.geometry.Box whisker3=   new    com.sun.j3d.utils.geometry.Box(.01f,1f,.01f,noseApp);
	whisker3Tg.addChild(whisker3);

	Viewer viewer=vp.getViewers()[0];
	viewer.setAvatar(viewerAvatar);
}

public void initRun()
{
	init();
}

public void initTrain()
{
	init();
}

/**
 * Initialize ModelAvatar at the start of a trial
*/
protected void init()
{
	// Initialize avatar view position
	initViewPosition();

	// Set orientation and position outputs
	setCurrentOrientation();
	setCurrentPosition();

	// Initialize last position and orientation
	lastPosition= new  double[2];
	lastOrientation=Math.PI;

	// Initialize current goal position and angle
	goalPosition.set( new  double[]{viewPosition.x,viewPosition.z});
	goalOrientation.set(Math.PI);

	// Initialize distance to current target in last time step
	lastDist=Double.POSITIVE_INFINITY;
}

/**
 * Set current orientation output
*/
protected void setCurrentOrientation()
{
	// Get orientation from current view transform
	viewTransformGroup.getTransform(viewTransform);
	Matrix3d m= new  Matrix3d();
	viewTransform.get(m);
	currentOrientation.set(NslOperator.atan2.eval(-m.m20,m.m00)+Math.PI);
}

/**
 * Set current position output
*/
protected void setCurrentPosition()
{
	// Get world coordinates from current view transform
	viewTransformGroup.getTransform(viewTransform);
	javax.vecmath.Vector3d worldCoord=  new   javax.vecmath.Vector3d();
	viewTransform.get(worldCoord);
	currentWorldPosition.set(0, worldCoord.x);
	currentWorldPosition.set(1, worldCoord.z);
	// Convert world coordinates to map coordinates
	Point2d mapCoord=mainCanvas.convertToMapCoordinate(worldCoord);
	currentMapPosition.set(0, mapCoord.x);
	currentMapPosition.set(1, mapCoord.y);
}

public void simRun()
{
	process();
}

public void simTrain()
{
	process();
}

/**
 * Process avatar for one simulation time step
*/
protected void process()
{
	// Compute distance to the current goal position
	double distance=NslOperator.distance.eval(currentWorldPosition.get(), goalPosition.get());
	// If we've just crossed the threshold distance of 1, set the arrival time to the current time
	if(distance<1&&lastDist>=1)
		arrivalTime=system.getCurrentTime();
	// Update the last distance to the current goal position
	lastDist=distance;

	// If we've reached the current goal, we've been there for at least .05s, and the go signal is over threshold
	//if(distance<.1 && goSignal.get()>actionThreshold && system.getCurrentTime()>=(arrivalTime+.05))
	if(goSignal.get()>2*actionThreshold/3)
	{
		// Compute goal coordinates from target position input - bound according to the dimensions of the maze
		goalPosition.set(0, NslMax.eval(-mainCanvas.getMazeDim().getX()+3,
						NslMin.eval(mainCanvas.getMazeDim().getX()-3,targetPosition.get(0))));
		goalPosition.set(1, NslMax.eval(-mainCanvas.getMazeDim().getZ()+3,
						NslMin.eval(mainCanvas.getMazeDim().getZ()-3,targetPosition.get(1))));

		Point2d mapCoord= new  Point2d(goalPosition.get());
		javax.vecmath.Vector3d worldCoord=mainCanvas.convertToWorldCoordinate(mapCoord);
		goalPosition.set(0, worldCoord.x);
		goalPosition.set(1, worldCoord.z);

		if(goSignal.get()>actionThreshold&&NslOperator.abs.eval(goalOrientation.get()-currentOrientation.get())<.1)
		{
			// Compute orientation vector from current orientation angle		
			double[] orientationVec= new  double[]{NslOperator.cos.eval(currentOrientation.get()-Math.PI), NslOperator.sin.eval(currentOrientation.get()-Math.PI)};
			// Compute goal position orientation vector from current position and goal position
			double[] goalVec= new  double[]{-goalPosition.get(1)+currentWorldPosition.get(1), -goalPosition.get(0)+currentWorldPosition.get(0)};
			// Angle between current orientation and direction of the current goal
			double angle=NslOperator.atan2.eval(goalVec[1],goalVec[0])-NslOperator.atan2.eval(orientationVec[1],orientationVec[0]);
			// Set the goal orientation
			goalOrientation.set((currentOrientation.get()+angle)%(Math.PI*2.0));
			if(goalOrientation.get()<0)
				goalOrientation.set(Math.PI*2+goalOrientation.get());
		}
	}
	else
	{
		goalPosition.set(currentWorldPosition.get());
	}

	/*if(nslAbs(goalOrientation.get()-currentOrientation.get())<.1 && goSignal.get()>actionThreshold)
	{
		// Compute goal coordinates from target position input - bound according to the dimensions of the maze
		goalPosition.set(0, nslMax(-mainCanvas.getMazeDim().getX()+6,
						nslMin(mainCanvas.getMazeDim().getX()-6,targetPosition.get(0))));
		goalPosition.set(1, nslMax(-mainCanvas.getMazeDim().getZ()+6,
						nslMin(mainCanvas.getMazeDim().getZ()-6,targetPosition.get(1))));

		Point2d mapCoord=new Point2d(goalPosition.get());
		javax.vecmath.Vector3d worldCoord=mainCanvas.convertToWorldCoordinate(mapCoord);
		goalPosition.set(0, worldCoord.x);
		goalPosition.set(1, worldCoord.z);

		// Compute orientation vector from current orientation angle		
		double[] orientationVec=new double[]{nslCos(currentOrientation.get()-Math.PI), nslSin(currentOrientation.get()-Math.PI)};
		// Compute goal position orientation vector from current position and goal position
		double[] goalVec=new double[]{-goalPosition.get(1)+currentWorldPosition.get(1), -goalPosition.get(0)+currentWorldPosition.get(0)};
		// Angle between current orientation and direction of the current goal
		double angle=nslArcTan2(goalVec[1],goalVec[0])-nslArcTan2(orientationVec[1],orientationVec[0]);
		// Set the goal orientation
		goalOrientation.set((currentOrientation.get()+angle)%(Math.PI*2.0));
		if(goalOrientation.get()<0)
			goalOrientation.set(Math.PI*2+goalOrientation.get());
	}*/
	
	// Move the avatar
	move();

	// Set orientation and position outputs
	setCurrentOrientation();
	setCurrentPosition();
}

/**
 * Run PD controllers to move the avatar - rotation and translation
*/
protected void move()
{
	// Run rotation PD-controller
	rotate();

	// Uopdate current position and orientation - just to be sure
	setCurrentOrientation();
	setCurrentPosition();
	
	// Run translation PD-controller
	translate();
}

/**
 * Run rotation PD-controller
*/
protected void rotate()
{
	// If past the first time step and we havent reached the goal position yet
	//if(system.getCurrentTime()>system.getDelta() && nslDistance(currentWorldPosition.get(), goalPosition.get())>.01)	
	if(system.getCurrentTime()>system.getDelta()&&NslOperator.distance.eval(currentWorldPosition.get(),lastPosition)<.1)
	{
		// Make p component the shortest angle between the current angle and goal orientation - could be positive or negative
		double pComp=goalOrientation.get()-currentOrientation.get();
		if(goalOrientation.get()<currentOrientation.get()&&(Math.PI*2-currentOrientation.get()+goalOrientation.get())<(currentOrientation.get()-goalOrientation.get()))
			pComp=Math.PI*2.0-currentOrientation.get()+goalOrientation.get();
		else if(goalOrientation.get()>currentOrientation.get()&&(__tempacq_tam_wg_ModelAvatar_v1_1_1_src_ModelAvatar1.setReference(__tempacq_tam_wg_ModelAvatar_v1_1_1_src_ModelAvatar0.setReference(Math.PI*2-goalOrientation.get()).get()+currentOrientation.get())).get()<(goalOrientation.get()-currentOrientation.get()))
			pComp=-(Math.PI*2.0-goalOrientation.get()+currentOrientation.get());

		// Make d component the shortest angle between the current angle and the angle at the last time step - could be positive or negative
		double dComp=currentOrientation.get()-lastOrientation;
		if(lastOrientation>currentOrientation.get()&&Math.PI*2-lastOrientation+currentOrientation.get()<lastOrientation-currentOrientation.get())
			dComp=Math.PI*2.0-lastOrientation+currentOrientation.get();
		else if(currentOrientation.get()>lastOrientation&&Math.PI*2-currentOrientation.get()+lastOrientation<currentOrientation.get()-lastOrientation)
			dComp=-(Math.PI*2.0-currentOrientation.get()+lastOrientation);

		// Compute rotation
		double rotation=rotateP*pComp-rotateD*dComp;

		// Modify view transform
		viewTransformGroup.getTransform(viewTransform);
		Transform3D rot=  new   Transform3D();
		rot.rotY(rotation);
		viewTransform.mul(rot);
		viewTransformGroup.setTransform(viewTransform);
	}
	// Update last orientation
	lastOrientation=currentOrientation.get();
}

/**
 * Run translation PD-controller
*/
protected void translate()
{
	// If past the first time step and we have reached the goal orientation yet
	if(system.getCurrentTime()>system.getDelta()&&NslOperator.abs.eval(getRelativeAngle(currentOrientation.get(),goalOrientation.get()))<.1)
	//if(system.getCurrentTime()>system.getDelta() && nslAbs(getRelativeAngle(currentOrientation.get(),lastOrientation))<.1)
	{
		// Construct transform vector
		Vector3d displacement=   new    Vector3d(translateP*(goalPosition.get(0)-currentWorldPosition.get(0))-translateD*(currentWorldPosition.get(0)-lastPosition[0]),
								0.0,
								translateP*(goalPosition.get(1)-currentWorldPosition.get(1))-translateD*(currentWorldPosition.get(1)-lastPosition[1]));
		viewTransformGroup.getTransform(viewTransform);
		Matrix3d rot= new  Matrix3d();
		viewTransform.get(rot);
		rot.invert();
		rot.transform(displacement);
        
		// Modify view transform
		Transform3D trans=  new   Transform3D();
		trans.setTranslation(displacement);
		viewTransform.mul(trans);

		// Check for collision
		//if(!mainCanvas.isCollision(viewTransform,true) )
		//{
			viewTransformGroup.setTransform(viewTransform);
		//}
	}
	// Update last position
	lastPosition=currentWorldPosition.get();
}

/**
 * Forward button pushed
*/
public void forwardPushed()
{
	// Compute displacement based on current orientation
	viewTransformGroup.getTransform(viewTransform);
	Vector3d displacement=   new    Vector3d(0.0, 0.0, -translateDelta);
	Matrix3d rot= new  Matrix3d();
	viewTransform.get(rot);
	rot.transform(displacement);

	// Update goal position
	Transform3D newTransform= new  Transform3D();
	viewTransformGroup.getTransform(newTransform);
	Transform3D toMove= new  Transform3D();
	toMove.setTranslation( new  Vector3d(goalPosition.get(0)+displacement.x-currentWorldPosition.get(0),0,goalPosition.get(1)+displacement.z-currentWorldPosition.get(1)));
	newTransform.mul(toMove);

	// Check for collision
	//if(!mainCanvas.isCollision(newTransform,true))
	//{
		goalPosition.set(0, goalPosition.get(0)+displacement.x);
		goalPosition.set(1, goalPosition.get(1)+displacement.z);
	//}
}

/**
 * Backward button pushed
*/
public void backPushed()
{
	// Compute displacement based on current orientation
	viewTransformGroup.getTransform(viewTransform);
	Vector3d displacement=   new    Vector3d(0.0, 0.0, translateDelta);
	Matrix3d rot= new  Matrix3d();
	viewTransform.get(rot);
	rot.transform(displacement);
	
	// Update goal position
	Transform3D newTransform= new  Transform3D();
	viewTransformGroup.getTransform(newTransform);
	Transform3D toMove= new  Transform3D();
	toMove.setTranslation( new  Vector3d(goalPosition.get(0)+displacement.x-currentWorldPosition.get(0),0,goalPosition.get(1)+displacement.z-currentWorldPosition.get(1)));
	newTransform.mul(toMove);

	// Check for collision
	//if(!mainCanvas.isCollision(newTransform,true))
	//{
		goalPosition.set(0, goalPosition.get(0)+displacement.x);
		goalPosition.set(1, goalPosition.get(1)+displacement.z);
	//}
}

/**
 * Right button pushed
*/
public void rightPushed()
{
	// Rotate right
	double rotateAngle=(goalOrientation.get()-rotateDelta)%(Math.PI*2.0);
	if(rotateAngle<0)
		rotateAngle=Math.PI*2+rotateAngle;
	goalOrientation.set(rotateAngle);
}

/**
 * Left button pushed
*/
public void leftPushed()
{
	// Rotate left
	double rotateAngle=(goalOrientation.get()+rotateDelta)%(Math.PI*2.0);
	goalOrientation.set(rotateAngle);
}

/**
 * Get avatar viewing platform
*/
public ViewingPlatform getVp()
{
	return modelPerspectiveCanvas.getVp();
}

/**
 * Compute the shortest angle between two orientation
*/
protected double getRelativeAngle(double ang1, double ang2)
{
	double relativeAngle=0.0;
	if(ang1>ang2)
	{
		double relAngleRight=(ang2-ang1);
		double relAngleLeft=2*Math.PI+relAngleRight;
		if(NslOperator.abs.eval(relAngleRight)<NslOperator.abs.eval(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	else
	{
		double relAngleLeft=ang2-ang1;
		double relAngleRight=relAngleLeft-2*Math.PI;
		if(NslOperator.abs.eval(relAngleRight)<NslOperator.abs.eval(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	return relativeAngle;
}

public void setStartPosition(double x, double y)
{
	startPosition.set(0, x);
	startPosition.set(1, y);
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
	double actionThreshold;

	/* Temporary variables */
		NslDouble0 __tempacq_tam_wg_ModelAvatar_v1_1_1_src_ModelAvatar0 = new NslDouble0();
		NslDouble0 __tempacq_tam_wg_ModelAvatar_v1_1_1_src_ModelAvatar1 = new NslDouble0();

	/* GENERIC CONSTRUCTOR: */
	public ModelAvatar(String nslName, NslModule nslParent, int size, double actionThreshold)
{
		super(nslName, nslParent);
		this.size=size;
		this.actionThreshold=actionThreshold;
		initSys();
		makeInstModelAvatar(nslName, nslParent, size, actionThreshold);
	}

	public void makeInstModelAvatar(String nslName, NslModule nslParent, int size, double actionThreshold)
{ 
		Object[] nslArgs=new Object[]{size, actionThreshold};
		callFromConstructorTop(nslArgs);
		goalPosition = new NslDouble1("goalPosition", this, 2);
		goalOrientation = new NslDouble0("goalOrientation", this);
		targetPosition = new NslDinDouble1("targetPosition", this, 2);
		goSignal = new NslDinDouble0("goSignal", this);
		currentMapPosition = new NslDoutDouble1("currentMapPosition", this, 2);
		currentWorldPosition = new NslDoutDouble1("currentWorldPosition", this, 2);
		currentOrientation = new NslDoutDouble0("currentOrientation", this);
		startPosition = new NslDouble1("startPosition", this, 2);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end ModelAvatar

