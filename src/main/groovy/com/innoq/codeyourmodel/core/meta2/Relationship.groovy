package com.innoq.codeyourmodel.core.meta2

import groovy.util.logging.Log

@Log
class Relationship<FROM extends Element, TO extends Element> {
  String name
  FROM from
  TO to
  Map attributes = [:]

  def get(String name) {
    log.fine("get('${name}')")
    if (!attributes?.get(name))
      return null
    log.info("property with name '${name}' found -> return value")
    return attributes[name]
  }

  @Override
  String toString() {
    "Relationship { name: '${name}', from: ${from.class.simpleName} '${from.name}', to: ${to.class.simpleName} '${to.name}' }"
  }

}
