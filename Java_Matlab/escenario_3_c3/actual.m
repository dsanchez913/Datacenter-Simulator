%Actualiza los valores de L y S segun el tiempo actual t
%t>s
function[L,S]=actual(t,L_old,S_old,alpha,gamma,x_t,s)
L_t=alpha*(x_t/S_old(t-s))+(1-alpha)*(L_old(t-(s-1)-1));
L=[L_old,L_t];
S_t=gamma*(x_t/L_t)+(1-gamma)*(S_old(t-s));
S=[S_old,S_t];
end