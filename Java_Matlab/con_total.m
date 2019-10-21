function[Etotal]=con_total(Nweb,Napp,Ndb,Non)
Ework=evalin('base', 'Ework;');
Eon=evalin('base', 'Eon;');
Ec=(Nweb+Napp+Ndb)*Ework;
Ecool=0.0001*5*(Ec^3);
Don=Non*Eon;
Etotal=Ec+Ecool+Don;
end