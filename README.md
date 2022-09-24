

## Purpose 

Some common "kits" or plugins for spring and grails

- We use `make` to wrap `gradle`.
- after cloning run `make check`, `./gradlew check` will also work.   
- `make` with no target to see help. 

> **NOTE:** Make might need to be upgraded to a version later than 3.8. The console message should advise you on using brew.  

while gradle is the build tool behind spring/grails, make is used for docker and setting up env for testing

## grails-kit

`implementation "org.yakworks:grails-kit:5.0.7"`

- adds common ConfigAware trait that makes it easy to inject the config into any beand
- AppResourceLoader for common configuration and helpers to get and save file resources.
- AppCtx for when you need static access to ApplicationContext and GrailsApplication
- GrailsWebEnvironment util for static access to setting up web environment. 
  Dependencies need to be added by hand to project, no transitive deps are added

## grails-external-config

`implementation "org.yakworks:grails-external-config:5.0.7"`

- Fork of [sbglasius/external-config](https://github.com/sbglasius/external-config) the stops the double loading and not dependent on micronaut
- handles wildcards for both external classpath and file configs


## grails-view-tools

`implementation "org.yakworks:grails-view-tools:5.0.7"`

[See the README](grail-view-tools/README.md)

## grails-jasper

`implementation "org.yakworks:grails-jasper:5.0.7"`

- grails plugin for jasper and generating dynamic reports from gorm. gorm-tools dependency needs to be added to project.

## grails-freemarker

`implementation "org.yakworks:grails-freemarker:5.0.7"`

- Freemarker support

## examples

constains a number of test projects and examples for using the above plugins. 
