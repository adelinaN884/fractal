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
import java.awt.event.InputEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import static java.lang.Math.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {

    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    private final History history = new History();

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
            var xMinOld = conv.getXMin();
            var xMaxOld = conv.getXMax();
            var yMinOld = conv.getYMin();
            var yMaxOld = conv.getYMax();

            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);

            currentScale = 0.0;
            recalculateBounds();

            history.push(xMinOld, xMaxOld, yMinOld, yMaxOld);
            ((FractalPainter) painter).invalidateCache();
            mainPanel.repaint();
        });
        setContent();
        createMenu();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ((FractalPainter) painter).shutdown();
            }
        });
    }
    private long lastShiftPushTime = 0;
    private boolean shiftSaved = false;
    public void shift(double dx, double dy) {
        long now = System.currentTimeMillis();

        if (!shiftSaved) {
            history.push(conv.getXMin(), conv.getXMax(), conv.getYMin(), conv.getYMax());
            shiftSaved = true;
            lastShiftPushTime = now;
        }
        else if (now - lastShiftPushTime > 200) {
            history.push(conv.getXMin(), conv.getXMax(), conv.getYMin(), conv.getYMax());
            lastShiftPushTime = now;
        }

        double xMin = conv.xScr2Crt(0);
        double xMax = conv.xScr2Crt(mainPanel.getWidth());
        double yMin = conv.yScr2Crt(mainPanel.getHeight());
        double yMax = conv.yScr2Crt(0);

        double scaleX = (xMax - xMin) / mainPanel.getWidth();
        double scaleY = (yMax - yMin) / mainPanel.getHeight();

        conv.setXShape(xMin - dx * scaleX, xMax - dx * scaleX);
        conv.setYShape(yMin + dy * scaleY, yMax + dy * scaleY);

        ((FractalPainter) painter).invalidateCache();
        mainPanel.repaint();
    }
    public void shiftEnd() {
        shiftSaved = false;
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

        saveFrac.addActionListener(_ -> saveFractal("frac"));
        saveJpg.addActionListener(_ -> saveFractal("jpg"));
        savePng.addActionListener(_ -> saveFractal("png"));

        JMenu editMenu = new JMenu("Правка");
        JMenuItem undo = new JMenuItem("Отменить действие (Ctrl+Z)");
        undo.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
        undo.addActionListener(_ -> performUndo());
        editMenu.add(undo);

        JMenuItem redo = new JMenuItem("Вернуть отмену (Ctrl+Y)"); // ← ДОБАВЛЕНО
        redo.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
        redo.addActionListener(_ -> performRedo());                // ← ДОБАВЛЕНО
        editMenu.add(redo);

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

    private double currentScale = 0.0;

    private void recalculateBounds() {
        int width = mainPanel.getWidth();
        int height = mainPanel.getHeight();
        if (width <= 0 || height <= 0) return;

        double xMin = conv.getXMin();
        double xMax = conv.getXMax();
        double yMin = conv.getYMin();
        double yMax = conv.getYMax();

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
    private void performUndo() {
        var snapshot = history.undo(
                conv.getXMin(), conv.getXMax(), conv.getYMin(), conv.getYMax()
        );
        if (snapshot != null) {
            conv.setXShape(snapshot.xMin(), snapshot.xMax());
            conv.setYShape(snapshot.yMin(), snapshot.yMax());
            currentScale = 0.0;
            recalculateBounds();
            ((FractalPainter) painter).invalidateCache();
            mainPanel.repaint();
        }
    }

    private void performRedo() {
        var snapshot = history.redo(
                conv.getXMin(), conv.getXMax(), conv.getYMin(), conv.getYMax()
        );
        if (snapshot != null) {
            conv.setXShape(snapshot.xMin(), snapshot.xMax());
            conv.setYShape(snapshot.yMin(), snapshot.yMax());
            currentScale = 0.0;
            recalculateBounds();
            ((FractalPainter) painter).invalidateCache();
            mainPanel.repaint();
        }
    }
    /// сохранение фрактала
    private void saveFractal(String format) {
        JFileChooser chooser = new JFileChooser();

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            var file = chooser.getSelectedFile();
            String path = file.getAbsolutePath();

            try {
                // PNG / JPG
                if (format.equals("png") || format.equals("jpg")) {

                    if (!path.endsWith("." + format)) {
                        path += "." + format;
                    }

                    BufferedImage img = new BufferedImage(
                            mainPanel.getWidth(),
                            mainPanel.getHeight(),
                            BufferedImage.TYPE_INT_RGB
                    );

                    Graphics2D g2 = img.createGraphics();
                    mainPanel.paint(g2);

                    // подпись координат
                    g2.setColor(Color.BLACK);
                    g2.drawString(
                            "x: " + conv.xScr2Crt(0) + " .. " + conv.xScr2Crt(mainPanel.getWidth()) +
                                    " y: " + conv.yScr2Crt(mainPanel.getHeight()) + " .. " + conv.yScr2Crt(0),
                            10, 20
                    );

                    ImageIO.write(img, format, new File(path));
                }

                //  FRAC
                else if (format.equals("frac")) {

                    if (!path.endsWith(".frac")) {
                        path += ".frac";
                    }

                    try (PrintWriter out = new PrintWriter(path)) {
                        out.println(conv.xScr2Crt(0));
                        out.println(conv.xScr2Crt(mainPanel.getWidth()));
                        out.println(conv.yScr2Crt(mainPanel.getHeight()));
                        out.println(conv.yScr2Crt(0));
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
