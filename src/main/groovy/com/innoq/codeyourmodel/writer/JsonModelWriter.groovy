package com.innoq.codeyourmodel.writer

import com.innoq.codeyourmodel.core.ModelRepository
import com.innoq.codeyourmodel.core.meta2.Element

import com.innoq.codeyourmodel.core.meta2.Relationship
import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.util.logging.Log

@Log
class JsonModelWriter {
  private final ModelRepository modelRepository

  JsonModelWriter(ModelRepository modelRepository) {
    this.modelRepository = modelRepository
  }

  def writeTo(OutputStream outputStream) {
    if (!modelRepository || !modelRepository.allElements())
      return

    outputStream.write(toJson(modelRepository.allElements()).bytes)
  }

  String toJson(List<Element> elements) {
    log.fine("toJson(List<Element>)")
    Map data = toMap(elements)
    JsonOutput.prettyPrint(
      new JsonGenerator.Options()
        .excludeNulls()
        .excludeFieldsByName('RESERVED_PROPERTY_NAMES')
        .excludeFieldsByType(Closure) // exclude relationshipDefinitions
        .build()
        .toJson(data))
  }

  Map toMap(List<Element> elements) {
    log.fine("toMap(List<Element>)")
    Map map = [:]
    map.put("elements", elements.collect { toMap(it) })
    map
  }

  Map toMap(Element element) {
    log.fine("toMap(${element.class.simpleName} '${element.name}')")
    Map map = [:]
    element.metaClass.properties
      .findAll { it.name != "class" && it.name != "relationships" }
      .each { map.put(it.name, element.getProperty(it.name)) }
    map.put("class", element.class.simpleName)
    map.put("relationships", element.relationships.collect { toMap(it) })
    map
  }

  Map toMap(Relationship relationship) {
    log.fine("toMap(Relationship '${relationship.name}')")
    Map map = [:]
    map.put("name", relationship.name)
    map.put("from", referenceMap(relationship.from))
    map.put("to", referenceMap(relationship.to))
    map
  }

  Map referenceMap(Element element) {
    log.fine("referenceMap(${element.class.simpleName} '${element.name}')")
    Map map = [:]
    map.put("class", element.class.simpleName)
    map.put("name", element.name)
    map
  }
}
