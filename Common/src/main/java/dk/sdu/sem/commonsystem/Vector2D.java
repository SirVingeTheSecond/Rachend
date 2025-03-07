package dk.sdu.sem.commonsystem;

public class Vector2D {
    private final float x;
	private final float y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    // Standard normalization (uses Math.sqrt)
    public Vector2D normalize() {
        float magnitude = (float)Math.sqrt(x*x + y*y);
        if (magnitude == 0) {
            throw new ArithmeticException("Cannot normalize a zero-length vector");
        }
        return new Vector2D(x / magnitude, y / magnitude);
    }

    // Fast normalization using fast inverse square root approximation
    public Vector2D fastNormalize() {
        float slope = (float)Math.sqrt(x*x + y*y);
        if (slope == 0) {
            throw new ArithmeticException("Cannot normalize a zero-length vector");
        }
        float invSqrt = fastInverseSqrt(slope);
        return new Vector2D(x * invSqrt, y * invSqrt);
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
        return "Vector2D [x=" + x + ", y=" + y + "]";
    }
}
