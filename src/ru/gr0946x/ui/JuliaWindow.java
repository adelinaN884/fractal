package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.JuliaSet;
import ru.gr0946x.ui.painting.FractalPainter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import static java.lang.Math.*;

public class JuliaWindow extends JFrame {

    private final FractalPainter juliaPainter;

    public JuliaWindow(double cx, double cy) {
        setTitle("Множество Жюлиа: c = " + String.format("%.6f + %.6fi", cx, cy));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setMinimumSize(new Dimension(400, 400));

        var juliaSet = new JuliaSet(cx, cy);
        var conv = new Converter(-2.0, 2.0, -2.0, 2.0);

        ColorFunction colorFunc = (value) -> {
            if (value == 1.0) return Color.BLACK;
            var r = (float) abs(sin(5 * value));
            var g = (float) abs(cos(8 * value) * sin(3 * value));
            var b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        };

        juliaPainter = new FractalPainter(juliaSet, conv, colorFunc);
        var juliaPanel = new PaintPanel(juliaPainter);
        juliaPanel.setBackground(Color.BLACK);

        setContentPane(juliaPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                juliaPainter.shutdown();
            }
        });
    }
}