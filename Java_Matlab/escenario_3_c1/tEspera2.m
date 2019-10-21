function W =tEspera2(P1,lambda,nW,nA,nD,miu,mdesp)

arribos=[lambda,0,0,0];

arribos_Estacion=arribos/(eye(4)-P1);

arribos_despachador=arribos_Estacion(1);
arribos_web=arribos_Estacion(2);
arribos_app=arribos_Estacion(3);
arribos_DB=arribos_Estacion(4);

%El tiempo de espera en el despachador
r=arribos_despachador/mdesp;
l_desp=r/(1-r); %cola M/M/1

%El tiempo de espera en el cluster web
r1=(arribos_web/nW)/(miu);
l_web=r1/(1-r1) ;%cola M/M/1

%El tiempo de espera en el cluster app
r2=(arribos_app/nA)/(miu);
l_app=r2/(1-r2);%cola M/M/1

%El tiempo de espera en el cluster web
r3=(arribos_DB/nD)/(miu);
l_db=r3/(1-r3); %cola M/M/1

%El tiempo en el sistema
L=l_desp+(nW*l_web)+(nA*l_app)+(nD*l_db);
W=L/lambda;
end