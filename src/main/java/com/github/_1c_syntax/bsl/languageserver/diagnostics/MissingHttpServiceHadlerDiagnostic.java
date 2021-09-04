/*
 * This file is a part of BSL Language Server.
 *
 * Copyright (c) 2018-2021
 * Alexey Sosnoviy <labotamy@gmail.com>, Nikita Gryzlov <nixel2007@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSL Language Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSL Language Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSL Language Server.
 */
package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.context.DocumentContext;
import com.github._1c_syntax.bsl.languageserver.context.symbol.MethodSymbol;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticScope;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.languageserver.utils.Ranges;
import com.github._1c_syntax.mdclasses.common.ConfigurationSource;
import com.github._1c_syntax.mdclasses.mdo.MDHttpService;
import com.github._1c_syntax.mdclasses.mdo.children.HTTPServiceMethod;
import com.github._1c_syntax.mdclasses.mdo.support.MDOType;
import com.github._1c_syntax.mdclasses.mdo.support.ModuleType;
import org.eclipse.lsp4j.Range;

import java.util.stream.Collectors;

@DiagnosticMetadata(
  type = DiagnosticType.ERROR,
  severity = DiagnosticSeverity.BLOCKER,
  minutesToFix = 10,
  tags = {
    DiagnosticTag.DESIGN,
    DiagnosticTag.SUSPICIOUS,
    DiagnosticTag.ERROR
  },
  scope = DiagnosticScope.BSL,
  modules = {
    // todo переделать, когда появится привязка к объектам метаданных
    ModuleType.SessionModule
  }

)
public class MissingHttpServiceHadlerDiagnostic extends AbstractDiagnostic {

  /**
   * Рендж на который будут повешены замечания
   * Костыль, но пока так
   */
  private Range diagnosticRange;

  @Override
  protected void check() {
    Ranges.getFirstSignificantTokenRange(documentContext.getTokens())
      .ifPresent(range -> diagnosticRange = range);

    if (diagnosticRange == null) {
      return;
    }

    var configuration = documentContext.getServerContext().getConfiguration();
    if (configuration.getConfigurationSource() == ConfigurationSource.EMPTY) {
      return;
    }

    final var httpServices = configuration.getChildren().stream()
      .filter(mdo -> mdo.getType() == MDOType.HTTP_SERVICE)
      .map(abstractMDObjectBase -> (MDHttpService) abstractMDObjectBase).collect(Collectors.toList());
    httpServices
      .forEach(this::checkService);
  }

  private void checkService(MDHttpService mdHttpService) {
    if (mdHttpService.getModules().isEmpty()) {
      //addDiagnostic("missingModule", method, handlerParts[1]);//todo может ли не быть модуля http-сервиса?
      return;
    }

    mdHttpService.getUrlTemplates().stream()
      .flatMap(httpServiceURLTemplate -> httpServiceURLTemplate.getHttpServiceMethods().stream())
      .forEach((HTTPServiceMethod service) -> {
        final var serviceName = service.getMdoReference().getMdoRef();

        if (service.getHandler().isEmpty()) {
          addMissingHandlerDiagnostic(serviceName);
          return;
        }
        checkMethod(mdHttpService, serviceName, service.getHandler());
      });

  }

  private void checkMethod(MDHttpService mdHttpService, String serviceName, String handlerName) {
    documentContext.getServerContext()
      .getDocument(mdHttpService.getMdoReference().getMdoRef(), ModuleType.HTTPServiceModule)
      .ifPresent((DocumentContext commonModuleContext) -> {
        final var foundMethod = commonModuleContext.getSymbolTree().getMethods().stream()
          .filter(methodSymbol -> methodSymbol.getName().equalsIgnoreCase(handlerName))
          .findFirst();
        foundMethod.ifPresentOrElse(
          methodSymbol -> checkMethodParams(serviceName, handlerName, methodSymbol),
          () -> addDefaultDiagnostic(serviceName, handlerName));
      });
  }

  private void checkMethodParams(String serviceName, String handlerName, MethodSymbol methodSymbol) {
    if (methodSymbol.getParameters().size() != 1) {
      addIncorrectHandlerDiagnostic(serviceName, handlerName);
    }
  }

  private void addDefaultDiagnostic(String serviceName, String handlerName) {
    diagnosticStorage.addDiagnostic(diagnosticRange,
      info.getMessage(handlerName, serviceName));
  }

  private void addMissingHandlerDiagnostic(String serviceName) {
    diagnosticStorage.addDiagnostic(diagnosticRange,
      info.getResourceString("missingHandler", serviceName));
  }

  private void addIncorrectHandlerDiagnostic(String serviceName, String handlerName) {
    diagnosticStorage.addDiagnostic(diagnosticRange,
      info.getResourceString("incorrectHandler", handlerName, serviceName));
  }
}
