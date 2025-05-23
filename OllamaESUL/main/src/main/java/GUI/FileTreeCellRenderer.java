package GUI;
import java.awt.Component;
import java.io.File;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView; // Útil para ícones de sistema
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

// Um renderizador customizado para JTree que exibe apenas o nome do arquivo
// e usa ícones de sistema (opcional, mas comum para árvores de arquivos)
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private FileSystemView fileSystemView;

    public FileTreeCellRenderer() {
        fileSystemView = FileSystemView.getFileSystemView();
        // Opcional: definir ícones padrão se preferir ícones genéricos do Swing
        // setClosedIcon(getDefaultClosedIcon());
        // setOpenIcon(getDefaultOpenIcon());
        // setLeafIcon(getDefaultLeafIcon());
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {

        // Chama o método do renderizador padrão para obter o componente base
        // Isso cuida da seleção de cor, fontes, etc.
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        // O objeto do nó na árvore é o 'value'. Vamos verificar se ele contém um File.
        // O DefaultMutableTreeNode armazena o objeto do usuário.
        if (value instanceof javax.swing.tree.DefaultMutableTreeNode) {
            Object userObject = ((javax.swing.tree.DefaultMutableTreeNode) value).getUserObject();

            if (userObject instanceof File) {
                File file = (File) userObject;

                // Define o texto para ser APENAS o nome do arquivo/diretório
                setText(file.getName()); // getDisplayName é melhor que getName() para nomes de sistema
                // Ou, se quiser apenas o nome simples do arquivo/diretório:
                // setText(file.getName());

                // Opcional: Define o ícone usando o FileSystemView para ícones de sistema
                setIcon(fileSystemView.getSystemIcon(file));

                // Opcional: Adiciona um tooltip com o caminho completo
                setToolTipText(file.getAbsolutePath());

            } else {
                // Se o objeto do usuário não for um File (talvez a raiz inicial se não for um File),
                // use a representação padrão (geralmente toString() do objeto do usuário)
                setText(value.toString());
                // Limpa o ícone e tooltip definidos para File
                setIcon(null); // Ou pode definir um ícone padrão para nós não File
                setToolTipText(null);
            }
        } else {
             // Se o próprio 'value' não for um DefaultMutableTreeNode (menos comum),
             // use a representação padrão.
             setText(value.toString());
             setIcon(null);
             setToolTipText(null);
        }


        return this; // Retorna a instância do renderizador (this) como o componente
    }
}