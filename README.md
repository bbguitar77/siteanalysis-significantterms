Site Analysis via ElasticSearch
===============================

Introduction
------------

A simple web crawler to recursively crawl content for a particular site (restricted to that site's domain) and index the content to an in-memory instance of ElasticSearch. We then run a simple analysis on the indexed content to retrieve "significant terms" against an input query string. ElasticSearch defines significant terms as those that have a high disparity between their frequency to the entire document corpus (background) vs. their frequency to the user's search (foreground). Significant terms could be valuable in surfacing recommended terms to a user's specific search. To read more, see [http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-significantterms-aggregation.html](ElasticSearch's documentation)

Currently, everything is done in one invocation of a script. My plan is to have the app run an embedded Jetty and expose some REST endpoints for the user to tweak their search queries after the site crawling has been completed.

*TODO*: Make sense of results ;)


Requirements
------------
+ Java 7+
+ Maven 3+


Usage
------------

To compile/assemble, run:

   mvn clean package

To run the Significant Terms analysis for a particular domain, run:

   ./start.sh -u 'http://stripe.com' -d 2 -q payment
   
+ u = seed uri to crawl
+ d = max-depth to crawl recursively
+ q = query term to mimic user search

Logging is configured under config/logback.xml
Application properties are configured under config/siteanalysis.properties
(JAR will need to be re-assembled for config changes to take effect)


Unit Tests
----------

To execute tests, run: 

    mvn test

You can configure test logging under src/test/resources/logback-test.xml to see more output