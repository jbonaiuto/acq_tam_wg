package acq_tam_wg.MotivationalSchema.v1_1_2.src;
nslImport acq_tam_wg.Hypothalamus.v1_1_1.src.*;
nslImport acq_tam_wg.WGCritic.v1_1_2.src.*;
nslImport acq_tam_wg.TAMCritic.v1_1_2.src.*;

nslJavaModule MotivationalSchema(int maxNodes, int numDrives, int angleRepSize, double[] d_min, double[] d_max, double maxDesirability){

//NSL Version: 3_0_1
//Sif Version: 9
//libNickName: acq_tam_wg
//moduleName:  MotivationalSchema
//versionName: 1_1_2


//variables 
public acq_tam_wg.Hypothalamus.v1_1_1.src.Hypothalamus hypothalamus(numDrives, d_min, d_max); // 
public acq_tam_wg.WGCritic.v1_1_2.src.WGCritic wgCritic(maxNodes, numDrives, d_min, d_max, maxDesirability); // 
public acq_tam_wg.TAMCritic.v1_1_2.src.TAMCritic tamCritic(maxNodes, numDrives, d_min, d_max, maxDesirability); // 
public NslDinDouble1 reductions(numDrives); // 
public NslDinDouble1 incentives(numDrives); // 
public NslDinInt0 currentNodeId(); // 
public NslDinDouble0 lastDist(); // 
public NslDoutDouble1 motivationalState(numDrives); // 
public NslDoutDouble1 wgReinforcement(numDrives); // 
public NslDoutDouble1 tamReinforcement(numDrives); // 

//methods 

public void makeConn(){
    nslRelabel(currentNodeId,wgCritic.currentNodeIdIn);
    nslRelabel(currentNodeId,tamCritic.currentNodeIdIn);
    nslRelabel(lastDist,wgCritic.lastNodeDist);
    nslRelabel(incentives,hypothalamus.incentives);
    nslRelabel(reductions,hypothalamus.reductions);
    nslRelabel(reductions,wgCritic.rewards);
    nslRelabel(reductions,tamCritic.rewards);
    nslRelabel(hypothalamus.drives,motivationalState);
    nslConnect(hypothalamus.drives,wgCritic.motivations);
    nslConnect(hypothalamus.drives,tamCritic.motivations);
    nslRelabel(wgCritic.reinforcement,wgReinforcement);
    nslRelabel(tamCritic.reinforcement,tamReinforcement);
}
}//end MotivationalSchema

