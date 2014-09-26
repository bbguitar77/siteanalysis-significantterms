Site Analysis - Significant Terms
=================================

Introduction
------------

A simple web crawler to recursively crawl content for a particular site (restricted to that site's domain) and index the content to an in-memory instance of ElasticSearch. We then can analyze the indexed content to retrieve significant terms aggregations for particular input queries. ElasticSearch defines [significant terms](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-significantterms-aggregation.html) as those that have a high disparity between their frequency to the entire document corpus (background) vs. their frequency to the user's search (foreground). 

Use case - Stripe is a company that provides APIs for managing and processing payments. Maybe we're interested in seeing what terms appear frequently next to the term 'payment'. So, we can crawl and index content for 'http://stripe.com' and then run the significant terms aggregation for the query term 'payment'. This will provide us with the results that we're interested in (hopefully)

At the moment, the results are a bit hard to understand - significant terms aggregation might need a sizable document corpus to return good results. Restricting it to a particular domain's content might not be providing enough contrast for foreground/background frequencies.


Requirements
------------
+ Java 7+
+ Maven 3+


Usage
------------

To compile/assemble, run:

    mvn clean package

To run the SiteAnalysis app, run the start.sh script. This will launch an embedded Jetty running on port 9080

    ./start.sh

To launch a web crawl for a particular domain, make the following REST call:

    curl -XPOST http://localhost:9080/site/analyze --data "url=http://stripe.com&max-depth=2"

Just update the form parameters with the desired site and depth. The web crawl / content indexing is execued asynchronously in the background, so tail the logs to see when it completes

Once done, we then can run significant terms aggregation queries on the indexed content. For example:

    curl -XGET http://localhost:9080/site/sigterms?query=payment

Logging is configured under config/logback.xml
Application properties are configured under config/siteanalysis.properties
(JAR will need to be re-assembled for config changes to take effect)


Unit Tests
----------

To execute tests, run: 

    mvn test

You can configure test logging under src/test/resources/logback-test.xml to see more output