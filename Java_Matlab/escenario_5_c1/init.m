% inicializa el metodo de holt-winters con una 
%estacionalidad de tamaño s para los primeros s datos de x 
%x tiene tamaño s
function [L,S]=init(s,x)
L_s=sum(x)/s;
L=[L_s];
S=x/L_s;
end