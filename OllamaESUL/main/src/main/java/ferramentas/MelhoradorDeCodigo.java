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
        - Optimizing variable names
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
        - Always wrap the code in ```java and ``` markers.
        """ + codigo;
    }

    private String gerarPromptSelecao(String codigo) {
        return """
        You are an expert in Java and object orientation.
        Your task:
        1. Read the selected Java code snippet provided.
        2. Improve only this specific code segment by:
        - Optimizing variable names
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
        """ + codigo;
    }
}
