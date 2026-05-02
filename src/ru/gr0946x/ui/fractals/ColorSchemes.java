package ru.gr0946x.ui.fractals;

import java.awt.*;
import static java.lang.Math.*;

public class ColorSchemes {

    // Схема 1: Радужная (та, что сейчас используется)
    public static final ColorFunction RAINBOW = (value) -> {
        if (value == 1.0) return Color.BLACK;
        var r = (float) abs(sin(5 * value));
        var g = (float) abs(cos(8 * value) * sin(3 * value));
        var b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
        return new Color(r, g, b);
    };

    // Схема 2: Огненная
    public static final ColorFunction FIRE = (value) -> {
        if (value == 1.0) return Color.BLACK;
        var r = (float) min(1.0, value * 3);
        var g = (float) min(1.0, value * 1.5);
        var b = (float) min(1.0, value * 0.5);
        return new Color(r, g, b);
    };

    // Схема 3: Сине-зеленая (океан)
    public static final ColorFunction OCEAN = (value) -> {
        if (value == 1.0) return Color.BLACK;
        var r = (float) 0.0;
        var g = (float) abs(sin(value * PI * 2));
        var b = (float) abs(cos(value * PI));
        return new Color(r, g, b);
    };

    // Схема 4: Черно-белая с оттенками серого
    public static final ColorFunction GRAYSCALE = (value) -> {
        if (value == 1.0) return Color.BLACK;
        float gray = (float) (1.0 - value);
        return new Color(gray, gray, gray);
    };
}