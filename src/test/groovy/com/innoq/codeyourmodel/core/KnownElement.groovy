package com.innoq.codeyourmodel.core

import com.innoq.codeyourmodel.core.meta2.Element


class KnownElement extends Element {
  int number
  String description
  Type type

  static relationshipDefinitions = {
    isChildOf(KnownElement)
  }

  enum Type {
    type1, type2
  }
}
