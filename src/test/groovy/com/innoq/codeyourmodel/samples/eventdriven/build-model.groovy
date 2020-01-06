package com.innoq.codeyourmodel.samples.eventdriven

import com.innoq.codeyourmodel.core.InMemoryModelRepository
import com.innoq.codeyourmodel.core.ModelReader
import com.innoq.codeyourmodel.core.ModelRepository
import com.innoq.codeyourmodel.samples.eventdriven.metamodel.Event
import com.innoq.codeyourmodel.samples.eventdriven.metamodel.Subsystem
import com.innoq.codeyourmodel.writer.SimpleModelWriter

ModelRepository modelRepository = new InMemoryModelRepository()
new ModelReader(modelRepository)
  .registerElementType(Subsystem)
  .registerElementType(Event)
  .read(new File("model.groovy").toURI())


println ""
println ""
println "-----"
println "Print out the whole model using the SimpleModelWriter"
println "-----"
new SimpleModelWriter(modelRepository).writeTo(System.out)


println ""
println ""
println "-----"
println "List all elements"
println "-----"
modelRepository.allElements().each { println "* ${it.class.simpleName} '${it.name}'" }


println ""
println ""
println "-----"
println "All Events with their publishers and consumers"
println "-----"

modelRepository.findAll(Event).each {
  println "* ${it.class.simpleName} '${it.name}'"
  def publishedBy = it.relationships.findAll { it.name == "publishedBy" }.collect {
    "${it.to.class.simpleName} '${it.to.name}'"
  }.join(", ")
  println "    published by: $publishedBy"

  def consumedBy = it.relationships.findAll { it.name == "consumedBy" }.collect {
    "${it.to.class.simpleName} '${it.to.name}'"
  }.join(", ")
  println "    consumed by: $consumedBy"
}


println ""
println ""
println "-----"
println "Generate diagram for each event"
println "-----"

modelRepository.findAll(Event).each {
  def filename = it.name.replace(' ', '-') + '-event-diagram.puml'
  def file = new File(filename)
  println "* '${file.absolutePath}'"
  file.text = new CustomPlantUmlEventDiagramRenderer(it).renderDiagram()
}
