package dk.sdu.sem.commonsystem;

/**
 * Immutable 2D vector with floating-point coordinates.
 */
public record Vector2D(float x, float y) {
	public static final Vector2D ZERO = new Vector2D(0, 0);
	public static final Vector2D UP = new Vector2D(0, -1);
	public static final Vector2D DOWN = new Vector2D(0, 1);
	public static final Vector2D LEFT = new Vector2D(-1, 0);
	public static final Vector2D RIGHT = new Vector2D(1, 0);
	public static final Vector2D[] DIRECTIONS = { UP, DOWN, LEFT, RIGHT	};

	/**
	 * Creates a new 2D vector with the specified coordinates.
	 *
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 */
	public Vector2D {
	}

	/**
	 * Gets the x-coordinate of this vector.
	 *
	 * @return The x-coordinate
	 */
	@Override
	public float x() {
		return x;
	}

	/**
	 * Gets the y-coordinate of this vector.
	 *
	 * @return The y-coordinate
	 */
	@Override
	public float y() {
		return y;
	}

	/**
	 * Adds another vector to this vector.
	 *
	 * @param other The vector to add
	 * @return A new vector representing the sum
	 */
	public Vector2D add(Vector2D other) {
		return new Vector2D(this.x + other.x(), this.y + other.y());
	}

	/**
	 * Subtracts another vector from this vector.
	 *
	 * @param other The vector to subtract
	 * @return A new vector representing the difference
	 */
	public Vector2D subtract(Vector2D other) {
		return new Vector2D(this.x - other.x(), this.y - other.y());
	}

	/**
	 * Scales this vector by a scalar value.
	 *
	 * @param scalar The scaling factor
	 * @return A new vector scaled by the factor
	 */
	public Vector2D scale(float scalar) {
		return new Vector2D(this.x * scalar, this.y * scalar);
	}

	/**
	 * Calculates the dot product of this vector with another vector.
	 *
	 * @param other The other vector
	 * @return The dot product scalar value
	 */
	public float dot(Vector2D other) {
		return this.x * other.x + this.y * other.y;
	}

	/**
	 * Calculates the magnitude (length) of this vector.
	 *
	 * @return The magnitude of the vector
	 */
	public float magnitude() {
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * Calculates the squared magnitude of this vector.
	 * This is more efficient than magnitude() when only comparing lengths.
	 *
	 * @return The squared magnitude of the vector
	 */
	public float magnitudeSquared() {
		return x * x + y * y;
	}

	/**
	 * Returns a normalized version of this vector (unit length).
	 * If the vector has zero length, returns the original vector.
	 *
	 * @return A new vector with unit length in the same direction
	 */
	public Vector2D normalize() {
		float mag = magnitude();
		if (mag == 0) {
			return this;
		}
		return new Vector2D(x / mag, y / mag);
	}

	/**
	 * Returns a normalized version of this vector using a fast approximation algorithm.
	 * This method is faster but less accurate than normalize().
	 *
	 * @return A new vector with approximately unit length
	 * @throws ArithmeticException if the vector has zero length
	 */
	public Vector2D fastNormalize() {
		float mag = magnitude();
		if (mag == 0) {
			throw new ArithmeticException("Cannot normalize a zero-length vector");
		}
		float invSqrt = fastInverseSqrt(mag);
		return new Vector2D(x * invSqrt, y * invSqrt);
	}

	/**
	 * Calculates the Euclidean distance between this vector and another vector.
	 *
	 * @param other The other vector
	 * @return The distance between the two vectors
	 */
	public float distance(Vector2D other) {
		float dx = other.x - x;
		float dy = other.y - y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Calculates the Euclidean distance between two vectors
	 *
	 * @param lhs The first vector
	 * @param rhs The second vector
	 * @return The distance between the two vectors
	 */
	public static float euclidean_distance(Vector2D lhs, Vector2D rhs) {
		float dx = lhs.x - rhs.x;
		float dy = lhs.y - rhs.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Calculates the Manhattan distance between two vectors
	 *
	 * @param lhs The first vector
	 * @param rhs The second vector
	 * @return The Manhattan distance between the two vectors
	 */
	public static float manhatten_distance(Vector2D lhs, Vector2D rhs) {
		float dx = Math.abs(lhs.x - rhs.x);
		float dy = Math.abs(lhs.y - rhs.y);
		return dx + dy;
	}

	/**
	 * Calculates the angle in radians between the positive x-axis and this vector.
	 *
	 * @return The angle in radians (between -pi and pi)
	 */
	public float angle() {
		return (float) Math.atan2(y, x);
	}

	/**
	 * Creates a new vector by rotating this vector by the specified angle.
	 *
	 * @param angleInRadians The angle in radians to rotate by
	 * @return A new vector rotated by the specified angle
	 */
	public Vector2D rotate(float angleInRadians) {
		float cos = (float) Math.cos(angleInRadians);
		float sin = (float) Math.sin(angleInRadians);
		return new Vector2D(x * cos - y * sin, x * sin + y * cos);
	}

	/**
	 * Returns a vector that is perpendicular (90 degrees counterclockwise) to this vector.
	 *
	 * @return A new Vector2D perpendicular to this vector
	 */
	public Vector2D perpendicular() {
		return new Vector2D(-y, x);
	}

	/**
	 * Projects this vector onto another vector.
	 *
	 * @param other The vector to project onto (should not be zero-length)
	 * @return A new Vector2D that is the projection of this vector onto other
	 */
	public Vector2D project(Vector2D other) {
		// Formula: projection = (this * other / |other|^2) * other
		float dotProduct = this.dot(other);
		float otherMagnitudeSquared = other.magnitudeSquared();

		// Avoid division by zero
		if (otherMagnitudeSquared < 0.0001f) {
			return new Vector2D(0, 0);
		}

		float scalar = dotProduct / otherMagnitudeSquared;
		return other.scale(scalar);
	}

	/**
	 * Linearly interpolates between this vector and target vector by t.
	 * When t = 0, returns this vector.
	 * When t = 1, returns the target vector.
	 *
	 * @param target The target vector to interpolate towards
	 * @param t      The interpolation factor (typically between 0 and 1)
	 * @return A new interpolated vector
	 */
	public Vector2D lerp(Vector2D target, float t) {
		return new Vector2D(
			x + t * (target.x - x),
			y + t * (target.y - y)
		);
	}

	/**
	 * Fast inverse square root calculation using a bit-hack approximation.
	 * This is a faster but less accurate alternative to 1.0 / Math.sqrt(number).
	 *
	 * @param number The number to calculate the inverse square root of
	 * @return The approximate inverse square root of the number
	 */
	// Source: https://en.wikipedia.org/wiki/Fast_inverse_square_root
	private float fastInverseSqrt(float number) {
		float x2 = number * 0.5f;
		int i = Float.floatToIntBits(number);
		// Magic number and bit-level hack
		i = 0x5f3759df - (i >> 1);
		float y = Float.intBitsToFloat(i);
		// Single iteration of Newton's method to improve the approximation
		y = y * (1.5f - (x2 * y * y));
		return y;
	}

	/**
	 * Returns a string representation of this vector.
	 *
	 * @return A string in the format "Vector2D [x = value; y = value]"
	 */
	@Override
	public String toString() {
		return "Vector2D [x = " + x + "; y = " + y + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vector2D vec))
			return false;

		return vec.x == x && vec.y == y;
	}

	public Vector2D floor() {
		return new Vector2D((float) Math.floor(x), (float) Math.floor(y));
    }
}