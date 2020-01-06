package com.innoq.codeyourmodel.samples.eventdriven

Subsystem("Web Shop") {
  publishes Event("order created")
  publishes Event("order cancelled")
}

Subsystem("Risk Assessment") {
  consumes Event("order created")

  publishes Event("order accepted")
  publishes Event("order rejected")
}

Subsystem("Logistic") {
  consumes Event("order accepted")

  publishes Event("order shipped")
  publishes Event("return received")
}

Subsystem("Invoicing") {
  consumes Event("order shipped")

  publishes Event("invoice created"), {
    description = "whenever a new invoice was created for a shipped order"
  }
}

Subsystem("Payment") {
  consumes Event("invoice created"), {
    description = "to trigger the capture of the invoice amount"
  }
  consumes Event("return received")

  publishes Event("amount captured")
  publishes Event("payment received")
  publishes Event("amount refunded")
}

Subsystem("Customer Service") {
  consumes Event("order created")
  consumes Event("order accepted")
  consumes Event("order rejected")
  consumes Event("order shipped")
  consumes Event("invoice created")
  consumes Event("payment received")
  consumes Event("return received")

  publishes Event("order cancelled")
}
