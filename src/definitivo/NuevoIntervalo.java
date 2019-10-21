package definitivo;



import matlabcontrol.MatlabProxy;
import umontreal.iro.lecuyer.simevents.Event;



public class NuevoIntervalo extends Event{

	private Datacenter2013 data;
	
	private int [] diferencias;
	public NuevoIntervalo(Datacenter2013 datacenter2013) {
		data=datacenter2013;
		diferencias=new int[4];
	}

	/**
	 * Retorna la posición del primer elemento positivo del vector de diferencias
	 */
	public int darPrimerPositivo()
	{
		int r=-1;
		boolean enc=false;
		for(int i=0;i<diferencias.length&&enc==false;i++)
		{
			int d=diferencias[i];
			if(d>0)
			{
				r=i;
				enc=true;
			}
		}
		return r;
	}
	/**
	 * Retorna la posición del primer elemento negativo del vector de diferencias
	 */
	public int darPrimerNegativo()
	{
		int r=-1;
		boolean enc=false;
		for(int i=0;i<diferencias.length&&enc==false;i++)
		{
			int d=diferencias[i];
			if(d<0)
			{
				r=i;
				enc=true;
			}
		}
		return r;
	}	
	@Override
	public void actions() {
		// TODO Auto-generated method stub
		

		
		
		try {
			data.imprimirResultados();
			data.AumentarInt();
			data.actualizarValores();
			System.out.println(data.darnWork()[data.darIntervaloActual()-1]);
			double[]resultado=data.pronosticarParametrosOptimizacion();
			double []arribos=new double[data.darNumClientes()];
			for(int i=0;i<data.darNumClientes();i++)
			{
				arribos[i]=resultado[i];
			}
			double [] opti=data.ejecutarOptimizacion(arribos, (Datacenter2013.cap_solar*resultado[data.darNumClientes()])+(Datacenter2013.cap_viento*resultado[data.darNumClientes()+1]), resultado[data.darNumClientes()+2], resultado[data.darNumClientes()+3], resultado[data.darNumClientes()+4], data.darnWork()[data.darIntervaloActual()-1]);
			int nWeb=(int)opti[1];
			int nApp=(int)opti[2];
			int nDB=(int)opti[3];
			data.actualizarnServ(nWeb, nApp, nDB);
			int apagados=Datacenter2013.TOTAL_SERVIDORES-((int)opti[1]+(int)opti[2]+(int)opti[3]);
			data.actualizarnWork((int)opti[6]);
			

			double [] tav=new double[data.darNumClientes()];
			for(int i=0;i<data.darNumClientes();i++)
			{
				tav[i]=opti[10+(i+1)];
			}
			data.actualizarResultados(opti[0],opti[7],opti[8],opti[9],opti[10],tav);
			
			int n=opti.length;
			MatlabProxy com=data.darCom();
		
			com.eval("A=[A,"+(opti[n-1])+"];");
			
			if(data.darEsc()==4)
			{
				data.aumentarVpr(opti[n-3]);
				data.aumentarVpnr(opti[n-2]);
			}
			if(data.darEsc()==6)
			{
				data.actualizarTsol(opti[n-2]);
			}
			int difWeb=data.darWeb().darNumServidores()-nWeb;
			int difApp=data.darApp().darNumServidores()-nApp;
			int difDB=data.darDB().darNumServidores()-nDB;
		
			int difOff=data.getServidoresApagados()-apagados;
			diferencias[0]=difWeb;
			diferencias[1]=difApp;
			diferencias[2]=difDB;
			diferencias[3]=difOff;
			while(diferencias[0]!=0 || diferencias[1]!=0 || diferencias[2]!=0 || diferencias[3]!=0)
			{
				int p1=darPrimerPositivo();
				int p2=darPrimerNegativo();
			
		
				int pos=diferencias[p1];
				int neg=diferencias[p2];
				
				int a=Math.min(pos,Math.abs(neg));
				int eliminados=0;
				if(p1!=3)
				{
					Cluster2013 actual=data.darCluster(p1+1);
	
					
						actual.darCluster().eliminarServidores(a);
						new TiempoMuerto(data,p2+1,a).schedule(Datacenter2013.darTiempoMuerto());
						eliminados=a;
				
				}
				else
				{
					int sa=data.getServidoresApagados();
					data.setServidoresApagados(sa-a);
					new TiempoMuerto(data,p2+1,a).schedule(Datacenter2013.darTiempoMuerto());
					eliminados=a;
				}
				
				pos-=eliminados;
				neg+=eliminados;
				diferencias[p1]=pos;
				diferencias[p2]=neg;
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(data.darIntervaloActual()<data.darNumInt())
		{
			new NuevoIntervalo(data).schedule(Datacenter2013.horizonte_Sim/data.darNumInt());
		}
	}

}
