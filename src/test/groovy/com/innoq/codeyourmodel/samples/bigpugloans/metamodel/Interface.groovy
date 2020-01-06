package com.innoq.codeyourmodel.samples.bigpugloans.metamodel

import com.innoq.codeyourmodel.core.meta2.Element

class Interface extends Element {

  enum ProviderRole {
    Free, Mutually_Dependent, Upstream
  }

  enum ConsumerRole {
    Free, Mutually_Dependent, Downstream
  }

  enum ProviderStrategy {
    Open_Host_Service, Supplier, Published_Language, Big_Ball_Of_Mud, Partnership
  }

  enum ConsumerStrategy {
    Conformist, Anti_Corruption_Layer, Customer, Partnership
  }

}
