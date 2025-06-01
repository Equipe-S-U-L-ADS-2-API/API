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
import java.awt.Font;
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
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import java.nio.file.*;

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
    private FileWatcher fileWatcher;

    public TelaPrincipalEsul() {
        getRootPane().putClientProperty("JRootPane.titleBarBackground", new Color(16,22,20));
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        
        initComponents();
        setIconImage(new ImageIcon(getClass().getResource("/Imagens/esul.png")).getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        jPanelTerminal.setVisible(false);
        panelResultado.setVisible(false);
        jScrollPane2.setVisible(false);
        painelCodigo = jPanel3;
        setLocationRelativeTo(null);
        openFilesMap = new HashMap<>();
        configurarCaixaTexto();
        
        // Initialize textAreaResultado com tema dark
        textAreaResultado = createDarkRSyntaxTextArea();
        textAreaResultado.setLineWrap(true);
        textAreaResultado.setWrapStyleWord(true);
        scrollPaneResultado.setViewportView(textAreaResultado);
        
        // Initialize textAreaCodigo com tema dark
        textAreaCodigo = createDarkRSyntaxTextArea();
        textAreaCodigo.setLineWrap(true);
        textAreaCodigo.setWrapStyleWord(true);
        scrollPaneCodigo.setViewportView(textAreaCodigo);
        
        jTreeArquivos.setCellRenderer(new FileTreeCellRenderer());
        
        // Adicionar menu de contexto à JTree
        JPopupMenu treePopupMenu = new JPopupMenu();
        JMenuItem closeItem = new JMenuItem("Fechar");
        closeItem.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeArquivos.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof File) {
                File file = (File) node.getUserObject();
                if (file.isFile() && file.getName().endsWith(".java") || file.isDirectory()) {
                    // Remover da JTree
                    DefaultTreeModel model = (DefaultTreeModel) jTreeArquivos.getModel();
                    model.removeNodeFromParent(node);
                    
                    // Se for um arquivo, remover da lista de arquivos abertos
                    if (file.isFile()) {
                        openFilesMap.remove(file);
                    }
                    
                    // Remover arquivos compilados
                    if (file.isFile()) {
                        String className = file.getName().replace(".java", "");
                        String packageName = extractPackageNameFromFile(file);
                        String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
                        String projectPath = System.getProperty("user.dir");
                        Path targetDir = Paths.get(projectPath, "API", "OllamaESUL", "main", "target");
                        Path binDir = targetDir.resolve("bin");
                        File classFile = new File(binDir.toFile(), fullClassName.replace('.', File.separatorChar) + ".class");
                        if (classFile.exists()) {
                            classFile.delete();
                        }
                    } else if (file.isDirectory()) {
                        removeCompiledFiles(file);
                    }
                }
            }
        });
        treePopupMenu.add(closeItem);
        
        jTreeArquivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopupMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupMenu(e);
            }

            private void showPopupMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = jTreeArquivos.getClosestRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        jTreeArquivos.setSelectionRow(row);
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                            jTreeArquivos.getLastSelectedPathComponent();
                        
                        if (node != null && node.getUserObject() instanceof File) {
                            File file = (File) node.getUserObject();
                            if (file.isFile() && file.getName().endsWith(".java") || file.isDirectory()) {
                                treePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        });

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
                String projectPath = System.getProperty("user.dir");
                Path targetDir = Paths.get(projectPath, "API", "OllamaESUL", "main", "target");
                Path binDir = targetDir.resolve("bin");
                if (Files.exists(binDir) && Files.isDirectory(binDir)) {
                    deleteDirectoryContents(binDir.toFile());
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
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        try {
            // Carregar o tema default
            InputStream in = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml");
            if (in != null) {
                Theme theme = Theme.load(in);
                theme.apply(textArea);
            }
        } catch (Exception e) {
            // Se houver erro ao carregar o tema, usar configurações padrão
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            textArea.setCaretColor(Color.BLACK);
        }
        
        return textArea;
    }

    private RSyntaxTextArea createDarkRSyntaxTextArea() {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        try {
            // Carregar o tema dark
            InputStream in = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml");
            if (in != null) {
                Theme theme = Theme.load(in);
                theme.apply(textArea);
            }
        } catch (Exception e) {
            // Se houver erro ao carregar o tema, usar configurações padrão dark
            textArea.setBackground(new Color(43, 43, 43));
            textArea.setForeground(Color.WHITE);
            textArea.setCaretColor(Color.WHITE);
        }
        
        return textArea;
    }

    private void configurarCaixaTexto() {
        // Configurações visuais do JTabbedPane (jPanel3)
        jPanel3.setBackground(new java.awt.Color(42, 45, 44));
        jPanel3.setForeground(new java.awt.Color(255, 211, 94));

        // Adicione uma aba inicial vazia ou de "Boas-vindas"
        RSyntaxTextArea initialTextArea = createNewRSyntaxTextArea();
        initialTextArea.setText("\n\n// Seu código aqui //\n                              ");
        addFileTab(new File("Novo Arquivo.java"), initialTextArea);
    }

    private void addFileTab(File file, RSyntaxTextArea textArea) {
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        
        // Crie um painel para o título da aba e o botão de fechar
        JPanel tabTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabTitlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel(file.getName());
        JButton closeButton = new JButton("x");
        closeButton.setFont(closeButton.getFont().deriveFont(10f));
        closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setForeground(new Color(255,211,54));

        tabTitlePanel.add(titleLabel);
        tabTitlePanel.add(closeButton);

        JPanel tabContentPanel = new JPanel(new java.awt.BorderLayout());
        tabContentPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
        
        jPanel3.addTab(null, tabContentPanel);
        int newTabIndex = jPanel3.indexOfComponent(tabContentPanel);
        jPanel3.setTabComponentAt(newTabIndex, tabTitlePanel);

        jPanel3.setSelectedIndex(newTabIndex);
        openFilesMap.put(file, textArea);

        closeButton.addActionListener(e -> {
            int index = jPanel3.indexOfComponent(tabContentPanel);
            if (index != -1) {
                jPanel3.removeTabAt(index);
                openFilesMap.remove(file);
            }
        });
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
        jPanel3 = new javax.swing.JTabbedPane();
        jPanelBotoes = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        btnAjuda = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanelTerminal = new javax.swing.JPanel();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        panelResultado = new javax.swing.JPanel();
        scrollPaneResultado = new javax.swing.JScrollPane();
        btnFecharResultado = new javax.swing.JButton();
        scrollPaneCodigo = new javax.swing.JScrollPane();
        btnCopiar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTreeArquivos = new javax.swing.JTree();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(102, 102, 102));
        setPreferredSize(new java.awt.Dimension(1366, 766));
        setSize(new java.awt.Dimension(1300, 768));

        jPanel1.setBackground(new java.awt.Color(16, 22, 20));
        jPanel1.setPreferredSize(new java.awt.Dimension(1920, 1080));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        jPanel3.setPreferredSize(new java.awt.Dimension(500, 200));
        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanelBotoes.setBackground(new java.awt.Color(16, 22, 20));
        jPanelBotoes.setPreferredSize(new java.awt.Dimension(0, 52));

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
        jButton4.setPreferredSize(new java.awt.Dimension(46, 46));
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
        jButton5.setPreferredSize(new java.awt.Dimension(118, 21));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

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

        jLabel1.setFont(new java.awt.Font("Inria Sans", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 211, 94));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("SUL");

        jLabel4.setFont(new java.awt.Font("Inria Sans", 1, 36)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("IDE ");

        javax.swing.GroupLayout jPanelBotoesLayout = new javax.swing.GroupLayout(jPanelBotoes);
        jPanelBotoes.setLayout(jPanelBotoesLayout);
        jPanelBotoesLayout.setHorizontalGroup(
            jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBotoesLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(58, 58, 58)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAjuda)
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanelBotoesLayout.setVerticalGroup(
            jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBotoesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnAjuda, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanelBotoes, java.awt.BorderLayout.PAGE_START);

        jPanel4.setBackground(new java.awt.Color(16, 22, 20));
        jPanel4.setPreferredSize(new java.awt.Dimension(0, 200));

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 618, Short.MAX_VALUE)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(210, 210, 210)
                .addComponent(jPanelTerminal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(266, 266, 266))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTerminal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        panelResultado.setBackground(new java.awt.Color(42, 45, 44));

        scrollPaneResultado.setMaximumSize(new java.awt.Dimension(268, 763));

        btnFecharResultado.setBackground(new java.awt.Color(255, 211, 94));
        btnFecharResultado.setForeground(new java.awt.Color(16, 22, 20));
        btnFecharResultado.setText("X");
        btnFecharResultado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharResultadoActionPerformed(evt);
            }
        });

        scrollPaneCodigo.setMaximumSize(new java.awt.Dimension(268, 763));

        btnCopiar.setBackground(new java.awt.Color(255, 211, 94));
        btnCopiar.setForeground(new java.awt.Color(16, 22, 20));
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelResultadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneResultado, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCopiar, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollPaneCodigo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFecharResultado, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        panelResultadoLayout.setVerticalGroup(
            panelResultadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelResultadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnFecharResultado, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneResultado, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCopiar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneCodigo, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.add(panelResultado, java.awt.BorderLayout.LINE_END);

        jPanel2.setBackground(new java.awt.Color(16, 22, 20));
        jPanel2.setForeground(new java.awt.Color(16, 22, 20));
        jPanel2.setPreferredSize(new java.awt.Dimension(212, 567));

        jScrollPane2.setBackground(new java.awt.Color(16, 22, 20));
        jScrollPane2.setForeground(new java.awt.Color(255, 211, 50));

        jTreeArquivos.setBackground(new java.awt.Color(16, 22, 20));
        jTreeArquivos.setForeground(new java.awt.Color(255, 211, 94));
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTreeArquivos.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane2.setViewportView(jTreeArquivos);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 561, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, java.awt.BorderLayout.LINE_START);

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

        jMenuItem5.setText("Abrir Terminal");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("LLM");

        jMenuItem2.setText("Gerar teste");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuItem1.setText("Gerar Melhoria");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Historicos");

        jMenuItem6.setText("Melhoria");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem6);

        jMenuItem7.setText("Teste");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem7);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1366, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 745, Short.MAX_VALUE)
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
        // Esconder área de código e botão copiar
        scrollPaneCodigo.setVisible(false);
        btnCopiar.setVisible(false);
        
        // Ajustar layout da área de resultado
        scrollPaneResultado.setPreferredSize(new java.awt.Dimension(512, 763)); // Dobrar a largura
        
        // Adicionar padding ao painel de resultado
        panelResultado.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
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
                String aiResponse = textAreaResultado.getText();

                // Salva no banco de dados
                BD.saveInteraction(userInput, aiResponse);
            }
        } catch (Exception ex) {
            Logger.getLogger(TelaPrincipalEsul.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        panelResultado.setVisible(true);
        // Mostrar área de código e botão copiar
        scrollPaneCodigo.setVisible(true);
        btnCopiar.setVisible(true);
        
        // Restaurar tamanho original da área de resultado
        scrollPaneResultado.setPreferredSize(new java.awt.Dimension(256, 763));
        
        // Remover padding do painel de resultado
        panelResultado.setBorder(null);
        
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
            BD.saveInteractionI(userInput, aiResponse);
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
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Selecionar Diretório do Projeto");

        int result = fileChooser.showOpenDialog(TelaPrincipalEsul.this);

        if (result == JFileChooser.APPROVE_OPTION) {
            jPanel2.setVisible(true);
            jScrollPane2.setVisible(true);
            jPanel2.revalidate();
            jPanel2.repaint();
            jPanel1.revalidate();
            jPanel1.repaint();
            File selectedDirectory = fileChooser.getSelectedFile();

            DefaultTreeModel treeModel = (DefaultTreeModel) jTreeArquivos.getModel();
            DefaultMutableTreeNode rootNode;

            if (treeModel == null || jTreeArquivos.getModel().getRoot() == null || 
                ((DefaultMutableTreeNode)jTreeArquivos.getModel().getRoot()).getUserObject().equals("root") && 
                ((DefaultMutableTreeNode)jTreeArquivos.getModel().getRoot()).getChildCount() == 0) {
                
                rootNode = new DefaultMutableTreeNode("Arquivos do Projeto");
                treeModel = new DefaultTreeModel(rootNode);
                jTreeArquivos.setModel(treeModel);
            } else {
                rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
                if (rootNode.getUserObject().equals("root") && rootNode.getChildCount() == 0) {
                     rootNode.setUserObject("Arquivos do Projeto");
                }
            }

            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(selectedDirectory);
            treeModel.insertNodeInto(projectNode, rootNode, rootNode.getChildCount());
            
            loadDirectoryToTree(selectedDirectory, projectNode);

            jTreeArquivos.expandPath(new TreePath(projectNode.getPath()));
            jTreeArquivos.scrollPathToVisible(new TreePath(projectNode.getPath()));

            // Iniciar o FileWatcher para o novo projeto
            try {
                if (fileWatcher != null) {
                    fileWatcher.stopWatching();
                }
                fileWatcher = new FileWatcher(jTreeArquivos);
                fileWatcher.startWatching();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Erro ao iniciar monitoramento de arquivos: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
            BD.saveInteractionI(userInput, aiResponse);
        } catch (Exception ex) {
            Logger.getLogger(TelaPrincipalEsul.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        panelResultado.setVisible(false);
        // Restaurar visibilidade e tamanhos ao fechar
        scrollPaneCodigo.setVisible(true);
        btnCopiar.setVisible(true);
        scrollPaneResultado.setPreferredSize(new java.awt.Dimension(256, 763));
        // Remover padding do painel de resultado
        panelResultado.setBorder(null);
    }//GEN-LAST:event_btnFecharResultadoActionPerformed

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

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
         jPanelTerminal.setVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
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
                String aiResponse = textAreaResultado.getText();

                // Salva no banco de dados
                BD.saveInteraction(userInput, aiResponse);
            }
        } catch (Exception ex) {
            Logger.getLogger(TelaPrincipalEsul.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
       TelaHistMelhoria telaHistorico = new TelaHistMelhoria(); 
          telaHistorico.setVisible(true); 
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
  
    TelaHistTeste telaHistorico = new TelaHistTeste(); 
    telaHistorico.setVisible(true); 
    }//GEN-LAST:event_jMenuItem7ActionPerformed

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
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelBotoes;
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

    @Override
    public void dispose() {
        if (fileWatcher != null) {
            fileWatcher.stopWatching();
        }
        super.dispose();
    }

    private String extractPackageNameFromFile(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            int packageStart = content.indexOf("package ");
            if (packageStart == -1) return "";

            int packageEnd = content.indexOf(";", packageStart);
            if (packageEnd == -1) return "";

            return content.substring(packageStart + 8, packageEnd).trim();
        } catch (IOException e) {
            return "";
        }
    }

    private void removeCompiledFiles(File directory) {
        if (directory == null || !directory.isDirectory()) return;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    String className = file.getName().replace(".java", "");
                    String packageName = extractPackageNameFromFile(file);
                    String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
                    
                    String projectPath = System.getProperty("user.dir");
                    Path targetDir = Paths.get(projectPath, "API", "OllamaESUL", "main", "target");
                    Path binDir = targetDir.resolve("bin");
                    File classFile = new File(binDir.toFile(), fullClassName.replace('.', File.separatorChar) + ".class");
                    if (classFile.exists()) {
                        classFile.delete();
                    }
                } else if (file.isDirectory()) {
                    removeCompiledFiles(file);
                }
            }
        }
    }
}
