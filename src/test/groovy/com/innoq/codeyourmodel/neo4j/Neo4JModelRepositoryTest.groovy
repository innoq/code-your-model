package com.innoq.codeyourmodel.neo4j

import com.innoq.codeyourmodel.core.KnownElement
import com.innoq.codeyourmodel.core.meta2.Relationship
import com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Domain
import com.innoq.codeyourmodel.samples.bigpugloans.metamodel.Subdomain
import org.neo4j.driver.internal.value.NodeValue
import org.neo4j.driver.internal.value.RelationshipValue
import org.neo4j.driver.v1.*
import org.neo4j.driver.v1.types.Node
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import static com.innoq.codeyourmodel.neo4j.Neo4JMatchers.isNode
import static com.innoq.codeyourmodel.neo4j.Neo4JMatchers.isRelationship

@Testcontainers
class Neo4JModelRepositoryTest extends Specification {

  @Shared
  Neo4jContainer neo4jContainer = new Neo4jContainer().withAdminPassword("pwd")

  @Shared
  Neo4JModelRepository repository
  @Shared
  Driver driver

  def setupSpec() {
    repository = new Neo4JModelRepository(neo4jContainer.boltUrl, "neo4j", "pwd")
    driver = GraphDatabase.driver(neo4jContainer.boltUrl, AuthTokens.basic("neo4j", "pwd"))
  }

  def setup() {
    cleanDb()
  }

  def cleanupSpec() {
    repository.close()
    driver.close()
  }

  def "it stores a list of elements"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")

    when:
    repository.addElements([domain1, domain2])

    then:
    def nodes = nodesInDb()
    nodes.size() == 2
    nodes[0] isNode(Domain, "domain1")
    nodes[1] isNode(Domain, "domain2")
  }

  def "it stores element with further attributes"() {
    given:
    def element = new KnownElement(name: "element1", type: KnownElement.Type.type1, number: 123, description: "description of element")

    when:
    repository.addElements([element])

    then:
    def node = nodesInDb()[0]
    node isNode(KnownElement, "element1")
    node.get('type').asString() == "type1"
    node.get('number').asInt() == 123
    node.get('description').asString() == "description of element"
  }

  def "it stores relationships between elements"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    domain1.relationships << new Relationship(name: "relatesTo", from: domain1, to: domain2)

    when:
    repository.addElements([domain1, domain2])

    then:
    List<Map> records = relationshipsInDb()
    records.size() == 1
    records[0]['from'] isNode(Domain, "domain1")
    records[0]['relationship'] isRelationship("relatesTo")
    records[0]['to'] isNode(Domain, "domain2")
  }

  def "it retrieves all elements"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    def subdomain = new Subdomain(name: "subdomain")
    repository.addElements([domain1, subdomain, domain2])

    when:
    def allElements = repository.allElements()

    then:
    allElements.size() == 3
    allElements.contains(domain1)
    allElements.contains(domain2)
    allElements.contains(subdomain)
  }

  def "it retrieves all elements of a given type"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    def subdomain = new Subdomain(name: "subdomain")
    repository.addElements([domain1, subdomain, domain2])

    when:
    def allDomains = repository.findAll(Domain)

    then:
    allDomains.size() == 2
    allDomains.contains(domain1)
    allDomains.contains(domain2)
  }

  def "it retrieves element by type and name"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    repository.addElements([domain1, domain2])

    when:
    def domain = repository.find(Domain, "domain2")

    then:
    domain == domain2
  }

  def "it (stores and) retrieves elements with further attributes"() {
    given:
    def element = new KnownElement(name: "element1", type: KnownElement.Type.type1, number: 123, description: "description of element")
    repository.addElements([element])

    when:
    def retrievedElement = repository.find(KnownElement, "element1")

    then:
    retrievedElement.name == "element1"
    retrievedElement.type == KnownElement.Type.type1
    retrievedElement.number == 123
    retrievedElement.description == "description of element"
  }

  def "it retrieves all related elements for a given relationship name"() {
    given:
    def domain1 = new Domain(name: "domain1")
    def domain2 = new Domain(name: "domain2")
    def domain3 = new Domain(name: "domain3")
    def domain4 = new Domain(name: "domain4")
    domain1.relationships << new Relationship(name: "relatesTo", from: domain1, to: domain2)
    domain1.relationships << new Relationship(name: "relatesTo", from: domain1, to: domain3)
    domain1.relationships << new Relationship(name: "dependsOn", from: domain1, to: domain4)
    repository.addElements([domain1, domain2, domain3, domain4])

    when:
    def allRelatesTo = repository.findAllRelated(domain1, "relatesTo")

    then:
    allRelatesTo.size() == 2
    allRelatesTo.contains(domain2)
    allRelatesTo.contains(domain3)
  }

  private List<Node> nodesInDb() {
    List<Node> nodes = []
    driver.session().with { session ->
      try {
        StatementResult statementResult = session.run("MATCH (n) RETURN n ORDER BY n._fullType, n.name")
        while (statementResult.hasNext()) {
          Record record = statementResult.next()
          nodes << (record.get('n') as NodeValue).asEntity()
        }
      } finally {
        session.close()
      }
    }
    return nodes
  }

  private List<Map> relationshipsInDb() {
    List<Map> results = []
    driver.session().with { session ->
      try {
        StatementResult statementResult = session.run("MATCH (n)-[r]->(m) RETURN n, r, m ORDER BY n._fullType, n.name, r.name")
        while (statementResult.hasNext()) {
          def record = statementResult.next()
          Node fromNode = (record.get('n') as NodeValue).asEntity()
          org.neo4j.driver.v1.types.Relationship relationship = (record.get('r') as RelationshipValue).asRelationship()
          Node toNode = (record.get('m') as NodeValue).asEntity()
          results << [from: fromNode, relationship: relationship, to: toNode]
        }
      } finally {
        session.close()
      }
    }
    return results
  }

  private cleanDb() {
    driver.session().with { session ->
      try {
        session.run("MATCH (n) DETACH DELETE n")
      } finally {
        session.close()
      }
    }
  }

}
