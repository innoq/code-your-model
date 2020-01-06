package com.innoq.codeyourmodel.writer

import com.innoq.codeyourmodel.core.meta2.Element


class TestElement extends Element {
  int number

  static relationshipDefinitions = {
    isChildOf(elementType: TestElement)
  }
}
