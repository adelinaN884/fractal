package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class Mandelbrot implements Fractal{

    private int baseIterations = 100;
    private int currentIterations = 100;
    private double currentZoom = 1.0;
    private static final double ITER_ZOOM_FACTOR = 0.15;
    private final double R2 = 4;
    public double getR(){
        return sqrt(R2);
    }

    @Override
    public float inSetProbability(double x, double y) {
        var c = new Complex(x, y);
        var z = new Complex();
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < currentIterations){
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float)i / currentIterations;
    }

    public void updateIterations(double zoom){
        currentZoom = zoom;
        currentIterations = (int)(baseIterations * (1 + ITER_ZOOM_FACTOR * Math.log(zoom + 0.5)));

        if (currentIterations < 20) currentIterations = 20;
        if (currentIterations > 2000) currentIterations = 2000;
    }

    public float inSet(double x, double y) {
        var c = new Complex(x,y);
        var z = new Complex();
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < currentIterations) {
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float) i / currentIterations;
    }
}
