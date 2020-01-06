package com.innoq.codeyourmodel.diagram

import com.innoq.codeyourmodel.core.meta2.Element


interface DiagramRenderer {

  def add(Element... elements)

  String renderDiagram()

}
