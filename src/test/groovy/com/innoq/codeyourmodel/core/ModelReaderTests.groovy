package com.innoq.codeyourmodel.core

import com.innoq.codeyourmodel.core.meta2.Element
import spock.lang.Ignore
import spock.lang.Specification

class ModelReaderTests extends Specification {

  ModelRepository modelRepository = Mock()

  def "building an empty model is fine"() {
    given:
    ModelReader modelReader = new ModelReader(modelRepository)

    when:
    modelReader.read {}

    then:
    noExceptionThrown()

    and:
    1 * modelRepository.addElements([])
  }

  def "building a model from a file in classpath is possible"() {
    given:
    def file = new File(this.getClass().getResource("/files/simple-model.groovy").toURI())
    ModelReader modelReader = new ModelReader(modelRepository)
      .registerElementType(KnownElement)

    when:
    modelReader.read(file)

    then:
    1 * modelRepository.addElements(_ as List<Element>) >> {
      assert it[0].size() == 1
      assert it[0][0].class == KnownElement
      assert it[0][0].name == "myName"
    }
  }

  // to run this test on a local machine, start a wiremock instance
  // and mount the files and mappings directory in /src/test/resources as follows
  //
  // docker run -it --rm \
  //            -p 8080:8080 \
  //            -v $PROJECT_HOME/src/test/resources/mappings:/home/wiremock/mappings \
  //            -v $PROJECT_HOME/src/test/resources/files:/home/wiremock/__files \
  //            rodolpheche/wiremock
  @Ignore("needs a proper integration test environment")
  def "building a model from a remote URI is possible"() {
    given:
    def uri = URI.create("http://localhost:8080/simple-model.groovy")
    ModelReader modelReader = new ModelReader(modelRepository)
      .registerElementType(KnownElement)

    when:
    modelReader.read(uri)

    then:
    1 * modelRepository.addElements(_ as List<Element>) >> {
      assert it[0].size() == 1
      assert it[0][0].class == KnownElement
      assert it[0][0].name == "myName"
    }
  }

  def "building a model from multiple files is possible"() {
    List<Element> elements

    given: "two model files"
    def uri1 = this.getClass().getResource("/files/simple-model.groovy").toURI()
    def uri2 = this.getClass().getResource("/files/other-model.groovy").toURI()
    ModelReader modelReader = new ModelReader(modelRepository)
      .registerElementType(KnownElement)

    when:
    modelReader.read(uri1, uri2)

    then: "model contains elements from both files"
    1 * modelRepository.addElements(_ as List<Element>) >> { elements = it[0] }
    elements.size() == 2
    elements[0].class == KnownElement
    elements[0].name == "myName"
    elements[1].class == KnownElement
    elements[1].name == "otherName"

    and: "element of one file can relate to element of other file"
    elements[1].relationships.size() == 1
    elements[1].relationships[0].name == "isChildOf"
    elements[1].relationships[0].to == elements[0]
  }

  def "using an unknown element type in a model throws an exception"() {
    given:
    ModelReader modelReader = new ModelReader(modelRepository)

    when:
    modelReader.read {
      unknown("name") {}
    }

    then:
    thrown(IllegalStateException)
  }

  def "element of registered type can be initialized"() {
    List<Element> elements

    given:
    ModelReader modelReader = new ModelReader(modelRepository)
      .registerElementType(KnownElement)

    when:
    modelReader.read {
      KnownElement("myName") {}
    }

    then:
    1 * modelRepository.addElements(_) >> { elements = it[0] }
    elements.size() == 1
    elements[0].class == KnownElement
    elements[0].name == "myName"
  }

}
