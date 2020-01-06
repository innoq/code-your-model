package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.RelationshipDefinition
import groovy.transform.PackageScope
import groovy.util.logging.Log

@Log
class ElementSpec<T extends Element> {
  @PackageScope
  final Class<T> elementType
  @PackageScope
  final RelationshipDefinitionsSpec relationshipDefinitionsSpec
  @PackageScope
  def relationshipSpecs = []
  @PackageScope
  Map<String, Object> props = [:]

  T element

  ElementSpec(Class<T> elementType, String name) {
    this.elementType = elementType
    this.props.name = name
    this.relationshipDefinitionsSpec = new RelationshipDefinitionsSpec().initFor(elementType)
  }

  ElementSpec<T> apply(Closure initClosure) {
    log.fine("apply(<Closure>)")
    this.with(true, initClosure)
  }

  void set(String name, Object value) {
    log.fine("set('${name}', ${value})")
    props[name] = value
  }

  def methodMissing(String name, Object args) {
    log.fine("methodMissing(${name}, ${args})")

    def argsList = args as List
    if (argsList[0].class != ElementSpec)
      throw new MissingMethodException(name, ElementSpec.class, args)

    ElementSpec targetElementSpec = argsList[0] as ElementSpec
    RelationshipDefinition relationshipDef = relationshipDefinitionsSpec.find(name, targetElementSpec.elementType)
    if (relationshipDef == null) {
      log.warning("relationship with name '${name}' and targetType ${targetElementSpec.elementType} is not defined in type ${elementType} and therefore skipped")
      return
    }

    log.info("relationship with name '${name}' and targetType ${targetElementSpec.elementType} found")
    def newRelationshipSpec = new RelationshipSpec(name: name, reverseName: relationshipDef.reverseName, from: this, to: targetElementSpec, attributeDefinitions: relationshipDef.attributeDefinitions)
    relationshipSpecs << newRelationshipSpec

    if (argsList.size() > 1)
      newRelationshipSpec.apply(argsList[1] as Closure)
  }

  def initElement() {
    log.fine("initElement()")
    if (element)
      throw new IllegalStateException("Element was already initialized")
    element = elementType.newInstance(props)
  }

  def initRelationships() {
    log.fine("initRelationships()")
    if (!element)
      throw new IllegalStateException("Element has to be initialized before Relationships")
    relationshipSpecs.each {
      def relationship = it.init()
      element.relationships.add(relationship)
      if (it.reverseName)
        relationship.to.relationships.add(it.initReverse())
    }
  }

  boolean matches(Class<T> elementType, String name) {
    return this.elementType == elementType && this.props.name == name
  }

  @Override
  String toString() {
    "${this.class.simpleName}<${elementType.simpleName}>('${this.props.name}')"
  }

}
