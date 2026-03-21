package ru.gr0946x.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final JPanel mainPanel = new JPanel();
    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mainPanel.setBackground(Color.WHITE);
        setContent();
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
}
