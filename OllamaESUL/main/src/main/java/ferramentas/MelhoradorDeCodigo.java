package ferramentas;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.OptionsBuilder;
import io.github.ollama4j.utils.Options;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import GUI.TelaPrincipalEsul;

public class MelhoradorDeCodigo {

    private final OllamaAPI ollamaAPI;
    private final String model;
    private final Options options;
    private RespostaHandler respostaHandler;

    public MelhoradorDeCodigo(String host, String model, float temperature) {
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

    public void melhorarCodigo(String codigoJava, RSyntaxTextArea textAreaResultado) throws Exception {
        String prompt = gerarPromptCompleto(codigoJava);
        processarResposta(prompt, textAreaResultado);
    }

    public void melhorarSelecao(String codigoSelecionado, RSyntaxTextArea textAreaResultado) throws Exception {
        String prompt = gerarPromptSelecao(codigoSelecionado);
        processarResposta(prompt, textAreaResultado);
    }

    private void processarResposta(String prompt, RSyntaxTextArea textAreaResultado) throws Exception {
        boolean raw = false;
        OllamaResult result = ollamaAPI.generate(model, prompt, raw, options);
        String resposta = result.getResponse();
        
        if (respostaHandler != null) {
            respostaHandler.separarCodigoETexto(resposta);
        } else {
            textAreaResultado.setText(resposta);
            textAreaResultado.setCaretPosition(0);
        }
    }

    private String gerarPromptCompleto(String codigo) {
        return """
        You are an expert in Java and object orientation.
        Your task:
        1. Read the complete Java code provided.
        2. Improve the entire code by:
        - Enhancing code structure and organization
        - Improving error handling and robustness
        - Applying design patterns where appropriate
        - Optimizing algorithms and data structures
        - Improving code reusability
        - Maintaining the original logic and variable names

        Answer format:
        - What was changed: [describe each change made]
        - Why it was changed: [explain the reason for the change]
        - How it was changed: [explain the solution applied]
        - Code with the changes:
        ```java
        [your improved code here]
        ```

        Rules:
        - Be clear and direct.
        - Translate the entire output to Brazilian Portuguese, only Brazilian Portuguese.
        - Summarize the information objectively.
        - Do not add extra explanations beyond what was requested.
        - Always wrap the code in ```java and ``` markers.
        """ + codigo;
    }

    private String gerarPromptSelecao(String codigo) {
        // Verifica se é um teste JUnit
        if (codigo.contains("@Test") || codigo.contains("assert")) {
            return """
            You are an expert in Java testing and JUnit.
            Your task:
            1. Read the selected JUnit test code provided.
            2. Analyze and fix the test case by:
            - Identifying incorrect assertions or expectations
            - Ensuring test logic matches the actual behavior of the code being tested
            - Maintaining test coverage and edge cases
            - Keeping the original test structure and naming
            - If a test expects an exception but the operation is valid, change it to verify the correct result instead
            - If a test expects a result but the operation should throw an exception, change it to expect the correct exception

            Common test scenarios to consider:
            - Mathematical operations should follow standard mathematical rules
            - String operations should handle null, empty, and special characters correctly
            - Collection operations should handle empty, null, and boundary cases
            - Object operations should verify correct state changes and relationships
            - Exception handling should verify correct exception types and messages
            - Edge cases should be properly tested with boundary values
            - State changes should be verified before and after operations

            Answer format:
            - What was wrong: [describe the issue in the test]
            - Why it was wrong: [explain why the test was incorrect]
            - How it was fixed: [explain the correction made]
            - Code with the changes:
            ```java
            [your corrected test code here]
            ```

            Rules:
            - Be clear and direct.
            - Translate the entire output to Brazilian Portuguese, only Brazilian Portuguese.
            - Summarize the information objectively.
            - Do not add extra explanations beyond what was requested.
            - Focus only on the selected test code.
            - Always wrap the code in ```java and ``` markers.
            - Do not change test method names.
            - Do not duplicate tests.
            - If the test expects an exception but the operation is valid, change it to verify the correct result.
            - If the test expects a result but the operation should throw an exception, change it to expect the correct exception.
            - Use appropriate assertions (assertEquals, assertThrows, etc.) based on what is being tested.
            - Consider the domain rules and expected behavior of the code being tested.
            - Ensure assertions match the actual behavior of the code.
            """ + codigo;
        }

        // Prompt original para código não-teste
        return """
        You are an expert in Java and object orientation.
        Your task:
        1. Read the selected Java code snippet provided.
        2. Improve only this specific code segment by:
        - Keep the original variable names.
        - Enhancing clarity and readability
        - Improving performance where possible
        - Maintaining the original logic

        Answer format:
        - What was changed: [describe each change made]
        - Why it was changed: [explain the reason for the change]
        - How it was changed: [explain the solution applied]
        - Code with the changes:
        ```java
        [your improved code here]
        ```

        Rules:
        - Be clear and direct.
        - Translate the entire output to Brazilian Portuguese, only Brazilian Portuguese.
        - Summarize the information objectively.
        - Do not add extra explanations beyond what was requested.
        - Focus only on the selected code segment.
        - Always wrap the code in ```java and ``` markers.
        - Do not change variable names.
        """ + codigo;
    }
}
