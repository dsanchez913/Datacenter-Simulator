package definitivo;



/**
 * Clase que representa un cluster que contiene cierto número de servidores
 * @author David Sanchez
 *
 */
public class Cluster2013 {

	public static final double miu=0.0038;
	
	//***************************
	// Atributos ****************
	//***************************
	
	/**
	 * El conjunto de linea de espera y servidores del cluster, modelado por un sistema M/M/K
	 */
	private ColaSistema2013 cluster;

	/**
	 * Identificacion del cluster
	 */
	private int ID;
	
	/**
	 * probabilidad de ir a un cluster	
	 */

	//***************************
	// Constructor ****************
	//***************************
	
	/**
	 * Método constructor del cluster
	 * @param nServidores
	 */
	public Cluster2013 (int nID,int nServidores)
	{
		ID=nID;
		cluster=new ColaSistema2013(miu,nServidores);
	}
	
	//***************************
	// Métodos ****************
	//***************************
	
	public ColaSistema2013 darCluster()
	{
		return cluster;
	}
	/**
	 * Retorna el destino de la solicitud que sale del cluster:
	 * 
	 */
	public int darDestinoSolicitud(double p1,double p2)
	{
		double x=Datacenter2013.lanzarDado();
		int destino=0;
		if(x>=0&&x<=p1)
		{
			destino=1;
		}
		else if(x>p1&&x<=p1+p2)
		{
			destino=2;
		}
		else
		{
			destino=3;
		}
		return destino;
	}
	
	/**
	 * Retorna el numero de servidores en el cluster
	 */
	public int darNumServidores()
	{
		return cluster.darNumServidores();
	}
	
	public int daID()
	{
		return ID;
	}
	
}
