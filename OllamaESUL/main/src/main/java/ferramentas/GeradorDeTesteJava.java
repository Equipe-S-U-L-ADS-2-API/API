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
            respostaHandler.separarCodigoETexto(resposta);
        } else {
            textAreaResultado.setText(resposta);
            textAreaResultado.setCaretPosition(0);
        }
    }

    private String gerarPromptCompleto(String codigoJava) {
        return """
        You are an expert in Java testing. Generate complete unit test code for the following Java code.
        
        Requirements:
        1. Generate actual test code, not instructions
        2. Include all necessary test cases to verify the functionality
        3. Test both normal cases and edge cases
        4. Include assertions to verify expected outcomes
        5. Add comments explaining what each test verifies
        6. Follow testing best practices
        
        Format your response as:
        ```java
        // Your complete test code here
        ```
        
        The code to test:
        """ + codigoJava;
    }
}
