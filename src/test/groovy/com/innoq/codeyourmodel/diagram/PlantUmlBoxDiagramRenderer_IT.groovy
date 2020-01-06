package com.innoq.codeyourmodel.diagram

import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.Relationship
import spock.lang.Specification

class PlantUmlBoxDiagramRenderer_IT extends Specification {

  DiagramRenderer renderer

  def setup() {
    renderer = new PlantUmlBoxDiagramRenderer()
  }

  def "renders empty diagram"() {
    when:
    def diagram = renderer.renderDiagram()

    then:
    def lines = diagram.split("\n")
    lines[0] == "@startuml"
    lines[1] == ""
    lines[2] == ""
    lines[3] == ""
    lines[4] == "@enduml"
  }

  def "renders diagram with single element"() {
    given:
    def element = new Element(name: "element")

    when:
    renderer.add(element)
    def diagram = renderer.renderDiagram()

    then:
    def lines = diagram.split("\n")
    lines[0] == "@startuml"
    lines[1] == ""
    lines[2] == """rectangle "element" <<Element>> as Element_element"""
    lines[3] == ""
    lines[4] == ""
    lines[5] == "@enduml"
  }

  def "renders element with spaces and special characters in name"() {
    given:
    def element = new Element(name: "element with spaces & special characters")

    when:
    renderer.add(element)
    def diagram = renderer.renderDiagram()

    then:
    def lines = diagram.split("\n")
    lines[0] == "@startuml"
    lines[1] == ""
    lines[2] == """rectangle "element with spaces & special characters" <<Element>> as Element_elementwithspacesspecialcharacters"""
    lines[3] == ""
    lines[4] == ""
    lines[5] == "@enduml"
  }

  def "renders diagram with multiple elements"() {
    given:
    def element1 = new Element(name: "element1")
    def element2 = new Element(name: "element2")
    def element3 = new Element(name: "element3")

    when:
    DiagramRenderer renderer = new PlantUmlBoxDiagramRenderer()
    renderer.add(element1, element2, element3)
    def diagram = renderer.renderDiagram()

    then:
    def lines = diagram.split("\n")
    lines[0] == "@startuml"
    lines[1] == ""
    lines[2] == """rectangle "element1" <<Element>> as Element_element1"""
    lines[3] == """rectangle "element2" <<Element>> as Element_element2"""
    lines[4] == """rectangle "element3" <<Element>> as Element_element3"""
    lines[5] == ""
    lines[6] == ""
    lines[7] == "@enduml"
  }

  def "renders relationship between two contained elements"() {
    given:
    def element1 = new Element(name: "element1")
    def element2 = new Element(name: "element2")
    element2.relationships = [new Relationship<Element, Element>(name: "dependsOn", from: element2, to: element1)]

    when:
    DiagramRenderer renderer = new PlantUmlBoxDiagramRenderer()
    renderer.add(element1, element2)
    def diagram = renderer.renderDiagram()

    then:
    def lines = diagram.split("\n")
    lines[0] == "@startuml"
    lines[1] == ""
    lines[2] == """rectangle "element1" <<Element>> as Element_element1"""
    lines[3] == """rectangle "element2" <<Element>> as Element_element2"""
    lines[4] == ""
    lines[5] == """Element_element2 --> Element_element1 : <<dependsOn>>"""
    lines[6] == ""
    lines[7] == "@enduml"
  }

  def "does not render relationship if diagram does not contain target elements"() {
    given:
    def element1 = new Element(name: "element1")
    def element2 = new Element(name: "element2")
    element2.relationships = [new Relationship<Element, Element>(name: "dependsOn", from: element2, to: element1)]

    when:
    DiagramRenderer renderer = new PlantUmlBoxDiagramRenderer()
    renderer.add(element2)
    def diagram = renderer.renderDiagram()

    then:
    def lines = diagram.split("\n")
    lines[0] == "@startuml"
    lines[1] == ""
    lines[2] == """rectangle "element2" <<Element>> as Element_element2"""
    lines[3] == ""
    lines[4] == ""
    lines[5] == "@enduml"
  }

}
