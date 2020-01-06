package com.innoq.codeyourmodel.samples.eventdriven

import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.Relationship
import com.innoq.codeyourmodel.samples.eventdriven.metamodel.Event


class CustomPlantUmlEventDiagramRenderer {
  private final Event event
  private String diagramData
  private String previousConsumedByElementKey

  CustomPlantUmlEventDiagramRenderer(Event event) {
    this.event = event
  }

  String renderDiagram() {
    startUml()
    writeEvent(event)
    event.relationships.findAll { it.name == "publishedBy" }.each {
      writePublishedByRelationship(it)
    }
    event.relationships.findAll { it.name == "consumedBy" }.each {
      writeConsumedByRelationship(it)
    }
    endUml()
    diagramData
  }

  private startUml() {
    diagramData = "@startuml\n\n"
  }

  private writeEvent(Event event) {
    diagramData += "($event.name) <<${event.class.simpleName}>> as event\n\n"
  }

  private writePublishedByRelationship(Relationship relationship) {
    def elementKey = keyFor(relationship.to)
    def elementName = relationship.to.name
    def elementType = relationship.to.class.simpleName
    diagramData += """rectangle "$elementName" <<$elementType>> as $elementKey\n"""
    diagramData += "$elementKey -> event : $relationship.name\n"

    if (relationship.attributes.description) {
      diagramData += "note left of $elementKey\n"
      diagramData += "  $relationship.attributes.description\n"
      diagramData += "end note\n"
    }

    diagramData += "\n"
  }

  private writeConsumedByRelationship(Relationship relationship) {
    def elementKey = keyFor(relationship.to)
    def elementName = relationship.to.name
    def elementType = relationship.to.class.simpleName
    diagramData += """rectangle "$elementName" <<$elementType>> as $elementKey\n"""
    diagramData += "event <- $elementKey : $relationship.name\n"

    if (relationship.attributes.description) {
      diagramData += "note right of $elementKey\n"
      diagramData += "  $relationship.attributes.description\n"
      diagramData += "end note\n"
    }

    // add some hidden relationships between the consumed by elements for layouting
    if (previousConsumedByElementKey) {
      diagramData += "\n$previousConsumedByElementKey --[hidden] $elementKey\n"
    }
    previousConsumedByElementKey = elementKey

    diagramData += "\n"
  }

  private endUml() {
    diagramData += "@enduml\n"
  }

  def keyFor(Element element) {
    "${element.class.simpleName}_${normalize(element.name)}"
  }

  def normalize(String name) {
    name.replaceAll(/[^\w\d]/, "")
  }

}
