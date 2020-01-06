package com.innoq.codeyourmodel.core

import com.innoq.codeyourmodel.core.ModelRepository
import com.innoq.codeyourmodel.core.meta2.Relationship
import com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Domain
import com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Subdomain
import spock.lang.Specification

class InMemoryModelRepositoryTest extends Specification {

  ModelRepository repository

  def setup() {
    repository = new InMemoryModelRepository()
  }

  def "it stores a list of elements"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")

    when:
    repository.addElements([domain1, domain2])

    then:
    repository.findAll(Domain).size() == 2
  }

  def "it stores relationships between elements"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    domain1.relationships << new Relationship(name: "relatesTo", from: domain1, to: domain2)

    when:
    repository.addElements([domain1, domain2])

    then:
    repository.findAllRelated(domain1, "relatesTo").size() == 1
  }

  def "it retrieves all elements"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    def subdomain = new Subdomain(name: "subdomain")
    repository.addElements([domain1, subdomain, domain2])

    when:
    def allElements = repository.allElements()

    then:
    allElements.size() == 3
    allElements.contains(domain1)
    allElements.contains(domain2)
    allElements.contains(subdomain)
  }

  def "it retrieves all elements of a given type"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    def subdomain = new Subdomain(name: "subdomain")
    repository.addElements([domain1, subdomain, domain2])

    when:
    def allDomains = repository.findAll(Domain)

    then:
    allDomains.size() == 2
    allDomains.contains(domain1)
    allDomains.contains(domain2)
  }

  def "it retrieves element by type and name"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    repository.addElements([domain1, domain2])

    when:
    def domain = repository.find(Domain, "domain2")

    then:
    domain == domain2
  }

  def "it (stores and) retrieves elements with further attributes"() {
    given:
    def element = new KnownElement(name: "element1", type: KnownElement.Type.type1, number: 123, description: "description of element")
    repository.addElements([element])

    when:
    def retrievedElement = repository.find(KnownElement, "element1")

    then:
    retrievedElement.name == "element1"
    retrievedElement.type == KnownElement.Type.type1
    retrievedElement.number == 123
    retrievedElement.description == "description of element"
  }

  def "it retrieves all related elements for a given relationship name"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    def domain3 = new Domain(name: "domain3")
    def domain4 = new Domain(name: "domain4")
    domain1.relationships << new Relationship(name: "relatesTo", from: domain1, to: domain2)
    domain1.relationships << new Relationship(name: "relatesTo", from: domain1, to: domain3)
    domain1.relationships << new Relationship(name: "dependsOn", from: domain1, to: domain4)
    repository.addElements([domain1, domain2, domain3, domain4])

    when:
    def allRelatesTo = repository.findAllRelated(domain1, "relatesTo")

    then:
    allRelatesTo.size() == 2
    allRelatesTo.contains(domain2)
    allRelatesTo.contains(domain3)
  }

}
