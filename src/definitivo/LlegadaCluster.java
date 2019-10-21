package definitivo;



import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Sim;

public class LlegadaCluster extends Event {

	private Datacenter2013 data;
	
	private Solicitud s;
	public LlegadaCluster(Datacenter2013 d, Solicitud so) {
		data=d;
		s=so;
	}

	@Override
	public void actions() {
		
ColaSistema2013 despacho=data.darDespachador();
		
		despacho.rOcupados();
		Cluster2013 c=darClusterLlegada();
		ColaSistema2013 cluster=c.darCluster();
		despacho.actualizarEstadisticasTiempos(Sim.time()-s.darTiempoLlInt());
		s.cambiarTLlegadaIntermedio(Sim.time());
		s.cambiarTServicio(cluster.darTiempoS());
		cluster.actualizarLlegadas();
		
		//Se hace el ingreso del paquete al cluster
		
		if(cluster.darOcupados()==cluster.darNumServidores())
		{
			cluster.agregarACola(s);
			cluster.actualizarEstadsticasTamanio(cluster.darTamanioCola());
		}
		else if(cluster.darOcupados()<cluster.darNumServidores())
		{
			//entradaCluster.actualizarEstadisticasTiempos(0.0);
			cluster.actualizarOcupados();
			new SalidaCluster(data, c,s).schedule(s.darTiempoS());
		}
		
		if(despacho.darTamanioCola()>0)
		{
			Solicitud sn=despacho.darPrimeroenCola();
			despacho.actualizarEstadsticasTamanio(despacho.darTamanioCola());
			//despacho.actualizarEstadisticasTiempos(Sim.time()-pa.darTiempoLlInt());
			despacho.actualizarOcupados();
			
			new LlegadaCluster(data,sn).schedule(sn.darTiempoS());
		}
	}
	/**
	 * Define a que cluster va el paquete que llegó al sistema
	 */
	private Cluster2013 darClusterLlegada()
	{
		double x = Datacenter2013.lanzarDado();
		double[][][]m=data.darMatrices();
		int cliente=s.darCliente();
		double a= m[cliente-1][0][0];
		double b=m[cliente-1][0][1];
		double c= m[cliente-1][0][2];
		
		Cluster2013 destino=null;
		
		if(x>=0.0&&x<=a)
		{
			destino=data.darWeb();
		}
		else if(x>a&&x<=a+b)
		{
			destino=data.darApp();
		}
		else
		{
			destino=data.darDB();
		}
		return destino;
	}
	
}
