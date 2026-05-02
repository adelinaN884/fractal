package ru.gr0946x.ui.animation;

// Импортируем нужные классы
import ru.gr0946x.Converter;
import ru.gr0946x.animation.KeyFrame;
import ru.gr0946x.animation.AnimationEngine;
import ru.gr0946x.ui.MainWindow;
import ru.gr0946x.ui.painting.FractalPainter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Окно для настройки анимационной экскурсии по фракталу.
 * Пользователь добавляет ключевые кадры и создаёт видео.
 */
public class AnimationSettingsDialog extends JDialog {

    // Список ключевых кадров
    private final ArrayList<KeyFrame> keyframes = new ArrayList<>();

    // Таблица для отображения кадров
    private final JTable table;
    private final DefaultTableModel tableModel;

    // Поля ввода
    private final JSpinner fpsSpinner;
    private final JSpinner durationSpinner;
    private final JSpinner iterationsSpinner;
    private final JProgressBar progressBar;

    // Ссылка на главное окно
    private final MainWindow mainWindow;

    // Конвертер координат (копия из главного окна)
    private final Converter tempConv;

    /**
     * Конструктор - создаёт окно настроек
     */
    public AnimationSettingsDialog(MainWindow owner) {
        super(owner, "Настройка экскурсии по фракталу", true);
        this.mainWindow = owner;

        // Копируем текущие координаты из главного окна
        Converter ownerConv = owner.getConverter();
        this.tempConv = new Converter(
                ownerConv.getXMin(), ownerConv.getXMax(),
                ownerConv.getYMin(), ownerConv.getYMax()
        );

        // Настройка окна
        setSize(900, 600);
        setLocationRelativeTo(owner);  // По центру главного окна
        setLayout(new BorderLayout(10, 10));

        // === СОЗДАЁМ ТАБЛИЦУ КЛЮЧЕВЫХ КАДРОВ ===
        String[] columnNames = {"№", "X min", "X max", "Y min", "Y max",
                "Итерации", "Длит. (сек)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Запрещаем редактировать ячейки напрямую
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Заголовок таблицы
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Ключевые кадры"));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // === СОЗДАЁМ ПАНЕЛЬ УПРАВЛЕНИЯ (правая часть) ===
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));

        // Кнопка "Захватить текущий вид"
        JButton captureButton = new JButton("📸 Захватить вид");
        captureButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        captureButton.addActionListener(e -> captureCurrentView());

        // Настройка длительности
        JPanel durationPanel = new JPanel(new FlowLayout());
        durationPanel.add(new JLabel("Длительность (сек):"));
        durationSpinner = new JSpinner(new SpinnerNumberModel(3.0, 0.5, 60.0, 0.5));
        durationSpinner.setPreferredSize(new Dimension(80, 25));
        durationPanel.add(durationSpinner);

        // Настройка итераций
        JPanel iterPanel = new JPanel(new FlowLayout());
        iterPanel.add(new JLabel("Итераций:"));
        iterationsSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        iterationsSpinner.setPreferredSize(new Dimension(80, 25));
        iterPanel.add(iterationsSpinner);

        // Настройка FPS
        JPanel fpsPanel = new JPanel(new FlowLayout());
        fpsPanel.add(new JLabel("FPS:"));
        fpsSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 60, 1));
        fpsSpinner.setPreferredSize(new Dimension(80, 25));
        fpsPanel.add(fpsSpinner);

        // Кнопка "Удалить кадр"
        JButton deleteButton = new JButton("🗑 Удалить кадр");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(e -> deleteSelectedFrame());

        // Прогресс-бар
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Кнопка "Создать видео"
        JButton renderButton = new JButton("🎬 Создать видео");
        renderButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        renderButton.setBackground(new Color(100, 200, 100));
        renderButton.addActionListener(e -> renderAnimation());

        // Добавляем всё на панель управления
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(captureButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(durationPanel);
        controlPanel.add(iterPanel);
        controlPanel.add(fpsPanel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(deleteButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(progressBar);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(renderButton);
        controlPanel.add(Box.createVerticalStrut(10));

        // === СОБИРАЕМ ВСЁ ОКНО ===
        add(tablePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        // Кнопка "Готово" внизу
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Захватывает текущие координаты из главного окна
     */
    private void captureCurrentView() {
        // Получаем актуальные координаты
        Converter conv = mainWindow.getConverter();
        double xMin = conv.getXMin();
        double xMax = conv.getXMax();
        double yMin = conv.getYMin();
        double yMax = conv.getYMax();
        int iterations = (int) iterationsSpinner.getValue();
        double duration = (double) durationSpinner.getValue();

        // Создаём новый ключевой кадр
        KeyFrame kf = new KeyFrame(xMin, xMax, yMin, yMax, iterations, duration);
        keyframes.add(kf);

        // Добавляем строку в таблицу
        Object[] row = {
                keyframes.size(),
                String.format("%.6f", xMin),
                String.format("%.6f", xMax),
                String.format("%.6f", yMin),
                String.format("%.6f", yMax),
                iterations,
                duration
        };
        tableModel.addRow(row);

        // Сообщаем пользователю
        JOptionPane.showMessageDialog(this,
                "Ключевой кадр №" + keyframes.size() + " добавлен!\n" +
                        "Переместите фрактал и добавьте следующий кадр.");
    }

    /**
     * Удаляет выбранный кадр из таблицы
     */
    private void deleteSelectedFrame() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            keyframes.remove(selectedRow);
            tableModel.removeRow(selectedRow);

            // Обновляем номера
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(i + 1, i, 0);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Выберите кадр для удаления!");
        }
    }

    /**
     * Запускает создание анимации
     */
    private void renderAnimation() {
        // Проверяем, что есть хотя бы 2 кадра
        if (keyframes.size() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Добавьте минимум 2 ключевых кадра!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Диалог сохранения файла
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить анимацию");
        fileChooser.setSelectedFile(new File("fractal_tour.gif"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();

            // Добавляем расширение .gif если нужно
            if (!outputFile.getName().toLowerCase().endsWith(".gif")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".gif");
            }

            int fps = (int) fpsSpinner.getValue();

            // Получаем painter и converter из главного окна
            FractalPainter painter = mainWindow.getPainter();
            Converter conv = mainWindow.getConverter();

            // Размеры как у главного окна
            int width = mainWindow.getMainPanel().getWidth();
            int height = mainWindow.getMainPanel().getHeight();

            // Создаём движок анимации
            AnimationEngine engine = new AnimationEngine(
                    keyframes,
                    fps,
                    (overall, segment) -> {
                        // Обновляем прогресс-бар
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue((int)(overall * 100));
                            progressBar.setString((int)(overall * 100) + "%");
                        });
                    }
            );

            // Запускаем рендеринг в отдельном потоке (чтобы окно не зависало)
            new Thread(() -> {
                try {
                    // Меняем текст кнопки
                    SwingUtilities.invokeLater(() ->
                            progressBar.setString("Рисую анимацию..."));

                    engine.renderAnimation(outputFile, painter, conv, width, height);

                    // Сообщаем об успехе
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(100);
                        progressBar.setString("Готово!");
                        JOptionPane.showMessageDialog(this,
                                "Анимация сохранена!\n" + outputFile.getAbsolutePath(),
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setString("Ошибка!");
                        JOptionPane.showMessageDialog(this,
                                "Ошибка при создании анимации:\n" + e.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
