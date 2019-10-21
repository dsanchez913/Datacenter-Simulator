package definitivo;

public class Solicitud {
	//Atributos
	
		/**
		 * tiempo de llegada al sistema
		 */
		private double tiempoLlegada;
		
		/**
		 * tiempo de llegada a cada etapa del sistema
		 */
		private double tiempoLlegadaIntermedio;
		
		/**
		 * tiempo de servicio en cada cola
		 */
		
		private double tiempoServicio;
		
		
		/**
		 * Representa el cliente que ha enviado esta solicitud
		 */
		private int cliente;
		//Constructor
		
		/**
		 * Método constructor del paquete
		 * @param tLleg
		 * @param tS
		 */
		public Solicitud(double tLleg, double tS,int elCliente)
		{
			tiempoLlegada=tLleg;
			tiempoServicio=tS;
			tiempoLlegadaIntermedio=tLleg;
			cliente=elCliente;
		}
		
		//Métodos 
		
		public double darTiempoLl()
		{
			return tiempoLlegada;
		}
		
		public double darTiempoS()
		{
			return tiempoServicio;
		}
		
		public void cambiarTLlegadaIntermedio(double tLleg)
		{
			tiempoLlegadaIntermedio=tLleg;
		}
		
		public double darTiempoLlInt()
		{
			return tiempoLlegadaIntermedio;
		}
		
		public void cambiarTServicio(double tServ)
		{
			tiempoServicio=tServ;
		}

		public int darCliente()
		{
			return cliente;
		}
		
}
