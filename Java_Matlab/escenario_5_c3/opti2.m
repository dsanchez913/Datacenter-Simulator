function[solution]=opti2(arribos,Gr,Pr,Pnr,Cgrid,Nwt_1)
%Declaro las va's de decisión
sdpvar Nweb Napp Ndb xgrid Grs Gnrs Gnr Non Noff
nc=length(arribos);
miu=evalin('base', 'miu;');
miu_desp=evalin('base', 'miu_desp;');
Tav=sdpvar(1,nc);
T1=sdpvar(1,nc);
T2=sdpvar(1,nc);
for i=1:nc
    Pi=evalin('base',[ 'P' num2str(i) ';' ]);
    Tav(i)=tEspera2(Pi,arribos(i),Nweb,Napp,Ndb,miu,miu_desp);
end

% assign(xgrid,1);
%eval([ 'A' num2str(i) ' = A;' ]); %---Para insertar en el workspace
Ns=evalin('base', 'Ns;');
Cnr=evalin('base', 'Cnr;');
Tc=evalin('base', 'Tc;');
% Cota inferior para el número de servidores
rho=0.55;
cota=cota_inf(arribos,rho)

assign(Nweb,cota(1));
assign(Napp,cota(2));
 assign(Ndb,cota(3));
%Restricciones
Constraints = [Tav==T1+T2,T1>=0,T1<=Tc,T2>=0,Nweb+Napp+Ndb<=Ns,Nweb>=cota(1),Napp>=cota(2),Ndb>=cota(3),Nweb+Napp+Ndb==Nwt_1+Non-Noff,Gnr+(Gr-Grs)+xgrid==con_total(Nweb,Napp,Ndb,Non),Noff>=0,Non>=0,Grs<=Gr, Gnrs+Gnr<=Cnr,xgrid>=0,Gnrs>=0,Grs>=0,Gnr>=0];
%Función objetivo

Objective =costo_tav(T2)+(xgrid*Cgrid)-(Grs*Pr)-(Gnrs*Pnr);

%Se resuelve
ops = sdpsettings('usex0',1,'solver','fmincon-standard', 'showprogress', 1);
sol = solvesdp(Constraints,Objective,ops);

if sol.problem == 0
Nweb_opt=round(double(Nweb));
Napp_opt=round(double(Napp));
Ndb_opt=round(double(Ndb));
ob=double(Objective);
costo_1=costo_tav(double(T2))
costo_2=double(xgrid)*Cgrid
costo_3=double(Grs)*Pr;
costo_4=double(Gnrs)*Pnr;

Nwk=Nweb_opt+Napp_opt+Ndb_opt;
if Nwk>Ns
    a=Nwk-Ns;
    Ndb_opt=Ndb_opt-a;
end
A=con_total(Nweb_opt,Napp_opt,Ndb_opt,double(Non));
 solution = [double(Objective),Nweb_opt,Napp_opt,Ndb_opt,round(double(Non)),round(double(Noff)),min(Nwk,Ns),double(xgrid),double(Gnr),double(Gnrs), double(Grs),double(Tav),A];
else
 display('Hmm, something went wrong!');
 sol.info
 yalmiperror(sol.problem)
end
end
