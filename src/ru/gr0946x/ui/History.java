package ru.gr0946x.ui;

import ru.gr0946x.ui.fractals.ColorFunction;
import java.util.ArrayDeque;
import java.util.Deque;
public class History {
    private static final int MAX_SIZE = 100;
    private final Deque<Snapshot> undoStack = new ArrayDeque<>();
    private final Deque<Snapshot> redoStack = new ArrayDeque<>();

    public void push(double xMin, double xMax, double yMin, double yMax, ColorFunction colorFunc) {
        if (undoStack.size() >= MAX_SIZE) {
            undoStack.removeLast();
        }
        undoStack.addFirst(new Snapshot(xMin, xMax, yMin, yMax, colorFunc));
        redoStack.clear();
    }

    public Snapshot undo(double xMin, double xMax, double yMin, double yMax, ColorFunction colorFunc) {
        if (undoStack.isEmpty()) return null;
        redoStack.addFirst(new Snapshot(xMin, xMax, yMin, yMax, colorFunc));
        return undoStack.removeFirst();
    }

    public Snapshot redo(double xMin, double xMax, double yMin, double yMax, ColorFunction colorFunc) {
        if (redoStack.isEmpty()) return null;
        undoStack.addFirst(new Snapshot(xMin, xMax, yMin, yMax, colorFunc));
        return redoStack.removeFirst();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public record Snapshot(double xMin, double xMax, double yMin, double yMax, ColorFunction colorFunc) {}
}
