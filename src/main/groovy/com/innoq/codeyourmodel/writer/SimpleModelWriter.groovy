package com.innoq.codeyourmodel.writer

import com.innoq.codeyourmodel.core.ModelRepository
import com.innoq.codeyourmodel.core.meta2.Element

import com.innoq.codeyourmodel.core.meta2.Relationship
import groovy.util.logging.Log

@Log
class SimpleModelWriter {
  private final ModelRepository modelRepository

  SimpleModelWriter(ModelRepository modelRepository) {
    this.modelRepository = modelRepository
  }

  void writeTo(OutputStream outputStream) {
    if (!modelRepository || !modelRepository.allElements())
      return

    modelRepository.allElements().forEach {
      writeElementTo(it, outputStream)
    }
  }

  private writeElementTo(Element element, OutputStream outputStream) {
    outputStream.write("- ${element.class.simpleName} '${element.name}'".bytes)

    String attributesAsString = element.metaClass.properties
      .findAll { !(it.name in Element.RESERVED_PROPERTY_NAMES) }
      .collect { "${it.name}=${element.getProperty(it.name)}" }
      .join(", ")
    if (attributesAsString.length() > 0)
      outputStream.write(" [${attributesAsString}]".bytes)

    outputStream.write("\n".bytes)

    element.relationships.forEach {
      writeRelationshipTo(it, outputStream)
    }
  }

  private writeRelationshipTo(Relationship relationship, OutputStream outputStream) {
    outputStream.write("   `-- ${relationship.name} ${relationship.to.class.simpleName} '${relationship.to.name}'".bytes)

    if (relationship.attributes?.size() > 0) {
      String attributesAsString = relationship.attributes.collect { "${it.key}=${it.value}" }.join(", ")
      outputStream.write(" [${attributesAsString}]".bytes)
    }

    outputStream.write("\n".bytes)
  }
}
