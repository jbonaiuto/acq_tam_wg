package acq_tam_wg.DNF.v1_1_1.src;

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

public class DNF extends NslJavaModule{

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  DNF
//versionName: 1_1_1


//variables 
public  NslDinDouble1 input; // 
public  NslDoutDouble1 output; // 
private  NslDouble1 weightKernel; // 
private NslDouble0 w_excite; // 
private NslDouble0 sigma_w; // 
private NslDouble0 w_inhibit; // 
private NslDouble0 h; // 
private  NslDouble1 u; // 
private NslDouble0 tau; // 
private NslDouble0 beta; // 
private NslDouble0 u_0; // 
private NslDouble0 q; // 
private  NslDouble1 noise; // 
private boolean initialized=false; // 

//methods 
public void initModule()
{
	h.set(-1);
	tau.set(.075);
	beta.set(1.5);
	u_0.set(0.0);
	w_excite.set(13.0);
	w_inhibit.set(20.0);
	sigma_w.set(20.0);
	q.set(5.0);
	h.nslSetAccess('W');
	w_excite.nslSetAccess('W');
	w_inhibit.nslSetAccess('W');
	sigma_w.nslSetAccess('W');
	q.nslSetAccess('W');
	updateWeightKernel();
	if(!initialized)
	{
		//nslAddSpatialCanvas("output", "weight", weightKernel, -1, 1);
		//nslAddSpatialCanvas("output", "output", output, 0, 1);
		nslSetColumns(1,"output");
		addPanel("dNFParameters", "input");
		addLabelToPanel("noiseStrength", "dNFParameters", "input");
		NslSlider ns_q=addSliderToPanel("noiseStrength", "dNFParameters", "input", NslSlider.HORIZONTAL);
		ns_q.setMinimum(0);
		ns_q.setMaximum(10);
		ns_q.setValue((int)q.get());
		addLabelToPanel("h", "dNFParameters", "input");
		NslSlider ns_h=addSliderToPanel("h", "dNFParameters", "input", NslSlider.HORIZONTAL);
		ns_h.setMinimum(-5);
		ns_h.setMaximum(0);
		ns_h.setValue((int)h.get());
		addLabelToPanel("sigma_w", "dNFParameters", "input");
		NslSlider ns_sigma_w=addSliderToPanel("sigma_w", "dNFParameters", "input", NslSlider.HORIZONTAL);
		ns_sigma_w.setMinimum(0);
		ns_sigma_w.setMaximum(20);
		ns_sigma_w.setValue((int)sigma_w.get());
		addLabelToPanel("w_excite", "dNFParameters", "input");
		NslSlider ns_w_excite=addSliderToPanel("w_excite", "dNFParameters", "input", NslSlider.HORIZONTAL);
		ns_w_excite.setMinimum(0);
		ns_w_excite.setMaximum(2000);
		ns_w_excite.setValue((int)(w_excite.get()*100));
		addLabelToPanel("w_inhibit", "dNFParameters", "input");
		NslSlider ns_w_inhibit=addSliderToPanel("w_inhibit", "dNFParameters", "input", NslSlider.HORIZONTAL);
		ns_w_inhibit.setMinimum(0);
		ns_w_inhibit.setMaximum(2000);
		ns_w_inhibit.setValue((int)(w_inhibit.get()*100));
		initialized=true;
	}
}

public void initRun()
{
	init();
}

public void initTrain()
{
	init();
}

public void init()
{
	output.set(0);
	u.set(0);
}

public void hValueChanged(int value)
{
	if(initialized)
	{
		h.set((double)value);
	}
}

public void sigma_wValueChanged(int value)
{
	if(initialized)
	{
		sigma_w.set((double)value);
		updateWeightKernel();
	}
}

public void w_exciteValueChanged(int value)
{
	if(initialized)
	{
		w_excite.set((double)(value/100.0));
		updateWeightKernel();
	}
}

public void w_inhibitValueChanged(int value)
{
	if(initialized)
	{
		w_inhibit.set((double)(value/100.0));
		updateWeightKernel();
	}
}

public void noiseStrengthValueChanged(int value)
{
	if(initialized)
	{
		q.set(value);
	}
}

public void updateWeightKernel()
{
	for(int i=0; i<dim*2; i++)
	{
		weightKernel.set(i, __tempacq_tam_wg_DNF_v1_1_1_src_DNF4.setReference(__tempacq_tam_wg_DNF_v1_1_1_src_DNF3.setReference(w_excite.get()*NslOperator.exp.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF2.setReference((double)(-(i-dim)*(i-dim))/(__tempacq_tam_wg_DNF_v1_1_1_src_DNF1.setReference(__tempacq_tam_wg_DNF_v1_1_1_src_DNF0.setReference(2.0*sigma_w.get()).get()*sigma_w.get())).get()))).get()-w_inhibit.get()));
	}
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
	u.set(system.nsldiff.eval(u, tau.get(), __tempacq_tam_wg_DNF_v1_1_1_src_DNF10.setReference(NslAdd.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF10.get(), __tempacq_tam_wg_DNF_v1_1_1_src_DNF9.setReference(NslAdd.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF9.get(), __tempacq_tam_wg_DNF_v1_1_1_src_DNF8.setReference(NslAdd.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF8.get(), __tempacq_tam_wg_DNF_v1_1_1_src_DNF7.setReference(NslAdd.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF7.get(), __tempacq_tam_wg_DNF_v1_1_1_src_DNF5.setReference(NslSub.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF5.get(), 0, u)), h)), input)), NslConv.eval(weightKernel,output))), __tempacq_tam_wg_DNF_v1_1_1_src_DNF6.setReference(NslElemMult.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF6.get(), q, NslRandom.eval(noise)))))));
	output.set(__tempacq_tam_wg_DNF_v1_1_1_src_DNF15 = (NslElemDiv.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF15, 1.0, (__tempacq_tam_wg_DNF_v1_1_1_src_DNF14 = (NslAdd.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF14, 1.0, NslOperator.exp.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF13.setReference(NslElemMult.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF13.get(), __tempacq_tam_wg_DNF_v1_1_1_src_DNF11.setReference(-beta.get()), (__tempacq_tam_wg_DNF_v1_1_1_src_DNF12.setReference(NslSub.eval(__tempacq_tam_wg_DNF_v1_1_1_src_DNF12.get(), u, u_0))))))))))));
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
	int dim;

	/* Temporary variables */
		NslDouble0 __tempacq_tam_wg_DNF_v1_1_1_src_DNF0 = new NslDouble0();
		NslDouble0 __tempacq_tam_wg_DNF_v1_1_1_src_DNF1 = new NslDouble0();
		NslDouble0 __tempacq_tam_wg_DNF_v1_1_1_src_DNF2 = new NslDouble0();
		NslDouble0 __tempacq_tam_wg_DNF_v1_1_1_src_DNF3 = new NslDouble0();
		NslDouble0 __tempacq_tam_wg_DNF_v1_1_1_src_DNF4 = new NslDouble0();
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF5 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF6 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF7 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF8 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF9 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF10 = new NslDouble1(1);
		NslDouble0 __tempacq_tam_wg_DNF_v1_1_1_src_DNF11 = new NslDouble0();
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF12 = new NslDouble1(1);
		NslDouble1 __tempacq_tam_wg_DNF_v1_1_1_src_DNF13 = new NslDouble1(1);
		double[] __tempacq_tam_wg_DNF_v1_1_1_src_DNF14 = new double[1];
		double[] __tempacq_tam_wg_DNF_v1_1_1_src_DNF15 = new double[1];

	/* GENERIC CONSTRUCTOR: */
	public DNF(String nslName, NslModule nslParent, int dim)
{
		super(nslName, nslParent);
		this.dim=dim;
		initSys();
		makeInstDNF(nslName, nslParent, dim);
	}

	public void makeInstDNF(String nslName, NslModule nslParent, int dim)
{ 
		Object[] nslArgs=new Object[]{dim};
		callFromConstructorTop(nslArgs);
		input = new NslDinDouble1("input", this, dim);
		output = new NslDoutDouble1("output", this, dim);
		weightKernel = new NslDouble1("weightKernel", this, 2*dim);
		w_excite = new NslDouble0("w_excite", this);
		sigma_w = new NslDouble0("sigma_w", this);
		w_inhibit = new NslDouble0("w_inhibit", this);
		h = new NslDouble0("h", this);
		u = new NslDouble1("u", this, dim);
		tau = new NslDouble0("tau", this);
		beta = new NslDouble0("beta", this);
		u_0 = new NslDouble0("u_0", this);
		q = new NslDouble0("q", this);
		noise = new NslDouble1("noise", this, dim);
		callFromConstructorBottom();
	}

	/******************************************************/
	/*                                                    */
	/* End of automatic declaration statements.           */
	/*                                                    */
	/******************************************************/


}//end DNF

