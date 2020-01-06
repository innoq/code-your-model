package com.innoq.codeyourmodel.neo4j

import com.innoq.codeyourmodel.core.meta2.Element
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.neo4j.driver.v1.types.Node
import org.neo4j.driver.v1.types.Relationship

class Neo4JMatchers {

  static TypeSafeMatcher<Node> isNode(Class<? extends Element> type, String name) {
    new NodeMatcher(type, name)
  }

  static TypeSafeMatcher<Relationship> isRelationship(String name) {
    new RelationshipMatcher(name)
  }

  static class NodeMatcher extends TypeSafeMatcher<Node> {
    private final Class<? extends Element> type
    private final String name

    NodeMatcher(Class<? extends Element> type, String name) {
      this.name = name
      this.type = type
    }

    protected boolean matchesSafely(Node node) {
      return node.get('_fullType').asString() == type.canonicalName &&
        node.get('name').asString() == name
    }

    void describeTo(Description description) {
      description.appendText("is Node for $type.simpleName '$name'")
    }

    protected void describeMismatchSafely(Node item, Description mismatchDescription) {
      mismatchDescription.appendText("was Node for ${item.get('_fullType').asString()} '${item.get('name').asString()}'")
    }
  }

  static class RelationshipMatcher extends TypeSafeMatcher<Relationship> {
    private final String name

    RelationshipMatcher(String name) {
      this.name = name
    }

    protected boolean matchesSafely(Relationship relationship) {
      return relationship.type() == name
    }

    void describeTo(Description description) {
      description.appendText("is Relationship '$name'")
    }

    protected void describeMismatchSafely(Relationship item, Description mismatchDescription) {
      mismatchDescription.appendText("was Relationship '${item.type()}'")
    }
  }

}
