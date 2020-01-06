package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.meta2.Element
import spock.lang.Specification


class RelationshipSpecTests extends Specification {

  def "init - creates new Relationship with specified name, from element and to element"() {
    given:
    Element fromElement = Mock()
    Element toElement = Mock()
    RelationshipSpec spec = new RelationshipSpec(
      name: "relationship name",
      from: Mock(ElementSpec) { getElement() >> fromElement },
      to: Mock(ElementSpec) { getElement() >> toElement }
    )

    when:
    def relationship = spec.init()

    then:
    relationship.name == "relationship name"
    relationship.from == fromElement
    relationship.to == toElement
  }

  def "apply - allows to set value for defined attribute"() {
    given:
    RelationshipSpec spec = new RelationshipSpec(
      attributeDefinitions: [attr: String])

    when:
    spec.apply {
      attr = "value"
    }

    then:
    spec.attributes.attr == "value"
  }

  def "apply - does not allow to set value for undefined attribute"() {
    given:
    RelationshipSpec spec = new RelationshipSpec(
      attributeDefinitions: [attr1: String])

    when:
    spec.apply {
      attr2 = "value"
    }

    then:
    spec.attributes.attr2 == null
  }

  def "apply - does not allow to set value for defined attribute with different"() {
    given:
    RelationshipSpec spec = new RelationshipSpec(
      attributeDefinitions: [attr: String])

    when:
    spec.apply {
      attr = 111
    }

    then:
    spec.attributes.attr2 == null
  }
}
