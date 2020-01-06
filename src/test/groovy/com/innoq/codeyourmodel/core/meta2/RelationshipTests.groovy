package com.innoq.codeyourmodel.core.meta2

import spock.lang.Specification


class RelationshipTests extends Specification {

  def "it is initialized with a name, a from and a to element, and an attributes map"() {
    given:
    Element element1 = Mock()
    Element element2 = Mock()

    when:
    def relationship = new Relationship(name: "name", from: element1, to: element2, attributes: [attr1: "value", attr2: 123])

    then:
    relationship.name == "name"
    relationship.from == element1
    relationship.to == element2
    relationship.attributes.size() == 2
    relationship.attributes.attr1 == "value"
    relationship.attributes.attr2 == 123
  }

  def "known attributes can be accessed as if there were a getter"() {
    given:
    def relationship = new Relationship(attributes: [attr1: "value", attr2: 123])

    expect:
    relationship.attr1 == "value"
    relationship.attr2 == 123
  }

  def "unknown attributes are returned as null"() {
    given:
    def relationship = new Relationship(attributes: [attr1: "value"])

    expect:
    relationship.attr2 == null
  }

}
