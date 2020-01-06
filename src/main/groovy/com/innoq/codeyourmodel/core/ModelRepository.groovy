package com.innoq.codeyourmodel.core

import com.innoq.codeyourmodel.core.meta2.Element

interface ModelRepository {

  void clear()

  def <T extends Element> void addElements(List<T> elements)

  def <T extends Element> List<T> allElements()

  def <T extends Element> T find(Class<T> type, String name)

  def <T extends Element> List<T> findAll(Class<T> elementType)

  def <T extends Element> List<T> findAllRelated(Element element, String relationshipName)

}
