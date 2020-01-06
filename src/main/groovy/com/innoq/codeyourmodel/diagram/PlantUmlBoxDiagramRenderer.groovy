package com.innoq.codeyourmodel.diagram

import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.Relationship
import groovy.text.GStringTemplateEngine
import groovy.text.Template


class PlantUmlBoxDiagramRenderer implements DiagramRenderer {

  private static final DEFAULT_ELEMENT_TEMPLATE = '''rectangle "${it.name}" <<${it.class.simpleName}>>'''
  private static final DEFAULT_RELATIONSHIP_LABEL_TEMPLATE = '''<<${it.name}>>'''

  List<Element> elements = []
  Map<Class<? extends Element>, String> elementTemplates = [:]
  Map<Closure, String> relationshipLabelTemplates = [:]

  PlantUmlBoxDiagramRenderer useElementTemplate(Class<? extends Element> type, String template) {
    elementTemplates.put(type, template)
    return this
  }

  PlantUmlBoxDiagramRenderer useRelationshipLabelTemplate(String template) {
    useRelationshipLabelTemplate(template, { true })
    return this
  }

  PlantUmlBoxDiagramRenderer useRelationshipLabelTemplate(String template, Closure predicate) {
    relationshipLabelTemplates.put(predicate, template)
    return this
  }

  PlantUmlBoxDiagramRenderer add(Element... elements) {
    this.elements.addAll(elements)
    return this
  }

  PlantUmlBoxDiagramRenderer add(List<Element> elements) {
    this.elements.addAll(elements)
    return this
  }

  String renderDiagram() {
    def diagramData = "@startuml\n\n"

    elements.each {
      diagramData += elementTemplate(it).make([it: it]).toString() + " as ${keyFor(it)}\n"
    }

    diagramData += "\n"

    elements.each {
      it.relationships.findAll {
        it.to in elements
      }.each {
        def label = relationshipLabelTemplate(it).make([it: it]).toString()
        label = label.replace('\n', '\\n')
        diagramData += """${keyFor(it.from)} --> ${keyFor(it.to)} : ${label}\n"""
      }
    }

    diagramData += "\n@enduml\n"
    diagramData
  }

  def keyFor(Element element) {
    "${element.class.simpleName}_${normalize(element.name)}"
  }

  private Template elementTemplate(Element element) {
    new GStringTemplateEngine().createTemplate(elementTemplates[element.class] ?: DEFAULT_ELEMENT_TEMPLATE)
  }

  private Template relationshipLabelTemplate(Relationship relationship) {
    def templateString = relationshipLabelTemplates.find { [relationship].any it.key }?.value
    new GStringTemplateEngine().createTemplate(templateString ?: DEFAULT_RELATIONSHIP_LABEL_TEMPLATE)
  }

  def normalize(String name) {
    name.replaceAll(/[^\w\d]/, "")
  }

}
