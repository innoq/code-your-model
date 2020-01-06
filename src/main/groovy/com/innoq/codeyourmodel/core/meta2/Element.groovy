package com.innoq.codeyourmodel.core.meta2

import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Log

@Log
@EqualsAndHashCode
class Element {
  static final RESERVED_PROPERTY_NAMES = ['name', 'class', 'relationships', 'relationshipDefinitions', 'RESERVED_PROPERTY_NAMES']

  String name
  List<Relationship> relationships = []

  @Override
  String toString() {
    "${this.class.simpleName}('${this.name}')"
  }

  transient List<String> customProperties() {
    this.metaClass.properties
      .findAll { !(it.name in RESERVED_PROPERTY_NAMES) }
      .findAll { this.getProperty(it.name) != null }
      .collect { it.name }
  }

}
