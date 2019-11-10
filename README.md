# Datacenter-Simulator

This was my masters degree dissertation project.

The idea here was to simulate a datacenter comprised of web, application and database servers with distributed generation and optimize its performance given some constraints:
- User demand
- Energy price
- Availability of distributed energy (wind and solar)

The project has 3 main modules:

1. A prediction module which uses exponential smoothing for predicting the value of several parameters which are needed for executing an optimization module
2. An optimization module which executes a non-linear program in MATLAB
3. A simulation model which simulates the beaviour of the Datacenter based on discrete event simulations and markovian processes.

The project is executed in Java and then it automatically opens a session in MATLAB for executing the optimization module and for creating the graphics.

For the simulation, I used the SSJ library: http://simul.iro.umontreal.ca/ssj/
For using MATLAB from a Java app, I used the matlabcontrol library: https://code.google.com/archive/p/matlabcontrol/


You can read the research paper i submitted at my university here: https://www.dropbox.com/s/gxpe1moa4nazk4k/articulo.pdf?dl=0

