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

public class ProjectCompiler {
    private final JTree jTreeArquivos;
    private final JTextArea outputArea;
    private final File outputDir;

    public ProjectCompiler(JTree jTreeArquivos, JTextArea outputArea) {
        this.jTreeArquivos = jTreeArquivos;
        this.outputArea = outputArea;
        this.outputDir = new File("bin"); // Diretório de saída para classes compiladas
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
        File tempFile = null; // Declare aqui para ser acessível no finally
        try {
            if (codeContent == null || codeContent.trim().isEmpty()) {
                outputArea.append("O conteúdo do código está vazio.\n");
                return;
            }

            // Onde o arquivo temporário será salvo
            // Use o nome do arquivo original (se houver) para o temporário,
            // ou um nome padrão, mas certifique-se de que ele não colida com o original.
            // Para evitar colisão, vamos usar um prefixo/sufixo único no tempFile.
            // OU: podemos sobrescrever o original no diretório bin se ele for o destino.
            // A abordagem atual de salvar em 'bin' com o nome original já é para sobrescrever
            // e compilar aquela versão, e não a de 'src'.

            // O tempFile será salvo no diretório 'bin' e terá o mesmo nome do arquivo original.
            // Isso efetivamente "substitui" a versão 'src' para o compilador.
            tempFile = new File(outputDir, fileName); 
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(codeContent);
            }

            outputArea.append("Compilando código do arquivo temporário: " + tempFile.getAbsolutePath() + "\n");

            // 1. Coletar todos os arquivos .java relevantes para a compilação
            List<File> filesToCompile = new ArrayList<>();
            
            // Adicionar outros arquivos .java do mesmo diretório ou subdiretórios
            // se o originalFile for de um diretório que já está na JTree.
            if (originalFile != null && originalFile.getParentFile() != null) {
                File projectRoot = getProjectRootForFile(originalFile); // Tenta encontrar a raiz do projeto na JTree
                if (projectRoot != null) {
                    collectJavaFilesFromDirectory(projectRoot, filesToCompile);
                } else {
                    // Se não for parte de um projeto carregado na JTree,
                    // tenta coletar do diretório pai do arquivo original.
                    collectJavaFilesFromDirectory(originalFile.getParentFile(), filesToCompile);
                }
            }
            
            // Remover o arquivo ORIGINAL da lista se ele foi coletado, para evitar a duplicata.
            // Usamos o canonical path para garantir que a comparação seja precisa.
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
                    // Ignorar erro, apenas continua sem remover
                }
            }

            // Adicionar o arquivo temporário (conteúdo da aba) para compilação.
            // Este é o que deve prevalecer.
            filesToCompile.add(tempFile); 
            
            // Opcional: Remover duplicatas que podem ter surgido de outras formas
            // Mas com a lógica de remoção acima, já deve estar bem limpo.
            // Set<File> uniqueFiles = new HashSet<>(filesToCompile);
            // filesToCompile = new ArrayList<>(uniqueFiles);


            outputArea.append("Arquivos a serem compilados:\n");
            for (File file : filesToCompile) {
                outputArea.append("- " + file.getAbsolutePath() + "\n");
            }

            // 2. Compilar os arquivos coletados
            boolean compilationSuccess = compileFiles(filesToCompile);
            if (!compilationSuccess) {
                outputArea.append("Compilação do código da aba falhou. Verifique os erros acima.\n");
                return;
            }

            // 3. Encontrar a classe principal (agora pode estar em qualquer um dos arquivos compilados)
            String mainClassName = findMainClass(filesToCompile);
            if (mainClassName == null) {
                outputArea.append("Nenhuma classe principal encontrada no código da aba ou arquivos relacionados. Certifique-se de que existe um método main.\n");
                return;
            }

            executeMainClass(mainClassName);

        } catch (Exception e) {
            outputArea.append("Erro durante a execução do código da aba: " + e.getMessage() + "\n");
            e.printStackTrace(new PrintStream(new TextAreaOutputStream(outputArea)));
        } finally {
            // Opcional: remover o arquivo temporário após a execução
            // Recomendo não remover imediatamente para depuração ou se você precisar
            // que os .class existam no bin por mais tempo.
            // Mas se quiser, pode descomentar a linha abaixo.
            // if (tempFile != null && tempFile.exists()) {
            //     tempFile.delete();
            // }
        }
    }


    // --- Métodos Auxiliares (mantidos como estão) ---
    private String findMainClass(List<File> javaFiles) {
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
            if (file.isFile() && file.getName().endsWith(".java")) {
                javaFiles.add(file);
            } else if (file.isDirectory()) {
                for (int i = 0; i < node.getChildCount(); i++) {
                    collectJavaFilesFromNode((DefaultMutableTreeNode) node.getChildAt(i), javaFiles);
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
            // Adiciona o outputDir ao sourcepath explicitamente para classes temporárias/compiladas
            sourcePaths.add(outputDir); 

            for (File path : sourcePaths) {
                options.add("-sourcepath");
                options.add(path.getAbsolutePath());
            }


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

    private void executeMainClass(String mainClassName) throws Exception {
        // Criar classloader com o diretório de saída
        URLClassLoader classLoader = new URLClassLoader(
            new URL[] { outputDir.toURI().toURL() },
            getClass().getClassLoader()
        );

        outputArea.append("\nTentando carregar a classe: " + mainClassName + "\n");

        // Carregar e executar a classe principal
        Class<?> mainClass = classLoader.loadClass(mainClassName);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) new String[0]);
    }
}