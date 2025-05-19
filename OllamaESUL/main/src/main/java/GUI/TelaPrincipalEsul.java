/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import esulDAO.BD;
import ferramentas.GeradorDeTesteJava;
import GUI.ProjectCompiler;
import ferramentas.MelhoradorDeCodigo;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import org.codehaus.janino.SimpleCompiler;
import java.awt.Color;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 *
 * @author Lincolzera
 */
public class TelaPrincipalEsul extends javax.swing.JFrame {
    
    private RSyntaxTextArea CaixaTexto;  
    private final javax.swing.JPanel painelCodigo; 
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Projetos"); // Nó raiz para múltiplos projetos
    private DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    private File arquivoAtual;


    public TelaPrincipalEsul() {
        getRootPane().putClientProperty("JRootPane.titleBarBackground", new Color(16,22,20));
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        
        initComponents();
        jTreeArquivos.setModel(treeModel); 
        setIconImage(new ImageIcon(getClass().getResource("/Imagens/esul.png")).getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        jPanelTerminal.setVisible(false);
        painelCodigo = jPanel3;
        setLocationRelativeTo(null);
        configurarCaixaTexto();
        jTreeArquivos.setCellRenderer(new FileTreeCellRenderer());
        jTreeArquivos.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeArquivos.getLastSelectedPathComponent();

                if (selectedNode == null) {
                    CaixaTexto.setText("");
                    arquivoAtual = null;
                    return;
                }

                Object userObject = selectedNode.getUserObject();

                if (userObject instanceof File) {
                    File file = (File) userObject;

                    if (file.isFile()) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            CaixaTexto.setText("");
                            String line;
                            while ((line = reader.readLine()) != null) {
                                CaixaTexto.append(line + "\n");
                            }
                            CaixaTexto.setCaretPosition(0); // Volta para o topo
                            arquivoAtual = file; // Atualiza o arquivo atual
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(TelaPrincipalEsul.this,
                                    "Erro ao ler o arquivo: " + ex.getMessage(),
                                    "Erro de Leitura de Arquivo", JOptionPane.ERROR_MESSAGE);
                            CaixaTexto.setText("Erro ao carregar o arquivo: " + ex.getMessage());
                            arquivoAtual = null;
                        }
                    } else {
                        CaixaTexto.setText(""); // Limpa se for diretório
                        arquivoAtual = null;
                    }
                } else {
                    CaixaTexto.setText(""); // Limpa se não for File
                    arquivoAtual = null;
                }
            }
        });
    }    
       
    private void configurarCaixaTexto() {
        CaixaTexto = new RSyntaxTextArea();
        CaixaTexto.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        CaixaTexto.setCodeFoldingEnabled(true);
        CaixaTexto.setAntiAliasingEnabled(true);
        CaixaTexto.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 18));
        CaixaTexto.setBackground(new java.awt.Color(255, 255, 255));
        CaixaTexto.setForeground(new java.awt.Color(0, 0, 0));
        try {
            InputStream in = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml");
            if (in == null) {
                System.err.println("Arquivo de tema não encontrado!");
            } else {
                Theme theme = Theme.load(in);
                theme.apply(CaixaTexto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String textoInicial = """
                             // Seu código aqui //
                             """;
        CaixaTexto.setText(textoInicial);

        // Configurar atalhos de teclado
        InputMap inputMap = CaixaTexto.getInputMap();
        ActionMap actionMap = CaixaTexto.getActionMap();
        
        // Ctrl+S para salvar no arquivo atual
        KeyStroke ctrlS = KeyStroke.getKeyStroke("control S");
        inputMap.put(ctrlS, "saveFile");
        actionMap.put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (arquivoAtual != null) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoAtual))) {
                        writer.write(CaixaTexto.getText());
                        JOptionPane.showMessageDialog(TelaPrincipalEsul.this, 
                            "Arquivo salvo com sucesso!", 
                            "Sucesso", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(TelaPrincipalEsul.this,
                            "Erro ao salvar o arquivo: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(TelaPrincipalEsul.this,
                        "Nenhum arquivo aberto para salvar. Use Ctrl+Shift+S para salvar em um novo arquivo.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        // Ctrl+Shift+S para salvar como
        KeyStroke ctrlShiftS = KeyStroke.getKeyStroke("control shift S");
        inputMap.put(ctrlShiftS, "saveAs");
        actionMap.put("saveAs", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".java");
                    }
                    
                    @Override
                    public String getDescription() {
                        return "Arquivos Java (*.java)";
                    }
                });
                
                int result = fileChooser.showSaveDialog(TelaPrincipalEsul.this);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    // Garantir que o arquivo tenha extensão .java
                    if (!selectedFile.getName().toLowerCase().endsWith(".java")) {
                        selectedFile = new File(selectedFile.getAbsolutePath() + ".java");
                    }
                    
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                        writer.write(CaixaTexto.getText());
                        arquivoAtual = selectedFile; // Atualiza o arquivo atual
                        JOptionPane.showMessageDialog(TelaPrincipalEsul.this,
                            "Arquivo salvo com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(TelaPrincipalEsul.this,
                            "Erro ao salvar o arquivo: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        RTextScrollPane scrollPane = new RTextScrollPane(CaixaTexto);
        painelCodigo.setLayout(new java.awt.BorderLayout());
        painelCodigo.add(scrollPane, java.awt.BorderLayout.CENTER);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jButton7 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanelTerminal = new javax.swing.JPanel();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        btnAjuda = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTreeArquivos = new javax.swing.JTree();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(102, 102, 102));
        setSize(new java.awt.Dimension(1300, 800));

        jPanel1.setBackground(new java.awt.Color(16, 22, 20));
        jPanel1.setPreferredSize(new java.awt.Dimension(1920, 1080));

        jLabel1.setFont(new java.awt.Font("Inria Sans", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("IDE SUL");

        jButton2.setBackground(new java.awt.Color(22, 28, 34));
        jButton2.setFont(new java.awt.Font("Inria Sans", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 211, 94));
        jButton2.setText("Limpar código");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(22, 28, 34));
        jButton4.setFont(new java.awt.Font("Inria Sans", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 211, 94));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/IconTeste.png"))); // NOI18N
        jButton4.setText("Gerar testes");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(22, 28, 34));
        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 211, 94));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/IconSQL.png"))); // NOI18N
        jButton5.setText("Teste(s)");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 211, 94));
        jLabel4.setText("Históricos:");

        jButton6.setBackground(new java.awt.Color(22, 28, 34));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 211, 94));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/IconSQL.png"))); // NOI18N
        jButton6.setText("Melhoria(s)");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jPanel3.setPreferredSize(new java.awt.Dimension(50, 446));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 477, Short.MAX_VALUE)
        );

        jButton7.setBackground(new java.awt.Color(22, 28, 34));
        jButton7.setFont(new java.awt.Font("Inria Sans", 1, 12)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 211, 94));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/IconArquivo.png"))); // NOI18N
        jButton7.setText("Abrir Aquivo");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(22, 28, 34));
        jButton3.setFont(new java.awt.Font("Inria Sans", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 211, 94));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/IconMelhoria.png"))); // NOI18N
        jButton3.setText("Gerar melhoria");
        jButton3.setFocusPainted(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(22, 28, 34));
        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton8.setForeground(new java.awt.Color(255, 255, 255));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/IconRUN_1.png"))); // NOI18N
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanelTerminal.setBackground(new java.awt.Color(42, 45, 44));
        jPanelTerminal.setToolTipText("");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 211, 94));
        jLabel2.setText("TERMINAL:");

        jButton9.setBackground(new java.awt.Color(255, 211, 94));
        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton9.setForeground(new java.awt.Color(16, 22, 20));
        jButton9.setText("Fechar");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setBackground(new java.awt.Color(255, 211, 94));
        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton10.setForeground(new java.awt.Color(16, 22, 20));
        jButton10.setText("Minimizar");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTerminalLayout = new javax.swing.GroupLayout(jPanelTerminal);
        jPanelTerminal.setLayout(jPanelTerminalLayout);
        jPanelTerminalLayout.setHorizontalGroup(
            jPanelTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTerminalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanelTerminalLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9)))
                .addContainerGap())
        );
        jPanelTerminalLayout.setVerticalGroup(
            jPanelTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTerminalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jButton9)
                    .addComponent(jButton10))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnAjuda.setBackground(new java.awt.Color(22, 28, 34));
        btnAjuda.setFont(new java.awt.Font("Inria Sans", 1, 14)); // NOI18N
        btnAjuda.setForeground(new java.awt.Color(255, 211, 94));
        btnAjuda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/IconAjuda_1.png"))); // NOI18N
        btnAjuda.setText("Ajuda");
        btnAjuda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAjudaActionPerformed(evt);
            }
        });

        jScrollPane2.setForeground(new java.awt.Color(255, 211, 50));

        jTreeArquivos.setBackground(new java.awt.Color(16, 22, 20));
        jTreeArquivos.setForeground(new java.awt.Color(255, 211, 94));
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTreeArquivos.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane2.setViewportView(jTreeArquivos);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1167, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton7)
                        .addGap(62, 62, 62)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)
                        .addGap(46, 46, 46)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAjuda))))
                    .addComponent(jPanelTerminal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(50, 50, 50))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnAjuda, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(1, 1, 1))
                            .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jPanelTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
        );

        jPanel3.getAccessibleContext().setAccessibleDescription("");

        jMenu1.setText("Arquivo");

        jMenuItem3.setText("Abrir arquivo");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setText("Salvar arquivo");
        jMenuItem4.setPreferredSize(new java.awt.Dimension(117, 22));
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Run");
        jMenuBar1.add(jMenu2);

        jMenu3.setText("LLM");

        jMenuItem2.setText("jMenuItem2");
        jMenu3.add(jMenuItem2);

        jMenuItem1.setText("jMenuItem1");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Historicos");
        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1427, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
     CaixaTexto.setText("");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        GeradorDeTesteJava g = new GeradorDeTesteJava("http://localhost:11434/", "qwen2.5-coder:3b", 0.5f);
        TelaResposta t = new TelaResposta();
        
        try {
            if (CaixaTexto.getText().equals("")){
                JOptionPane.showMessageDialog(null, "A caixa de texto está vazia");
            }
            else{
                  g.gerarTestes(CaixaTexto.getText(), t);
                  t.setVisible(true);
                   
        String userInput = CaixaTexto.getText();
        String aiResponse = t.resposta.getText();
        String respostaLimpa = aiResponse.replaceAll("<[^>]*>", ""); // Remove tags HTML

        // Salva no banco de dados
        BD.saveInteraction(userInput, respostaLimpa);
    
            }
          
        } catch (Exception ex) {
            Logger.getLogger(TelaPrincipalEsul.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
            MelhoradorDeCodigo melhorador = new MelhoradorDeCodigo("http://localhost:11434/", "qwen2.5-coder:3b", 0.5f);
         TelaResposta telaResposta = new TelaResposta();

         try {
             if (CaixaTexto.getText().isEmpty()) {
                 JOptionPane.showMessageDialog(null, "A caixa de texto está vazia");
             } else {
                 melhorador.melhorarCodigo(CaixaTexto.getText(), telaResposta);
                 telaResposta.setVisible(true);
                  // Captura entrada do usuário e resposta da IA
        String userInput = CaixaTexto.getText();
        String aiResponse = telaResposta.resposta.getText();
        String respostaLimpa = aiResponse.replaceAll("<[^>]*>", ""); // Remove tags HTML

        // Salva no banco de dados
        BD.saveInteractionI(userInput, respostaLimpa);
             }
         } catch (Exception ex) {
             Logger.getLogger(TelaPrincipalEsul.class.getName()).log(Level.SEVERE, null, ex);
         }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

    TelaHistTeste telaHistorico = new TelaHistTeste(); 
    telaHistorico.setVisible(true); 
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
          TelaHistMelhoria telaHistorico = new TelaHistMelhoria(); 
          telaHistorico.setVisible(true); 
    }//GEN-LAST:event_jButton6ActionPerformed
    private void printTreeStructure(DefaultMutableTreeNode node, String indent) {
    System.out.println(indent + node.getUserObject());
    for (int i = 0; i < node.getChildCount(); i++) {
        printTreeStructure((DefaultMutableTreeNode) node.getChildAt(i), indent + "  ");
    }
}
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
  JFileChooser fileChooser = new JFileChooser();

        // Configurar para aceitar apenas diretórios
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Abrir Projeto Java");
        fileChooser.setApproveButtonText("Abrir Projeto");

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();

            // Criar um novo nó para o projeto selecionado
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(selectedDirectory);
            addFilesToTree(selectedDirectory, projectNode);

            // Adicionar o nó do projeto ao nó raiz
            treeModel.insertNodeInto(projectNode, rootNode, rootNode.getChildCount());

            // Expandir o nó do projeto
            jTreeArquivos.expandPath(new TreePath(projectNode.getPath()));

            // Imprimir a estrutura da árvore APÓS adicionar o projeto
            printTreeStructure(rootNode, "");
        }
    }

    private void addFilesToTree(File directory, DefaultMutableTreeNode parentNode) {
        File[] files = directory.listFiles();
        if (files != null) {
            // Ordenar arquivos: diretórios primeiro, depois arquivos
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            for (File file : files) {
                // Ignorar arquivos que não são .java
                if (file.isFile() && !file.getName().toLowerCase().endsWith(".java")) {
                    continue;
                }

                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
                parentNode.add(childNode);

                // Se for um diretório, adicionar seus arquivos recursivamente
                if (file.isDirectory()) {
                    addFilesToTree(file, childNode);
                }
            }
        }
        
    }//GEN-LAST:event_jButton7ActionPerformed
     
    private void btnAjudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAjudaActionPerformed
            Ajuda telaAjuda = new Ajuda(); 
            telaAjuda.setVisible(true); 
    }//GEN-LAST:event_btnAjudaActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
                jPanelTerminal.setVisible(true);
                jTextArea1.setText("");
                ProjectCompiler p = new ProjectCompiler(jTreeArquivos, jTextArea1);
                java.time.LocalDateTime agora = java.time.LocalDateTime.now();
                java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                jTextArea1.append("Terminal iniciado em: " + agora.format(formato) + "\n");

                // Configurar redirecionamento de saída
                TextAreaOutputStream taOutputStream = new TextAreaOutputStream(jTextArea1);
                PrintStream printStream = new PrintStream(taOutputStream, true);
                System.setOut(printStream);
                System.setErr(printStream);

                try {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeArquivos.getLastSelectedPathComponent();
                    File executionDirectory = null;

                    if (selectedNode != null) {
                        Object userObject = selectedNode.getUserObject();
                        if (userObject instanceof File && ((File) userObject).isDirectory()) {
                            executionDirectory = (File) userObject;
                        } else if (userObject instanceof File && ((File) userObject).getName().endsWith(".java")) {
                            executionDirectory = ((File) userObject).getParentFile();
                        }
                    }

                    if (executionDirectory != null) {
                        // Há um diretório selecionado na JTree
                        p.executarProjeto(executionDirectory);
                    } else {
                        // Se nenhum diretório estiver selecionado, compile o conteúdo do editor
                        ProjectCompiler compilerEditor = new ProjectCompiler(jTextArea1, CaixaTexto.getText());
                        compilerEditor.executarProjeto(null); // Passa null para indicar que não há diretório selecionado
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erro ao executar o projeto: " + ex.getMessage());
                }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // Botão Fechar Terminal
        jTextArea1.setText("");
    jPanelTerminal.setVisible(false);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // Botão Minimizar Terminal
        jPanelTerminal.setVisible(false);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            arquivoAtual = selectedFile; // Atualiza o arquivo atual
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                CaixaTexto.setText(""); // Limpa a área de texto antes
                String line;
                while ((line = reader.readLine()) != null) {
                    CaixaTexto.append(line + "\n"); // Adiciona linha por linha
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo: " + ex.getMessage());
            }
        }         // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null); // Mostra o diálogo de salvar

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                writer.write(CaixaTexto.getText()); // Escreve o conteúdo da área de texto
                JOptionPane.showMessageDialog(null, "Arquivo salvo com sucesso!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Erro ao salvar o arquivo: " + ex.getMessage());
            }
        }        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem4ActionPerformed
    
    
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
            java.util.logging.Logger.getLogger(TelaPrincipalEsul.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipalEsul.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipalEsul.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipalEsul.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TelaPrincipalEsul tela = new TelaPrincipalEsul();
                tela.setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAjuda;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelTerminal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTree jTreeArquivos;
    // End of variables declaration//GEN-END:variables

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode rootNode, File selectedFile) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
