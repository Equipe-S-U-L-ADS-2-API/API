/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import esulDAO.BD;
import ferramentas.GeradorDeTesteJava;
import ferramentas.MelhoradorDeCodigo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Arrays; 
import javax.swing.AbstractAction; 
import javax.swing.ActionMap;      
import javax.swing.InputMap;      
import javax.swing.KeyStroke;      
import java.awt.event.ActionEvent; 
import javax.swing.JComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author Lincolzera
 */
public class TelaPrincipalEsul extends javax.swing.JFrame implements ferramentas.RespostaHandler {
    
    private Map<File, RSyntaxTextArea> openFilesMap; // Para rastrear arquivos abertos e suas áreas de texto
    // O painelCodigo já é o jPanel3 (JTabbedPane agora)
    private final javax.swing.JTabbedPane painelCodigo;
    private RSyntaxTextArea textAreaResultado;
    private RSyntaxTextArea textAreaCodigo; // Nova área de texto para código

    public TelaPrincipalEsul() {
        getRootPane().putClientProperty("JRootPane.titleBarBackground", new Color(16,22,20));
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        
        initComponents();
        setIconImage(new ImageIcon(getClass().getResource("/Imagens/esul.png")).getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        jPanelTerminal.setVisible(false);
        panelResultado.setVisible(true);
        painelCodigo = jPanel3;
        setLocationRelativeTo(null);
        openFilesMap = new HashMap<>();
        configurarCaixaTexto();
        
        // Initialize textAreaResultado
        textAreaResultado = new RSyntaxTextArea();
        textAreaResultado.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textAreaResultado.setCodeFoldingEnabled(true);
        textAreaResultado.setAntiAliasingEnabled(true);
        textAreaResultado.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 14));
        textAreaResultado.setBackground(new java.awt.Color(255, 255, 255));
        textAreaResultado.setForeground(new java.awt.Color(0, 0, 0));
        textAreaResultado.setLineWrap(true);
        textAreaResultado.setWrapStyleWord(true);
        scrollPaneResultado.setViewportView(textAreaResultado);
        
        // Initialize textAreaCodigo
        textAreaCodigo = new RSyntaxTextArea();
        textAreaCodigo.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textAreaCodigo.setCodeFoldingEnabled(true);
        textAreaCodigo.setAntiAliasingEnabled(true);
        textAreaCodigo.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 14));
        textAreaCodigo.setBackground(new java.awt.Color(255, 255, 255));
        textAreaCodigo.setForeground(new java.awt.Color(0, 0, 0));
        textAreaCodigo.setLineWrap(true);
        textAreaCodigo.setWrapStyleWord(true);
        scrollPaneCodigo.setViewportView(textAreaCodigo);
        
        jTreeArquivos.setCellRenderer(new FileTreeCellRenderer());
jTreeArquivos.addTreeSelectionListener(new TreeSelectionListener() {
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeArquivos.getLastSelectedPathComponent();

        if (selectedNode == null) {
            return;
        }

        Object userObject = selectedNode.getUserObject();

        if (userObject instanceof File) {
            File file = (File) userObject;

            if (file.isFile()) {
                if (openFilesMap.containsKey(file)) {
                    RSyntaxTextArea existingTextArea = openFilesMap.get(file);
                    for (int i = 0; i < jPanel3.getTabCount(); i++) { // Use jPanel3 diretamente
                        JPanel tabContentPanel = (JPanel) jPanel3.getComponentAt(i);
                        RTextScrollPane scrollPane = (RTextScrollPane) tabContentPanel.getComponent(0);
                        if (scrollPane.getViewport().getView() == existingTextArea) {
                            jPanel3.setSelectedIndex(i); // Use jPanel3 diretamente
                            break;
                        }
                    }
                } else {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        RSyntaxTextArea newTextArea = createNewRSyntaxTextArea();
                        newTextArea.setText("");
                        String line;
                        while ((line = reader.readLine()) != null) {
                            newTextArea.append(line + "\n");
                        }
                        newTextArea.setCaretPosition(0);

                        addFileTab(file, newTextArea);

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(TelaPrincipalEsul.this,
                                "Erro ao ler o arquivo: " + ex.getMessage(),
                                "Erro de Leitura de Arquivo", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
});
        
        // Configurar atalhos de teclado para a janela principal
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        // Ctrl+S para salvar
        KeyStroke ctrlS = KeyStroke.getKeyStroke("control S");
        inputMap.put(ctrlS, "saveFile");
        actionMap.put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jMenuItem4ActionPerformed(null);
            }
        });

        // Ctrl+Shift+S para salvar como
        KeyStroke ctrlShiftS = KeyStroke.getKeyStroke("control shift S");
        inputMap.put(ctrlShiftS, "saveAs");
        actionMap.put("saveAs", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jMenuItem4ActionPerformed(null);
            }
        });

        // Add window listener to clean up bin directory on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                File binDir = new File("bin");
                if (binDir.exists() && binDir.isDirectory()) {
                    deleteDirectoryContents(binDir);
                }
            }
        });
    }

    private void deleteDirectoryContents(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryContents(file);
                }
                file.delete();
            }
        }
    }

    private RSyntaxTextArea createNewRSyntaxTextArea() {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 18));
        textArea.setBackground(new java.awt.Color(255, 255, 255));
        textArea.setForeground(new java.awt.Color(0, 0, 0));
        try {
            InputStream in = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml");
            if (in == null) {
                System.err.println("Arquivo de tema não encontrado!");
            } else {
                Theme theme = Theme.load(in);
                theme.apply(textArea);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return textArea;
    }

private void addFileTab(File file, RSyntaxTextArea textArea) {
    RTextScrollPane scrollPane = new RTextScrollPane(textArea);
    
    // Crie um painel para o título da aba e o botão de fechar
    JPanel tabTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tabTitlePanel.setOpaque(false); // Torna-o transparente
    JLabel titleLabel = new JLabel(file.getName());
    JButton closeButton = new JButton("x"); // Um pequeno 'x' para fechar
    closeButton.setFont(closeButton.getFont().deriveFont(10f)); // Torna a fonte menor
    closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0)); // Remove a margem
    closeButton.setContentAreaFilled(false); // Torna a área de conteúdo transparente
    closeButton.setBorderPainted(false); // Sem borda
    closeButton.setFocusPainted(false); // Sem borda de foco
    closeButton.setForeground(Color.RED); // Colore o 'x' de vermelho
    

    tabTitlePanel.add(titleLabel);
    tabTitlePanel.add(closeButton);

    // Adicione o scrollPane a um novo painel para conter o conteúdo da aba
    JPanel tabContentPanel = new JPanel(new java.awt.BorderLayout());
    tabContentPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
    
    // Adicione a aba ao JTabbedPane (que agora é jPanel3)
    jPanel3.addTab(null, tabContentPanel); // Adiciona com título nulo para usar componente de aba personalizado
    int newTabIndex = jPanel3.indexOfComponent(tabContentPanel);
    jPanel3.setTabComponentAt(newTabIndex, tabTitlePanel); // Define o componente de aba personalizado

    jPanel3.setSelectedIndex(newTabIndex); // Seleciona a aba recém-criada
    openFilesMap.put(file, textArea); // Adiciona ao mapa

    // Adiciona um listener de ação para o botão de fechar
    closeButton.addActionListener(e -> {
        int index = jPanel3.indexOfComponent(tabContentPanel);
        if (index != -1) {
            jPanel3.removeTabAt(index);
            openFilesMap.remove(file); // Remove do mapa quando a aba é fechada
        }
    });
}
       
private void configurarCaixaTexto() {
    // painelCodigo já é jPanel3, que já é um JTabbedPane
    // Então, as configurações de layout para painelCodigo não são mais necessárias aqui,
    // pois o JTabbedPane já gerencia suas abas.
    
    // Configurações visuais do JTabbedPane (jPanel3)
    jPanel3.setBackground(new java.awt.Color(42, 45, 44)); // Exemplo de cor de fundo para as abas
    jPanel3.setForeground(new java.awt.Color(255, 211, 94)); // Exemplo de cor de primeiro plano para as abas

    // Adicione uma aba inicial vazia ou de "Boas-vindas"
    RSyntaxTextArea initialTextArea = createNewRSyntaxTextArea();
    initialTextArea.setText("\n\n// Seu código aqui //\n                              ");
    // Usamos um objeto File dummy para a aba inicial, pois ela não representa um arquivo salvo.
    addFileTab(new File("Novo Arquivo.java"), initialTextArea); 
}
private RSyntaxTextArea getSelectedTextArea() {
    int selectedIndex = jPanel3.getSelectedIndex();
    if (selectedIndex == -1) {
        return null; // Nenhuma aba selecionada
    }
    JPanel tabContentPanel = (JPanel) jPanel3.getComponentAt(selectedIndex);
    RTextScrollPane scrollPane = (RTextScrollPane) tabContentPanel.getComponent(0); // Assume que o RTextScrollPane é o primeiro componente
    return (RSyntaxTextArea) scrollPane.getViewport().getView();
}
private File getSelectedFile() {
    RSyntaxTextArea currentTextArea = getSelectedTextArea();
    if (currentTextArea == null) {
        return null;
    }
    for (Map.Entry<File, RSyntaxTextArea> entry : openFilesMap.entrySet()) {
        if (entry.getValue() == currentTextArea) {
            return entry.getKey();
        }
    }
    return null; // Retorna null se não encontrar o arquivo associado (ex: aba "Novo Arquivo")
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
        jPanel3 = new javax.swing.JTabbedPane();
        jLabel4 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
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
        jButton11 = new javax.swing.JButton();
        panelResultado = new javax.swing.JPanel();
        scrollPaneResultado = new javax.swing.JScrollPane();
        jLabel3 = new javax.swing.JLabel();
        btnFecharResultado = new javax.swing.JButton();
        btnMinimizarResultado = new javax.swing.JButton();
        scrollPaneCodigo = new javax.swing.JScrollPane();
        btnCopiar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
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
        jLabel1.setForeground(new java.awt.Color(255, 211, 94));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("SUL");

        jButton2.setBackground(new java.awt.Color(22, 28, 34));
        jButton2.setFont(new java.awt.Font("Lucida Sans", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Limpar código");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(22, 28, 34));
        jButton4.setFont(new java.awt.Font("Inria Sans", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/teste.png"))); // NOI18N
        jButton4.setText("Gerar teste");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(22, 28, 34));
        jButton5.setFont(new java.awt.Font("Lucida Sans", 1, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/database.png"))); // NOI18N
        jButton5.setText("Teste(s)");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 211, 94));
        jLabel4.setText("Históricos:");

        jButton6.setBackground(new java.awt.Color(22, 28, 34));
        jButton6.setFont(new java.awt.Font("Lucida Sans", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/database.png"))); // NOI18N
        jButton6.setText("Melhoria(s)");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setBackground(new java.awt.Color(22, 28, 34));
        jButton7.setFont(new java.awt.Font("Inria Sans", 1, 12)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 211, 94));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/abrir.png"))); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(22, 28, 34));
        jButton3.setFont(new java.awt.Font("Inria Sans", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/melhorias.png"))); // NOI18N
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
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/run.png"))); // NOI18N
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
        btnAjuda.setFont(new java.awt.Font("Lucida Sans", 1, 14)); // NOI18N
        btnAjuda.setForeground(new java.awt.Color(255, 255, 255));
        btnAjuda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/information.png"))); // NOI18N
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

        jButton11.setBackground(new java.awt.Color(22, 28, 34));
        jButton11.setFont(new java.awt.Font("Inria Sans", 1, 12)); // NOI18N
        jButton11.setForeground(new java.awt.Color(255, 211, 94));
        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/criar.png"))); // NOI18N
        jButton11.setActionCommand("Abrir Arquivo");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        panelResultado.setBackground(new java.awt.Color(42, 45, 44));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 211, 94));
        jLabel3.setText("Resultado");

        btnFecharResultado.setBackground(new java.awt.Color(255, 211, 94));
        btnFecharResultado.setForeground(new java.awt.Color(16, 22, 20));
        btnFecharResultado.setText("Fechar");
        btnFecharResultado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharResultadoActionPerformed(evt);
            }
        });

        btnMinimizarResultado.setBackground(new java.awt.Color(255, 211, 94));
        btnMinimizarResultado.setForeground(new java.awt.Color(16, 22, 20));
        btnMinimizarResultado.setText("Minimizar");
        btnMinimizarResultado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMinimizarResultadoActionPerformed(evt);
            }
        });

        btnCopiar.setText("Copiar");
        btnCopiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopiarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelResultadoLayout = new javax.swing.GroupLayout(panelResultado);
        panelResultado.setLayout(panelResultadoLayout);
        panelResultadoLayout.setHorizontalGroup(
            panelResultadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelResultadoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelResultadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneResultado)
                    .addGroup(panelResultadoLayout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMinimizarResultado)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFecharResultado))
                    .addComponent(scrollPaneCodigo)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelResultadoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCopiar)))
                .addContainerGap())
        );
        panelResultadoLayout.setVerticalGroup(
            panelResultadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelResultadoLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(panelResultadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(btnFecharResultado)
                    .addComponent(btnMinimizarResultado))
                .addGap(18, 18, 18)
                .addComponent(scrollPaneResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCopiar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        jLabel5.setFont(new java.awt.Font("Inria Sans", 1, 36)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("IDE");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelTerminal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                .addComponent(btnAjuda)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelResultado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(317, 317, 317))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelResultado, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jPanelTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(btnAjuda))
                                            .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel3)))
                        .addGap(194, 194, 194)))
                .addGap(46, 46, 46))
        );

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
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1856, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 813, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    int selectedIndex = jPanel3.getSelectedIndex();
    if (selectedIndex != -1) {
        JPanel tabContentPanel = (JPanel) jPanel3.getComponentAt(selectedIndex);
        RTextScrollPane scrollPane = (RTextScrollPane) tabContentPanel.getComponent(0);
        RSyntaxTextArea currentTextArea = (RSyntaxTextArea) scrollPane.getViewport().getView();
        currentTextArea.setText("");
    } else {
        JOptionPane.showMessageDialog(this, "Nenhuma aba de código está aberta para limpar.");
    }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        panelResultado.setVisible(true);
        RSyntaxTextArea currentTextArea = getSelectedTextArea();
        if (currentTextArea == null) {
            JOptionPane.showMessageDialog(this, "Nenhuma aba de código está aberta para gerar testes.");
            return;
        }

        GeradorDeTesteJava g = new GeradorDeTesteJava("http://localhost:11434/", "qwen2.5-coder:3b", 0.5f);
        g.setRespostaHandler(this);
        
        try {
            if (currentTextArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "A caixa de texto está vazia");
            } else {
                g.gerarTestes(currentTextArea.getText(), textAreaResultado);
                
                String userInput = currentTextArea.getText();
                String aiResponse = textAreaResultado.getText() + "\n" + textAreaCodigo.getText();

                // Salva no banco de dados
                BD.saveInteraction(userInput, aiResponse);
            }
        } catch (Exception ex) {
            Logger.getLogger(TelaPrincipalEsul.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        panelResultado.setVisible(true);
        // Obtém a área de texto atual
        RSyntaxTextArea currentTextArea = getSelectedTextArea();
        if (currentTextArea == null) {
            JOptionPane.showMessageDialog(this, "Nenhuma aba de código está aberta para gerar melhoria.");
            return;
        }

        // Cria instância do melhorador
        MelhoradorDeCodigo melhorador = new MelhoradorDeCodigo("http://localhost:11434/", "qwen2.5-coder:3b", 0.5f);
        melhorador.setRespostaHandler(this);

        try {
            // Verifica se há texto na área
            if (currentTextArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "A caixa de texto está vazia");
                return;
            }

            // Obtém o texto selecionado ou todo o conteúdo
            String textoSelecionado = currentTextArea.getSelectedText();
            String userInput;

            // Verifica se há texto selecionado
            if (textoSelecionado != null && !textoSelecionado.trim().isEmpty()) {
                // Melhora apenas o texto selecionado
                melhorador.melhorarSelecao(textoSelecionado, textAreaResultado);
                userInput = textoSelecionado;
            } else {
                // Melhora o código inteiro
                String codigoCompleto = currentTextArea.getText();
                melhorador.melhorarCodigo(codigoCompleto, textAreaResultado);
                userInput = codigoCompleto;
            }
            
            // Captura resposta da IA
            String aiResponse = textAreaResultado.getText() + "\n" + textAreaCodigo.getText();

            // Salva no banco de dados
            BD.saveInteraction(userInput, aiResponse);
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
        private void loadDirectoryToTree(File directory, DefaultMutableTreeNode parentNode) {
        if (!directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        Arrays.sort(files); // Opcional: para manter os arquivos e diretórios em ordem alfabética

        for (File file : files) {
            // Ignorar arquivos e diretórios ocultos
            if (file.isHidden()) {
                continue;
            }

            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file);
            parentNode.add(newNode);

            if (file.isDirectory()) {
                loadDirectoryToTree(file, newNode); // Recursão para subdiretórios
            }
        }
    }
    // Método auxiliar para encontrar um nó na JTree (já existente)

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Apenas diretórios
        fileChooser.setDialogTitle("Selecionar Diretório do Projeto");

        int result = fileChooser.showOpenDialog(TelaPrincipalEsul.this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();

            DefaultTreeModel treeModel = (DefaultTreeModel) jTreeArquivos.getModel();
            DefaultMutableTreeNode rootNode;

            // Se o modelo da árvore não existir ou for o nó padrão vazio, inicialize-o
            if (treeModel == null || jTreeArquivos.getModel().getRoot() == null || 
                ((DefaultMutableTreeNode)jTreeArquivos.getModel().getRoot()).getUserObject().equals("root") && 
                ((DefaultMutableTreeNode)jTreeArquivos.getModel().getRoot()).getChildCount() == 0) {
                
                rootNode = new DefaultMutableTreeNode("Arquivos do Projeto");
                treeModel = new DefaultTreeModel(rootNode);
                jTreeArquivos.setModel(treeModel);
            } else {
                rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
                // Se a raiz for apenas "root", e não "Arquivos do Projeto", podemos mudar
                if (rootNode.getUserObject().equals("root") && rootNode.getChildCount() == 0) {
                     rootNode.setUserObject("Arquivos do Projeto");
                }
            }

            // Adiciona o diretório selecionado como um novo nó raiz ou filho da raiz existente
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(selectedDirectory);
            treeModel.insertNodeInto(projectNode, rootNode, rootNode.getChildCount());
            
            // Carrega recursivamente os arquivos e subdiretórios no novo nó
            loadDirectoryToTree(selectedDirectory, projectNode);

            // Expande o nó do projeto e rola para torná-lo visível
            jTreeArquivos.expandPath(new TreePath(projectNode.getPath()));
            jTreeArquivos.scrollPathToVisible(new TreePath(projectNode.getPath()));
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void btnAjudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAjudaActionPerformed
            Ajuda telaAjuda = new Ajuda(); 
            telaAjuda.setVisible(true); 
    }//GEN-LAST:event_btnAjudaActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
    jPanelTerminal.setVisible(true);
    jTextArea1.setText("");

    java.time.LocalDateTime agora = java.time.LocalDateTime.now();
    java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    jTextArea1.append("Terminal iniciado em: " + agora.format(formato) + "\n");

    // Configurar redirecionamento de saída
    TextAreaOutputStream taOutputStream = new TextAreaOutputStream(jTextArea1);
    PrintStream printStream = new PrintStream(taOutputStream, true);
    System.setOut(printStream);
    System.setErr(printStream);

    RSyntaxTextArea currentTextArea = getSelectedTextArea();
    if (currentTextArea == null) {
        jTextArea1.append("Erro: Nenhuma aba de código selecionada para executar.\n");
        return;
    }
    
    File currentFile = getSelectedFile(); // Obtém o File associado à aba
    String fileName = (currentFile != null && !currentFile.getName().equals("Novo Arquivo.java")) 
                      ? currentFile.getName() 
                      : "UnnamedClass.java"; 

    try {
        ProjectCompiler compiler = new ProjectCompiler(jTreeArquivos, jTextArea1); 
        // PASSE O ARQUIVO ORIGINAL (currentFile) AQUI!
        compiler.executarCodigoDoTexto(currentTextArea.getText(), fileName, currentFile); 

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Erro ao executar o projeto: " + e.getMessage());
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

        if (openFilesMap.containsKey(selectedFile)) {
            RSyntaxTextArea existingTextArea = openFilesMap.get(selectedFile);
            for (int i = 0; i < jPanel3.getTabCount(); i++) {
                JPanel tabContentPanel = (JPanel) jPanel3.getComponentAt(i);
                RTextScrollPane scrollPane = (RTextScrollPane) tabContentPanel.getComponent(0);
                if (scrollPane.getViewport().getView() == existingTextArea) {
                    jPanel3.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                RSyntaxTextArea newTextArea = createNewRSyntaxTextArea();
                newTextArea.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    newTextArea.append(line + "\n");
                }
                newTextArea.setCaretPosition(0);

                addFileTab(selectedFile, newTextArea);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo: " + ex.getMessage());
            }
        }
        // Opcional: Se desejar, pode adicionar a lógica de atualização do jTreeArquivos aqui também,
        // similar ao que foi feito em jButton7ActionPerformed.
    }       // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
       RSyntaxTextArea currentTextArea = getSelectedTextArea();
    if (currentTextArea == null) {
        JOptionPane.showMessageDialog(null, "Nenhuma aba selecionada para salvar.");
        return;
    }

    File currentFile = getSelectedFile(); // Tenta obter o File associado a esta aba

    JFileChooser fileChooser = new JFileChooser();
    // Se a aba tem um arquivo associado e não é o "Novo Arquivo.java" dummy, pré-seleciona o arquivo no JFileChooser
    if (currentFile != null && !currentFile.getName().equals("Novo Arquivo.java")) { 
        fileChooser.setSelectedFile(currentFile); 
    }
    
    int result = fileChooser.showSaveDialog(null);

    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
            writer.write(currentTextArea.getText());
            JOptionPane.showMessageDialog(null, "Arquivo salvo com sucesso!");
            
            // Atualiza o mapa se o arquivo foi salvo com um novo nome ou para um novo arquivo
            // E atualiza o título da aba se o nome do arquivo mudou
            if (!openFilesMap.containsKey(selectedFile) || (currentFile != null && !currentFile.equals(selectedFile))) {
                // Remove a entrada antiga se o nome do arquivo mudou
                if (currentFile != null) {
                    openFilesMap.remove(currentFile);
                }
                openFilesMap.put(selectedFile, currentTextArea);
                // Atualiza o título da aba
                int selectedIndex = jPanel3.getSelectedIndex();
                if (selectedIndex != -1) {
                    JLabel titleLabel = (JLabel) ((JPanel)jPanel3.getTabComponentAt(selectedIndex)).getComponent(0);
                    titleLabel.setText(selectedFile.getName());
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar o arquivo: " + ex.getMessage());
        }
    }  // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
       // Adicione uma aba inicial vazia ou de "Boas-vindas"
        RSyntaxTextArea initialTextArea2 = createNewRSyntaxTextArea();
        initialTextArea2.setText("\n\n                             // Seu código aqui //\n                              ");
        // Usamos um objeto File dummy para a aba inicial, pois ela não representa um arquivo salvo.
        addFileTab(new File("Novo.java"), initialTextArea2);
    }//GEN-LAST:event_jButton11ActionPerformed

    private void btnFecharResultadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharResultadoActionPerformed
        // TODO add your handling code here:
        // Botão Fechar Terminal
        panelResultado.setVisible(false);
    }//GEN-LAST:event_btnFecharResultadoActionPerformed

    private void btnMinimizarResultadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMinimizarResultadoActionPerformed
        // TODO add your handling code here:
        panelResultado.setVisible(false);
    }//GEN-LAST:event_btnMinimizarResultadoActionPerformed

    private void btnCopiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopiarActionPerformed
        if (textAreaCodigo != null && textAreaCodigo.getText().length() > 0) {
            textAreaCodigo.selectAll();
            textAreaCodigo.copy();
            textAreaCodigo.setSelectionStart(0);
            textAreaCodigo.setSelectionEnd(0);
            JOptionPane.showMessageDialog(this, "Código copiado para a área de transferência!");
        } else {
            JOptionPane.showMessageDialog(this, "Não há código para copiar!");
        }
    }//GEN-LAST:event_btnCopiarActionPerformed

    // Método para separar código e texto da resposta do Ollama
    @Override
    public void separarCodigoETexto(String resposta) {
        StringBuilder texto = new StringBuilder();
        StringBuilder codigo = new StringBuilder();
        boolean dentroDoCodigo = false;
        int indentacao = 0;
        
        String[] linhas = resposta.split("\n");
        for (String linha : linhas) {
            // Verifica se é início de bloco de código
            if (linha.trim().startsWith("```java") || linha.trim().startsWith("```")) {
                dentroDoCodigo = true;
                continue;
            }
            
            // Verifica se é fim de bloco de código
            if (dentroDoCodigo && linha.trim().equals("```")) {
                dentroDoCodigo = false;
                continue;
            }
            
            // Se estiver dentro do código, adiciona ao StringBuilder de código
            if (dentroDoCodigo) {
                codigo.append(linha).append("\n");
            } else {
                // Se não estiver dentro do código, verifica se a linha parece código por indentação
                int espacosInicio = linha.length() - linha.replaceAll("^\\s+", "").length();
                if (espacosInicio >= 4 && linha.trim().length() > 0) {
                    codigo.append(linha).append("\n");
                } else {
                    texto.append(linha).append("\n");
                }
            }
        }
        
        // Atualiza as áreas de texto
        textAreaResultado.setText(texto.toString().trim());
        textAreaCodigo.setText(codigo.toString().trim());
        
        // Posiciona o cursor no início
        textAreaResultado.setCaretPosition(0);
        textAreaCodigo.setCaretPosition(0);
    }
    
    
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
    private javax.swing.JButton btnCopiar;
    private javax.swing.JButton btnFecharResultado;
    private javax.swing.JButton btnMinimizarResultado;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
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
    private javax.swing.JTabbedPane jPanel3;
    private javax.swing.JPanel jPanelTerminal;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTree jTreeArquivos;
    private javax.swing.JPanel panelResultado;
    private javax.swing.JScrollPane scrollPaneCodigo;
    private javax.swing.JScrollPane scrollPaneResultado;
    // End of variables declaration//GEN-END:variables

private DefaultMutableTreeNode findNode(DefaultMutableTreeNode root, File targetFile) {
    if (root == null || targetFile == null) {
        return null;
    }

    Object userObject = root.getUserObject();

    // Verifica se o userObject é um File e se é igual ao targetFile
    if (userObject instanceof File && ((File) userObject).equals(targetFile)) {
        return root; // Encontrou o nó!
    }

    // Percorre os filhos do nó raiz recursivamente
    for (int i = 0; i < root.getChildCount(); i++) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
        DefaultMutableTreeNode foundNode = findNode(child, targetFile);
        if (foundNode != null) {
            return foundNode; // Encontrou o nó em um dos sub-ramos
        }
    }

    return null; // Não encontrou o nó neste ramo
}
}
