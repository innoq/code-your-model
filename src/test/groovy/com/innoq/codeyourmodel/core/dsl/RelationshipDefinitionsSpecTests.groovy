package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.RelationshipDefinition
import spock.lang.Specification
import spock.lang.Unroll

class RelationshipDefinitionsSpecTests extends Specification {

  def "accepts element types without relationshipDefinitions"() {
    when:
    def spec = new RelationshipDefinitionsSpec().initFor(ElementWithoutRelationshipDefinitions)

    then:
    spec.relationshipDefinitions.empty
  }

  def "accepts element types with one relationshipDefinition"() {
    when:
    def spec = new RelationshipDefinitionsSpec().initFor(ElementWithOneRelationshipDefinition)

    then:
    spec.relationshipDefinitions.size() == 1
    spec.relationshipDefinitions[0].name == "belongsTo"
    spec.relationshipDefinitions[0].targetType == ElementWithoutRelationshipDefinitions
  }

  def "accepts element types with multiple relationshipDefinitions"() {
    when:
    def spec = new RelationshipDefinitionsSpec().initFor(ElementWithMultipleRelationshipDefinitions)

    then:
    spec.relationshipDefinitions.size() == 2
    spec.relationshipDefinitions[0].name == "relation1"
    spec.relationshipDefinitions[0].targetType == ElementWithoutRelationshipDefinitions
    spec.relationshipDefinitions[1].name == "relation2"
    spec.relationshipDefinitions[1].targetType == ElementWithOneRelationshipDefinition
  }

  def "accepts element types with reverse relationshipDefinition"() {
    when:
    def spec = new RelationshipDefinitionsSpec().initFor(ElementWithReverseRelationshipDefinition)

    then:
    spec.relationshipDefinitions.size() == 1
    spec.relationshipDefinitions[0].name == "belongsTo"
    spec.relationshipDefinitions[0].reverseName == "reverseRelationship"
    spec.relationshipDefinitions[0].targetType == ElementWithoutRelationshipDefinitions
  }

  @Unroll
  def "matches defined relationship definitions by name and target type"() {
    given:
    RelationshipDefinition relationshipDefinition = Mock() {
      getName() >> "someName"
      getTargetType() >> ElementWithoutRelationshipDefinitions
    }
    def spec = new RelationshipDefinitionsSpec(relationshipDefinitions: [relationshipDefinition])

    expect:
    spec.find(name, targetType) == (defined ? relationshipDefinition : null)

    where:
    name        | targetType                                 | defined
    "someName"  | ElementWithoutRelationshipDefinitions      | true
    "otherName" | ElementWithoutRelationshipDefinitions      | false
    "someName"  | ElementWithMultipleRelationshipDefinitions | false
    null        | ElementWithoutRelationshipDefinitions      | false
    "someName"  | null                                       | false
  }

  static class ElementWithoutRelationshipDefinitions extends Element {
  }

  static class ElementWithOneRelationshipDefinition extends Element {
    static relationshipDefinitions = {
      belongsTo(ElementWithoutRelationshipDefinitions)
    }
  }

  static class ElementWithMultipleRelationshipDefinitions extends Element {
    static relationshipDefinitions = {
      relation1(ElementWithoutRelationshipDefinitions)
      relation2(ElementWithOneRelationshipDefinition)
    }
  }

  static class ElementWithReverseRelationshipDefinition extends Element {
    static relationshipDefinitions = {
      belongsTo(ElementWithoutRelationshipDefinitions) { reverse("reverseRelationship") }
    }
  }

}
