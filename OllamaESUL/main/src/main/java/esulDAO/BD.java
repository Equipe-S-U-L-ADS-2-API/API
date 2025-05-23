package esulDAO;

import java.sql.*;


public class BD {

    // Configuração do banco de dados
    private static final String URL = "jdbc:mysql://localhost:3306/esulbd"; // Nome do seu banco de dados
    private static final String USER = "root"; // Seu usuário do banco de dados
    private static final String PASSWORD = "fatec"; // Sua senha do banco de dados

    // Método para salvar interação do usuário e resposta da IA
    public static void saveInteraction(String userInput, String aiResponse) {
        String sql = "INSERT INTO testes (inp_teste, out_teste) VALUES (?, ?)";

        // Conexão com o banco de dados e execução do comando SQL
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Substitui os parâmetros da consulta com os dados reais
            stmt.setString(1, userInput);
            stmt.setString(2, aiResponse);

            // Executa a atualização no banco
            stmt.executeUpdate();
            System.out.println("Interação salva com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar no banco: " + e.getMessage());
        }
    }
    public static void saveInteractionI(String userInputmel, String aiResponsemel) {
        String sql = "INSERT INTO melhorias (inp_melhoria, out_melhoria) VALUES (?, ?)";

        // Conexão com o banco de dados e execução do comando SQL
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Substitui os parâmetros da consulta com os dados reais
            stmt.setString(1, userInputmel);
            stmt.setString(2, aiResponsemel);

            // Executa a atualização no banco
            stmt.executeUpdate();
            System.out.println("Interação salva com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar no banco: " + e.getMessage());
        }
    }
}