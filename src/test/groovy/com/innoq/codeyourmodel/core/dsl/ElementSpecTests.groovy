package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.meta2.Element
import spock.lang.Specification
import spock.lang.Unroll

class ElementSpecTests extends Specification {

  def "constructor - initializes ElementSpec with element type and name"() {
    when:
    ElementSpec spec = new ElementSpec(SomeElementType, "someName")

    then:
    spec.elementType == SomeElementType
    spec.props.name == "someName"
  }

  def "constructor - initializes RelationshipDefinitionSpec for element type"() {
    when:
    ElementSpec spec = new ElementSpec(SomeElementType, "someName")

    then:
    spec.relationshipDefinitionsSpec.relationshipDefinitions.size() == 1
    spec.relationshipDefinitionsSpec.relationshipDefinitions[0].name == "relatesTo"
    spec.relationshipDefinitionsSpec.relationshipDefinitions[0].targetType == SomeElementType
  }

  def "apply - allows to set defined property"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")

    when:
    spec.apply {
      someProperty = "some value"
    }

    then:
    spec.props.someProperty == "some value"
  }

  def "apply - allows to add defined relationship"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")
    ElementSpec otherSpec = new ElementSpec(SomeElementType, "other element")

    when:
    spec.apply {
      relatesTo otherSpec
    }

    then:
    spec.relationshipSpecs[0].name == "relatesTo"
    spec.relationshipSpecs[0].from == spec
    spec.relationshipSpecs[0].to == otherSpec
  }

  def "apply - allows to add defined relationship with attributes closure"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")
    ElementSpec otherSpec = new ElementSpec(SomeElementType, "other element")

    when:
    spec.apply {
      relatesTo(otherSpec, {
        attr1 = "value"
        attr2 = 123
      })
    }

    then:
    spec.relationshipSpecs[0].name == "relatesTo"
    spec.relationshipSpecs[0].from == spec
    spec.relationshipSpecs[0].to == otherSpec
    spec.relationshipSpecs[0].attributes.attr1 == "value"
    spec.relationshipSpecs[0].attributes.attr2 == 123
  }

  def "apply - skips undefined relationship"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")
    ElementSpec otherSpec = new ElementSpec(SomeElementType, "other element")

    when:
    spec.apply {
      undefRelates otherSpec
    }

    then:
    spec.relationshipSpecs.size() == 0
  }

  def "apply - throws MissingMethodException if argument of relationship definition is no Element"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")

    when:
    spec.apply {
      relatesTo "String"
    }

    then:
    thrown(MissingMethodException)
  }

  def "apply - accepts empty closure"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")

    when:
    spec.apply {}

    then:
    spec.props.someProperty == null
  }

  def "initElement - initializes element type with name"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "someName")

    when:
    spec.initElement()

    then:
    spec.element.class == SomeElementType
    spec.element.name == "someName"
  }

  def "initElement - initializes element with defined attributes"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "someName")
    spec.apply {
      someProperty = "some value"
    }

    when:
    spec.initElement()

    then:
    spec.element.someProperty == "some value"
  }

  def "initElement - cannot be called twice"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "someName")
    spec.initElement()

    when:
    spec.initElement()

    then:
    thrown(IllegalStateException)
  }

  def "initRelationships - adds relationships to element"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")
    ElementSpec otherSpec = new ElementSpec(SomeElementType, "other element")
    spec.apply {
      relatesTo otherSpec
    }

    and:
    spec.initElement()
    otherSpec.initElement()

    when:
    spec.initRelationships()

    then:
    spec.element.relationships.size() == 1
    spec.element.relationships[0].name == "relatesTo"
    spec.element.relationships[0].from.class == SomeElementType
    spec.element.relationships[0].from.name == "some element"
    spec.element.relationships[0].to.class == SomeElementType
    spec.element.relationships[0].to.name == "other element"
  }

  def "initRelationships - adds reverse relationship to related element"() {
    given:
    ElementSpec spec = new ElementSpec(ElementWithReverseRelationship, "some element")
    ElementSpec otherSpec = new ElementSpec(SomeElementType, "other element")
    spec.apply {
      refersTo otherSpec
    }

    and:
    spec.initElement()
    otherSpec.initElement()

    when:
    spec.initRelationships()

    then:
    spec.element.relationships.size() == 1
    spec.element.relationships[0].name == "refersTo"
    spec.element.relationships[0].from.class == ElementWithReverseRelationship
    spec.element.relationships[0].from.name == "some element"
    spec.element.relationships[0].to.class == SomeElementType
    spec.element.relationships[0].to.name == "other element"

    and:
    Element relatedElement = spec.element.relationships[0].to
    relatedElement.relationships.size() == 1
    relatedElement.relationships[0].name == "isReferedBy"
    relatedElement.relationships[0].from.class == SomeElementType
    relatedElement.relationships[0].from.name == "other element"
    relatedElement.relationships[0].to.class == ElementWithReverseRelationship
    relatedElement.relationships[0].to.name == "some element"
  }

  def "initRelationships - element has to be initialized before"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "some element")
    ElementSpec otherSpec = new ElementSpec(SomeElementType, "other element")
    spec.apply {
      relatesTo otherSpec
    }

    when:
    spec.initRelationships()

    then:
    thrown(IllegalStateException)
  }

  def "ElementSpec(SomeElementType, 'someName') matches element type SomeElementType and name 'someName'"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "someName")

    expect:
    spec.matches(SomeElementType, "someName")
  }

  @Unroll
  def "ElementSpec(SomeElementType, 'someName') don't matches element type #elementType and name '#name'"() {
    given:
    ElementSpec spec = new ElementSpec(SomeElementType, "someName")

    expect:
    !spec.matches(elementType, name)

    where:
    elementType      | name
    OtherElementType | "someName"
    SomeElementType  | "otherName"
    null             | "someName"
    SomeElementType  | null
  }


  static class SomeElementType extends Element {
    String someProperty

    static relationshipDefinitions = {
      relatesTo(SomeElementType) { attributes(attr1: String, attr2: Integer) }
    }
  }

  static class ElementWithReverseRelationship extends Element {
    static relationshipDefinitions = {
      refersTo(SomeElementType) { reverse("isReferedBy") }
    }
  }

  static class OtherElementType extends Element {}
}
