package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.Painter;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
public class SelectablePanel extends PaintPanel{
    private SelectedRect rect = null;
    private Graphics g;
    private Point lastDragPoint = null;
    private MainWindow window;
    private final ArrayList<SelectListener> selectHandlers = new ArrayList<>();
    public void addSelectListener(SelectListener listener){
        selectHandlers.add(listener);
    }
    public void setWindow(MainWindow w){
        this.window = w;
    }
    public void removeSelectListener(SelectListener listener){
        selectHandlers.remove(listener);
    }

    public SelectablePanel(Painter painter) {
        super(painter);
        g = getGraphics();
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    lastDragPoint = e.getPoint();
                } else {
                    rect = new SelectedRect(e.getX(), e.getY());
                    paintSelectedRect();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    lastDragPoint = null;
                    if (window != null) window.shiftEnd();
                } else {
                    paintSelectedRect();
                    for (var handler : selectHandlers) {
                        handler.onSelect(new Rectangle(
                                rect.getUpperLeft().x,
                                rect.getUpperLeft().y,
                                rect.getWidth(),
                                rect.getHeight()
                        ));
                    }
                    rect = null;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e) && lastDragPoint != null) {

                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;

                    lastDragPoint = e.getPoint();

                    if (window != null) {
                        window.shift(dx, dy);
                    }

                } else {
                    paintSelectedRect();
                    if (rect != null){
                        rect.setLastPoint(e.getX(), e.getY());
                    }
                    paintSelectedRect();
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                g = getGraphics();
            }
        });
    }

    private void paintSelectedRect(){
        if (g != null){
            g.setXORMode(Color.WHITE);
            g.setColor(Color.BLACK);
            g.drawRect(
                    rect.getUpperLeft().x,
                    rect.getUpperLeft().y,
                    rect.getWidth(),
                    rect.getHeight()
            );
            g.setPaintMode();
        }
    }
}
