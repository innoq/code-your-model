package com.innoq.codeyourmodel.samples.bigpugloans.metamodel

import com.innoq.codeyourmodel.core.meta2.Element


class Subdomain extends Element {

  Type type

  enum Type {
    Core, Supporting, Generic
  }

  static relationshipDefinitions = {
    contains(BoundedContext)
  }

}
