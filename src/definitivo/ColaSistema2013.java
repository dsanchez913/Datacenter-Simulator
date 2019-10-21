package definitivo;

import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Accumulate;
import umontreal.iro.lecuyer.stat.Tally;


/**
 * Clase que representa una cola M/M/K con K>=1
 * @author David Sanchez
 *
 */
public class ColaSistema2013 {
	//***************************
	// Atributos ****************
	//***************************

	/**
	 * numero de servidores
	 */
	
	private int numServidores;
	
	/**
	 * Generador aleatorio de tiempos de servicio exponenciales
	 */
	private RandomVariateGen genServicios;
	
	/**
	 * La cola del sistema
	 */
	private LinkedList<Solicitud> espera;
	
	/**
	 * Estructura que calcula el tiempo promedio en el sistema
	 */
	private Tally estEspera;
	
	/**
	 * Estructura que calcula el número promedio de entidades en cola
	 */
	private Accumulate tamaño;
	
	/**
	 * número de servidores ocuapdos
	 */
	private int ocupados;
	
	/**
	 * contador del número de llegadas al sistema
	 */
	private int llegadas;
	
	
	//***************************
	// Constructor ****************
	//***************************
	
	/**
	 * Constructor de la clase
	 * @param tServicios
	 * @param numServidoresf
	 */
	public ColaSistema2013( double tServicios, int numServidoresf)
	
	{
		llegadas=0;
		numServidores=numServidoresf;
		genServicios= new ExponentialGen(new MRG32k3a(),tServicios);
		estEspera=new Tally("Tiempos de espera");
		tamaño=new Accumulate("Tamaño de la cola");
		espera= new LinkedList<Solicitud>();
		ocupados=0;
		
	}
	
	
	//***************************
	// Métodos *******************
	//*****************************
	/**
	 * Actualiza el número de llegadas al sistema
	 */
	public void actualizarLlegadas()
	{
		llegadas++;
	}
	
	/**
	 * retorna el númeor de llegadas al sistema
	 * @return
	 */
	public int darLlegadas()
	{
		return llegadas;
	}
	
	/**
	 * Retorna el tiempo de servicio para el siguiente paquete a servir
	 * @return
	 */
	public double darTiempoS(){
		return genServicios.nextDouble();
	}
	
	/**
	 * Retorna el número de servidores ocupados
	 * @return
	 */
	public int darOcupados()
	{
		return ocupados;
	}
	
	/**
	 * Agrega un paquete p a la cola
	 * @param p
	 */
	public void agregarACola(Solicitud s)
	{
		espera.addLast(s);
	}
	
	/**
	 * Agrega un paquete p a la cola, pero en la primera posición de la misma
	 * @param p
	 */
	public void agregarAColadePrimero(Solicitud s)
	{
		espera.addFirst(s);
	}
	
	
	/**
	 * Retorna el tamaño de la cola
	 * @return
	 */
	public int darTamanioCola()
	{
		return espera.size();
	}
	
	/**
	 * Actualiza las estadisticas de tamaño
	 * @param t
	 */
	public void actualizarEstadsticasTamanio(int t)
	{
		tamaño.update(t);
	}
	
	/**
	 * Actualiza las estadisticas de tiempo promedio en el sistema
	 * @param t
	 */
	public void actualizarEstadisticasTiempos(double t)
	{
		estEspera.add(t);
	}
	
	/**
	 * Retorna el primer paquete en cola
	 * @return
	 */
	public Solicitud darPrimeroenCola()
	{
		return espera.removeFirst();
	}
	
	/**
	 * Aumenta el número de servidores ocupados
	 */

	public void actualizarOcupados() {
		ocupados++;
		
	}
	/**
	 * disminuye el número de servidores ocupados
	 */

	public void rOcupados() {
		
	ocupados--;	
	}
	/**
	 * Retorna el númeor de servidores de la cola
	 */
	public int darNumServidores()
	{
		return numServidores;
	}
	/**
	 * Cambia el número de servidores de la cola
	 */
	public void cambiarNumServidores(int n)
	{
		numServidores=n;
	}
	
	/**
	 * Retorna el estadístico del tiempo promedio en el sistema
	 * @return
	 */
	public Tally darEspera()
	{
		return estEspera;
	}
	
	/**
	 * Retorna el estadístico del númeor promedio de entidades en el sistema
	 * @return
	 */
	public Accumulate darTamanio()
	{
		return tamaño;
	}

	/**
	 * Renueva el Tally
	 */
	
	public void renovarTally()
	{
		estEspera= new Tally("Tiempos de espera");
	}
	/**
	 * Renueva el Accumulate
	 */

	public void renovarAcc(){
		tamaño=new Accumulate("Tamaño de la cola");
	}
	public int darServidoresLibres()
	{
		return numServidores-ocupados;
	}


	public void eliminarServidores(int aEliminar) {
		int a=numServidores;
		numServidores=a-aEliminar;
		
	}


	public void agregarServidores(int numNuevos) {
		// TODO Auto-generated method stub
		int a=numServidores;
		numServidores=a+numNuevos;
	}
	
	

	
}
