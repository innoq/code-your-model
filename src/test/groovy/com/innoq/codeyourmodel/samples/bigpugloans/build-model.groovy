package com.innoq.codeyourmodel.samples.bigpugloans

import com.innoq.codeyourmodel.core.InMemoryModelRepository
import com.innoq.codeyourmodel.core.ModelReader
import com.innoq.codeyourmodel.core.ModelRepository
import com.innoq.codeyourmodel.diagram.PlantUmlBoxDiagramRenderer
import com.innoq.codeyourmodel.samples.bigpugloans.metamodel.*
import com.innoq.codeyourmodel.writer.JsonModelWriter
import com.innoq.codeyourmodel.writer.SimpleModelWriter

ModelRepository modelRepository = new InMemoryModelRepository()
new ModelReader(modelRepository)
  .registerElementType(Domain)
  .registerElementType(Subdomain)
  .registerElementType(BoundedContext)
  .registerElementType(ExternalSystem)
  .registerElementType(Interface)
  .read(new File("model.groovy").toURI())


println ""
println ""
println "-----"
println "Print out the whole model using the JsonModelWriter"
println "-----"
new JsonModelWriter(modelRepository).writeTo(System.out)


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
println "List all Subdomains"
println "-----"
modelRepository.findAll(Subdomain).each { println "* ${it.name} (${it.type})" }


println ""
println ""
println "-----"
println "List all Relationships of Domain 'Retail Mortage Loans'"
println "-----"
modelRepository.find(Domain, "Retail Mortage Loans").relationships.each {
  println "* ${it.name} ${it.to.class.simpleName} '${it.to.name}'"
}


println ""
println ""
println "--------"
def file = new File("context-map.puml")
println "Write context map to file '${file.absolutePath}'"
println "--------"

def renderer = new PlantUmlBoxDiagramRenderer()
  .add(modelRepository.findAll(BoundedContext))
  .add(modelRepository.findAll(Interface))
  .add(modelRepository.findAll(ExternalSystem))
  .useElementTemplate(Interface, '''() "${it.name}"''')
  .useRelationshipLabelTemplate('<<${it.name}>>\\n[${it.role}]\\n${it.strategies?:""}', { it.to.class == Interface })
  .useRelationshipLabelTemplate('<<${it.name}>>\\n[${it.strategy?:""}]', { it.to.class == BoundedContext })

file.text = renderer.renderDiagram()
