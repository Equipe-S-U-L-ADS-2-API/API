package GUI;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.io.File;

public class FileWatcher {
    private final WatchService watchService;
    private final Map<WatchKey, Path> watchKeys;
    private final JTree jTreeArquivos;
    private final DefaultTreeModel treeModel;
    private boolean running;

    public FileWatcher(JTree jTreeArquivos) throws IOException {
        this.jTreeArquivos = jTreeArquivos;
        this.treeModel = (DefaultTreeModel) jTreeArquivos.getModel();
        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchKeys = new HashMap<>();
        this.running = true;
    }

    public void startWatching() {
        // Registrar todos os diretórios do projeto
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        registerDirectories(root);

        // Iniciar thread de monitoramento
        Thread watchThread = new Thread(() -> {
            while (running) {
                try {
                    WatchKey key = watchService.take();
                    Path dir = watchKeys.get(key);
                    if (dir == null) {
                        continue;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path name = ev.context();
                        Path child = dir.resolve(name);

                        // Atualizar a árvore no EDT
                        SwingUtilities.invokeLater(() -> {
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                handleFileCreated(child.toFile());
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                handleFileDeleted(child.toFile());
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                handleFileModified(child.toFile());
                            }
                        });

                        // Se for um diretório, registrá-lo para monitoramento
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            try {
                                if (Files.isDirectory(child)) {
                                    registerDirectory(child);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!key.reset()) {
                        watchKeys.remove(key);
                        if (watchKeys.isEmpty()) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        watchThread.setDaemon(true);
        watchThread.start();
    }

    private void registerDirectories(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof File) {
            File file = (File) userObject;
            if (file.isDirectory()) {
                try {
                    registerDirectory(file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Registrar subdiretórios recursivamente
        for (int i = 0; i < node.getChildCount(); i++) {
            registerDirectories((DefaultMutableTreeNode) node.getChildAt(i));
        }
    }

    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY);
        watchKeys.put(key, dir);
    }

    private void handleFileCreated(File file) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        DefaultMutableTreeNode parentNode = findParentNode(root, file.getParentFile());
        
        if (parentNode != null) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file);
            treeModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
            jTreeArquivos.expandPath(new TreePath(parentNode.getPath()));
        }
    }

    private void handleFileDeleted(File file) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        DefaultMutableTreeNode node = findNode(root, file);
        
        if (node != null) {
            treeModel.removeNodeFromParent(node);
        }
    }

    private void handleFileModified(File file) {
        // Atualizar o ícone ou outras propriedades visuais se necessário
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        DefaultMutableTreeNode node = findNode(root, file);
        
        if (node != null) {
            treeModel.nodeChanged(node);
        }
    }

    private DefaultMutableTreeNode findParentNode(DefaultMutableTreeNode root, File parentFile) {
        if (root == null || parentFile == null) {
            return null;
        }

        Object userObject = root.getUserObject();
        if (userObject instanceof File && ((File) userObject).equals(parentFile)) {
            return root;
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            DefaultMutableTreeNode found = findParentNode(child, parentFile);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode root, File targetFile) {
        if (root == null || targetFile == null) {
            return null;
        }

        Object userObject = root.getUserObject();
        if (userObject instanceof File && ((File) userObject).equals(targetFile)) {
            return root;
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            DefaultMutableTreeNode found = findNode(child, targetFile);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    public void stopWatching() {
        running = false;
        try {
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 