function[c]= costo_tav(T)
nc=length(T);
c=0;

b=evalin('base', 'b;');
 mc=evalin('base', 'mc;');   
    for i=1:nc
    costos=b(i)+mc(i)*T(i);
    c=c+costos;
    end
end