package com.mycompany.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;

import GUI.TelaPrincipalEsul;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            // Aplica o tema FlatLaf
            UIManager.setLookAndFeel(new FlatDarkLaf());

            // Configurações da tabela
            UIManager.put("Table.alternateRowColor", new Color(244,244,244));
            UIManager.put("TableHeader.background", new Color(22, 28, 34));
            UIManager.put("Table.background", new Color(47,58, 65));
            UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 15));
            UIManager.put("Table.font", new Font("SansSerif", Font.BOLD, 12));
            UIManager.put("TableHeader.foreground", new Color(255 ,211, 94));
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", new Color(224, 224, 224));
            UIManager.put("TableHeader.centerTableColumnLabels", true);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));

            // Configurações do TabbedPane
            UIManager.put("TabbedPane.selectedBackground", new Color(40, 45, 50));
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("TabbedPane.tabHeight", 30);

            // Configurações da árvore
            UIManager.put("Tree.selectionBackground", new Color(255,211,54));
            UIManager.put("Tree.selectionForeground", new Color(47,58,65));
            UIManager.put("Tree.background", Color.WHITE);
            UIManager.put("Tree.foreground", Color.DARK_GRAY);
            UIManager.put("Tree.font", new Font("Inria Sans", Font.ITALIC, 12));

        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Erro ao aplicar FlatLaf: " + e.getMessage());
        }
        TelaPrincipalEsul tp = new TelaPrincipalEsul();
        tp.setVisible(true);
    }
}
