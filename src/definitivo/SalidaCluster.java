package definitivo;



import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Sim;

public class SalidaCluster extends Event{

	private Datacenter2013 data;
	
	private Cluster2013 cl;
	
	private Solicitud s;
	
	public SalidaCluster(Datacenter2013 d, Cluster2013 c, Solicitud sol) {
		// TODO Auto-generated constructor stub
		data=d;
		cl=c;
		s=sol;
	}
	//*******************************************
	//Métodos ***********************************
	//*******************************************
	
	public void actions() {
		// TODO Auto-generated method stub
		ColaSistema2013 cluster=cl.darCluster();
		cluster.rOcupados();
		cluster.actualizarEstadisticasTiempos(Sim.time()-s.darTiempoLlInt());
		int idCluster=cl.daID();
		double[][][]m=data.darMatrices();
		int cliente=s.darCliente();
		int destino=cl.darDestinoSolicitud(m[cliente-1][idCluster][0],m[cliente-1][idCluster][1]);
		
		if(idCluster==Datacenter2013.WEB)
		{
			if (destino==1)
			{
				Cluster2013 dest=data.darApp();
				llegadaSolicitud(dest, s);
				
			}
			else if(destino ==2)
			{
				Cluster2013 dest=data.darDB();
				llegadaSolicitud(dest, s);
			
			}
			else
			{
				data.actualizarEstadisticasTiempos(Sim.time()-s.darTiempoLl(),s.darCliente());
			
				
			}
		}
		else if(idCluster==Datacenter2013.APP)
		{
			if (destino==1)
			{
				Cluster2013 dest=data.darWeb();
				llegadaSolicitud(dest, s);
				
				
			}
			else if(destino ==2)
			{
				Cluster2013 dest=data.darDB();
				llegadaSolicitud(dest, s);
				
				
			}
			else
			{
				data.actualizarEstadisticasTiempos(Sim.time()-s.darTiempoLl(),s.darCliente());
				
				
				
			}
		}
		else
		{
			if (destino==1)
			{
				Cluster2013 dest=data.darWeb();
				llegadaSolicitud(dest, s);
				
			}
			else if(destino ==2)
			{
				Cluster2013 dest=data.darApp();
				llegadaSolicitud(dest, s);
				
			}
			else
			{
				data.actualizarEstadisticasTiempos(Sim.time()-s.darTiempoLl(),s.darCliente());
			
				
				
			}
		}
		if(cluster.darTamanioCola()>0)
		{
			Solicitud sn=cluster.darPrimeroenCola();
			cluster.actualizarEstadsticasTamanio(cluster.darTamanioCola());
			//despacho.actualizarEstadisticasTiempos(Sim.time()-pa.darTiempoLlInt());
			cluster.actualizarOcupados();
			
			new SalidaCluster(data,cl, sn).schedule(sn.darTiempoS());
		}
		
	}
	public void llegadaSolicitud(Cluster2013 dest,Solicitud sol)
	{
		
		ColaSistema2013 cluster=dest.darCluster();
		sol.cambiarTLlegadaIntermedio(Sim.time());
		sol.cambiarTServicio(cluster.darTiempoS());
		
		
		if(cluster.darOcupados()==cluster.darNumServidores())
		{
			cluster.agregarACola(sol);
			cluster.actualizarEstadsticasTamanio(cluster.darTamanioCola());
		}
		else if(cluster.darOcupados()<cluster.darNumServidores())
		{
		
			cluster.actualizarOcupados();
			
			new SalidaCluster(data,dest, sol).schedule(sol.darTiempoS());
		}
	}
	

}
