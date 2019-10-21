package definitivo;



import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Sim;

public class LlegadaSistema extends Event{

	private Datacenter2013 data;
	
	private int cliente;
	public LlegadaSistema(Datacenter2013 d2013,int elCliente) {
		data=d2013;
		cliente=elCliente;
	}

	@Override
	public void actions() {
		                        //
		int clienteF=data.lanzardadoCliente();
		new LlegadaSistema(data,clienteF).schedule(data.darTiempoLlegada(clienteF));
		ColaSistema2013 desp=data.darDespachador();
		Solicitud s= new Solicitud(Sim.time(),desp.darTiempoS(),cliente);
		desp.actualizarLlegadas();
		if(desp.darOcupados()==desp.darNumServidores())
		{
			desp.agregarACola(s);
			desp.actualizarEstadsticasTamanio(desp.darTamanioCola());
			
			
		}
		else if(desp.darOcupados()<desp.darNumServidores())
		{
			//desp.actualizarEstadisticasTiempos(0.0);
			desp.actualizarOcupados();
			new LlegadaCluster(data,s).schedule(s.darTiempoS());
		}
		
	}

}
