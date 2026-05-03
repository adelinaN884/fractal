package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorSchemes;
import ru.gr0946x.ui.fractals.JuliaSet;
import ru.gr0946x.ui.painting.FractalPainter;
import javax.swing.*;
import java.awt.*;

public class JuliaWindow extends JFrame {

    private final FractalPainter juliaPainter;

    public JuliaWindow(double cx, double cy) {
        setTitle("Множество Жюлиа: c = " + String.format("%.6f + %.6fi", cx, cy));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setMinimumSize(new Dimension(400, 400));

        var juliaSet = new JuliaSet(cx, cy);
        var conv = new Converter(-2.0, 2.0, -2.0, 2.0);

        juliaPainter = new FractalPainter(juliaSet, conv, ColorSchemes.RAINBOW);

        var juliaPanel = new PaintPanel(juliaPainter);
        juliaPanel.setBackground(Color.BLACK);

        setContentPane(juliaPanel);

    }
}