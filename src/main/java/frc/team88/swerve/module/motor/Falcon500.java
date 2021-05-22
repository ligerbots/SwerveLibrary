package frc.team88.swerve.module.motor;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import frc.team88.swerve.configuration.Falcon500Configuration;
import java.util.Objects;

/** SwerveMotor implementation for the Falcon 500. */
public class Falcon500 extends TalonFX implements SwerveMotor {

  // The configuration data for this motor.
  private final Falcon500Configuration config;

  // The last commanded velocity.
  private double commandVelocity = 0;

  // The offset to add to position values, in rotations.
  private double offset = 0;

  /**
   * Constructor. Sets up the default configuration for a Talon FX.
   *
   * @param canID The canID for the Talon FX.
   * @param config The config data for this motor.
   */
  public Falcon500(int canID, Falcon500Configuration config) {
    super(canID);

    this.config = Objects.requireNonNull(config);

    this.configFactoryDefault();
    this.setInverted(config.isInverted());
    this.setNeutralMode(NeutralMode.Brake);
    this.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
    this.configNeutralDeadband(0);
  }

  /**
   * {@inheritDoc}
   *
   * @return The motor shaft position in rotations
   */
  @Override
  public double getPosition() {
    return this.getSelectedSensorPosition() / 2048 + offset;
  }

  @Override
  public double getVelocity() {
    return this.getSelectedSensorVelocity() * 10. / 2048.;
  }

  /**
   * Offsets the sensor position such that the current position becomes the given position.
   *
   * @param position The position to set, in rotations
   */
  public void calibratePosition(double position) {
    this.offset = position - this.getPosition() + this.offset;
  }

  @Override
  public void setVelocity(double velocity) {
    this.set(ControlMode.PercentOutput, velocity / this.getMaxVelocity());
    this.commandVelocity = velocity;
  }

  @Override
  public double getMaxVelocity() {
    return this.config.getMaxSpeed();
  }

  @Override
  public double getCurrentDraw() {
    return this.getSupplyCurrent();
  }

  @Override
  public double getCommandVoltage() {
    return this.getMotorOutputVoltage();
  }

  @Override
  public double getCommandVelocity() {
    return this.commandVelocity;
  }

  @Override
  public void setCoast() {
    this.setNeutralMode(NeutralMode.Coast);
  }

  @Override
  public void setBrake() {
    this.setNeutralMode(NeutralMode.Brake);
  }
}
