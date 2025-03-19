package dk.sdu.sem.commonsystem;

public final class Vector2D {
    private final float x;
	private final float y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.getX(), this.y + other.getY());
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.getX(), this.y - other.getY());
    }

    public Vector2D scale(float scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D dotProduct(Vector2D other) {
        return new Vector2D(this.x * other.getX(), this.y * other.getY());
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float magnitudeSquared() {
        return x * x + y * y;
    }

    // Standard normalization (uses Math.sqrt)
    public Vector2D normalize() {
        float mag = magnitude();
        if (mag == 0) {
            return this;
        }
        return new Vector2D(x / mag, y / mag);
    }

    // Fast normalization using fast inverse square root approximation
    public Vector2D fastNormalize() {
        float mag = magnitude();
        if (mag == 0) {
            throw new ArithmeticException("Cannot normalize a zero-length vector");
        }
        float invSqrt = fastInverseSqrt(mag);
        return new Vector2D(x * invSqrt, y * invSqrt);
    }

    public float distance(Vector2D other) {
        float dx = other.x - x;
        float dy = other.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy); // Could use fastInverseSqrt here
    }

    // Angle (in radians) from the positive x-axis to the vector
    public float angle() {
        return (float) Math.atan2(y, x);
    }

    // A new vector that is this vector rotated by angleRadians
    public Vector2D rotate(float angleInRadians) {
        // This might not be correct, initial thought
        float cos = (float) Math.cos(angleInRadians);
        float sin = (float) Math.sin(angleInRadians);
        return new Vector2D(x * cos, y * sin);
    }

    // Fast inverse square root method (approximation using Newton's method)
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

    @Override
    public String toString() {
        return "Vector2D [x = " + x + "; y = " + y + "]";
    }
}
