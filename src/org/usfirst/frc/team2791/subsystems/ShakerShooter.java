package org.usfirst.frc.team2791.subsystems;

import org.usfirst.frc.team2791.configuration.Constants;
import org.usfirst.frc.team2791.configuration.PID;
import org.usfirst.frc.team2791.configuration.Ports;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ShakerShooter extends ShakerSubsystem implements Runnable {
	private static final int updateDelayMs = 1000 / 100; // run at 100 Hz
	// private final double[] speed = {0.25, 0.5, 0.75, 1.0};
	private final double delayTimeBeforeShooting = 1.5;// time for wheels to
	// get
	// to speed
	private final double delayTimeForServo = 0.5;// time for servo to push
	private final int encoderTicks = 20 * 4;
	private double fireSpeed;
	private boolean autoFire;
	private CANTalon leftShooterTalon;
	private CANTalon rightShooterTalon;
	private DoubleSolenoid firstLevelSolenoid;
	private DoubleSolenoid secondLevelSolenoid;
	private Servo ballAidServo;
	private AnalogInput ballDistanceSensor;

	public ShakerShooter() {
		leftShooterTalon = new CANTalon(Ports.SHOOTER_TALON_LEFT_PORT);
		rightShooterTalon = new CANTalon(Ports.SHOOTER_TALON_RIGHT_PORT);
		ballAidServo = new Servo(Ports.BALL_AID_SERVO_PORT);
		firstLevelSolenoid = new DoubleSolenoid(Ports.PCM_MODULE, Ports.SHOOTER_PISTON_CHANNEL_FIRST_LEVEL_FORWARD,
				Ports.SHOOTER_PISTON_CHANNEL_FIRST_LEVEL_REVERSE);
		secondLevelSolenoid = new DoubleSolenoid(Ports.SECOND_PCM_MODULE,
				Ports.SHOOTER_PISTON_CHANNEL_SECOND_LEVEL_FORWARD, Ports.SHOOTER_PISTON_CHANNEL_SECOND_LEVEL_REVERSE);
		ballDistanceSensor = new AnalogInput(Ports.BALL_DISTANCE_SENSOR_PORT);
		rightShooterTalon.setInverted(true);
		leftShooterTalon.reverseOutput(true);
		leftShooterTalon.reverseSensor(true);
		SmartDashboard.putNumber("Shooter p", PID.SHOOTER_P);
		SmartDashboard.putNumber("Shooter i", PID.SHOOTER_I);
		SmartDashboard.putNumber("Shooter d", PID.SHOOTER_D);
		PID.SHOOTER_P = SmartDashboard.getNumber("Shooter p");
		PID.SHOOTER_I = SmartDashboard.getNumber("Shooter i");
		PID.SHOOTER_D = SmartDashboard.getNumber("Shooter d");
		leftShooterTalon.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		rightShooterTalon.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		leftShooterTalon.configEncoderCodesPerRev(encoderTicks);
		rightShooterTalon.configEncoderCodesPerRev(encoderTicks);

	}

	@Override
	public void run() {
		while (true) {
			if (autoFire) {
				// gets fire speed from the dashboard (FOR TESTING ONLY!!!!)

				// soft limits for fire speed
				// fireSpeed = fireSpeed >= 1.0 ? 1.0 : fireSpeed;
				// fireSpeed = fireSpeed <= -1.0 ? -1.0 : fireSpeed;
				// converts the fireSpeed percentage to speed values and sets
				// pid to that
				// setShooterSpeeds(fireSpeed,false);
				// System.out.println(fireSpeed);
				// double time = Timer.getFPGATimestamp();
				// // buffer time for wheels to get to speed
				// while (Timer.getFPGATimestamp() - time <
				// delayTimeBeforeShooting) {
				// setShooterSpeeds(fireSpeed,false);
				// System.out.println("The wheels are spinning up");
				// }
				//
				// // update current time
				double time = Timer.getFPGATimestamp();
				// push ball
				while (Timer.getFPGATimestamp() - time < delayTimeForServo) {
					// setShooterSpeeds(fireSpeed,false);
					pushBall();
					System.out.println("The ball is being pushed out");
				}
				resetServoAngle();
				// reset everything
				stopMotors();
			}
			autoFire = false;
			// leftShooterTalon.clearIAccum();
			//
			// rightShooterTalon.clearIAccum();

			try {
				// slows down the rate at which this method is called(so it
				// doesn't run too fast)
				Thread.sleep(updateDelayMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setShooterSpeeds(double targetSpeed, boolean withPID) {
		if (withPID) {
			targetSpeed *= 15000;
			leftShooterTalon.setP(SmartDashboard.getNumber("Shooter p"));
			leftShooterTalon.setI(SmartDashboard.getNumber("Shooter i"));
			leftShooterTalon.setD(SmartDashboard.getNumber("Shooter d"));
			rightShooterTalon.setP(SmartDashboard.getNumber("Shooter p"));
			rightShooterTalon.setI(SmartDashboard.getNumber("Shooter i"));
			rightShooterTalon.setD(SmartDashboard.getNumber("Shooter d"));
			leftShooterTalon.setF(20);
			leftShooterTalon.changeControlMode(TalonControlMode.Speed);
			rightShooterTalon.changeControlMode(TalonControlMode.Speed);
			leftShooterTalon.enableControl();
			rightShooterTalon.enableControl();
			leftShooterTalon.enable();
			rightShooterTalon.enable();
			leftShooterTalon.reverseOutput(false);

			leftShooterTalon.set(targetSpeed);
			rightShooterTalon.set(targetSpeed);

		} else {
			leftShooterTalon.changeControlMode(TalonControlMode.PercentVbus);
			rightShooterTalon.changeControlMode(TalonControlMode.PercentVbus);
			leftShooterTalon.set(targetSpeed);
			rightShooterTalon.set(targetSpeed);
		}

	}

	@Override
	public void updateSmartDash() {
		// TODO Auto-generated method stub
		SmartDashboard.putNumber("LeftShooterSpeed", leftShooterTalon.getEncVelocity());
		SmartDashboard.putNumber("RightShooterSpeed", rightShooterTalon.getEncVelocity());
		SmartDashboard.putNumber("Left Shooter Error", leftShooterTalon.getClosedLoopError());
		SmartDashboard.putNumber("Right Shooter Error", -rightShooterTalon.getClosedLoopError());

	}

	@Override
	public void reset() {
		stopMotors();
		// reset the PID on the Talons
		leftShooterTalon.reset();
		rightShooterTalon.reset();
		// TODO Auto-generated method stub

	}

	public void stopMotors() {
		leftShooterTalon.set(0);
		rightShooterTalon.set(0);
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
		stopMotors();
	}

	public boolean hasBall() {
		return ballDistanceSensor.getVoltage() > 0.25;
	}

	public void pushBall() {
		// will be used to push ball toward the shooter
		ballAidServo.set(1);

	}

	public void resetServoAngle() {
		// bring servo back to original position
		ballAidServo.set(0);
	}

	public void autoFire(double speed) {
		autoFire = true;
		fireSpeed = speed;
	}

	public ShooterHeight getShooterHeight() {
		// get current shooter height by determining which solenoid are true
		if (firstLevelSolenoid.get().equals(Constants.SHOOTER_HIGH_STATE)
				&& secondLevelSolenoid.get().equals(Constants.SHOOTER_HIGH_STATE))
			return ShooterHeight.HIGH;
		else if (firstLevelSolenoid.get().equals(Constants.SHOOTER_HIGH_STATE))
			return ShooterHeight.MID;
		else
			return ShooterHeight.LOW;

	}

	public void setShooterLow() {
		// set shooter height to low , set both pistons to false
		firstLevelSolenoid.set(Constants.SHOOTER_LOW_STATE);
		secondLevelSolenoid.set(Constants.SHOOTER_LOW_STATE_LEVEL_TWO);
	}

	public void setShooterMiddle() {
		// set shooter height to middle meaning only one piston will be true
		firstLevelSolenoid.set(Constants.SHOOTER_HIGH_STATE);
		secondLevelSolenoid.set(Constants.SHOOTER_LOW_STATE_LEVEL_TWO);
	}

	public void setShooterHigh() {
		// both pistons will be set to true to get max height
		firstLevelSolenoid.set(Constants.SHOOTER_HIGH_STATE);
		secondLevelSolenoid.set(Constants.SHOOTER_HIGH_STATE_LEVEL_TWO);
	}

	public enum ShooterHeight {
		LOW, MID, HIGH
	}

}