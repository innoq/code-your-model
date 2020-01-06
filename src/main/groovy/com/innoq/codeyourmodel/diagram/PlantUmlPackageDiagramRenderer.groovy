package com.innoq.codeyourmodel.diagram

import com.innoq.codeyourmodel.core.meta2.Element


class PlantUmlPackageDiagramRenderer {

  Element rootElement
  Class subpackageType
  Class componentType

  String diagramData


  def rootPackage(Element element) {
    this.rootElement = element
    this
  }

  def subpackagesForType(Class elementType) {
    subpackageType = elementType
    this
  }

  def componentsForType(Class elementType) {
    componentType = elementType
    this
  }

  String renderDiagram() {
    startUml()
    writePackage(rootElement)
    endUml()
    diagramData
  }

  private startUml() {
    diagramData = "@startuml\n\n"
  }

  private endUml() {
    diagramData += "@enduml\n"
  }

  private writePackage(Element element) {
    startPackage(element.name)

    element.relationships.each {
      if (it.to.class == subpackageType) {
        writePackage(it.to)
      }

      if (it.to.class == componentType) {
        writeComponent(it.to)
      }
    }

    endPackage()
  }

  private startPackage(String name) {
    diagramData += """package "${name}" {\n\n"""
  }

  private endPackage() {
    diagramData += "}\n\n"
  }

  private writeComponent(Element element) {
    diagramData += """["${element.name}"]\n"""
  }
}
