package io.openems.edge.battery.soltaro.cluster.versionc.statemachine;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.battery.soltaro.cluster.versionc.statemachine.StateMachine.Context;

public class GoConfiguration extends State.Handler {

	@Override
	public State getNextState(Context context) throws OpenemsNamedException {
		System.out.println("Stuck in GO_CONFIGURATION");
		return State.GO_CONFIGURATION;
	}

}
