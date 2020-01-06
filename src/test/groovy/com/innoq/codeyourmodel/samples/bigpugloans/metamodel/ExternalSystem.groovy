package com.innoq.codeyourmodel.samples.bigpugloans.metamodel

import com.innoq.codeyourmodel.core.meta2.Element

class ExternalSystem extends Element {

  static relationshipDefinitions = {
    provides(Interface) { attributes(role: Interface.ProviderRole, strategies: ArrayList) }
    consumes(Interface) { attributes(role: Interface.ConsumerRole, strategies: ArrayList) }
  }

}
