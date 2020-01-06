package com.innoq.codeyourmodel.samples.bigpugloans


import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.BoundedContext.RelationshipStrategy.Separate_Ways
import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Interface.ConsumerRole.Downstream
import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Interface.ConsumerStrategy.Anti_Corruption_Layer
import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Interface.ConsumerStrategy.Conformist
import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Interface.ProviderRole.Upstream
import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Interface.ProviderStrategy.*
import static com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Subdomain.Type.*

Domain("Retail Mortage Loans") {

  contains Subdomain("Loan Applications") {
    type = Core

    contains BoundedContext("Application Registration and Verification") {

      provides Interface("mortgage application"), {
        role = Upstream
        strategies = [Open_Host_Service, Published_Language]
      }

    }
  }

  contains Subdomain("Scoring") {
    type = Core

    contains BoundedContext("Scoring") {

      consumes Interface("mortgage application"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }

      consumes Interface("market- vs. collateral value"), {
        role = Downstream
        strategies = [Conformist]
      }

      consumes Interface("creditworthiness"), {
        role = Downstream
        strategies = [Conformist]
      }

      provides Interface("scoring results"), {
        role = Upstream
      }

      consumes Interface("bank customers/deposits"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }
    }
  }

  contains Subdomain("Contracting") {
    type = Generic

    contains BoundedContext("Contract Offering & Closing") {
      consumes Interface("credit accounts"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }

      consumes Interface("credits"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }

      consumes Interface("bank customers/deposits"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }

      relatesTo BoundedContext("Credit Decision"), {
        strategy = Separate_Ways
      }

      relatesTo BoundedContext("Application Registration and Verification"), {
        strategy = Separate_Ways
      }

    }
  }

  contains Subdomain("Credit Decision") {
    type = Supporting

    contains BoundedContext("Credit Decision") {
      consumes Interface("mortgage application"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }

      consumes Interface("scoring results"), {
        role = Downstream
        strategies = [Conformist]
      }

      consumes Interface("market- vs. collateral value"), {
        role = Downstream
        strategies = [Conformist]
      }

      consumes Interface("bank customers/deposits"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }
    }
  }

  contains Subdomain("Real Estate Assessment") {
    type = Supporting

    contains BoundedContext("Real Estate Assessment") {
      consumes Interface("mortgage application"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }

      provides Interface("market- vs. collateral value"), {
        role = Upstream
      }

      consumes Interface("real estate data"), {
        role = Downstream
        strategies = [Anti_Corruption_Layer]
      }
    }
  }

  contains Subdomain("Postal Communication") {
    type = Generic

    contains BoundedContext("Postal Communication")
  }

}

ExternalSystem("Credit Agency") {
  provides Interface("creditworthiness"), {
    role = Upstream
    strategies = [Open_Host_Service, Published_Language]
  }
}

ExternalSystem("Real Estate Data Brokers") {
  provides Interface("real estate data"), {
    role = Upstream
    strategies = [Open_Host_Service]
  }
}

ExternalSystem("Core Banking System") {
  provides Interface("credit accounts"), {
    role = Upstream
    strategies = [Open_Host_Service, Big_Ball_Of_Mud]
  }
}

ExternalSystem("Credit Agency") {
  provides Interface("credits"), {
    role = Upstream
    strategies = [Open_Host_Service, Published_Language]
  }
}

ExternalSystem("CRM System") {
  provides Interface("bank customers/deposits"), {
    role = Upstream
    strategies = [Open_Host_Service]
  }
}
