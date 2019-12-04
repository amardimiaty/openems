package io.openems.edge.evcs.ocpp.core;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.model.Request;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.evcs.api.Evcs;
import io.openems.edge.evcs.api.ManagedEvcs;
import io.openems.edge.evcs.api.Status;

/**
 * Handles writes. Called in every cycle
 */
public class WriteHandler implements Runnable {

	private final Logger log = LoggerFactory.getLogger(WriteHandler.class);

	private final AbstractOcppEvcsComponent parent;

	// Minimum pause between two consecutive writes
	private final static int WRITE_INTERVAL_SECONDS = 30;

	public WriteHandler(AbstractOcppEvcsComponent parent) {
		this.parent = parent;
	}

	/**
	 * Sends commands to the loading station depending on which profiles it
	 * implements.
	 * 
	 * <p>
	 * It is not sending an command, if the communication failed or the write
	 * channel is not set.
	 */
	@Override
	public void run() {
		Channel<Boolean> communicationChannel = this.parent
				.channel(Evcs.ChannelId.CHARGINGSTATION_COMMUNICATION_FAILED);
		if (communicationChannel.value().orElse(true)) {
			return;
		}

		for (OcppProfileType ocppProfileType : this.parent.profileTypes) {
			switch (ocppProfileType) {
			case CORE:
				if (!(this.parent instanceof ManagedEvcs)) {
					break;
				}
				this.setEnergyLimit();
				this.setPower();
				break;
			case FIRMWARE_MANAGEMENT:
				break;
			case LOCAL_AUTH_LIST_MANAGEMENT:
				break;
			case REMOTE_TRIGGER:
				break;
			case RESERVATION:
				break;
			case SMART_CHARGING:
				break;
			}
		}
	}

	private Integer lastTarget = null;
	private LocalDateTime nextPowerWrite = LocalDateTime.MIN;

	/**
	 * Sets the current or power from SET_CHARGE_POWER channel.
	 * <p>
	 * Depending on the charging type it will send different commands with different
	 * units. Invalid values are discarded. If the energy limit is reached it will
	 * send zero.
	 */
	private void setPower() {
		WriteChannel<Integer> energyLimitChannel = this.parent.channel(ManagedEvcs.ChannelId.SET_ENERGY_LIMIT);
		int energyLimit = energyLimitChannel.getNextValue().orElse(0);

		if (energyLimit == 0 || energyLimit > this.parent.getEnergySession().getNextValue().orElse(0)) {
			WriteChannel<Integer> channel = this.parent.channel(ManagedEvcs.ChannelId.SET_CHARGE_POWER_LIMIT);
			Optional<Integer> valueOpt = channel.getNextWriteValueAndReset();

			if (valueOpt.isPresent()) {
				Integer power = valueOpt.get();

				/*
				 * Only if the target has changed or a time has passed.
				 */
				if (!power.equals(lastTarget) || this.nextPowerWrite.isBefore(LocalDateTime.now())) {

					Request request = this.parent.getSupportedRequests().setChargePowerLimit(power.toString());

					UUID id = UUID.fromString(this.parent.getChargingSessionId().getNextValue().orElse(""));

					try {
						this.parent.getConfiguredOcppServer().send(id, request)
								.whenComplete((confirmation, throwable) -> {
									this.parent.logInfo(log, confirmation.toString());
								});

						this.parent.logInfo(this.log,
								"Setting EVCS " + this.parent.alias() + " charge power to [" + power + "]");

						this.nextPowerWrite = LocalDateTime.now().plusSeconds(WRITE_INTERVAL_SECONDS);
						this.lastTarget = power;

					} catch (OccurenceConstraintException e) {
						this.parent.logWarn(log, "The request is not a valid OCPP request.");
					} catch (UnsupportedFeatureException e) {
						this.parent.logWarn(log, "This feature is not implemented by the charging station.");
					} catch (NotConnectedException e) {
						this.parent.logWarn(log, "The server is not connected.");
					}
				}
			}
		} else {
			this.parent.logInfo(this.log, "Maximum energy limit reached");
			this.parent.status().setNextValue(Status.ENERGY_LIMIT_REACHED);
		}
	}

	private Integer lastEnergySession = null;
	private LocalDateTime nextEnergySessionWrite = LocalDateTime.MIN;

	/**
	 * Sets the nextValue of the SET_ENERGY_LIMIT channel.
	 */
	private void setEnergyLimit() {
		WriteChannel<Integer> channel = this.parent.channel(ManagedEvcs.ChannelId.SET_ENERGY_LIMIT);
		Optional<Integer> valueOpt = channel.getNextWriteValueAndReset();
		if (valueOpt.isPresent()) {
			Integer energyLimit = valueOpt.get();

			/*
			 * Only if the target has changed or a time has passed.
			 */
			if (!energyLimit.equals(lastEnergySession) || this.nextEnergySessionWrite.isBefore(LocalDateTime.now())) {

				this.parent.channel(ManagedEvcs.ChannelId.SET_ENERGY_LIMIT).setNextValue(energyLimit);
				this.parent.logInfo(this.log, "Setting OCPP EVCS " + this.parent.alias()
						+ " Energy Limit in this Session to [" + energyLimit + " Wh]");
				lastEnergySession = energyLimit;
				nextEnergySessionWrite = LocalDateTime.now().plusSeconds(WRITE_INTERVAL_SECONDS);
			}
		}
	}
}
