package GUI;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public class ProjectCompiler {
    private final JTree jTreeArquivos;
    private final JTextArea outputArea;
    private final File outputDir;
    private final Set<File> closedFiles;

    public ProjectCompiler(JTree jTreeArquivos, JTextArea outputArea) {
        this.jTreeArquivos = jTreeArquivos;
        this.outputArea = outputArea;
        this.outputDir = new File("bin");
        this.closedFiles = new HashSet<>();
        
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
    }

    public void executarProjeto() {
        try {
            // 1. Coletar todos os arquivos .java da JTree
            List<File> javaFiles = collectJavaFiles();
            if (javaFiles.isEmpty()) {
                outputArea.append("Nenhum arquivo .java encontrado no projeto.\n");
                return;
            }

            outputArea.append("Arquivos .java encontrados na JTree:\n");
            for (File file : javaFiles) {
                outputArea.append("- " + file.getAbsolutePath() + "\n");
            }

            // 2. Compilar os arquivos
            boolean compilationSuccess = compileFiles(javaFiles);
            if (!compilationSuccess) {
                outputArea.append("Compilação falhou. Verifique os erros acima.\n");
                return;
            }

            outputArea.append("\nArquivos compilados em " + outputDir.getAbsolutePath() + ":\n");
            listCompiledFiles(outputDir, "");

            // 3. Encontrar e executar a classe principal
            String mainClassName = findMainClass(javaFiles);
            if (mainClassName == null) {
                outputArea.append("Nenhuma classe principal encontrada. Certifique-se de que existe uma classe com método main.\n");
                return;
            }

            executeMainClass(mainClassName);

        } catch (Exception e) {
            outputArea.append("Erro durante a execução do projeto: " + e.getMessage() + "\n");
            e.printStackTrace(new PrintStream(new TextAreaOutputStream(outputArea)));
        }
    }

    // NOVO MÉTODO ATUALIZADO: Para compilar e executar código diretamente de uma String
    public void executarCodigoDoTexto(String codeContent, String fileName, File originalFile) {
        File tempFile = null;
        try {
            if (codeContent == null || codeContent.trim().isEmpty()) {
                outputArea.append("O conteúdo do código está vazio.\n");
                return;
            }

            // Salvar o arquivo temporário
            tempFile = new File(outputDir, fileName);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(codeContent);
            }

            outputArea.append("Compilando código do arquivo temporário: " + tempFile.getAbsolutePath() + "\n");

            // Coletar arquivos para compilação
            List<File> filesToCompile = new ArrayList<>();
            
            // Adicionar arquivos relacionados
            if (originalFile != null && originalFile.getParentFile() != null) {
                File projectRoot = getProjectRootForFile(originalFile);
                if (projectRoot != null) {
                    collectJavaFilesFromDirectory(projectRoot, filesToCompile);
                } else {
                    collectJavaFilesFromDirectory(originalFile.getParentFile(), filesToCompile);
                }
            }
            
            // Remover o arquivo original se existir
            if (originalFile != null) {
                try {
                    String originalCanonicalPath = originalFile.getCanonicalPath();
                    filesToCompile.removeIf(f -> {
                        try {
                            return f.getCanonicalPath().equals(originalCanonicalPath);
                        } catch (IOException ex) {
                            return false;
                        }
                    });
                } catch (IOException ex) {
                    // Ignorar erro
                }
            }

            // Adicionar o arquivo temporário
            filesToCompile.add(tempFile);

            outputArea.append("Arquivos a serem compilados:\n");
            for (File file : filesToCompile) {
                outputArea.append("- " + file.getAbsolutePath() + "\n");
            }

            // Compilar os arquivos
            boolean compilationSuccess = compileFiles(filesToCompile);
            if (!compilationSuccess) {
                outputArea.append("Compilação falhou. Verifique os erros acima.\n");
                return;
            }

            // Determinar se é uma classe de teste ou principal
            String className = fileName.replace(".java", "");
            String packageName = extractPackageNameFromFileContent(tempFile);
            String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;

            // Verificar se é uma classe de teste
            boolean isTestClass = fileName.endsWith("Test.java") || 
                                codeContent.contains("@Test") ||
                                codeContent.contains("extends TestCase");

            if (isTestClass) {
                outputArea.append("\nExecutando testes da classe: " + fullClassName + "\n");
                executeTestClass(fullClassName);
            } else {
                // Verificar se tem método main
                if (codeContent.contains("public static void main(String[]")) {
                    outputArea.append("\nExecutando classe principal: " + fullClassName + "\n");
                    executeMainClass(fullClassName);
                } else {
                    outputArea.append("\nClasse compilada com sucesso, mas não contém método main para execução.\n");
                }
            }

        } catch (Exception e) {
            outputArea.append("Erro durante a execução: " + e.getMessage() + "\n");
            e.printStackTrace(new PrintStream(new TextAreaOutputStream(outputArea)));
        }
    }

    // --- Métodos Auxiliares (mantidos como estão) ---
    private String findMainClass(List<File> javaFiles) {
        // Primeiro procura por classes de teste
        for (File file : javaFiles) {
            if (file.getName().endsWith("Test.java")) {
                try {
                    String packageName = extractPackageNameFromFileContent(file);
                    String className = file.getName().replace(".java", "");
                    return packageName.isEmpty() ? className : packageName + "." + className;
                } catch (Exception e) {
                    outputArea.append("Erro ao processar arquivo de teste " + file.getName() + ": " + e.getMessage() + "\n");
                }
            }
        }

        // Se não encontrar classe de teste, procura por classe com main
        for (File file : javaFiles) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                if (content.contains("public static void main(String[]")) {
                    String packageName = extractPackageName(content);
                    String className = file.getName().replace(".java", "");
                    return packageName.isEmpty() ? className : packageName + "." + className;
                }
            } catch (IOException e) {
                outputArea.append("Erro ao ler arquivo " + file.getName() + ": " + e.getMessage() + "\n");
            }
        }
        return null;
    }

    private String extractPackageName(String content) {
        int packageStart = content.indexOf("package ");
        if (packageStart == -1) return "";

        int packageEnd = content.indexOf(";", packageStart);
        if (packageEnd == -1) return "";

        return content.substring(packageStart + 8, packageEnd).trim();
    }

    private void listCompiledFiles(File dir, String indent) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                outputArea.append(indent + "- " + file.getName() + "\n");
                if (file.isDirectory()) {
                    listCompiledFiles(file, indent + "  ");
                }
            }
        }
    }

    private List<File> collectJavaFiles() {
        List<File> javaFiles = new ArrayList<>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTreeArquivos.getModel().getRoot();
        collectJavaFilesFromNode(root, javaFiles);
        return javaFiles;
    }

    private void collectJavaFilesFromNode(DefaultMutableTreeNode node, List<File> javaFiles) {
        Object userObject = node.getUserObject();
        if (userObject instanceof File) {
            File file = (File) userObject;
            if (!isFileClosed(file)) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                } else if (file.isDirectory()) {
                    for (int i = 0; i < node.getChildCount(); i++) {
                        collectJavaFilesFromNode((DefaultMutableTreeNode) node.getChildAt(i), javaFiles);
                    }
                }
            }
        }
    }

    private void collectJavaFilesFromDirectory(File directory, List<File> javaFiles) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                } else if (file.isDirectory()) {
                    collectJavaFilesFromDirectory(file, javaFiles); // Chamada recursiva
                }
            }
        }
    }

    private File getProjectRootForFile(File file) {
        if (file == null) return null;

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTreeArquivos.getModel().getRoot();
        if (root == null || !(root.getUserObject() instanceof String)) {
             return null;
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            Object userObject = child.getUserObject();
            if (userObject instanceof File && ((File) userObject).isDirectory()) {
                File projectDir = (File) userObject;
                try {
                    if (file.getCanonicalPath().startsWith(projectDir.getCanonicalPath())) {
                        return projectDir;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private boolean compileFiles(List<File> javaFiles) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            outputArea.append("Erro: JDK não encontrado. Certifique-se de estar usando o JDK, não apenas o JRE.\n");
            return false;
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        try {
            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(outputDir.getAbsolutePath());
            
            // Adicionar encoding UTF-8
            options.add("-encoding");
            options.add("UTF-8");
            
            // Adicionar os diretórios pai de todos os arquivos a serem compilados ao sourcepath
            Set<File> sourcePaths = new HashSet<>();
            for (File file : javaFiles) {
                if (file.getParentFile() != null) {
                    sourcePaths.add(file.getParentFile());
                }
                String packageName = extractPackageNameFromFileContent(file);
                if (!packageName.isEmpty()) {
                    File packageRoot = getPackageRoot(file, packageName);
                    if (packageRoot != null) {
                        sourcePaths.add(packageRoot);
                    }
                }
            }
            sourcePaths.add(outputDir); 

            // Build classpath string
            StringBuilder classpath = new StringBuilder();
            classpath.append(outputDir.getAbsolutePath());

            // Add source paths to classpath
            for (File path : sourcePaths) {
                classpath.append(File.pathSeparator).append(path.getAbsolutePath());
            }

            // Check if any file contains JUnit imports
            boolean hasJUnit = false;
            for (File file : javaFiles) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    if (content.contains("import org.junit.jupiter.api") || 
                        content.contains("import org.junit.Test") ||
                        content.contains("import org.junit.Assert")) {
                        hasJUnit = true;
                        break;
                    }
                } catch (IOException e) {
                    // Ignore
                }
            }

            // Add JUnit to classpath if needed
            if (hasJUnit) {
                try {
                    // Tentar diferentes métodos para encontrar o diretório da IDE
                    String idePath = null;
                    
                    // Método 1: Usar o diretório atual
                    idePath = System.getProperty("user.dir");
                    
                    // Método 2: Tentar encontrar o JAR da aplicação
                    try {
                        String jarPath = new File(ProjectCompiler.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                            .getParentFile()
                            .getAbsolutePath();
                        if (jarPath != null && !jarPath.isEmpty()) {
                            idePath = jarPath;
                        }
                    } catch (Exception e) {
                        // Ignora erro e continua com o user.dir
                    }
                    
                    // Procurar na pasta lib dentro da IDE
                    File ideLibDir = new File(idePath, "lib");
                    
                    if (ideLibDir.exists() && ideLibDir.isDirectory()) {
                        // Adicionar todos os JARs do diretório lib ao classpath
                        File[] jars = ideLibDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
                        if (jars != null && jars.length > 0) {
                            for (File jar : jars) {
                                classpath.append(File.pathSeparator).append(jar.getAbsolutePath());
                            }
                            outputArea.append("Adicionando dependências da pasta lib da IDE ao classpath\n");
                            
                            // Mostrar os JARs adicionados
                            outputArea.append("\nJARs da pasta lib adicionados:\n");
                            for (File jar : jars) {
                                outputArea.append("- " + jar.getName() + "\n");
                            }
                            outputArea.append("\n");
                        } else {
                            outputArea.append("ERRO: Nenhum JAR encontrado na pasta lib: " + ideLibDir.getAbsolutePath() + "\n");
                        }
                    } else {
                        outputArea.append("ERRO: Pasta lib não encontrada em: " + ideLibDir.getAbsolutePath() + "\n");
                        outputArea.append("Tentando usar classpath do sistema...\n");
                        
                        // Fallback: usar o classpath do sistema
                        String systemClasspath = System.getProperty("java.class.path");
                        if (systemClasspath != null && !systemClasspath.isEmpty()) {
                            classpath.append(File.pathSeparator).append(systemClasspath);
                            outputArea.append("Usando classpath do sistema\n");
                        }
                    }
                } catch (Exception e) {
                    outputArea.append("Erro ao adicionar dependências ao classpath: " + e.getMessage() + "\n");
                }
            }

            // Add classpath to options
            options.add("-classpath");
            options.add(classpath.toString());

            // Mostrar o classpath final usado na compilação
            outputArea.append("\nClasspath final usado na compilação:\n");
            String[] finalClasspath = classpath.toString().split(File.pathSeparator);
            for (String entry : finalClasspath) {
                outputArea.append("- " + entry + "\n");
            }
            outputArea.append("\n");

            Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(javaFiles);

            JavaCompiler.CompilationTask task = compiler.getTask(
                new PrintWriter(new TextAreaOutputStream(outputArea)),
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits
            );

            boolean success = task.call();

            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                outputArea.append(diagnostic.getMessage(null) + "\n");
            }

            return success;

        } catch (Exception e) {
            outputArea.append("Erro durante a compilação: " + e.getMessage() + "\n");
            e.printStackTrace(new PrintStream(new TextAreaOutputStream(outputArea)));
            return false;
        } finally {
            try {
                fileManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String extractPackageNameFromFileContent(File file) {
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

    private File getPackageRoot(File file, String packageName) {
        File currentDir = file.getParentFile();
        if (currentDir == null) return null;

        String[] packageParts = packageName.split("\\.");
        // Navega para cima na hierarquia de diretórios para encontrar a raiz do pacote
        for (int i = packageParts.length - 1; i >= 0; i--) {
            if (currentDir == null || !currentDir.getName().equals(packageParts[i])) {
                // Se o nome do diretório não corresponde à parte do pacote, a estrutura está errada
                return null;
            }
            currentDir = currentDir.getParentFile();
        }
        return currentDir; // Este é o diretório raiz do pacote
    }

    private void executeTestClass(String testClassName) throws Exception {
        // Criar classloader com o diretório de saída
        URLClassLoader classLoader = new URLClassLoader(
            new URL[] { outputDir.toURI().toURL() },
            getClass().getClassLoader()
        );

        // Carregar a classe de teste
        Class<?> testClass = classLoader.loadClass(testClassName);
        
        // Executar testes JUnit
        outputArea.append("\nExecutando testes JUnit...\n");
        
        // Criar um LauncherDiscoveryRequest para encontrar os testes
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
            .request()
            .selectors(DiscoverySelectors.selectClass(testClass))
            .build();

        // Criar um listener para capturar os resultados
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        // Criar e executar o launcher
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        // Obter e exibir o resumo dos testes
        TestExecutionSummary summary = listener.getSummary();
        outputArea.append("\nResumo dos Testes:\n");
        outputArea.append("Total de testes: " + summary.getTestsStartedCount() + "\n");
        outputArea.append("Testes que passaram: " + summary.getTestsSucceededCount() + "\n");
        outputArea.append("Testes que falharam: " + summary.getTestsFailedCount() + "\n");
        
        // Exibir detalhes dos testes que falharam
        if (summary.getTestsFailedCount() > 0) {
            outputArea.append("\nDetalhes dos testes que falharam:\n");
            summary.getFailures().forEach(failure -> {
                outputArea.append("- " + failure.getTestIdentifier().getDisplayName() + "\n");
                outputArea.append("  Erro: " + failure.getException().getMessage() + "\n");
            });
        }
    }

    private void executeMainClass(String mainClassName) throws Exception {
        // Criar classloader com o diretório de saída
        URLClassLoader classLoader = new URLClassLoader(
            new URL[] { outputDir.toURI().toURL() },
            getClass().getClassLoader()
        );

        // Carregar a classe principal
        Class<?> mainClass = classLoader.loadClass(mainClassName);
        
        // Executar método main
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) new String[0]);
    }

    /**
     * Fecha um arquivo da JTree, removendo-o e seu arquivo compilado
     * @param file Arquivo a ser fechado
     * @param removeFromBin Se true, remove o arquivo compilado da pasta bin
     */
    public void closeFile(File file, boolean removeFromBin) {
        if (file == null) return;

        // Adiciona o arquivo ao conjunto de arquivos fechados
        closedFiles.add(file);

        // Remove o nó da JTree
        DefaultTreeModel model = (DefaultTreeModel) jTreeArquivos.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        
        // Procura o nó que contém o arquivo
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            if (child.getUserObject() instanceof File) {
                File childFile = (File) child.getUserObject();
                if (childFile.equals(file)) {
                    model.removeNodeFromParent(child);
                    break;
                }
            }
        }

        // Remove arquivo compilado se solicitado
        if (removeFromBin) {
            String className = file.getName().replace(".java", "");
            String packageName = extractPackageNameFromFileContent(file);
            String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
            
            File classFile = new File(outputDir, fullClassName.replace('.', File.separatorChar) + ".class");
            if (classFile.exists()) {
                classFile.delete();
            }
        }

        outputArea.append("Arquivo fechado: " + file.getName() + "\n");
    }

    /**
     * Verifica se um arquivo está fechado
     * @param file Arquivo a ser verificado
     * @return true se o arquivo estiver fechado
     */
    public boolean isFileClosed(File file) {
        return closedFiles.contains(file);
    }
}