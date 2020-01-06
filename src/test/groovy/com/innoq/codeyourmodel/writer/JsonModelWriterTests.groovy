package com.innoq.codeyourmodel.writer

import com.innoq.codeyourmodel.core.ModelRepository
import com.innoq.codeyourmodel.core.meta2.Element

import com.innoq.codeyourmodel.core.meta2.Relationship
import groovy.json.JsonSlurper
import spock.lang.Specification


class JsonModelWriterTests extends Specification {

  OutputStream outputStream = Mock()
  ModelRepository modelRepository = Mock()
  JsonModelWriter writer = new JsonModelWriter(modelRepository)

  def "writes empty model"() {
    given:
    modelRepository.allElements() >> []

    when:
    writer.writeTo(outputStream)

    then:
    0 * outputStream.write(_ as byte[])
  }

  def "writes all elements"() {
    byte[] writtenBytes = []

    given:
    Element element1 = Mock()
    Element element2 = Mock()
    modelRepository.allElements() >> [element1, element2]

    when:
    writer.writeTo(outputStream)

    then:
    1 * outputStream.write(_ as byte[]) >> { writtenBytes = it[0] }

    when:
    def writtenJson = new JsonSlurper().parseText(new String(writtenBytes))

    then:
    writtenJson != null
    writtenJson.elements != null
    writtenJson.elements.size() == 2
  }

  def "writes all element properties"() {
    byte[] writtenBytes = []

    given:
    TestElement element = new TestElement(name: "elementName", number: 123)
    modelRepository.allElements() >> [element]

    when:
    writer.writeTo(outputStream)

    then:
    1 * outputStream.write(_ as byte[]) >> { writtenBytes = it[0] }

    when:
    def writtenJson = new JsonSlurper().parseText(new String(writtenBytes))

    then:
    writtenJson.elements[0].size() == 4
    writtenJson.elements[0]["class"] == "TestElement"
    writtenJson.elements[0].name == "elementName"
    writtenJson.elements[0].number == 123
    writtenJson.elements[0].relationships == []
  }

  def "writes relationships"() {
    byte[] writtenBytes = []

    given:
    Element element1 = new TestElement(name: "element1")
    Element element2 = new TestElement(name: "element2")
    Relationship relationship = Mock() {
      getName() >> "fromTo"
      getFrom() >> element2
      getTo() >> element1
    }
    element2.relationships = [relationship]
    modelRepository.allElements() >> [element1, element2]

    when:
    writer.writeTo(outputStream)

    then:
    1 * outputStream.write(_ as byte[]) >> { writtenBytes = it[0] }

    when:
    def writtenJson = new JsonSlurper().parseText(new String(writtenBytes))

    then:
    writtenJson.elements[1].relationships.size() == 1
    writtenJson.elements[1].relationships[0].name == "fromTo"
    writtenJson.elements[1].relationships[0].from["class"] == "TestElement"
    writtenJson.elements[1].relationships[0].from.name == "element2"
    writtenJson.elements[1].relationships[0].to["class"] == "TestElement"
    writtenJson.elements[1].relationships[0].to.name == "element1"
  }

}
