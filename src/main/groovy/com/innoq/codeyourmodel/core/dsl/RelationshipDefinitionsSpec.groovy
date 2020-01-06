package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.RelationshipDefinition
import groovy.util.logging.Log

@Log
class RelationshipDefinitionsSpec {
  def relationshipDefinitions = []

  def initFor(Class<? extends Element> elementType) {
    Closure initClosure
    try {
      initClosure = elementType.relationshipDefinitions
    } catch (MissingPropertyException ignored) {
      log.fine("initFor(Class<${elementType.simpleName}>): no relationships closure found")
      return this
    }

    log.fine("initFor(Class<${elementType.simpleName}>): relationships closure found")
    log.fine("apply(<Closure>)")
    this.with(initClosure)

    log.info("initFor(Class<${elementType.simpleName}>): ${relationshipDefinitions.size()} relationship definitions initialized")
    this
  }

  RelationshipDefinition find(String name, Class<? extends Element> type) {
    relationshipDefinitions.find { it.name == name && it.targetType == type }
  }

  def methodMissing(String name, Object args) {
    log.fine("methodMissing('${name}', ${args})")
    Class<? extends Element> targetType = (args as List)[0] as Class
    def definition = new RelationshipDefinition(name: name, targetType: targetType)
    relationshipDefinitions << definition

    if ((args as List)[1] != null && (args as List)[1] instanceof Closure) {
      Closure closure = (args as List)[1] as Closure
      if (closure) {
        log.fine("init Closure found for relationshipDefinition")
        definition.with closure
      }
    }
  }

}
