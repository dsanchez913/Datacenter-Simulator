%Predice el siguiente valor de la serie de tiempo para un tiempo t+1 donde
%t>=s
function [F]=predict_1(L,S,t,F_old,s)
F_t_1=L(t-(s-1))*S(t-s+1);
F=[F_old,F_t_1];
end