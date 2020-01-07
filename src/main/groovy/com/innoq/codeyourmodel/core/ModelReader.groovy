package com.innoq.codeyourmodel.core

import com.innoq.codeyourmodel.core.dsl.ModelSpec
import com.innoq.codeyourmodel.core.meta2.Element
import groovy.util.logging.Log

/**
 * The <code>ModelReader</code> is used to read the model definition into a <code>ModelRepository</code>.
 */
@Log
class ModelReader {
  final ModelRepository modelRepository

  ModelSpec modelSpec = new ModelSpec()

  ModelReader(ModelRepository modelRepository) {
    this.modelRepository = modelRepository
  }

  def registerElementType(Class<? extends Element> elementType) {
    modelSpec.registerElementType(elementType)
    this
  }

  def read(Closure closure) {
    log.info("init model")
    modelSpec.initWith(closure)
    modelRepository.addElements(modelSpec.initElements())
  }

  def read(List<File> files) {
    read(files.collect { it.toURI() } as URI[])
  }

  def read(File... files) {
    read(files.collect { it.toURI() } as URI[])
  }

  def read(URI... uris) {
    log.info("init model from ${uris}")
    modelSpec.initFrom(uris)
    modelRepository.addElements(modelSpec.initElements())
  }
}
