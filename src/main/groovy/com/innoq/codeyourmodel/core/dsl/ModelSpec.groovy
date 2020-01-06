package com.innoq.codeyourmodel.core.dsl

import com.innoq.codeyourmodel.core.meta2.Element
import groovy.util.logging.Log
import org.codehaus.groovy.control.CompilerConfiguration

@Log
class ModelSpec {
  Map<String, Class<? extends Element>> elementTypes = [:]
  List<ElementSpec> elementSpecs = []

  def registerElementType(Class<? extends Element> elementType) {
    elementTypes[elementType.simpleName] = elementType
  }

  def initWith(Closure initClosure) {
    log.fine("apply(<Closure>)")
    this.with(true, initClosure)
  }

  def methodMissing(String elementTypeName, Object args) {
    log.fine("methodMissing(${elementTypeName}, ${args})")

    Class<? extends Element> elementType = elementTypes[elementTypeName]
    if (!elementType)
      throw new IllegalStateException("'${elementTypeName}' is no registered element type")

    String elementName = elementNameOf(args as List)

    def elementSpec = elementSpec(elementType, elementName)
    if (!elementSpec) {
      log.fine("${elementTypeName}('${elementName}') not found yet -> init new")
      elementSpec = new ElementSpec(elementType, elementName)
      elementSpecs << elementSpec
    }

    if ((args as List).size() > 1)
      elementSpec.apply(initElementClosure(args as List))

    return elementSpec
  }

  private static String elementNameOf(List args) {
    if (args.size() < 1 || !(args[0] instanceof String))
      throw new IllegalArgumentException("first argument has to be the element name (as a String)")
    args[0]
  }

  private static Closure initElementClosure(List args) {
    if (!(args[1] instanceof Closure))
      throw new IllegalArgumentException("second argument has to be a Closure that describes/inits the element")
    args[1] as Closure
  }

  private ElementSpec elementSpec(Class<? extends Element> type, String name) {
    elementSpecs.find { it.matches(type, name) }
  }

  def initFrom(URI... uris) {
    def compilerConfiguration = new CompilerConfiguration()
    compilerConfiguration.scriptBaseClass = DelegatingScript.class.name
    def shell = new GroovyShell(this.class.classLoader, new Binding(), compilerConfiguration)

    uris.each {
      log.info("process ${it.toString()} ...")
      def scriptText = it.toURL().getText(
        connectTimeout: 5000,
        readTimeout: 10000,
        useCaches: true,
        allowUserInteraction: false,
        requestProperties: ['Connection': 'close'])
      Script script = shell.parse(scriptText)
      script.setDelegate(this)
      script.run()
    }

    this
  }

  def initElements() {
    log.fine("init Elements...")
    elementSpecs.forEach { it.initElement() }
    log.fine("init Relationships...")
    elementSpecs.forEach { it.initRelationships() }
    log.fine("collect Elements...")
    elementSpecs.collect { it.element }
  }

}
