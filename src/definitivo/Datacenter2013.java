package definitivo;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.swing.JOptionPane;


import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Sim;
import umontreal.iro.lecuyer.stat.Tally;


/**
 * Clase principal de la simulaci�n que modela un Datacenter con 3 clusters 
 * de servidores (web, database, aplicaciones)
 * 
 * @author David Sanchez
 *
 */
public class Datacenter2013 {

	public static final int WEB=1;
	public static final int APP=2;
	public static final int DATABASE=3;
		
	public static final double servicio_despachador=100.0;
	
	public static final double horizonte_Sim=86400.0;
	
	public static final double cap_solar=1000;
	
	public static final double cap_viento= 250;
	
	public static final int TOTAL_SERVIDORES=500;
	

	
	//Path to YALMIP --
	public static final String path_YALMIP="/Users/dsanchez/Dropbox/YALMIP-master";
	
	
	//******************************************
	//Atributos *********************************
	//*******************************************

	/**
	 * Despachador inicial
	 */
	
	private ColaSistema2013 despachador;
	/**
	 * Cluster web
	 */
	
	private Cluster2013 web;
	
	/**
	 * Cluster de aplicacion
	 */
	
	private Cluster2013 app;
	
	/**
	 * Cluster de base de datos
	 */
	
	private Cluster2013 dataB;
	
	/**
	 * Arreglo que contiene los generadores exponenciales para cada cliente
	 */
	private  RandomVariateGen[] genLlegadas;
	
	/**
	 * Generador de n�meros aletorios que siguen una distribuci�n uniforme
	 */
	private static final RandomVariateGen uniforme=new UniformGen(new MRG32k3a(),0.0,1.0);
	/**
	 * Generador de n�meros aletorios que siguen una distribuci�n exponencial
	 */
	private static final RandomVariateGen tMuerto=new ExponentialGen(new MRG32k3a(),10000);
	
	/**
	 * Estad�stico del tiempo de espera promedio en el datacenter
	 */
	private Tally []estEspera;

	
	/**
	 * ArrayList con las probabilidades de ocurrencia en la llegada de solicitudes 
	 * para cada cliente
	 */
	private double[] probOcur;
	
	/**
	 * Matriz con los datos de alpha y gamma para el pron�stico de los aribos por cliente
	 */
	private double [][] clientesPronostico; 
	/**
	 * Arreglo que contiene las matrices de transici�n dependiendo del cliente
	 */
	private double[][][] matrices;
	/**
	 * El n�mero de servidores que se encuentran apagados
	 */
	
	private int servidoresApagados;

	/**
	 * El intervalo actual de la simulaci�n
	 */
	private int intervaloActual;
	
	/**
	 * Atributo estatico que representa el flujo de datos de salida para las estadisticas de la simulaci�n en cada 
	 */
	private static PrintWriter[] out;
	/**
	 * Numero de intervalos en la simulaci�n
	 */
	private int numIntervalos;
	
	/**
	 * Numero de clientes en el datacenter
	 */
	
	private int numClientes;
	
	/**
	 * Comunicacion con Matlab
	 */
	private MatlabProxy com;
	
	// - - - Atributos relacionados con los resultados obtenidos en la simulaci�n y la optimizaci�n
	//----------- ----      ----    -------
	/**
	 * N�mero de servidores trabajando en un intervalo de tiempo
	 */
	private int[] nWork;
	
	/**
	 * Valor te�rico de la funci�n objetivo en cada intervalo de tiempo
	 */
	private double [] fObjTeo;
	
	/**
	 * Valor de la funci�n objetivo al realizar la simulaci�n
	 */
	
	private double [] fObjSim;
	
	/**
	 * Valor del tiempo promedio de respuesta (tanto te�rico como simulado)
	 * [teorico o sim] [cliente] [intervalo]
	 */
	private double [][][] tiempoAv;
	
	/**
	 * Energ�a usada de la red el�ctrica
	 */
	private double [] xGrid;
	
	/**
	 * Energ�a renovable vendida
	 */
	private double [] Grs;
	/**
	 * Energ�a no renovable vendida
	 */
	private double [] Gnrs;
	
	/**
	 * Energ�a no renovable usada en el datacenter
	 */
	private double [] Gnr;
	
	/**
	 * N�mero de servidores usados en cada cluster, en cada intervalo
	 */
	
	private double[][]nServs;
	
	/**
	 * Para el escenario 4 - almacena el promedio de las ganancias obtenidas por vender energia 
	 * renovable
	 */
	private double Vpr;
	
	/**
	 * Para el escenario 4 - almacena el promedio de las ganancias obtenidas por vender energia 
	 * no renovable
	 */
	private double Vpnr;
	
	
	/**
	 * indPr
	 */
	
	private double indPr;
	
	/**
	 * indPr
	 */
	
	private double indPnr;
	
	/**
	 * escenario
	 */
	
	private int esc;
	
	/**
	 * Para el escenario 6
	 * tiempo de soluci�n del algoritmo de optmizaci�n
	 */
	
	private double [] tiempoSolucion;
//*******************************************
//Constructor *******************************
//*******************************************

public Datacenter2013(int escN, double indPrN, double indPnrN) throws Exception
{
	esc=escN;
	intervaloActual=1;
	
	BufferedReader lector = new BufferedReader( new FileReader( new File("./parametros_entrada/prueba_escenario_base.txt")) );
	
	String linea=lector.readLine();
	numIntervalos=Integer.parseInt(linea);
	
	fObjTeo=new double[numIntervalos];
	fObjSim=new double[numIntervalos];
	
	xGrid= new double[numIntervalos];
	Grs= new double[numIntervalos];
	Gnrs= new double[numIntervalos];
	Gnr= new double[numIntervalos];
	nServs= new double[3][numIntervalos];
	
	tiempoSolucion= new double [numIntervalos];
	
	linea=lector.readLine();
	numClientes=Integer.parseInt(linea);
	tiempoAv= new double[2][numClientes][numIntervalos];
	clientesPronostico=new double[numClientes][2];
	for (int i=0;i<numClientes;i++)
	{
		linea=lector.readLine();
		String[] pp=linea.split(" ");
		clientesPronostico[i][0]=Double.parseDouble(pp[0]);
		clientesPronostico[i][1]=Double.parseDouble(pp[1]);
	}
	out = new PrintWriter[numClientes];
	

	
	linea=lector.readLine();
	String[] probs=linea.split(" ");
	probOcur=new double[numClientes];
	estEspera=new Tally[numClientes];
	for(int i=0;i<numClientes;i++)
	{
		probOcur[i]=Double.parseDouble(probs[i]);
		estEspera[i]=new Tally("Tiempo de espera en el sistema para el cliente "+(i+1));
		out[i]=new PrintWriter(new File("./resultados/cliente_"+(i+1)+".txt"));
	}
	
	
	
	matrices=new double[numClientes][4][3];
	for (int i=0;i<numClientes;i++)
	{
		for(int j=0;j<4;j++)
		{
			linea=lector.readLine();
			String[] pr=linea.split(" ");
			for(int k=0;k<3;k++)
			{
				matrices[i][j][k]=Double.parseDouble(pr[k]);
			}
		}
	}
	despachador= new ColaSistema2013(Datacenter2013.servicio_despachador,1);

	lector.close();
	
	// Matlab connection
	
	//Laptop David
	MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder().setMatlabLocation("/Applications/Matlab.app/bin/matlab").setMatlabStartingDirectory(new File("./Java_Matlab/escenario_base")).setUsePreviouslyControlledSession(true).build();
	
    MatlabProxyFactory factory = new MatlabProxyFactory(options);
    com=factory.getProxy();
    com.eval("clear all;clc;");
    com.eval("addpath(genpath('"+Datacenter2013.path_YALMIP+"'));");
    com.feval("load", "datos_prueba_opti.mat");
    com.eval("Ns="+Datacenter2013.TOTAL_SERVIDORES+";");
    
	com.feval("load", "Gr_solar.mat");
	com.eval("datos_Gr_solar=Gr_solar");
	
	com.feval("load", "Gr_viento.mat");
	com.eval("datos_Gr_viento=Gr_viento");
	
	com.feval("load", "Pr.mat");

	
	com.feval("load", "Pnr.mat");

	
	com.feval("load", "Cgrid.mat");
	com.eval("datos_Cgrid=Cgrid");
    
    if(esc==4)
    {
    	Vpr=0;
    	Vpnr=0;
    	indPr=indPrN;
    	indPnr=indPnrN;
    	
    	com.eval("Pr=Cgrid*(1-"+indPr+");");
    	com.eval("Pnr=Cgrid*(1-"+indPnr+");");
    }
	com.eval("datos_Pr=Pr");
	com.eval("datos_Pnr=Pnr");
    double [] resultado=pronosticarParametrosOptimizacion();
    
    nWork=new int[numIntervalos+1];
    nWork[0]=0;
	genLlegadas=new RandomVariateGen[numClientes];
	for(int i=0;i<numClientes;i++)
	{
		genLlegadas[i]=new ExponentialGen(new MRG32k3a(),((double[])com.getVariable("c"+(i+1)+"("+(numIntervalos+intervaloActual)+")"))[0]);
	}
	double []arribos=new double[numClientes];
	for(int i=0;i<numClientes;i++)
	{
		arribos[i]=resultado[i];
	}
	double [] opti=ejecutarOptimizacion(arribos, (Datacenter2013.cap_solar*resultado[numClientes])+(Datacenter2013.cap_viento*resultado[numClientes+1]), resultado[numClientes+2], resultado[numClientes+3], resultado[numClientes+4], nWork[0]);
	web=new Cluster2013(1,(int)opti[1]);
	nServs[0][intervaloActual-1]=(int)opti[1];
	app=new Cluster2013(2,(int)opti[2]);
	nServs[1][intervaloActual-1]=(int)opti[2];
	dataB=new Cluster2013(3,(int)opti[3]);
	nServs[2][intervaloActual-1]=(int)opti[3];
	xGrid[intervaloActual-1]=opti[7];
	Gnr[intervaloActual-1]=opti[8];
	Gnrs[intervaloActual-1]=opti[9];
	Grs[intervaloActual-1]=opti[10];
	
	for(int i=1;i<=numClientes;i++)
	{
		tiempoAv[0][i-1][intervaloActual-1]=opti[10+i]/60;
	}
	fObjTeo[intervaloActual-1]=opti[0];
	actualizarnWork((int)opti[6]);
	servidoresApagados=Datacenter2013.TOTAL_SERVIDORES-((int)opti[1]+(int)opti[2]+(int)opti[3]);

	int n=opti.length;
	com.eval("A=[];");
	com.eval("A=[A,"+(opti[n-1])+"];");
	if(esc==4)
	{
		aumentarVpr(opti[n-3]);
		aumentarVpnr(opti[n-2]);
		
	}
	if(esc==6)
	{
		tiempoSolucion[intervaloActual-1]=opti[n-2];
	}
}

//*******************************************
//M�todos ***********************************
//*******************************************
/**
 * Realiza el pron�stico de los par�metros que entrar�n en el algoritmo de optimizaci�n
 * por ahora s�lo se est� pronosticando una traza de prueba!!!
 * @throws MatlabInvocationException 
 */
public double[] pronosticarParametrosOptimizacion() throws MatlabInvocationException {
	
	double [] retorno=new double[numClientes+5];
	
	if(intervaloActual==1)
	{
		//Creaci�n de los par�metros para la prediccion de una variable
		com.eval("s="+numIntervalos+";");
		double dat=1;
		com.eval("mc=mc.*"+dat+";");
		for(int i=1;i<=numClientes;i++)
		{
			com.feval("load", "c"+i+".mat");
			com.eval("c"+i+"=c"+i+".*"+dat+";");
		
			com.eval("datos_c"+i+"=c"+i+";");
			
			//s es el tama�o de la estacionalidad!!! (�el cual deberia ser el num de Intervalos?)
			
			com.eval("alpha_c"+i+"="+clientesPronostico[i-1][0]+";gamma_c"+i+"="+clientesPronostico[i-1][1]+";t_c"+i+"=s;x_init_c"+i+"=datos_c"+i+"(1:s);");
			
			//Ahora viene la predicci�n del primer intervalo
			
			//Inicializaci�n del m�todo
			com.eval("[L_c"+i+",S_c"+i+"]=init(s,x_init_c"+i+")");
			
			//Primera predicci�n
			
			com.eval("F_c"+i+"=predict_1(L_c"+i+",S_c"+i+",t_c"+i+",[],s);t_c"+i+"=t_c"+i+"+1");
		
			retorno[i-1]=((double[]) com.getVariable("F_c"+i+"(1)"))[0];
		}
		
	
		
		//s es el tama�o de la estacionalidad!!! 
		
		com.eval("alpha_Gr_solar=0.5;gamma_Gr_solar=0.5;t_Gr_solar=s;x_init_Gr_solar=datos_Gr_solar(1:s);");
		com.eval("alpha_Gr_viento=0.5;gamma_Gr_viento=0.5;t_Gr_viento=s;x_init_Gr_viento=datos_Gr_viento(1:s);");
		com.eval("alpha_Pr=0.5;gamma_Pr=0.5;t_Pr=s;x_init_Pr=datos_Pr(1:s);");
		com.eval("alpha_Pnr=0.5;gamma_Pnr=0.5;t_Pnr=s;x_init_Pnr=datos_Pnr(1:s);");
		com.eval("alpha_Cgrid=0.5;gamma_Cgrid=0.5;t_Cgrid=s;x_init_Cgrid=datos_Cgrid(1:s);");
		
		//Ahora viene la predicci�n del primer intervalo
		
		//Inicializaci�n del m�todo
		com.eval("[L_Gr_solar,S_Gr_solar]=init(s,x_init_Gr_solar)");
		com.eval("[L_Gr_viento,S_Gr_viento]=init(s,x_init_Gr_viento)");
		com.eval("[L_Pr,S_Pr]=init(s,x_init_Pr)");
		com.eval("[L_Pnr,S_Pnr]=init(s,x_init_Pnr)");
		com.eval("[L_Cgrid,S_Cgrid]=init(s,x_init_Cgrid)");
		
		//Primera predicci�n
		com.eval("F_Gr_solar=predict_1(L_Gr_solar,S_Gr_solar,t_Gr_solar,[],s);t_Gr_solar=t_Gr_solar+1");
		retorno[numClientes]=((double[]) com.getVariable("F_Gr_solar(1)"))[0];
		com.eval("F_Gr_viento=predict_1(L_Gr_viento,S_Gr_viento,t_Gr_viento,[],s);t_Gr_viento=t_Gr_viento+1");
		retorno[numClientes+1]=((double[]) com.getVariable("F_Gr_viento(1)"))[0];
		com.eval("F_Pr=predict_1(L_Pr,S_Pr,t_Pr,[],s);t_Pr=t_Pr+1");
		retorno[numClientes+2]=((double[]) com.getVariable("F_Pr(1)"))[0];
		com.eval("F_Pnr=predict_1(L_Pnr,S_Pnr,t_Pnr,[],s);t_Pnr=t_Pnr+1");
		retorno[numClientes+3]=((double[]) com.getVariable("F_Pnr(1)"))[0];
		com.eval("F_Cgrid=predict_1(L_Cgrid,S_Cgrid,t_Cgrid,[],s);t_Cgrid=t_Cgrid+1");
		retorno[numClientes+4]=((double[]) com.getVariable("F_Cgrid(1)"))[0];
	}
	else
	{
		for(int i=1;i<=numClientes;i++)
		{
			com.eval(" [L_c"+i+",S_c"+i+"]=actual(t_c"+i+",L_c"+i+",S_c"+i+",alpha_c"+i+",gamma_c"+i+",datos_c"+i+"(t_c"+i+"),s);F_c"+i+"=predict_1(L_c"+i+",S_c"+i+",t_c"+i+",F_c"+i+",s);t_c"+i+"=t_c"+i+"+1");
			retorno[i-1]=((double[]) com.getVariable("F_c"+i+"("+intervaloActual+")"))[0];
		}
		com.eval(" [L_Gr_solar,S_Gr_solar]=actual(t_Gr_solar,L_Gr_solar,S_Gr_solar,alpha_Gr_solar,gamma_Gr_solar,datos_Gr_solar(t_Gr_solar),s);F_Gr_solar=predict_1(L_Gr_solar,S_Gr_solar,t_Gr_solar,F_Gr_solar,s);t_Gr_solar=t_Gr_solar+1");
		retorno[numClientes]=((double[]) com.getVariable("F_Gr_solar("+intervaloActual+")"))[0];
		com.eval(" [L_Gr_viento,S_Gr_viento]=actual(t_Gr_viento,L_Gr_viento,S_Gr_viento,alpha_Gr_viento,gamma_Gr_viento,datos_Gr_viento(t_Gr_viento),s);F_Gr_viento=predict_1(L_Gr_viento,S_Gr_viento,t_Gr_viento,F_Gr_viento,s);t_Gr_viento=t_Gr_viento+1");
		retorno[numClientes+1]=((double[]) com.getVariable("F_Gr_viento("+intervaloActual+")"))[0];
		com.eval(" [L_Pr,S_Pr]=actual(t_Pr,L_Pr,S_Pr,alpha_Pr,gamma_Pr,datos_Pr(t_Pr),s);F_Pr=predict_1(L_Pr,S_Pr,t_Pr,F_Pr,s);t_Pr=t_Pr+1");
		retorno[numClientes+2]=((double[]) com.getVariable("F_Pr("+intervaloActual+")"))[0];
		com.eval(" [L_Pnr,S_Pnr]=actual(t_Pnr,L_Pnr,S_Pnr,alpha_Pnr,gamma_Pnr,datos_Pnr(t_Pnr),s);F_Pnr=predict_1(L_Pnr,S_Pnr,t_Pnr,F_Pnr,s);t_Pnr=t_Pnr+1");
		retorno[numClientes+3]=((double[]) com.getVariable("F_Pnr("+intervaloActual+")"))[0];
		com.eval(" [L_Cgrid,S_Cgrid]=actual(t_Cgrid,L_Cgrid,S_Cgrid,alpha_Cgrid,gamma_Cgrid,datos_Cgrid(t_Cgrid),s);F_Cgrid=predict_1(L_Cgrid,S_Cgrid,t_Cgrid,F_Cgrid,s);t_Cgrid=t_Cgrid+1");
		retorno[numClientes+4]=((double[]) com.getVariable("F_Cgrid("+intervaloActual+")"))[0];
		
	}
	return retorno;
}
/**
 * Retorna un n�mero entre 0 y 1 con distribuci�n uniforme
 * @return
 */
public static double lanzarDado()
{
	
	double a=uniforme.nextDouble();
	return a;
	
}
/**
 * Retorna la realizaci�n del tiempo muerto
 * @return
 */
public static double darTiempoMuerto()
{
	
	double a=tMuerto.nextDouble();
	return a;
	
}
/**
 * retorna el despachador
 */
public ColaSistema2013 darDespachador()
{
	return despachador;
}
/**
 * Actualiza las estadisitcas del tiempo promedio en el sistema
 * @param t
 */
public void actualizarEstadisticasTiempos(double t,int cliente)
{
	estEspera[cliente-1].add(t);
}
/**
 * Retorna el tiempo de llegada de una solicitud al sistema
 * @return
 */
public double darTiempoLlegada(int cliente)
{
	return genLlegadas[cliente-1].nextDouble();
}
/**
 * Retorna el estadistico del tiempo promedio en el sistema
 * @return
 */
public Tally[] darEspera()
{
	return estEspera;
}
/**
 * Retorna el cluster web
 */
public Cluster2013 darWeb()
{
	return web;
}

/**
 * Retorna el cluster app
 */
public Cluster2013 darApp()
{
	return app;
}
/**
 * Retorna el cluster web
 */
public Cluster2013 darDB()
{
	return dataB;
}
/**
 * Ejecuta la simulaci�n del comportamiento del datacenter
 * @param tiempoFinal
 */
public void simular(double tiempoFinal)
{
	Sim.init();
	new FinSim().schedule(tiempoFinal);
	int cliente=lanzardadoCliente();
	new LlegadaSistema(this,cliente).schedule(darTiempoLlegada(cliente));
	if(numIntervalos>1)
	{
	new NuevoIntervalo(this).schedule((Datacenter2013.horizonte_Sim/numIntervalos));
	}
	Sim.start();
}

public int lanzardadoCliente() {
	double x=Datacenter2013.lanzarDado();
	double s=0;
	int cliente=0;
	boolean enc=false;
	for(int i=0;i<numClientes&&enc==false;i++)
	{
		if(x>=s&&x<=s+probOcur[i])
		{
			cliente=i+1;
			enc=true;
		}
		else
		{
			s+=probOcur[i];
		}
	}
	return cliente;
}

	
	

/**
 * Imprime los resultados para el intervalo actual
 * @param out 
 * @throws Exception 
 */
public void imprimirResultados() throws Exception
{
	System.out.println("------|-------------|-----------|--------------|-----------------|-----------------------------|-------|");
	
	System.out.println("Tiempo promedio de respuesta --- Intervalo  "+intervaloActual);
	double [] t_av=new double[numClientes];
	for (int i=0;i<numClientes;i++)
	{
		System.out.println("-------------------------------------------------------------------------------------------------------");
		System.out.println("Datos para el cliente "+(i+1)+"-----------------------------");
	System.out.println(darEspera()[i].report());
	double[]intervalo=new double[2];
	double prob=0.95;
	darEspera()[i].confidenceIntervalNormal(prob, intervalo);
	double centro=intervalo[0];
	double radio=intervalo[1];
	double min=centro-radio;
	double max=centro+radio;
	System.out.println("Cliente "+ (i+1)+" -- Intervalo de confianza del "+prob*100+"%"+":  ["+min+", "+max+"]");
	double av=Redondear(darEspera()[i].average(),3);
	tiempoAv[1][i][intervaloActual-1]=av/60;
	out[i].println(Sim.time()+ " "+av);
	t_av[i]=av;
	}
	
	calcularFobjSim(t_av);
	
}


/**
 * C�lculo de la fobj con los datos de la simulaci�n
 * @param tAv
 * @throws Exception 
 */
private void calcularFobjSim(double[] tAv) throws Exception {
	double sum=0;
	for (int i=0;i<numClientes;i++)
	{
		double t=tAv[i];
		double tc=((double[]) com.getVariable("Tc("+(i+1)+")"))[0]; 
		double r=0;
		if(t<=tc)
		{
			r=((double[]) com.getVariable("b("+(i+1)+")"))[0]; 
		}
		else
		{
			r=(t-tc)*(((double[]) com.getVariable("mc("+(i+1)+")"))[0]);
		}
		sum+=r;
	}
	double Cgrid=((double[]) com.getVariable("Cgrid("+(numIntervalos+intervaloActual)+")"))[0];
	double Pr=((double[]) com.getVariable("Pr("+(numIntervalos+intervaloActual)+")"))[0];
	double Pnr=((double[]) com.getVariable("Pnr("+(numIntervalos+intervaloActual)+")"))[0];

	double retorno=sum+(xGrid[intervaloActual-1]*Cgrid)-(Grs[intervaloActual-1]*Pr)-(Gnrs[intervaloActual-1]*Pnr);
	
	fObjSim[intervaloActual-1]=retorno;
}

/**
 * Retorna el cluster asociado al Id par�metro
 */
public Cluster2013 darCluster(int ID)
{
		Cluster2013 c=null;
		
		if(ID==1)
		{
			c=web;
		}
		else if(ID==2)
		{
			c=app;
		}
		else
		{
			c=dataB;
		}
		return c;
}
/**
 * @return the servidoresApagados
 */
public int getServidoresApagados() {
	return servidoresApagados;
}

/**
 * @param servidoresApagados the servidoresApagados to set
 */
public void setServidoresApagados(int servidoresApagados) {
	this.servidoresApagados = servidoresApagados;
}

/**
 * Aumenta el intervalo actual
 */
public void AumentarInt()
{
	intervaloActual++;
}
/**
 * retorna el intervalo actual
 */
public int darIntervaloActual()
{
	return intervaloActual;
}

/**
 * Actualiza los valores en cada nuevo intervalo
 * @throws Exception 
 */
public void actualizarValores() throws Exception
{
	
	for(int i=0;i<numClientes;i++)
	{
	estEspera[i]=new Tally("Tiempo de espera en el sistema para el cliente "+(i+1));
	genLlegadas[i]=new ExponentialGen(new MRG32k3a(),((double[])com.getVariable("c"+(i+1)+"("+(numIntervalos+intervaloActual)+")"))[0]);
System.out.println(((double[])com.getVariable("c"+(i+1)+"("+(numIntervalos+intervaloActual)+")"))[0]);
	}

}

public int darNumInt()
{
	return numIntervalos;
}

/**
 * Redondea el numero 
 * @param numero
 * @param digitos
 * @return
 */
public static double Redondear(double numero,int digitos)

{
      int cifras=(int) Math.pow(10,digitos);
      return Math.rint(numero*cifras)/cifras;
}

/**
 * Revisa si el n�mero de servidores en cada cluster sumado en total de el total de servidores
 */
public boolean check()
{
	return (web.darNumServidores()+app.darNumServidores()+dataB.darNumServidores()+servidoresApagados==Datacenter2013.TOTAL_SERVIDORES)?true:false;
}

/**
 * retorna el objeto que comunica con matlab
 */
public MatlabProxy darCom()
{
	return com;
}

/**
 * Ejecuta el algoritmo de optmizaci�n desde matlab. Recibe por par�metro:
 * - arribos (el vector con las tasas de arribos pronosticadas para el pr�ximo intervalo)
 * - Gr (Energ�a renovable disponible que se pronostic�)
 * - Pr (Precio de la energ�a renovable en ese intervalo)
 * - Pnr (Precio de la energ�a no renovable en ese intervalo)
 * - Cgrid (Costo de la energ�a de la red el�ctrica)
 * - Nwt_1 (servidores trabajando en el intervalo anterior)
 * @throws Exception 
 */

public double[] ejecutarOptimizacion(double []arribos,double Gr, double Pr,double Pnr,double Cgrid, double Nwt_1) throws Exception
{
	Object []o=com.returningFeval("opti2", 1,arribos,Gr,Pr,Pnr,Cgrid,Nwt_1);
	double [] r=(double[])o[0];
	return r;
}

public int [] darnWork()
{
	return nWork;
}

public void actualizarnWork(int param)
{
	nWork[intervaloActual]=param;
}

/**
 * Realiza las gr�ficas de los resultados obtenidos al correr el programa
 * @throws MatlabInvocationException 
 */
public void realizarGraficas() throws MatlabInvocationException
{
	com.eval("hora=[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]"); //// ojo!!!
	
	double []hora=((double[]) com.getVariable("hora"));
	com.feval("plot",hora, fObjSim,"r","LineWidth",2);
	com.eval("hold on");
	com.feval("plot",hora, fObjTeo,"b","LineWidth",2);
	com.eval("grid on");

	com.eval("ylabel('Costo [COP]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Simulaci\\''on','Optimizaci\\''on');set(l,'Interpreter','LaTex');");
	com.eval("figure");
	
	com.eval("subplot(3,1,1)");	
	com.feval("plot",hora, xGrid,"r","LineWidth",2);
	com.eval("grid on");

	com.eval("ylabel('Energ\\''ia [kWh]','Interpreter','LaTex');");

	com.eval("subplot(3,1,2)");	
	
	com.feval("plot",hora, Grs,"r","LineWidth",2);
	com.eval("hold on");
	com.eval("plot(hora,"+Datacenter2013.cap_solar+"*Gr_solar("+(numIntervalos+1)+":"+2*numIntervalos+")+"+Datacenter2013.cap_viento+"*Gr_viento("+(numIntervalos+1)+":"+2*numIntervalos+"),'b','LineWidth',2)");
	com.eval("grid on");
	
	com.eval("ylabel('Energ\\''ia [kWh]','Interpreter','LaTex');l=legend('Vendida','Generada');set(l,'Interpreter','LaTex');");

	com.eval("subplot(3,1,3)");	
	
	com.feval("plot",hora, Gnr,"r","LineWidth",2);
	com.eval("hold on");
	com.feval("plot",hora, Gnrs,"b","LineWidth",2);
	com.eval("grid on");
	com.eval("ylabel('Energ\\''ia [kWh]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Usada','Vendida');set(l,'Interpreter','LaTex');");

	com.eval("figure");	
	
	for(int i=0;i<numClientes;i++)
	{
		com.eval("subplot(2,2,"+(i+1)+")");	
		com.feval("plot",hora, tiempoAv[0][i],"r","LineWidth",2);
		com.eval("hold on");
		com.feval("plot", hora,tiempoAv[1][i],"b","LineWidth",2);
		com.eval("grid on");
		com.eval("ylabel('Tiempo [min]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Optimizaci\\''on','Simulaci\\''on');set(l,'Interpreter','LaTex');");
	
		
	}
	
	com.eval("figure");	
	for(int i=0;i<numClientes;i++)
	{
		//com.eval("figure");
		com.eval("subplot(2,2,"+(i+1)+")");	
		
		com.eval("plot(hora,60*c"+(i+1)+"("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
		com.eval("hold on");
		com.eval("plot(hora,60*F_c"+(i+1)+",'r','LineWidth',2)");
		com.eval("grid on");
		com.eval("ylabel('arribos/min','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");
	}
	
	com.eval("figure");	
	com.eval("subplot(2,1,1)");	
	com.eval("plot(hora,"+Datacenter2013.cap_solar+"*Gr_solar("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,"+Datacenter2013.cap_solar+"*F_Gr_solar,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('Energ\\''ia[kWh]','Interpreter','LaTex');l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");

	com.eval("subplot(2,1,2)");	
	com.eval("plot(hora,"+Datacenter2013.cap_viento+"*Gr_viento("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,"+Datacenter2013.cap_viento+"*F_Gr_viento,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('Energ\\''ia[kWh]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");

	com.eval("figure");	
	com.eval("subplot(3,1,1)");	
	com.eval("plot(hora,Pr("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,F_Pr,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');;l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");

	
	com.eval("subplot(3,1,2)");	
	
	com.eval("plot(hora,Pnr("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,F_Pnr,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");


	com.eval("subplot(3,1,3)");	
	com.eval("plot(hora,Cgrid("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,F_Cgrid,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");
	
	com.eval("figure");
	com.eval("subplot(2,1,1)");	
	MatlabTypeConverter processor = new MatlabTypeConverter(com);
	processor.setNumericArray("Nserv", new MatlabNumericArray(nServs, null));
	com.eval("bar(Nserv','grouped')");
	com.eval("grid on");
	com.eval("ylabel('N\\''umero de servidores','Interpreter','LaTex');l=legend('C. Web','C. App','C. Base datos');set(l,'Interpreter','LaTex');");
	
	com.eval("subplot(2,1,2)");	
	com.eval("s_total=sum(Nserv,1);");
	com.eval("comp_s=[s_total;Ns-s_total];");
	com.eval("bar(comp_s','grouped')");
	com.eval("grid on");
	com.eval("ylabel('N\\''umero de servidores','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Encendidos','Apagados');set(l,'Interpreter','LaTex');");

	if(esc==6)
	{
		com.eval("figure");
		
		com.feval("plot",hora, tiempoSolucion,"r","LineWidth",2);
		com.eval("grid on");
		com.eval("ylabel('Tiempo [s]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');");
		
		com.setVariable("tiempo_solucion", tiempoSolucion);
	}
}

/**
 * Realiza las gr�ficas de los resultados obtenidos al correr el programa
 * @throws MatlabInvocationException 
 */
public void realizarGraficasEng() throws MatlabInvocationException
{
	com.eval("hora=[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]"); //// ojo!!!
	
	double []hora=((double[]) com.getVariable("hora"));
	com.feval("plot",hora, fObjSim,"r","LineWidth",2);
	com.eval("hold on");
	com.feval("plot",hora, fObjTeo,"b","LineWidth",2);
	com.eval("grid on");

	com.eval("ylabel('Cost [COP]','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');l=legend('Simulation','Optimization');set(l,'Interpreter','LaTex');");
	com.eval("figure");
	
	com.eval("subplot(3,1,1)");	
	com.feval("plot",hora, xGrid,"r","LineWidth",2);
	com.eval("grid on");

	com.eval("ylabel('Energy [kWh]','Interpreter','LaTex');");

	com.eval("subplot(3,1,2)");	
	
	com.feval("plot",hora, Grs,"r","LineWidth",2);
	com.eval("hold on");
	com.eval("plot(hora,"+Datacenter2013.cap_solar+"*Gr_solar("+(numIntervalos+1)+":"+2*numIntervalos+")+"+Datacenter2013.cap_viento+"*Gr_viento("+(numIntervalos+1)+":"+2*numIntervalos+"),'b','LineWidth',2)");
	com.eval("grid on");
	
	com.eval("ylabel('Energy [kWh]','Interpreter','LaTex');l=legend('Sold','Generated');set(l,'Interpreter','LaTex');");

	com.eval("subplot(3,1,3)");	
	
	com.feval("plot",hora, Gnr,"r","LineWidth",2);
	com.eval("hold on");
	com.feval("plot",hora, Gnrs,"b","LineWidth",2);
	com.eval("grid on");
	com.eval("ylabel('Energy [kWh]','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');l=legend('Used','Sold');set(l,'Interpreter','LaTex');");

	com.eval("figure");	
	
	for(int i=0;i<numClientes;i++)
	{
		com.eval("subplot(2,2,"+(i+1)+")");	
		com.feval("plot",hora, tiempoAv[0][i],"r","LineWidth",2);
		com.eval("hold on");
		com.feval("plot", hora,tiempoAv[1][i],"b","LineWidth",2);
		com.eval("grid on");
		com.eval("ylabel('Time [min]','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');l=legend('Optimization','Simulation');set(l,'Interpreter','LaTex');");
	
		
	}
	
	com.eval("figure");	
	for(int i=0;i<numClientes;i++)
	{
		//com.eval("figure");
		com.eval("subplot(2,2,"+(i+1)+")");	
		
		com.eval("plot(hora,60*c"+(i+1)+"("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
		com.eval("hold on");
		com.eval("plot(hora,60*F_c"+(i+1)+",'r','LineWidth',2)");
		com.eval("grid on");
		com.eval("ylabel('arrivals/min','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');l=legend('Real','Forecast');set(l,'Interpreter','LaTex');");
	}
	
	com.eval("figure");	
	com.eval("subplot(2,1,1)");	
	com.eval("plot(hora,"+Datacenter2013.cap_solar+"*Gr_solar("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,"+Datacenter2013.cap_solar+"*F_Gr_solar,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('Energy [kWh]','Interpreter','LaTex');l=legend('Real','Forecast');set(l,'Interpreter','LaTex');");

	com.eval("subplot(2,1,2)");	
	com.eval("plot(hora,"+Datacenter2013.cap_viento+"*Gr_viento("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,"+Datacenter2013.cap_viento+"*F_Gr_viento,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('Energy [kWh]','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');l=legend('Real','Forecast');set(l,'Interpreter','LaTex');");

	com.eval("figure");	
	com.eval("subplot(3,1,1)");	
	com.eval("plot(hora,Pr("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,F_Pr,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');;l=legend('Real','Forecast');set(l,'Interpreter','LaTex');");

	
	com.eval("subplot(3,1,2)");	
	
	com.eval("plot(hora,Pnr("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,F_Pnr,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');l=legend('Real','Forecast');set(l,'Interpreter','LaTex');");


	com.eval("subplot(3,1,3)");	
	com.eval("plot(hora,Cgrid("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
	com.eval("hold on");
	com.eval("plot(hora,F_Cgrid,'r','LineWidth',2)");
	com.eval("grid on");
	com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');l=legend('Real','Forecast');set(l,'Interpreter','LaTex');");
	
	com.eval("figure");
	com.eval("subplot(2,1,1)");	
	MatlabTypeConverter processor = new MatlabTypeConverter(com);
	processor.setNumericArray("Nserv", new MatlabNumericArray(nServs, null));
	com.eval("bar(Nserv','grouped')");
	com.eval("grid on");
	com.eval("ylabel('Number of servers','Interpreter','LaTex');l=legend('Web','App','Database');set(l,'Interpreter','LaTex');");
	
	com.eval("subplot(2,1,2)");	
	com.eval("s_total=sum(Nserv,1);");
	com.eval("comp_s=[s_total;Ns-s_total];");
	com.eval("bar(comp_s','grouped')");
	com.eval("grid on");
	com.eval("ylabel('Number of servers','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');l=legend('On','Off');set(l,'Interpreter','LaTex');");

	if(esc==6)
	{
		com.eval("figure");
		
		com.feval("plot",hora, tiempoSolucion,"r","LineWidth",2);
		com.eval("grid on");
		com.eval("ylabel('Time [s]','Interpreter','LaTex');xlabel('Hour','Interpreter','LaTex');");
		
		com.setVariable("tiempo_solucion", tiempoSolucion);
	}
}

/**
 * actualiza el valor
 */
public void actualizarResultados(double fob,double xg, double Ngnr, double Ngnrs,double Ngrs,double []tav)
{
	fObjTeo[intervaloActual-1]=fob;
	xGrid[intervaloActual-1]=xg;
	Gnr[intervaloActual-1]=Ngnr;
	Gnrs[intervaloActual-1]=Ngnrs;
	Grs[intervaloActual-1]=Ngrs;
	for(int i=0;i<numClientes;i++)
	{
		tiempoAv[0][i][intervaloActual-1]=tav[i]/60;
	}
	
}
/**
 * retorna el nServ
 */
public void actualizarnServ(int a, int b, int c)
{
	nServs[0][intervaloActual-1]=a;
	nServs[1][intervaloActual-1]=b;
	nServs[2][intervaloActual-1]=c;
}

public double darVpr()
{
	return Vpr;
}

public void aumentarVpr(double au)
{
	Vpr=Vpr+au;
}

public void promedioVpr()
{
	Vpr=Vpr/24;
}

public double darVpnr()
{
	return Vpnr;
}

public void aumentarVpnr(double au)
{
	Vpnr=Vpnr+au;
}

public void promedioVpnr()
{
	Vpnr=Vpnr/24;
}

public int darEsc()
{
	return esc;
}
	//*******************************************
	//Main **************************************
	//*******************************************
	/**
	 * @param args
	 */
	public static void main(String[] args) {
try{
	String retorno=JOptionPane.showInputDialog( null, "Defina el escenario (1 - otros, 4 - esc. 4, 6 - esc. 6)");
	if(Integer.parseInt(retorno)!=4)
	{
		Datacenter2013 d = new Datacenter2013(Integer.parseInt(retorno),0,0);
		d.simular(Datacenter2013.horizonte_Sim);
		d.imprimirResultados();
		System.out.println(d.check());
		for (int i=0;i<d.darNumClientes();i++)
		{
		out[i].close();
		}
		
		
		d.realizarGraficasEng();
		d.darCom().disconnect();
	}
	else
	{
		PrintWriter a= new PrintWriter(new File("./resultados/escenario_4"));
		double[] Pr= new double[8];
		double[] Pnr= new double[8];
		
		Pr[0]=0.25;
		Pr[1]=0.2;
		Pr[2]=0.15;
		Pr[3]=0.125;
		Pr[4]=0.1;
		Pr[5]=0.0625;
		Pr[6]=0.04;
		Pr[7]=0.01;
		
		Pnr[0]=0.25;
		Pnr[1]=0.2;
		Pnr[2]=0.15;
		Pnr[3]=0.125;
		Pnr[4]=0.1;
		Pnr[5]=0.0625;
		Pnr[6]=0.04;
		Pnr[7]=0.01;
		
		for (int i=0;i<8;i++)
		{
			for(int j=0;j<8;j++)
			{
				Datacenter2013 d = new Datacenter2013(4,Pr[i],Pr[j]);
				d.simular(Datacenter2013.horizonte_Sim);
				d.imprimirResultados();
				System.out.println(d.check());
				for (int k=0;k<d.darNumClientes();k++)
				{
				out[k].close();
				}
				
				
				d.promedioVpr();
				d.promedioVpnr();
				a.println("Para Pr="+Pr[i]+"y Pnr="+Pnr[j]+":  "+Datacenter2013.Redondear(d.darVpr(), 3)+" ||| "+Datacenter2013.Redondear(d.darVpnr(), 3)+" ||| "+(Datacenter2013.Redondear(d.darVpr(), 3)+Datacenter2013.Redondear(d.darVpnr(), 3)));
				//d.graficarEnerg�a();
				d.darCom().disconnect();
				
			}
		}
		a.close();
	}
	
}
catch (Exception e)
{
	System.out.println(e.getMessage());
	e.printStackTrace();
}
	}
	private void graficarEnergia() throws Exception {
		com.eval("hora=[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]"); //// ojo!!!
		
		double []hora=((double[]) com.getVariable("hora"));
		com.eval("figure");
		
		com.eval("subplot(3,1,1)");	
		com.feval("plot",hora, xGrid,"r","LineWidth",2);
		com.eval("grid on");

		com.eval("ylabel('Energ\\''ia [kWh]','Interpreter','LaTex');");

		com.eval("subplot(3,1,2)");	
		
		com.feval("plot",hora, Grs,"r","LineWidth",2);
		com.eval("hold on");
		com.eval("plot(hora,"+Datacenter2013.cap_solar+"*Gr_solar("+(numIntervalos+1)+":"+2*numIntervalos+")+"+Datacenter2013.cap_viento+"*Gr_viento("+(numIntervalos+1)+":"+2*numIntervalos+"),'b','LineWidth',2)");
		com.eval("grid on");
		
		com.eval("ylabel('Energ\\''ia [kWh]','Interpreter','LaTex');l=legend('Vendida','Generada');set(l,'Interpreter','LaTex');");

		com.eval("subplot(3,1,3)");	
		
		com.feval("plot",hora, Gnr,"r","LineWidth",2);
		com.eval("hold on");
		com.feval("plot",hora, Gnrs,"b","LineWidth",2);
		com.eval("grid on");
		com.eval("ylabel('Energ\\''ia [kWh]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Usada','Vendida');set(l,'Interpreter','LaTex');");

		com.eval("figure");	
		com.eval("subplot(3,1,1)");	
		com.eval("plot(hora,Pr("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
		com.eval("hold on");
		com.eval("plot(hora,F_Pr,'r','LineWidth',2)");
		com.eval("grid on");
		com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');;l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");

		
		com.eval("subplot(3,1,2)");	
		
		com.eval("plot(hora,Pnr("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
		com.eval("hold on");
		com.eval("plot(hora,F_Pnr,'r','LineWidth',2)");
		com.eval("grid on");
		com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");


		com.eval("subplot(3,1,3)");	
		com.eval("plot(hora,Cgrid("+(numIntervalos+1)+":"+(2*numIntervalos)+"),'b','LineWidth',2)");
		com.eval("hold on");
		com.eval("plot(hora,F_Cgrid,'r','LineWidth',2)");
		com.eval("grid on");
		com.eval("ylabel('[COP/kWh]','Interpreter','LaTex');xlabel('Hora del d\\''ia','Interpreter','LaTex');l=legend('Real','Pron\\''ostico');set(l,'Interpreter','LaTex');");
		
		
		
	}

	public int darNumClientes() {
		// TODO Auto-generated method stub
		return numClientes;
	}

	public double [][][] darMatrices()
	{
		return matrices;
	}

	public void actualizarTsol(double d) {
		// TODO Auto-generated method stub
		tiempoSolucion[intervaloActual-1]=d;
	}
	}