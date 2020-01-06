package com.innoq.codeyourmodel.samples.bigpugloans.metamodel

import com.innoq.codeyourmodel.core.meta2.Element

class Domain extends Element {

  static relationshipDefinitions = {
    contains(Subdomain)
  }

}
