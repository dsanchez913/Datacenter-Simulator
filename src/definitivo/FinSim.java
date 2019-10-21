package definitivo;

import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Sim;

public class FinSim extends Event{

	@Override
	public void actions() {
		// TODO Auto-generated method stub
		Sim.stop();
	}

}
