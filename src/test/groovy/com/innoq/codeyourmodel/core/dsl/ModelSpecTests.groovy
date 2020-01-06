package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.KnownElement
import com.innoq.codeyourmodel.core.meta2.Element
import spock.lang.Specification


class ModelSpecTests extends Specification {

  def "registerElementType - adds given Type to list of element types"() {
    given:
    ModelSpec spec = new ModelSpec()

    when:
    spec.registerElementType(KnownElement)

    then:
    spec.elementTypes.size() == 1
    spec.elementTypes['KnownElement'] == KnownElement
  }

  def "initWith - Closure - inits no element specs when called with empty Closure"() {
    given:
    ModelSpec spec = new ModelSpec()

    when:
    spec.initWith {}

    then:
    spec.elementSpecs.size() == 0
  }

  def "initWith - Closure - throws exception if refers to unregistered element type"() {
    given:
    ModelSpec spec = new ModelSpec()

    when:
    spec.initWith {
      KnownElement("known element")
    }

    then:
    thrown(IllegalStateException)
  }

  def "initWith - Closure - throws exception if calls element type method without name argument"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    when:
    spec.initWith {
      KnownElement()
    }

    then:
    thrown(IllegalArgumentException)
  }

  def "initWith - Closure - throws exception if calls element type method with name argument other than String"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    when:
    spec.initWith {
      KnownElement(123)
    }

    then:
    thrown(IllegalArgumentException)
  }

  def "initWith - Closure - inits element spec"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    when:
    spec.initWith {
      KnownElement("known element")
    }

    then:
    spec.elementSpecs.size() == 1
    spec.elementSpecs[0].elementType == KnownElement
    spec.elementSpecs[0].props.name == "known element"
  }

  def "initWith - Closure - applies init elememt closure to initialized element spec"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    when:
    spec.initWith {
      KnownElement("known element") {
        description = "known description"
      }
    }

    then:
    spec.elementSpecs[0].props.description == "known description"
  }

  def "initWith - Closure - throws exception if calls element type method with 2nd argument other than Closure"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    when:
    spec.initWith {
      KnownElement("known element", 123)
    }

    then:
    thrown(IllegalArgumentException)
  }

  def "initWith - Closure - refers to already initialized element spec"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    when:
    spec.initWith {
      KnownElement("known element")

      KnownElement("other element") {
        isChildOf KnownElement("known element")
      }
    }

    then:
    def knownElementSpec = spec.elementSpecs.find { it.props.name == "known element" }
    def otherElementSpec = spec.elementSpecs.find { it.props.name == "other element" }
    otherElementSpec.relationshipSpecs[0].to == knownElementSpec
  }

  def "initWith - Closure - applies another init closure to already initialized element spec"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    when:
    spec.initWith {
      KnownElement("known element") {
        description = "description"
      }

      KnownElement("known element") {
        type = KnownElement.Type.type2
      }
    }

    then:
    spec.elementSpecs.size() == 1
    spec.elementSpecs[0].props.description == "description"
    spec.elementSpecs[0].props.type == KnownElement.Type.type2
  }

  def "initElements - inits Elements and then Relationships of all Element Specs"() {
    given:
    Element element1 = Mock()
    ElementSpec elementSpec1 = Mock() {
      getElement() >> element1
    }
    Element element2 = Mock()
    ElementSpec elementSpec2 = Mock() {
      getElement() >> element2
    }
    ModelSpec spec = new ModelSpec(elementSpecs: [elementSpec1, elementSpec2])

    when:
    def elements = spec.initElements()

    then:
    1 * elementSpec1.initElement()
    1 * elementSpec2.initElement()
    1 * elementSpec1.initRelationships()
    1 * elementSpec2.initRelationships()

    and:
    elements == [element1, element2]
  }

  def "initFrom URIs - inits model spec from closures behind all URIs"() {
    given:
    ModelSpec spec = new ModelSpec()
    spec.registerElementType(KnownElement)

    and:
    def url1 = GroovyMock(URL)
    def uri1 = GroovyMock(URI) { toURL() >> url1 }
    url1.getText(_ as Map) >> """KnownElement("element1")"""
    def url2 = GroovyMock(URL)
    def uri2 = GroovyMock(URI) { toURL() >> url2 }
    url2.getText(_ as Map) >> """KnownElement("element2")"""

    when:
    spec.initFrom(uri1, uri2)

    then:
    spec.elementSpecs.size() == 2
    spec.elementSpecs.find { it.props.name == "element1" }
    spec.elementSpecs.find { it.props.name == "element2" }
  }

}
