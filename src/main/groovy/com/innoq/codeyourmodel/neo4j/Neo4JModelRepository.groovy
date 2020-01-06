package com.innoq.codeyourmodel.neo4j

import com.innoq.codeyourmodel.core.ModelRepository
import com.innoq.codeyourmodel.core.meta2.Element
import com.innoq.codeyourmodel.core.meta2.Relationship
import org.neo4j.driver.v1.*

import static org.neo4j.driver.v1.Values.parameters

class Neo4JModelRepository implements ModelRepository, AutoCloseable {

  private Driver driver

  Neo4JModelRepository(String uri, String username, String password) {
    driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password))
  }

  void close() throws Exception {
    driver.close()
  }

  void clear() {
    driver.session().with { session ->
      try {
        session.run("MATCH (n) DETACH DELETE n")
      } finally {
        session.close()
      }
    }
  }

  def <T extends Element> void addElements(List<T> elements) {
    elements.each {
      merge(it)
      it.relationships.each {
        merge(it.to)
        merge(it)
      }
    }
  }

  def <T extends Element> List<T> allElements() {
    return queryForElements("MATCH (n) RETURN n")
  }

  def <T extends Element> T find(Class<T> type, String name) {
    def elements = queryForElements("MATCH (n:${type.simpleName} {name:'${name}'}) RETURN n")
    if (elements.size() == 0)
      return null
    if (elements.size() == 1)
      return elements[0] as T
    throw new IllegalStateException("ambigious Element")
  }

  def <T extends Element> List<T> findAll(Class<T> type) {
    return queryForElements("MATCH (n:${type.simpleName}) RETURN n")
  }

  def <T extends Element> List<T> findAllRelated(Element element, String relationshipName) {
    return queryForElements("MATCH (from:${element.class.simpleName})-[r:${relationshipName}]->(to) " +
      "WHERE from.name = '${element.name}' " +
      "RETURN to", 'to')
  }

  private <T extends Element> void merge(T element) {
    String mergeQuery = "MERGE (n:${element.class.simpleName} {" +
      (element.customProperties().collect { "$it: {$it}, " }.join("")) +
      "_fullType: {fullType}, " +
      "name: {name}" +
      "})"

    List params = [
      "fullType", element.class.canonicalName,
      "name", element.name]

    element.customProperties().each {
      params << it
      def property = element.getProperty(it)
      if (property instanceof Enum)
        params << property.name()
      else
        params << property
    }

    driver.session().with { session ->
      try {
        session.run(mergeQuery, parameters(*params))
      } finally {
        session.close()
      }
    }
  }

  private merge(Relationship relationship) {
    driver.session().with { session ->
      try {
        session.run("MATCH (from:${relationship.from.class.simpleName}), " +
          "(to:${relationship.to.class.simpleName}) " +
          "WHERE from.name = '${relationship.from.name}' " +
          "AND to.name = '${relationship.to.name}' " +
          "MERGE (from)-[r:${relationship.name}]->(to)")
      } finally {
        session.close()
      }
    }
  }

  private List queryForElements(String query, String key = 'n') {
    List elements = []
    driver.session().with { session ->
      try {
        StatementResult result = session.run(query)
        while (result.hasNext()) {
          Record record = result.next()
          elements.add toElement(record.get(key))
        }
      } finally {
        session.close()
      }
    }
    elements
  }

  private static def toElement(Value nodeValue) {
    def fullType = nodeValue.get('_fullType').asString()
    def name = nodeValue.get('name').asString()
    def clasz = Class.forName(fullType)
    Map propMap = [name: name]

    def propertyNames = clasz.metaClass.properties
      .findAll { !(it.name in Element.RESERVED_PROPERTY_NAMES) }
      .collect { it.name }

    def map = nodeValue.asMap()
    propertyNames.each {
      if (map.containsKey(it)) {
        propMap[it] = map[it]
      }
    }

    return clasz.metaClass.invokeConstructor(propMap)
  }

}
