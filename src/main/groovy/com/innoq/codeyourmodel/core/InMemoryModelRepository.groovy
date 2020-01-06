package com.innoq.codeyourmodel.core

import com.innoq.codeyourmodel.core.meta2.Element

import groovy.util.logging.Log

@Log
class InMemoryModelRepository implements ModelRepository {

  List elements = []

  void clear() {
    this.elements = []
  }

  def <T extends Element> void addElements(List<T> elements) {
    this.elements.addAll(elements)
  }

  def <T extends Element> List<T> allElements() {
    elements
  }

  def <T extends Element> T find(Class<T> type, String name) {
    elements.find { it.class == type && it.name == name }
  }

  def <T extends Element> List<T> findAll(Class<T> elementType) {
    elements.findAll { it.class == elementType } as T[]
  }

  def <T extends Element> List<T> findAllRelated(Element element, String relationshipName) {
    element.relationships.findAll { it.name == relationshipName }.collect { it.to } as T[]
  }

}
