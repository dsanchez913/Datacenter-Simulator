function[Etotal]=con_total(Nweb,Napp,Ndb,Non)
dif=7;
Ework=evalin('base', 'Ework;');
Eon=evalin('base', 'Eon;');
Ec=(Nweb+Napp+Ndb)*Ework;
Ecool=0.003*(1/dif)*(Ec^3);
Don=Non*Eon;
Etotal=Ec+Ecool+Don;
end