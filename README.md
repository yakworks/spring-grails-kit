

## Purpose 

Some common "kits" or plugins for spring and grails

We use `make` to wrap `gradle`.
after cloning run `make check`  `make` with no target to see help. 

while gradle is the build tool behind spring/grails, make is used for docker and setting up env for testing

## grails-kit

- adds common ConfigAware trait that makes it easy to inject the config into any beand
- AppResourceLoader for common configuration and helpers to get and save file resources.
- AppCtx for when you need static access to ApplicationContext and GrailsApplication
- GrailsWebEnvironment util for static access to setting up web environment. 
  Dependencies need to be added by hand to project, no transitive deps are added

## grails-external-config

- Fork of [sbglasius/external-config](https://github.com/sbglasius/external-config) the stops the double loading and not dependent on micronaut
- handles wildcards for both external classpath and file configs


## grails-view-tools

[See the README](grail-view-tools/README.md)

## grails-jasper

- grails plugin for jasper and generating dynamic reports from gorm. gorm-tools dependency needs to be added to project.

## freemarker

- Freemarker support

## examples

constains a number of test projects and examples for using the above plugins. 
