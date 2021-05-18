package frc.team88.swerve.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.electronwill.nightconfig.core.Config;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import edu.wpi.first.networktables.NetworkTable;
import frc.team88.swerve.data.NetworkTablePopulator;
import frc.team88.swerve.util.Vector2D;

/**
 * Captures all of the configuration information about a SwerveModule.
 */
public class SwerveModuleConfiguration implements NetworkTablePopulator {

    // Configuration values. See getters for documentation.
    private Vector2D location;
    private final RealMatrix forwardMatrix;
    private final RealMatrix inverseMatrix;
    private double wheelDiameter;
    private final AzimuthControllerConfiguration azimuthControllerConfig;
    private final PIDConfiguration wheelControllerConfig;

    private transient boolean firstNetworkTableCall = true;

    /**
     * Constructs this configuration from an instantiated module template.
     * 
     * @param instantiatedConfig The instantiated module template.
     */
    public SwerveModuleConfiguration(Config instantiatedConfig) {
        Objects.requireNonNull(instantiatedConfig);
        this.location = Vector2D.createCartesianCoordinates((double) instantiatedConfig.get("location-inches.x"),
                (double) instantiatedConfig.get("location-inches.y"));

        // Convert 2D list to 2D array
        this.forwardMatrix = new Array2DRowRealMatrix(convertObjectListTo2DDoubleArray(instantiatedConfig.get("differential-matrix")), true);
        this.inverseMatrix = MatrixUtils.inverse(this.forwardMatrix);
        
        this.wheelDiameter = (double) instantiatedConfig.get("wheel-diameter-inches");
        this.azimuthControllerConfig = new AzimuthControllerConfiguration(instantiatedConfig.get("azimuth-controller"));
        this.wheelControllerConfig = new PIDConfiguration(instantiatedConfig.get("wheel-controller"));
    }

    /**
     * Gets the location of the swerve module.
     * 
     * @return The location of the swerve module in inches.
     */
    public Vector2D getLocation() {
        return this.location;
    }

    /**
     * Gets the forward differential matrix.
     * 
     * @return The 2x2 forward differential matrix for the module, which converts
     *         from inputs [motor0, motor1] to outputs [azimuthSpeed, wheelSpeed].
     *         Unitless.
     */
    public RealMatrix getForwardMatrix() {
        return this.forwardMatrix;
    }

    /**
     * Gets the inverse differential matrix.
     * 
     * @return The 2x2 inverse differential matrix for the module, which converts
     *         from outputs [azimuthSpeed, wheelSpeed] to inputs [motor0, motor1].
     *         Unitless.
     */
    public RealMatrix getInverseMatrix() {
        return this.inverseMatrix;
    }

    /**
     * Gets the wheel size.
     * 
     * @return The diameter of the wheel on the module, in inches.
     */
    public double getWheelDiameter() {
        return this.wheelDiameter;
    }

    /**
     * Gets the azimuth controller configuration.
     * 
     * @return The configuration info for the azimuth position controller.
     */
    public AzimuthControllerConfiguration getAzimuthControllerConfig() {
        return this.azimuthControllerConfig;
    }

    /**
     * Gets the wheel controller configuration
     * 
     * @return The configuration info for the wheel velocity contoller.
     */
    public PIDConfiguration getWheelControllerConfig() {
        return this.wheelControllerConfig;
    }

    @Override
    public void populateNetworkTable(NetworkTable table) {
        this.wheelControllerConfig.populateNetworkTable(table.getSubTable("wheelController"));
        this.azimuthControllerConfig.populateNetworkTable(table.getSubTable("azimuthController"));
        if (this.firstNetworkTableCall) {
            this.firstNetworkTableCall = false;
            table.getEntry("wheelDiameter").setDouble(this.wheelDiameter);
            table.getEntry("locationX").setDouble(this.location.getX());
            table.getEntry("locationY").setDouble(this.location.getY());
        } else {
            this.wheelDiameter = table.getEntry("wheelDiameter").getDouble(this.wheelDiameter);
            this.location = Vector2D.createCartesianCoordinates(table.getEntry("locationX").getDouble(this.location.getX()), table.getEntry("locationY").getDouble(this.location.getY()));
        }
    }

    /**
     * Converts a 2D list of Doubles typed as Object into a 2D double array.
     * 
     * @param list A 2x2 nested list of Objects which will be casted to Double
     *             objects.
     * @return A 2x2 double array.
     */
    private double[][] convertObjectListTo2DDoubleArray(List<?> list) {
        double arr[][] = new double[2][2];
        if (!(list instanceof List<?>)) {
            throw new IllegalArgumentException("Differential matrix is not a list.");
        }
        List<Object> outerList = new ArrayList<Object>(list);
        if (outerList.size() != 2) {
            throw new IllegalArgumentException("Differential matrix does not have height 2.");
        }
        
        for (int row = 0; row < 2; row++) {
            Object outerItem = outerList.get(row);
            if (!(outerItem instanceof List<?>)) {
                throw new IllegalArgumentException("Differential matrix is not a list of lists.");
            }
            List<Object> innerList = new ArrayList<Object>((List<?>)outerItem);
            if (innerList.size() != 2) {
                throw new IllegalArgumentException("Differential matrix does not have width 2.");
            }

            for (int col = 0; col < 2; col++) {
                Object innerItem = innerList.get(col);
                if (!(innerItem instanceof Double)) {
                    throw new IllegalArgumentException("Differential matrix contains a non-double element.");
                }
                Double value = (Double)(innerItem);
                arr[row][col] = value.doubleValue();
            }
        }
        return arr;
    }

    /**
     * Captures all of the configuration information about a SwerveModule's azimuth
     * controller.
     */
    public class AzimuthControllerConfiguration implements NetworkTablePopulator {

        // Configuration values. See getters for documentation.
        private final PIDConfiguration pidConfig;
        private double maxSpeed;
        private double maxAcceleration;

        private transient boolean firstNetworkTableCall = true;

        /**
         * Constructs from a raw configuration.
         * 
         * @param config The raw configuration.
         */
        public AzimuthControllerConfiguration(Config config) {
            Objects.requireNonNull(config);
            this.pidConfig = new PIDConfiguration(config);
            this.maxSpeed = (double) config.get("max-speed");
            this.maxAcceleration = (double) config.get("max-acceleration");
        }

        /**
         * Gets the PID configuration.
         * 
         * @return The PID configuration for the azimuth controller.
         */
        public PIDConfiguration getPIDConfig() {
            return this.pidConfig;
        }

        /**
         * Gets the max speed.
         * 
         * @return The max speed for the trapezoidal controller.
         */
        public double getMaxSpeed() {
            return this.maxSpeed;
        }

        /**
         * Gets the max acceleration.
         * 
         * @return The max acceleration for the trapezoidal controller.
         */
        public double getMaxAcceleration() {
            return this.maxAcceleration;
        }

        @Override
        public void populateNetworkTable(NetworkTable table) {
            this.pidConfig.populateNetworkTable(table);
            if (this.firstNetworkTableCall) {
                this.firstNetworkTableCall = false;
                table.getEntry("maxSpeed").setDouble(this.maxSpeed);
                table.getEntry("maxAcceleration").setDouble(this.maxAcceleration);
            } else {
                this.maxSpeed = table.getEntry("maxSpeed").getDouble(this.maxSpeed);
                this.maxAcceleration = table.getEntry("maxAcceleration").getDouble(this.maxAcceleration);
            }
        }
    }
}
