package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FractalPainter implements Painter{

    private final Fractal fractal;
    private final Converter conv;
    private ColorFunction colorFunction;
    private final ExecutorService executor;
    private final int numThreads;

    private volatile long currentGeneration = 0;
    private final Object drawLock = new Object();

    private BufferedImage cachedImage = null;
    private double cachedXMin, cachedXMax, cachedYMin, cachedYMax;
    private int cachedWidth, cachedHeight;

    private double lastZoom = 1.0;

    public FractalPainter(Fractal f, Converter conv, ColorFunction cf) {
        this.fractal = f;
        this.conv = conv;
        this.colorFunction = cf;
        this.numThreads = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(numThreads);

        updateFractalIterations();
    }

    @Override
    public int getWidth() {
        return conv.getWidth();
    }

    @Override
    public int getHeight() {
        return conv.getHeight();
    }

    @Override
    public void setWidth(int width) {
        conv.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        conv.setHeight(height);
    }



    public void setColorFunction(ColorFunction cf) {
        this.colorFunction = cf;
        invalidateCache();
    }

    public void invalidateCache() {
        synchronized (drawLock) {
            cachedImage = null;
        }
    }

    private void updateFractalIterations() {
        if (fractal instanceof Mandelbrot) {
            double xMin = conv.getXMin();
            double xMax = conv.getXMax();
            double zoom = 1.0 / (xMax - xMin);

            if (Math.abs(zoom - lastZoom) > 0.01) {
                lastZoom = zoom;
                ((Mandelbrot) fractal).updateIterations(zoom);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        var w = getWidth();
        var h = getHeight();
        if (w <= 0 || h <= 0) return;

        var xMin = conv.getXMin();
        var xMax = conv.getXMax();
        var yMin = conv.getYMin();
        var yMax = conv.getYMax();

        updateFractalIterations();

        BufferedImage img = null;
        synchronized (drawLock) {
            if (cachedImage != null
                    && cachedWidth == w
                    && cachedHeight == h
                    && Math.abs(cachedXMin - xMin) < 1e-15
                    && Math.abs(cachedXMax - xMax) < 1e-15
                    && Math.abs(cachedYMin - yMin) < 1e-15
                    && Math.abs(cachedYMax - yMax) < 1e-15) {
                img = cachedImage;
            }
        }

        if (img == null) {
            final var myGeneration = ++currentGeneration;

            var newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            List<Future<Void>> futures = new ArrayList<>();

            for (int t = 0; t < numThreads; t++) {
                final var threadId = t;
                final var imageRef = newImage;
                final var xMinF = xMin;
                final var xMaxF = xMax;
                final var yMinF = yMin;
                final var yMaxF = yMax;

                futures.add(executor.submit(() -> {
                    var xRange = xMaxF - xMinF;
                    var yRange = yMaxF - yMinF;

                    for (var y = threadId; y < h; y += numThreads) {
                        if (currentGeneration != myGeneration) {
                            return null;
                        }
                        var cy = yMinF + yRange * (1.0 - (double) y / h);
                        for (var x = 0; x < w; x++) {
                            var cx = xMinF + xRange * ((double) x / w);
                            var value = fractal.inSetProbability(cx, cy);
                            var rgb = colorFunction.getColor(value).getRGB();
                            imageRef.setRGB(x, y, rgb);
                        }
                    }
                    return null;
                }));
            }

            try {
                for (var future : futures) {
                    future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            synchronized (drawLock) {
                if (currentGeneration == myGeneration) {
                    cachedImage = newImage;
                    cachedXMin = xMin;
                    cachedXMax = xMax;
                    cachedYMin = yMin;
                    cachedYMax = yMax;
                    cachedWidth = w;
                    cachedHeight = h;
                    img = cachedImage;
                } else {
                    img = cachedImage;
                }
            }
        }

        if (img != null) {
            g.drawImage(img, 0, 0, null);
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
