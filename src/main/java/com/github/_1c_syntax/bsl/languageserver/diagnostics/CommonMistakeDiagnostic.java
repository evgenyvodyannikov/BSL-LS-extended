package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.parser.BSLParser;
import com.github._1c_syntax.mdclasses.Configuration;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

@DiagnosticMetadata(
  type = DiagnosticType.ERROR,
  severity = DiagnosticSeverity.MAJOR,
  minutesToFix = 5,
  tags = {
    DiagnosticTag.STANDARD,
    DiagnosticTag.BADPRACTICE,
    DiagnosticTag.CLUMSY
  }
)
public class CommonMistakeDiagnostic extends AbstractVisitorDiagnostic {
  @Override
  public ParseTree visitStatement(BSLParser.StatementContext ctx) { // выбранный визитер
    if (ctx.SEMICOLON() == null) {                                // получение дочернего узла SEMICOLON
      diagnosticStorage.addDiagnostic(ctx);                     // добавление замечания
    }
    // Для не-терминальных выражений в качестве возвращаемого значения
    // обязательно должен вызываться super-метод.
    return super.visitStatement(ctx);
  }
}