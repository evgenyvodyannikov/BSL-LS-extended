package com.github._1c_syntax.bsl.languageserver.diagnostics;

//import com.github._1c_syntax.bsl.languageserver.util.Assertions;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;
import java.util.List;
// возможна ошикба
import static com.github._1c_syntax.bsl.languageserver.util.Assertions.assertThat;

class CommonMistakeDiagnosticTest extends AbstractDiagnosticTest<CommonMistakeDiagnostic>{

  CommonMistakeDiagnosticTest() {
    super(CommonMistakeDiagnostic.class);
  }

  @Test
  void test() {
    List<Diagnostic> diagnostics = getDiagnostics(); // Получение диагностик

    assertThat(diagnostics).hasSize(1); // Проверка количества
    assertThat(diagnostics, true)
      .hasRange(4, 0, 4, 19)  // Проверка конкретного случая
      .hasRange(3, 6, 3, 7); // Проверка конкретного случая
  }
}