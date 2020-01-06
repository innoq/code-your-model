package com.innoq.codeyourmodel.core.meta2


class RelationshipDefinition {
  String name
  String reverseName
  Class<? extends Element> targetType
  Map<String, Class> attributeDefinitions

  def reverse(String name) {
    reverseName = name
  }

  def attributes(Map attributeDefinitions) {
    this.attributeDefinitions = attributeDefinitions
  }
}
