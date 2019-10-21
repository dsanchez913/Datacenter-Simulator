function [c]=cota_inf(arribos,rho)
nc=length(arribos);
servidores=zeros(nc,3);
miu=evalin('base', 'miu;');
for i=1:nc
    arrib=[arribos(i),0,0,0];
Pi=evalin('base',[ 'P' num2str(i) ';' ]);

arribos_Estacion=arrib/(eye(4)-Pi);
arribos_web=arribos_Estacion(2);
arribos_app=arribos_Estacion(3);
arribos_DB=arribos_Estacion(4);
    
servidores(i,1)=round(arribos_web/(rho*miu));
servidores(i,2)=round(arribos_app/(rho*miu));
servidores(i,3)=round(arribos_DB/(rho*miu));
end
c=max(servidores);
end