package io.openems.edge.controller.ess.predictivedelaycharge;

import io.openems.common.types.OptionsEnum;

public enum State implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	ACTIVE_LIMIT(0, "Active Limit"), //
	PASSED_TARGET_HOUR(1, "Passed target Hour"), //
	NO_REMAINING_CAPACITY(2, "No remaining capacity"), //
	TARGET_HOUR_NOT_CALCULATED(3, "target hour not calculated, initial run not done"); //

	private final int value;
	private final String name;

	private State(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}

}
