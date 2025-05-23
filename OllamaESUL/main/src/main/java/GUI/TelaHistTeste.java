/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author santo
 */
public class TelaHistTeste extends javax.swing.JFrame {

    /**
     * Creates new form TelaHistTeste
     */
    
   class SingleLineCellRenderer extends JTextArea implements TableCellRenderer {
    public SingleLineCellRenderer() {
        setLineWrap(false);  // Desabilitar a quebra de linha
        setWrapStyleWord(true);  // Manter o estilo de palavra, mas sem quebrar
        setOpaque(true);
        setEditable(false);  // Impede edição do texto diretamente na célula
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
        setText(value == null ? "" : value.toString());

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        setFont(table.getFont());

        // Ajustar a altura da linha, mas sem múltiplas linhas
        int lineHeight = getFontMetrics(getFont()).getHeight();
        table.setRowHeight(row, lineHeight + 5); // Pequeno espaço adicional para a altura

        return this;
    }
}

    private void carregarDados() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        DefaultTableModel modelo = (DefaultTableModel) tabelaHistoricoT.getModel();
        modelo.setRowCount(0); // Limpa os dados atuais

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/esulbd", "root", "fatec");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM testes")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp dataHora = rs.getTimestamp("data_hora");
                String input = rs.getString("inp_teste");
                String output = rs.getString("out_teste");

                modelo.addRow(new Object[]{id, dataHora, input, output});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
        }
    }

 public TelaHistTeste() {
        initComponents();
        setLocationRelativeTo(null);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tabelaHistoricoT.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        tabelaHistoricoT.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // DataHora

        // Usar SingleLineCellRenderer para Entrada e Saída
        tabelaHistoricoT.getColumnModel().getColumn(2).setCellRenderer(new SingleLineCellRenderer()); // Entrada Usuário
        tabelaHistoricoT.getColumnModel().getColumn(3).setCellRenderer(new SingleLineCellRenderer()); // Saída IA

        // Adicionar MouseListener para abrir nova janela com o conteúdo completo
        tabelaHistoricoT.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tabelaHistoricoT.rowAtPoint(e.getPoint());
                int column = tabelaHistoricoT.columnAtPoint(e.getPoint());

                // Abrir nova tela para Entrada Usuário ou Saída IA
                if (column == 2 || column == 3) {
                    String content = tabelaHistoricoT.getValueAt(row, column).toString();
                    openDetailWindow(content);
                }
            }
        });
        setResizable(false);
        carregarDados();
    }

   private void openDetailWindow(String content) {
    JFrame detailFrame = new JFrame();

    // Título com margens superior e inferior
    JLabel titleLabel = new JLabel("Detalhes do Teste", SwingConstants.CENTER);
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    titleLabel.setForeground(new Color(255, 211, 94));
    titleLabel.setOpaque(true);
    titleLabel.setBackground(new Color(30, 39, 42));

    JPanel titlePanel = new JPanel(new BorderLayout());
    titlePanel.setBackground(new Color(30, 39, 42));
    titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Top e bottom = 5px
    titlePanel.add(titleLabel, BorderLayout.CENTER);

    // Área de texto
    JTextArea textArea = new JTextArea(content);
    textArea.setEditable(false);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setBackground(new Color(30, 39, 42));
    textArea.setForeground(Color.WHITE);
    textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
    textArea.setMargin(new Insets(10, 10, 10, 10)); // Margem interna do texto

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.getViewport().setBackground(new Color(30, 39, 42));

    // Painel principal com margem geral
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(new Color(30, 39, 42));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margem geral de 5px
    mainPanel.add(titlePanel, BorderLayout.NORTH);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    // Adiciona o painel ao frame
    detailFrame.setContentPane(mainPanel);
    detailFrame.setSize(500, 400);
    detailFrame.setLocationRelativeTo(this);
    detailFrame.setVisible(true);
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaHistoricoT = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabelaHistoricoT.setAutoCreateRowSorter(true);
        tabelaHistoricoT.setBackground(new java.awt.Color(47, 58, 65));
        tabelaHistoricoT.setForeground(new java.awt.Color(255, 255, 255));
        tabelaHistoricoT.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Data e Hora", "Entrada Usuário", "Saída IA"
            }
        ));
        tabelaHistoricoT.setFocusable(false);
        tabelaHistoricoT.setGridColor(new java.awt.Color(47, 58, 65));
        jScrollPane1.setViewportView(tabelaHistoricoT);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 211, 94));
        jLabel1.setText("HISTÓRICO DE TESTE");

        jButton1.setBackground(new java.awt.Color(255, 211, 94));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(16, 22, 20));
        jButton1.setText("Sair");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSairHistTeste(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 211, 94));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(16, 22, 20));
        jButton2.setText("Atualizar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(78, 78, 78))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 726, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(287, 287, 287)
                        .addComponent(jLabel1)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(34, 34, 34)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSairHistTeste(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSairHistTeste
        // TODO add your handling code here:
            // Fechar a tela de histórico
    this.dispose();
    }//GEN-LAST:event_BtnSairHistTeste

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        carregarDados();
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaHistTeste.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaHistTeste.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaHistTeste.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaHistTeste.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaHistTeste().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabelaHistoricoT;
    // End of variables declaration//GEN-END:variables
}
