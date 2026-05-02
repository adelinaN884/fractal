package ru.gr0946x.ui.animation;

/**
 * Хранит параметры одного ключевого кадра анимации.
 * Каждый кадр - это определённый вид фрактала.
 */
public class KeyFrame {
    // Координаты области просмотра фрактала
    private final double xMin;
    private final double xMax;
    private final double yMin;
    private final double yMax;

    // Сколько итераций использовать для этого кадра
    private final int maxIterations;

    // Сколько секунд длится переход от предыдущего кадра к этому
    private final double duration;

    // Конструктор - создаёт новый ключевой кадр
    public KeyFrame(double xMin, double xMax, double yMin, double yMax,
                    int maxIterations, double duration) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.maxIterations = maxIterations;
        this.duration = duration;
    }

    // Методы для получения значений (геттеры)
    public double getXMin() { return xMin; }
    public double getXMax() { return xMax; }
    public double getYMin() { return yMin; }
    public double getYMax() { return yMax; }
    public int getMaxIterations() { return maxIterations; }
    public double getDuration() { return duration; }
}