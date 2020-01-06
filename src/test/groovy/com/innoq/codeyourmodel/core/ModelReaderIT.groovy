package com.innoq.codeyourmodel.core

import com.innoq.codeyourmodel.samples.bigpugloans.metamodel.*
import spock.lang.Specification

import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Subdomain.Type.Core

class ModelReaderIT extends Specification {

  def "big pug loans sample model can be initialized"() {
    given:
    ModelRepository modelRepository = new InMemoryModelRepository()
    ModelReader modelReader = new ModelReader(modelRepository)
      .registerElementType(Domain)
      .registerElementType(Subdomain)
      .registerElementType(BoundedContext)
      .registerElementType(ExternalSystem)
      .registerElementType(Interface)

    when:
    def file = new File("src/test/groovy/com/innoq/codeyourmodel/samples/bigpugloans/model.groovy")
    modelReader.read(file.toURI())

    then:
    modelRepository.allElements().size() == 25

    and:
    modelRepository.findAll(Domain).size() == 1
    modelRepository.findAll(Subdomain).size() == 6
    modelRepository.findAll(BoundedContext).size() == 6
    modelRepository.findAll(ExternalSystem).size() == 4
    modelRepository.findAll(Interface).size() == 8

    and:
    def domain = modelRepository.find(Domain, "Retail Mortage Loans")
    domain != null
    domain.name == "Retail Mortage Loans"

    and:
    def scoringSubdomain = modelRepository.find(Subdomain, "Scoring")
    scoringSubdomain != null
    scoringSubdomain.name == "Scoring"
    scoringSubdomain.type == Core
    scoringSubdomain.relationships.size() == 1
    scoringSubdomain.relationships[0].name == "contains"
    scoringSubdomain.relationships[0].to.class == BoundedContext
    scoringSubdomain.relationships[0].to.name == "Scoring"

    and:
    def crmSystem = modelRepository.find(ExternalSystem, "CRM System")
    crmSystem != null
    crmSystem.name == "CRM System"
    crmSystem.relationships.size() == 1
    crmSystem.relationships[0].name == "provides"
    crmSystem.relationships[0].to.class == Interface
    crmSystem.relationships[0].to.name == "bank customers/deposits"
  }

}
