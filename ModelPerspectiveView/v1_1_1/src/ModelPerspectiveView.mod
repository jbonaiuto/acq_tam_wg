package acq_tam_wg.ModelPerspectiveView.v1_1_1.src;
nslImport javax.media.j3d.*;
nslImport com.sun.j3d.utils.universe.*;
nslImport com.sun.j3d.utils.geometry.*;
nslImport javax.vecmath.*;
nslImport org.odejava.*;

nslJavaModule ModelPerspectiveView(int size, double actionThreshold) implements KeyListener{

//NSL Version: 3_0_n
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  ModelPerspectiveView
//versionName: 1_1_1
//floatSubModules: true


//variables 
private javax.vecmath.Vector3d viewPosition; // 
private javax.media.j3d.Transform3D viewTransform; // 
private ViewerAvatar viewerAvatar; // 
private nslj.src.display.j3d.Cone rat; // 
private double[] lastPosition; // 
private TransformGroup viewTransformGroup; // 
private double p; // 
private double d; // 
private Nsl3dCanvas mainCanvas; // 
private double lastAngle; // 
public Nsl3dCanvas modelPerspectiveCanvas; // 
public NslDoutDouble1 currentMapPosition(2); // 
private NslDouble1 goalPosition(2); // 
private NslDouble0 goalOrientation; // 
private double translateDelta; // 
private double rotateDelta; // 
public NslDinDouble1 targetPosition(2); // 
public NslDinDouble0 goSignal(); // 
private NslDouble0 currentOrientation; // 
private double lastDist; // 
private double arrivalTime; // 
public NslDinDouble1 efferenceCopyIn(size); // 
public NslDoutDouble1 efferenceCopyOut(size); // 
private double delta; // 
public NslDoutDouble1 currentWorldPosition(2); // 

//methods 
public void initSys()
{
	translateDelta=0.25;
	rotateDelta=Math.PI/100;
	delta=.99;
}

public void init(Nsl3dCanvas mc)
{
	p=.25;
	d=.5;
	
	mainCanvas=mc;
	modelPerspectiveCanvas=nslAdd3dCanvas("modelView","MODEL_VIEW",mainCanvas.get3dLocale());
	ViewingPlatform vp=modelPerspectiveCanvas.getVp();

	//viewPosition=new javax.vecmath.Vector3d ( 0, 3, 0 );
	//viewPosition=new javax.vecmath.Vector3d(1, 3, 1);
	viewPosition=new javax.vecmath.Vector3d(0, 5, 0);
	Transform3D t3d = new Transform3D();
	t3d.setTranslation(viewPosition);
	vp.getViewPlatformTransform().setTransform( t3d );
	initAvatar(vp);
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
	ViewingPlatform vp=modelPerspectiveCanvas.getVp();
	//viewPosition=new javax.vecmath.Vector3d ( 0, 3, 0 );
	viewPosition=new javax.vecmath.Vector3d ( 0, 5, 0 );
	Transform3D t3d =  new  Transform3D();
	t3d.setTranslation(viewPosition);
	vp.getViewPlatformTransform().setTransform( t3d );

	setCurrentOrientation();
	setCurrentPosition();

	lastPosition=new double[2];
	lastAngle=Math.PI;
	goalPosition.set(new double[2]);
	goalOrientation.set(Math.PI);

	lastDist=Double.POSITIVE_INFINITY;
	efferenceCopyOut=0;
}

public void initAvatar(ViewingPlatform vp)
{
	// Get view transform
	viewTransform= new  javax.media.j3d.Transform3D();
	vp.getViewPlatformTransform().getTransform(viewTransform);
	viewTransformGroup=vp.getViewPlatformTransform();

	vp.setPlatformGeometry(createPlatformGeometry());
	createViewerAvatar();
	Viewer viewer=vp.getViewers()[0];
	viewer.setAvatar(viewerAvatar);
}

protected void createViewerAvatar()
{
	viewerAvatar= new  ViewerAvatar();

	//create appearance and material for the Cone
	javax.vecmath.Color3f black= new  javax.vecmath.Color3f(0f, 0f, 0f);
	javax.vecmath.Color3f white= new  javax.vecmath.Color3f(1f, 1f, 1f);
	javax.vecmath.Color3f objColor= new  javax.vecmath.Color3f(1f, 1f, 1f);

	Appearance bodyApp= new  Appearance();
	bodyApp.setMaterial( new  Material(objColor, black, objColor, white, 90.0f));
	//rat= new  nslj.src.display.j3d.Cone("avatar", bodyApp,  new  org.openmali.vecmath2.Vector3f(1f,3.0f,1f),
	rat= new  nslj.src.display.j3d.Cone("avatar", bodyApp,  new  org.openmali.vecmath2.Vector3f(3f,6.0f,3f),
                                        new  org.openmali.vecmath2.Vector3f(0,-1.35f,-1.975f),
					new  float[]{(float)(Math.PI),0,0});
	rat.getWorldTransform().mul(viewTransform, rat.getLocalTransform());
	rat.init(null, null);
	viewerAvatar.addChild(rat.getBranchGroup());

	TransformGroup noseTg=  new   TransformGroup();
	Transform3D noseT3d=  new   Transform3D();
	noseT3d.set(  new   javax.vecmath.Vector3f(0,0,3.0f));
	noseTg.setTransform(noseT3d);
	rat.getTransformGroup().addChild(noseTg);
	
	Appearance noseApp=  new   Appearance();
	noseApp.setMaterial(  new   Material(black, black, black, black, 90.0f));
	com.sun.j3d.utils.geometry.Sphere nose=  new   com.sun.j3d.utils.geometry.Sphere(.2f,noseApp);
	noseTg.addChild(nose);

	TransformGroup whisker1Tg=  new   TransformGroup();
	Transform3D whisker1T3d=  new   Transform3D();
	whisker1T3d.set(  new   javax.vecmath.Vector3f(0f,0f,3.1f));
	Transform3D temp=  new   Transform3D();
	temp.setEuler(  new   javax.vecmath.Vector3d(Math.PI/8,0,Math.PI/2));
	whisker1T3d.mul(temp);
	whisker1Tg.setTransform(whisker1T3d);
	rat.getTransformGroup().addChild(whisker1Tg);
	
	com.sun.j3d.utils.geometry.Box whisker1=  new   com.sun.j3d.utils.geometry.Box(.01f,1f,.01f,noseApp);
	whisker1Tg.addChild(whisker1);

	TransformGroup whisker2Tg=  new   TransformGroup();
	Transform3D whisker2T3d=  new   Transform3D();
	whisker2T3d.set(  new   javax.vecmath.Vector3f(0f,0f,3.1f));
	temp=  new   Transform3D();
	temp.setEuler(  new   javax.vecmath.Vector3d(-Math.PI/8,0,Math.PI/2));
	whisker2T3d.mul(temp);
	whisker2Tg.setTransform(whisker2T3d);
	rat.getTransformGroup().addChild(whisker2Tg);
	
	com.sun.j3d.utils.geometry.Box whisker2=  new   com.sun.j3d.utils.geometry.Box(.01f,1f,.01f,noseApp);
	whisker2Tg.addChild(whisker2);

	TransformGroup whisker3Tg=  new   TransformGroup();
	Transform3D whisker3T3d=  new   Transform3D();
	whisker3T3d.set(  new   javax.vecmath.Vector3f(0f,0f,3.1f));
	temp=  new   Transform3D();
	temp.setEuler(  new   javax.vecmath.Vector3d(0,0,Math.PI/2));
	whisker3T3d.mul(temp);
	whisker3Tg.setTransform(whisker3T3d);
	rat.getTransformGroup().addChild(whisker3Tg);
	
	com.sun.j3d.utils.geometry.Box whisker3=  new   com.sun.j3d.utils.geometry.Box(.01f,1f,.01f,noseApp);
	whisker3Tg.addChild(whisker3);
}

protected PlatformGeometry createPlatformGeometry()
{
	PlatformGeometry pg =  new  PlatformGeometry();
	return pg;
}

public void simRun()
{
	process();
}

public void simTrain()
{
	process();
}

protected void setCurrentOrientation()
{
	viewTransformGroup.getTransform(viewTransform);
	Matrix3d m=new Matrix3d();
	viewTransform.get(m);
	currentOrientation.set(nslArcTan2(-m.m20, m.m00)+Math.PI);
}

protected void setCurrentPosition()
{
	viewTransformGroup.getTransform(viewTransform);
	javax.vecmath.Vector3d worldCoord= new  javax.vecmath.Vector3d();
	viewTransform.get(worldCoord);
	currentWorldPosition.set(0, worldCoord.x);
	currentWorldPosition.set(1, worldCoord.z);
	Point2d mapCoord=mainCanvas.convertToMapCoordinate(worldCoord);
	currentMapPosition.set(0, mapCoord.x);
	currentMapPosition.set(1, mapCoord.y);
}

protected void process()
{
	double goalX=currentWorldPosition.get(0);
	double goalY=currentWorldPosition.get(1);
	double distance=nslDistance(currentWorldPosition.get(), goalPosition.get());
	if(distance<1 && lastDist>=1)
	{
		arrivalTime=system.getCurrentTime();
	}
	lastDist=distance;
	efferenceCopyOut=delta*efferenceCopyOut;
	if(distance<.1 && goSignal.get()>actionThreshold && system.getCurrentTime()>=(arrivalTime+.1))
	{
		//goalX=nslMax(-mainCanvas.getMazeDim().getX()+6,nslMin(mainCanvas.getMazeDim().getX()-6,2*(targetPosition.get(0)-mainCanvas.getMazeDim().getX()/2)));
		goalX=nslMax(-mainCanvas.getMazeDim().getX()+6,nslMin(mainCanvas.getMazeDim().getX()-6,targetPosition.get(0)));
		//goalY=nslMax(-mainCanvas.getMazeDim().getZ()+6,nslMin(mainCanvas.getMazeDim().getZ()-6,2*(targetPosition.get(1)-mainCanvas.getMazeDim().getZ()/2)));
		goalY=nslMax(-mainCanvas.getMazeDim().getZ()+6,nslMin(mainCanvas.getMazeDim().getZ()-6,targetPosition.get(1)));

		goalPosition.set(0, goalX);
		goalPosition.set(1, goalY);
		
		double[] orientationVec=new double[]{nslCos(currentOrientation.get()-Math.PI), nslSin(currentOrientation.get()-Math.PI)};
		double[] goalVec=new double[]{-goalPosition.get(1)+currentWorldPosition.get(1), -goalPosition.get(0)+currentWorldPosition.get(0)};
		double angle=nslArcTan2(goalVec[1],goalVec[0])-nslArcTan2(orientationVec[1],orientationVec[0]);
		goalOrientation.set((currentOrientation.get()+angle)%(Math.PI*2.0));
		if(goalOrientation.get()<0)
			goalOrientation.set(Math.PI*2+goalOrientation.get());		
		efferenceCopyOut.set(efferenceCopyIn.get());
	}
	
	setCurrentOrientation();
	setCurrentPosition();
	
	updatePositionOrientation();
}

protected void updatePositionOrientation()
{
	if(system.getCurrentTime()>system.getDelta() && nslDistance(currentWorldPosition.get(), goalPosition.get())>.01)
	{
		double pComp=goalOrientation.get()-currentOrientation.get();
		if(goalOrientation.get()<currentOrientation.get() && (Math.PI*2-currentOrientation.get()+goalOrientation.get())<(currentOrientation.get()-goalOrientation.get()))
			pComp=Math.PI*2.0-currentOrientation.get()+goalOrientation.get();
		else if(goalOrientation.get()>currentOrientation.get() && (Math.PI*2-goalOrientation+currentOrientation.get())<(goalOrientation.get()-currentOrientation.get()))
			pComp=-(Math.PI*2.0-goalOrientation.get()+currentOrientation.get());
		double dComp=currentOrientation.get()-lastAngle;
		if(lastAngle>currentOrientation.get() && Math.PI*2-lastAngle+currentOrientation.get()<lastAngle-currentOrientation.get())
			dComp=Math.PI*2.0-lastAngle+currentOrientation.get();
		else if(currentOrientation.get() > lastAngle && Math.PI*2-currentOrientation.get()+lastAngle<currentOrientation.get()-lastAngle)
			dComp=-(Math.PI*2.0-currentOrientation.get()+lastAngle);
		double rotation=.5*p*pComp-.5*d*dComp;

		// Modify view transform
		viewTransformGroup.getTransform(viewTransform);
		Transform3D rot= new  Transform3D();
		rot.rotY(rotation);

		viewTransform.mul(rot);
		viewTransformGroup.setTransform(viewTransform);
	}
	lastAngle=currentOrientation.get();

	// Current position and orientation
	setCurrentOrientation();
	setCurrentPosition();
	
	if(system.getCurrentTime()>system.getDelta() && nslAbs(getRelativeAngle(currentOrientation.get(),goalOrientation.get()))<.01)
	{
		// Construct transform vector
		Vector3d displacement=  new   Vector3d(p*(goalPosition.get(0)-currentWorldPosition.get(0))-d*(currentWorldPosition.get(0)-lastPosition[0]),
								0.0,
								p*(goalPosition.get(1)-currentWorldPosition.get(1))-d*(currentWorldPosition.get(1)-lastPosition[1]));
		viewTransformGroup.getTransform(viewTransform);
		Matrix3d rot=new Matrix3d();
		viewTransform.get(rot);
		rot.invert();
		rot.transform(displacement);
        
		// Modify view transform
		Transform3D trans= new  Transform3D();
		trans.setTranslation(displacement);

		viewTransform.mul(trans);
		if(!mainCanvas.isCollision(viewTransform,true) )
		{
			viewTransformGroup.setTransform(viewTransform);
		}
	}
	lastPosition=currentWorldPosition.get();
	//nslPrintln("("+currentWorldPosition.get(0)+", "+currentWorldPosition.get(1)+")");
}

public void forwardPushed()
{
	viewTransformGroup.getTransform(viewTransform);
	Vector3d displacement=  new   Vector3d(0.0, 0.0, -translateDelta);
	Matrix3d rot=new Matrix3d();
	viewTransform.get(rot);
	rot.transform(displacement);

	Transform3D newTransform=new Transform3D();
	viewTransformGroup.getTransform(newTransform);
	Transform3D toMove=new Transform3D();
	toMove.setTranslation(new Vector3d(goalPosition.get(0)+displacement.x-currentWorldPosition.get(0),0,goalPosition.get(1)+displacement.z-currentWorldPosition.get(1)));
	newTransform.mul(toMove);
	if(!mainCanvas.isCollision(newTransform,true))
	{
		goalPosition.set(0, goalPosition.get(0)+displacement.x);
		goalPosition.set(1, goalPosition.get(1)+displacement.z);
	}
}

public void backPushed()
{
	viewTransformGroup.getTransform(viewTransform);
	Vector3d displacement=  new   Vector3d(0.0, 0.0, translateDelta);
	Matrix3d rot=new Matrix3d();
	viewTransform.get(rot);
	rot.transform(displacement);
	
	Transform3D newTransform=new Transform3D();
	viewTransformGroup.getTransform(newTransform);
	Transform3D toMove=new Transform3D();
	toMove.setTranslation(new Vector3d(goalPosition.get(0)+displacement.x-currentWorldPosition.get(0),0,goalPosition.get(1)+displacement.z-currentWorldPosition.get(1)));
	newTransform.mul(toMove);
	if(!mainCanvas.isCollision(newTransform,true))
	{
		goalPosition.set(0, goalPosition.get(0)+displacement.x);
		goalPosition.set(1, goalPosition.get(1)+displacement.z);
	}
}

public void rightPushed()
{
	double rotateAngle=(goalOrientation.get()-rotateDelta)%(Math.PI*2.0);
	if(rotateAngle<0)
		rotateAngle=Math.PI*2+rotateAngle;
	goalOrientation.set(rotateAngle);
}

public void leftPushed()
{
	double rotateAngle=(goalOrientation.get()+rotateDelta)%(Math.PI*2.0);
	goalOrientation.set(rotateAngle);
}

public ViewingPlatform getVp()
{
	return modelPerspectiveCanvas.getVp();
}

protected double getRelativeAngle(double ang1, double ang2)
{
	double relativeAngle=0.0;
	if(ang1>ang2)
	{
		double relAngleRight=(ang2-ang1);
		double relAngleLeft=2*Math.PI+relAngleRight;
		if(nslAbs(relAngleRight)<nslAbs(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	else
	{
		double relAngleLeft=ang2-ang1;
		double relAngleRight=relAngleLeft-2*Math.PI;
		if(nslAbs(relAngleRight)<nslAbs(relAngleLeft))
			relativeAngle=relAngleRight;
		else
			relativeAngle=relAngleLeft;
	}
	return relativeAngle;
}
public void makeConn(){
}
}//end ModelPerspectiveView

