package ru.gr0946x.ui.animation;

// Импорты - говорим Java какие классы нам нужны
import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.FractalPainter;
import com.madgag.gif.fmsware.AnimatedGifEncoder;  // Библиотека для GIF

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Движок анимации.
 * Берёт список ключевых кадров и создаёт из них видео (GIF).
 */
public class AnimationEngine {

    // Ключевые кадры анимации
    private final List<KeyFrame> keyframes;

    // Кадров в секунду
    private final int fps;

    // Функция для отчёта о прогрессе (сколько процентов готово)
    private final BiConsumer<Double, Double> onProgress;

    // Конструктор
    public AnimationEngine(List<KeyFrame> keyframes, int fps,
                           BiConsumer<Double, Double> onProgress) {
        this.keyframes = keyframes;
        this.fps = fps;
        this.onProgress = onProgress;
    }

    /**
     * Главный метод - создаёт анимацию и сохраняет в файл
     */
    public void renderAnimation(File outputFile, FractalPainter painter,
                                Converter conv, int width, int height) throws IOException {

        // Создаём объект для записи GIF
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(outputFile.getAbsolutePath());  // Куда сохранять
        encoder.setFrameRate(fps);                     // Сколько кадров в секунду
        encoder.setRepeat(0);                          // 0 = бесконечный повтор

        // Считаем общее время анимации
        double totalDuration = 0;
        for (KeyFrame kf : keyframes) {
            totalDuration += kf.getDuration();
        }

        int totalFrames = (int)(totalDuration * fps);  // Всего кадров
        int currentFrame = 0;                           // Текущий номер кадра

        // Перебираем все пары соседних кадров
        for (int i = 0; i < keyframes.size() - 1; i++) {
            KeyFrame start = keyframes.get(i);      // Начальный кадр
            KeyFrame end = keyframes.get(i + 1);     // Конечный кадр

            // Сколько кадров в этом сегменте
            int framesInSegment = (int)(start.getDuration() * fps);

            // Создаём каждый кадр этого сегмента
            for (int f = 0; f < framesInSegment; f++) {

                // t - насколько мы далеко от начала (0 = начало, 1 = конец)
                double t = (double)f / framesInSegment;

                // Делаем движение плавным (замедление в начале и конце)
                double easedT = smoothstep(t);

                // Вычисляем координаты для этого кадра
                double xMin = interpolate(start.getXMin(), end.getXMin(), easedT);
                double xMax = interpolate(start.getXMax(), end.getXMax(), easedT);
                double yMin = interpolate(start.getYMin(), end.getYMin(), easedT);
                double yMax = interpolate(start.getYMax(), end.getYMax(), easedT);

                // Устанавливаем координаты в конвертер
                conv.setXShape(xMin, xMax);
                conv.setYShape(yMin, yMax);

                // Рисуем фрактал с этими координатами
                BufferedImage frame = renderFrame(painter, conv, width, height);

                // Добавляем кадр в GIF
                encoder.addFrame(frame);

                // Сообщаем о прогрессе
                currentFrame++;
                onProgress.accept(
                        (double)currentFrame / totalFrames,    // Общий прогресс
                        (double)f / framesInSegment             // Прогресс сегмента
                );
            }
        }

        // Заканчиваем создание GIF
        encoder.finish();
    }

    /**
     * Рисует один кадр фрактала в картинку
     */
    private BufferedImage renderFrame(FractalPainter painter, Converter conv,
                                      int width, int height) {
        // Создаём пустую картинку
        BufferedImage image = new BufferedImage(
                width, height,
                BufferedImage.TYPE_INT_RGB  // Тип: цветная картинка
        );

        // Получаем "кисть" для рисования
        Graphics2D g = image.createGraphics();

        // Устанавливаем размер
        painter.setWidth(width);
        painter.setHeight(height);

        // Рисуем фрактал
        painter.paint(g);

        // Освобождаем ресурсы
        g.dispose();

        return image;
    }

    /**
     * Интерполяция - вычисляет промежуточное значение между a и b
     * Пример: a=0, b=10, t=0.5 → результат 5
     */
    private double interpolate(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Функция плавности - делает начало и конец движения более плавными
     */
    private double smoothstep(double t) {
        // Формула, которая даёт плавное ускорение и замедление
        return t * t * (3 - 2 * t);
    }
}