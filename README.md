code-your-model
===

*code-your-model* allows to document a complex, modularized model in an easy, concise and project-/domain-specific way and load it into a graph database to browse/query it.

Its core features are 

* allows to define a project-specific meta-model
* provides a DSL based on that meta-model to describe (code) the concrete model
* loads one or more model files into a [neo4j graph database][neo4j] to browse/query it

*code-your-model* is based on the [groovy programming language][groovy]. Its [published as an open source project at 
github][github] under the [Apache Software License, Version 2.0][apache-license-v2].  

Its currently released as a **beta version** which means that it is still work-in-progress and might/will contain some 
[bugs/issues][github-issues].

Copyright (C) 2019, 2020 innoQ Deutschland GmbH

---

## Introduction

There is a [blog post](https://www.innoq.com/en/blog/code-your-model/) that introduces the idea behind *code-your-model* and describes how it can be used. The sample project that is build up from scratch in this blog post can be found [here](https://github.com/innoq/code-your-model-example).

## Differentiation

*code-your-model* was designed and implemented to analyse a complex, project-specific, modularized model that *already exists*. Its main focus lies on the ability to define and use an individual meta-model and to analyze (browse/query) the model.

If you have to come up with a new model, you will probably find a more appropriate tool to assist you. Especially one with a stronger focus on graphical representations to share and discuss your ideas.

In case you want to build a model explicitly and only based on a fix and well-known meta-model like [DDD](https://www.oreilly.com/library/view/domain-driven-design-distilled/9780134434964/) or the [C4 model](https://c4model.com/), you should probably think about using tools like [ContextMapper](https://contextmapper.org/) or [Structurizr](https://structurizr.com/) which focus on those models and will be more powerful.

*code-your-model* does not want to replace any of those or any other modelling tools.

## Usage

Create a new meta-model [groovy] project and add *`code-your-model`* as a compile time dependency. 
To be able to load the model into a [neo4j] database, you have to add a dependency to the neo4j-java-driver as well.

```xml
<dependency>
    <groupId>com.innoq</groupId>
    <artifactId>code-your-model</artifactId>
    <version>0.1</version>
</dependency>
<dependency>
    <groupId>org.neo4j.driver</groupId>
    <artifactId>neo4j-java-driver</artifactId>
    <version>1.7.5</version>
</dependency>
```

### define the meta-model

A project-specific meta-model can be defined by creating a couple of classes that extend 
`com.innoq.codeyourmodel.core.meta2.Element`.

```groovy
import com.innoq.codeyourmodel.core.meta2.Element

class Subdomain extends Element {

  Type type

  enum Type {
    Core, Supporting, Generic
  }

}
```

Each element automatically contains a property `name` (will be used later when defining the concrete model). 
Other, project-specific properties (here the `type`) can be defined as needed.

Allowed relationships between elements can be defined in a static `relationshipDefinitions` closure. 

```groovy
class Subdomain extends Element {

  ...

  static relationshipDefinitions = {
    contains(BoundedContext)
  }

}
```

The name of the allowed relationships (here `contains`) can be individually selected. Even multiple retationships (with 
the same name but different target types) are possible.

```groovy
class BoundedContext extends Element {

  static relationshipDefinitions = {
    relatesTo(BoundedContext)
    relatesTo(ExternalSystem)
  }

}
```

Relationship definitions can also define some valid attributes of the relationship (name and type).

```groovy
class BoundedContext extends Element {

  static relationshipDefinitions = {
    relatesTo(BoundedContext) { attributes(type: RelationshipType, sourceRole: RelationshipRole, targetRole: RelationshipRole) }
  }

}
```

It's also the possible to define *reverse* relationship names.

```groovy
class Subdomain extends Element {

  ...

  static relationshipDefinitions = {
    contains(BoundedContext) { reverse("belongsTo") }
  }

}
```

If a *reverse* relationship name is defined, a reverse relationship will automatically be created and added to the 
related element. So in the example above, whenever a Subdomain is defined in the model, that *contains* a BoundedContext, 
the BoundedContext will *belong to* that Subdomain as well.
 

See the `metamodel` packages of the samples under [`com.innoq.codeyourmodel.samples.bigpugloans`][sample-bigpugloans] and 
[`com.innoq.codeyourmodel.samples.eventdriven`][sample-eventdriven] for more examples.


### describe the concrete model

Based on the defined meta-model, the concrete model can be described.

```groovy
Domain("Retail Mortage Loans") {
  contains Subdomain("Loan Applications")
  ...
```

In this example, `Domain("Retail Mortage Loans")` creates a new element of type `Domain` with name `Retail Mortage Loans`. 
The further definition of the element is done within a succeeding closure. Here, a relationship with name `contains` is 
created. The `Subdomain("Loan Applications")` statement creates an element of type `Subdomain` as a target of the 
relationship.

Element definition closures can even be hierarchical:

```groovy
Domain("Retail Mortage Loans") {
  contains Subdomain("Loan Applications") {
    type = Core
    contains BoundedContext("Application Registration and Verification")
  }
  ...
```

See the `model.groovy` files of the samples under [`com.innoq.codeyourmodel.samples.bigpugloans`][sample-bigpugloans] and 
[`com.innoq.codeyourmodel.samples.eventdriven`][sample-eventdriven] for more examples.


### load and query the model definition(s)

The `ModelReader` is used to read the model definition into a `ModelRepository`. 
To be able to do this, the meta-model elements (classes) used in the model definition have to be registered with the 
`ModelReader`.

Currently, there are two `ModelRepository` implementations provided by the project.

* the `com.innoq.codeyourmodel.core.InMemoryModelRepository` loads and keeps the model in memory
* the `com.innoq.codeyourmodel.neo4j.Neo4JModelRepository` loads the model into a [neo4j graph database][neo4j]

The following examples use the `InMemoryModelRepository` to demonstrate the usage. The usage of [neo4j] and the 
`Neo4JModelRepository` is described in the next section.

```groovy
import com.innoq.codeyourmodel.core.*

ModelRepository modelRepository = new InMemoryModelRepository()
new ModelReader(modelRepository)
  .registerElementType(Domain)
  .registerElementType(Subdomain)
  ...
```

The model definition can be passed as an inline closure.

```groovy
new ModelReader(modelRepository)
  ...
  .read {
    Domain("Retail Mortage Loans") {
      contains Subdomain("Loan Applications")
    }
  }
```

Usually, the model definition will be stored in one or multiple a groovy script(s).

File `model.groovy`:
```groovy
Domain("Retail Mortage Loans") {
  contains Subdomain("Loan Applications")
  ...
```

```groovy
new ModelReader(modelRepository)
  ...
  .read(new File("model.groovy"))
```


The `ModelRepository` can provide *all elements* defined in the concrete model.

```
modelRepository.allElements().each { println "* ${it.class.simpleName} '${it.name}'" }

* Domain 'Retail Mortage Loans'
* Subdomain 'Loan Applications'
* BoundedContext 'Application Registration and Verification'
* Subdomain 'Scoring'
...
```

It can also provide all elements of a given type.

```
modelRepository.findAll(Subdomain).each { println "* ${it.name} (${it.type})" }

* Loan Applications (Core)
* Scoring (Core)
* Contracting (Generic)
```

One concrete element can be requested (by its name), e.g. to list its relationships.

```
modelRepository.find(Domain, "Retail Mortage Loans").relationships.each {
  println "* ${it.name} ${it.to.class.simpleName} '${it.to.name}'"
}

* contains Subdomain 'Loan Applications'
* contains Subdomain 'Scoring'
* contains Subdomain 'Contracting'
...
```

For debugging the `JsonModelWriter` converts all elements and relationships within the `ModelRepository` into a JSON 
format and writes it to an OutputStream:

```
new JsonModelWriter(modelRepository).writeTo(System.out)

{
    "elements": [
        {
            "name": "Retail Mortage Loans",
            "class": "Domain",
            "relationships": [
                {
                    "name": "contains",
                    "from": {
                        "class": "Domain",
                        "name": "Retail Mortage Loans"
                    },
                    "to": {
                        "class": "Subdomain",
                        "name": "Loan Applications"
                    }
                },
                {
                    "name": "contains",
                    "from": {
                        "class": "Domain",
                        "name": "Retail Mortage Loans"
                    },
                    "to": {
                        "class": "Subdomain",
                        "name": "Scoring"
                    }
                },
                ...
```

There's also the `SimpleModelWriter` that produces a less verbose output:

```
new SimpleModelWriter(modelRepository).writeTo(System.out)

- Domain 'Retail Mortage Loans'
   `-- contains Subdomain 'Loan Applications'
   `-- contains Subdomain 'Scoring'
   `-- contains Subdomain 'Contracting'
   `-- contains Subdomain 'Credit Decision'
   `-- contains Subdomain 'Real Estate Assessment'
   `-- contains Subdomain 'Postal Communication'
- Subdomain 'Loan Applications' [type=Core]
   `-- contains BoundedContext 'Application Registration and Verification'
- BoundedContext 'Application Registration and Verification'
- Subdomain 'Scoring' [type=Core]
   `-- contains BoundedContext 'Scoring'
- BoundedContext 'Scoring'
   `-- relatesTo BoundedContext 'Application Registration and Verification'
...
```

See the `build-model.groovy` scripts of the samples under [`com.innoq.codeyourmodel.samples.bigpugloans`][sample-bigpugloans] and 
[`com.innoq.codeyourmodel.samples.eventdriven`][sample-eventdriven] for more examples.


### load and query the model using a neo4j database

The `Neo4JModelRepository` can be used similar to the previously described `InMemoryModelRepository` to load the 
elements and relationships of the model into a [neo4j graph database][neo4j]. You have to provide the URL, username and
password to connect to your database. 

```groovy
import com.innoq.codeyourmodel.core.*
import com.innoq.codeyourmodel.neo4j.*

ModelRepository modelRepository = new Neo4JModelRepository("bolt://localhost:7687", "neo4j", "test")
new ModelReader(modelRepository)
  .registerElementType(Domain)
  .registerElementType(Subdomain)
  ...
  .read(new File("model.groovy"))
```

If you just want to try out *code-your-model* together with a neo4j database, you can run it as a docker container on
your local machine. You can use the `scripts/run-local-neo4j.sh` script to start it.

After loading the model you can open the [neo4j browser][neo4j-browser-localhost] and execute the following cipher query
to select and display all elements
    
    MATCH (n) RETURN n

To select (and return) all elements (nodes) of type `Subdomain` execute

    MATCH (n:Subdomain) RETURN n 

---

[apache-license-v2]: https://www.apache.org/licenses/LICENSE-2.0
[github]: https://github.com/innoq/code-your-model
[github-issues]: https://github.com/innoq/code-your-model/issues
[groovy]: http://groovy-lang.org/
[neo4j]: https://neo4j.com/
[neo4j-browser-localhost]: http://localhost:7474
[sample-bigpugloans]: https://github.com/innoq/code-your-model/blob/master/src/test/groovy/com/innoq/codeyourmodel/samples/bigpugloans
[sample-eventdriven]: https://github.com/innoq/code-your-model/blob/master/src/test/groovy/com/innoq/codeyourmodel/samples/eventdriven

