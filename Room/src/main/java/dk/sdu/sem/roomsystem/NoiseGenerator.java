package dk.sdu.sem.roomsystem;

import java.util.Random;

public class NoiseGenerator {
	private final int PERMUTATION_SIZE = 512;
	private final int[] p = new int[PERMUTATION_SIZE];

	// Initialization of the permutation table
	public NoiseGenerator() {
		Random rand = new Random();
		for (int i = 0; i < PERMUTATION_SIZE; i++) {
			p[i] = i;
		}
		// Shuffle the array using Fisher-Yates shuffle
		for (int i = 0; i < PERMUTATION_SIZE; i++) {
			int j = rand.nextInt(PERMUTATION_SIZE);
			int tmp = p[i];
			p[i] = p[j];
			p[j] = tmp;
		}
	}

	// Fade function to smooth the result
	private double fade(double t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}

	// Gradient function to calculate gradients at each corner
	private double grad(int hash, double x, double y) {
		int h = hash & 15;
		double u = h < 8 ? x : y;
		double v = h < 4 ? y : (h == 12 || h == 14) ? x : 0;
		return (h & 1) == 0 ? u + v : u - v;
	}

	// Perlin noise function that outputs a value between 0 and 1
	public double perlin(double x, double y) {
		int X = (int) Math.floor(x) & 255;
		int Y = (int) Math.floor(y) & 255;
		double xf = x - Math.floor(x);
		double yf = y - Math.floor(y);
		double u = fade(xf);
		double v = fade(yf);

		int aa = p[(X + p[Y]) & 255];
		int ab = p[(X + p[(Y + 1) & 255]) & 255];
		int ba = p[(X + 1 + p[Y]) & 255];
		int bb = p[(X + 1 + p[(Y + 1) & 255]) & 255];

		double x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
		double x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);

		double value = lerp(x1, x2, v);

		// Normalize the value to be between 0 and 1
		return (value + 1) / 2.0;
	}

	// Linear interpolation function
	private double lerp(double a, double b, double t) {
		return a + t * (b - a);
	}

	// Function to print the Perlin noise value as a gradient (black to white)
	private void printWithGradient(double value) {
		// Map the value to the range of 232 (black) to 255 (white)
		int colorValue = (int) (232 + value * 23); // 23 is the range between 232 (black) and 255 (white)

		// ANSI escape code to set the background color: grayscale colors
		System.out.print("\u001B[48;5;" + colorValue + "m  ");
	}
}