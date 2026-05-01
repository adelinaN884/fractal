package ru.gr0946x.ui.fractals;


import ru.smak.math.Complex;

public class JuliaSet implements Fractal {

    private final Complex c;
    private int maxIterations = 100;
    private final double R2 = 4;

    public JuliaSet(Complex c) {
        this.c = c;
    }

    public JuliaSet(double real, double imag) {
        this.c = new Complex(real, imag);
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public Complex getC() {
        return c;
    }

    @Override
    public float inSetProbability(double x, double y) {
        var z = new Complex(x, y);
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations) {
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float) i / maxIterations;
    }
}