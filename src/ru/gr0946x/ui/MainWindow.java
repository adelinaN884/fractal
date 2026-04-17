package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

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
        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.addSelectListener((r)->{
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            mainPanel.repaint();
        });
        setContent();
        createMenu();
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

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Меню «Файл»
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem saveFrac = new JMenuItem("Сохранить как .frac");
        JMenuItem saveJpg = new JMenuItem("Сохранить как JPG");
        JMenuItem savePng = new JMenuItem("Сохранить как PNG");
        JMenuItem openFrac = new JMenuItem("Открыть .frac");

        fileMenu.add(saveFrac);
        fileMenu.add(saveJpg);
        fileMenu.add(savePng);
        fileMenu.addSeparator();
        fileMenu.add(openFrac);

        JMenu editMenu = new JMenu("Правка");
        JMenuItem undo = new JMenuItem("Отменить действие (Ctrl+Z)");
        undo.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
        editMenu.add(undo);

        JMenu viewMenu = new JMenu("Вид");
        JMenuItem showJulia = new JMenuItem("Показать множество Жюлиа");
        viewMenu.add(showJulia);

        JMenu animationMenu = new JMenu("Анимация");
        JMenuItem setupAnimation = new JMenuItem("Настройка экскурсии");
        animationMenu.add(setupAnimation);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(animationMenu);

        setJMenuBar(menuBar);
    }


}
