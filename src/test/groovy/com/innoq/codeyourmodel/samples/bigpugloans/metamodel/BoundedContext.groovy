package com.innoq.codeyourmodel.samples.bigpugloans.metamodel

import com.innoq.codeyourmodel.core.meta2.Element

class BoundedContext extends Element {

  static relationshipDefinitions = {
    provides(Interface) { attributes(role: Interface.ProviderRole, strategies: ArrayList) }
    consumes(Interface) { attributes(role: Interface.ConsumerRole, strategies: ArrayList) }
    shares(Kernel)
    relatesTo(BoundedContext) { attributes(strategy: RelationshipStrategy) }
  }

  enum RelationshipStrategy {
    Separate_Ways, Partnership
  }

}
