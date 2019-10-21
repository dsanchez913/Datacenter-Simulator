package definitivo;



import umontreal.iro.lecuyer.simevents.Event;

public class TiempoMuerto extends Event{

	private int clusterSig;

	private int numNuevos;
	
	private Datacenter2013 data;
	
	public TiempoMuerto(Datacenter2013 data2, int i, int nuevos) {
		clusterSig=i;
		numNuevos=nuevos;
		data=data2;
	}
	@Override
	public void actions() {
		if(clusterSig==4)
		{
			int sa=data.getServidoresApagados();
			data.setServidoresApagados(sa+numNuevos);
		}
		else
		{
			Cluster2013 c=data.darCluster(clusterSig);
			c.darCluster().agregarServidores(numNuevos);
		}
		
	}

}
