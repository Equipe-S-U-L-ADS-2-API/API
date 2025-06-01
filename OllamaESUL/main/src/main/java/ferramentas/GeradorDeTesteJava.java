package ferramentas;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.OptionsBuilder;
import io.github.ollama4j.utils.Options;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class GeradorDeTesteJava {

    private final OllamaAPI ollamaAPI;
    private final String model;
    private final Options options;
    private RespostaHandler respostaHandler;

    public GeradorDeTesteJava(String host, String model, float temperature) {
        this.ollamaAPI = new OllamaAPI(host);
        this.ollamaAPI.setRequestTimeoutSeconds(500);
        this.model = model;

        this.options = new OptionsBuilder()
                .setTemperature(temperature)
                .build();
    }

    public void setRespostaHandler(RespostaHandler handler) {
        this.respostaHandler = handler;
    }

    public void gerarTestes(String codigoJava, RSyntaxTextArea textAreaResultado) throws Exception {
        String prompt = gerarPromptCompleto(codigoJava);
        processarResposta(prompt, textAreaResultado);
    }

    private void processarResposta(String prompt, RSyntaxTextArea textAreaResultado) throws Exception {
        boolean raw = false;
        OllamaResult result = ollamaAPI.generate(model, prompt, raw, options);
        String resposta = result.getResponse();
        
        if (respostaHandler != null) {
            StringBuilder texto = new StringBuilder();
            StringBuilder codigo = new StringBuilder();
            boolean dentroDoCodigo = false;
            
            String[] linhas = resposta.split("\n");
            for (String linha : linhas) {
                if (linha.trim().startsWith("```java") || linha.trim().startsWith("```")) {
                    dentroDoCodigo = true;
                    continue;
                }
                
                if (dentroDoCodigo && linha.trim().equals("```")) {
                    dentroDoCodigo = false;
                    continue;
                }
                
                if (dentroDoCodigo) {
                    codigo.append(linha).append("\n");
                } else {
                    int espacosInicio = linha.length() - linha.replaceAll("^\\s+", "").length();
                    if (espacosInicio >= 4 && linha.trim().length() > 0) {
                        codigo.append(linha).append("\n");
                    } else {
                        texto.append(linha).append("\n");
                    }
                }
            }
            
            textAreaResultado.setText(texto.toString().trim() + "\n\n" + codigo.toString().trim());
            textAreaResultado.setCaretPosition(0);
        } else {
            textAreaResultado.setText(resposta);
            textAreaResultado.setCaretPosition(0);
        }
    }

    private String gerarPromptCompleto(String codigoJava) {
        return """
        You are an expert in Java testing. Generate complete unit test code for the following Java code.
        
        Requirements:
        1. Use JUnit 5 exclusively:
           - Import and use @Test from org.junit.jupiter.api.Test
           - DO NOT use JUnit 4 annotations or imports
           - Use modern JUnit 5 assertions and features
           - IMPORTANT: Never use @Test(expected=...) as it's JUnit 4 style
        
        2. For exception testing (CRITICAL):
           - ALWAYS use assertThrows() for exception tests
           - NEVER use @Test(expected=...)
           - Example of correct exception testing:
             ```java
             @Test
             void testDividirPorZero() {
                 // Arrange
                 int a = 10, b = 0;
                 Calculadora calc = new Calculadora();
                 
                 // Act & Assert
                 assertThrows(ArithmeticException.class, () -> {
                     calc.dividir(a, b);
                 });
             }
             ```
        
        3. Test Structure:
           - Follow AAA pattern (Arrange, Act, Assert)
           - Use descriptive test names that explain the scenario
           - Include test cases for both success and failure scenarios
           - Test edge cases and boundary conditions
           - Document test cases with clear comments
        
        4. Before writing each test case:
           - Analyze the implementation logic carefully
           - Calculate the expected values manually
           - Double-check all mathematical operations
           - Verify that the expected values match the actual implementation
           - If in doubt, test the implementation manually first
        
        5. For each test case:
           - Document the input values
           - Show the calculation steps
           - Explain why the expected value is correct
           - Include a comment with the manual calculation
        
        Format your response as:
        ```java
        // Your complete test code here
        ```
        
        The code to test:
        """ + codigoJava;
    }
}
