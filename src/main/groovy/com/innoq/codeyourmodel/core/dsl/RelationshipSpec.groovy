package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.Relationship
import groovy.util.logging.Log

@Log
class RelationshipSpec<S extends Element, T extends Element> {
  String name
  String reverseName
  ElementSpec from
  ElementSpec to
  Map attributes = [:]
  Map<String, Class> attributeDefinitions = [:]

  Relationship<S, T> init() {
    new Relationship<S, T>(name: name, from: from.element, to: to.element, attributes: attributes)
  }

  Relationship<T, S> initReverse() {
    new Relationship<T, S>(name: reverseName, from: to.element, to: from.element, attributes: attributes)
  }

  RelationshipSpec<S, T> apply(Closure initClosure) {
    log.fine("apply(<Closure>)")
    this.with(true, initClosure)
  }

  void set(String name, Object value) {
    log.fine("set('${name}', ${value})")

    if (!isAttributeDefined(name, value.class)) {
      log.warning("no attribute with name '${name}' and type '${value.class.simpleName}' defined for relationship '${this.from?.elementType?.simpleName}.${this.name}' -> skipped")
      return
    }

    attributes[name] = value
  }

  private boolean isAttributeDefined(String name, Class type) {
    return attributeDefinitions[name] == type
  }
}
