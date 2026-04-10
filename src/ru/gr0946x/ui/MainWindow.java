package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static java.lang.Math.*;

public class MainWindow extends JFrame {

    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter(mandelbrot, conv, (value)->{
            if (value == 1.0) return Color.BLACK;
            var r = (float)abs(sin(5 * value));
            var g = (float)abs(cos(8 * value) * sin (3 * value));
            var b = (float)abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });
        mainPanel = new SelectablePanel(painter);   // СНАЧАЛА создаём

        mainPanel.setWindow(this);                  // ПОТОМ используем

        mainPanel.setBackground(Color.WHITE);

        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalculateBounds();
                mainPanel.repaint();
            }
        });

        mainPanel.addSelectListener((r)->{
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);

            currentScale = 0.0;
            recalculateBounds();
            mainPanel.repaint();
        });

        setContent();
    }
    public void shift(double dx, double dy) {
        double xMin = conv.xScr2Crt(0);
        double xMax = conv.xScr2Crt(mainPanel.getWidth());
        double yMin = conv.yScr2Crt(mainPanel.getHeight());
        double yMax = conv.yScr2Crt(0);

        double scaleX = (xMax - xMin) / mainPanel.getWidth();
        double scaleY = (yMax - yMin) / mainPanel.getHeight();

        conv.setXShape(xMin - dx * scaleX, xMax - dx * scaleX);
        conv.setYShape(yMin + dy * scaleY, yMax + dy * scaleY);

        mainPanel.repaint();
    }
    private void setContent(){
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
    }

    private double currentScale = 0.0;

    private void recalculateBounds() {
        int width = mainPanel.getWidth();
        int height = mainPanel.getHeight();
        if (width <= 0 || height <= 0) return;

        double xMin = conv.getXMin();
        double xMax = conv.getXMax();
        double yMin = conv.getYMin();
        double yMax = conv.getXMax();

        double xRange = xMax - xMin;
        double yRange = yMax - yMin;

        double scaleX = xRange / width;
        double scaleY = yRange / height;

        double newScale = Math.max(scaleX, scaleY);
        if (currentScale == 0.0 || newScale < currentScale) {
            currentScale = newScale;
        }

        double newXRange = currentScale * width;
        double newYRange = currentScale * height;

        double xDiff = (newXRange - xRange) / 2.0;
        double yDiff = (newYRange - yRange) / 2.0;

        conv.setXShape(xMin - xDiff, xMax + xDiff);
        conv.setYShape(yMin - yDiff, yMax + yDiff);
    }

}
