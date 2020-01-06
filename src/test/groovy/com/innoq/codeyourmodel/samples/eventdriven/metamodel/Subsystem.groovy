package com.innoq.codeyourmodel.samples.eventdriven.metamodel

import com.innoq.codeyourmodel.core.meta2.Element

class Subsystem extends Element {

  static relationshipDefinitions = {
    publishes(Event) {
      reverse("publishedBy")
      attributes(description: String)
    }
    consumes(Event) {
      reverse("consumedBy")
      attributes(description: String)
    }
  }

}
