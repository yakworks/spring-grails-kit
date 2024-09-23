> NOTE:  
> The changes in https://github.com/sbglasius/external-config after 2.0 is causing configs to load twice. 
> This is a fork from 2.0 tag

> NOTE:  
> THIS HAS BEEN DEPRECATED in favor of using stock spring and its  Kept here for reference. 

External-Config
===============
[![Build Status](https://travis-ci.org/sbglasius/external-config.svg?branch=master)](https://travis-ci.org/sbglasius/external-config)

This plugin will mimic the Grails 2 way of handling external configurations defined in `grails.config.locations`.

VERSIONS!
---------
The `grails-3.x` branch is for Grails 3.x, `master` will, moving forward be for Grails 4.x

IMPORTANT!
----------
The External Config Plugin (1.1.0 and above) no longer needs to implement `ExternalConfig` on `Application.groovy`. It now uses a `SpringApplicationRunListener`and hooks into the startup automagically. So if you used the plugin in prior versions, please remove `implements ExternalConfig` from `Application.groovy`


Contributors
------------

Major contributors

* [Jesper Steen Møller](https://github.com/jespersm)
* [Tucker J. Pelletier](https://github.com/virtualdogbert)
* [Dennie de Lange](https://github.com/tkvw)
* [Sudhir Nimavat](https://github.com/snimavat) 
* [Anders Aaberg](https://github.com/andersaaberg)

Thank you!

Installation
------------

Add dependency to your `build.gradle`:

```
dependencies {
    compile 'org.grails.plugins:external-config:1.2.2'
}
```

To use a snapshot-version

add JFrog OSS Repository to the `repositories`:
```
repositories {
    maven { url "https://oss.jfrog.org/repo/" }
}
```

and specify the snapshot version as a dependency:
```
dependencies {
    compile 'org.grails.plugins:external-config:1.3.0.BUILD-SNAPSHOT'
}
```

Usage
-----

When you add this plugin to your Grails build, it will automatically look for the property `grails.config.locations`. Define this in in either `application.yml` like this:

```
grails:
    config:
        locations:
            - classpath:myconfig.groovy
            - classpath:myconfig.yml
            - classpath:myconfig.properties
            - file:///etc/app/myconfig.groovy
            - file:///etc/app/myconfig.yml
            - file:///etc/app/myconfig.properties
            - ~/.grails/myconfig.groovy
            - ~/.grails/myconfig.yml
            - ~/.grails/myconfig.properties
            - file:${catalina.base}/myconfig.groovy
            - file:${catalina.base}/myconfig.yml
            - file:${catalina.base}/myconfig.properties
```

or in `application.groovy` like this:

```
grails.config.locations = [
        "classpath:myconfig.groovy",
        "classpath:myconfig.yml",
        "classpath:myconfig.properties",
        "file:///etc/app/myconfig.groovy",
        "file:///etc/app/myconfig.yml",
        "file:///etc/app/myconfig.properties",
        "~/.grails/myconfig.groovy",
        "~/.grails/myconfig.yml",
        "~/.grails/myconfig.properties",
        'file:${catalina.base}/myconfig.groovy',
        'file:${catalina.base}/myconfig.yml',
        'file:${catalina.base}/myconfig.properties',
]
```

It is also possible to define it in an environment specific block (groovy):
```$xslt
environments {
    test {
        grails {
            config {
                locations = [...]
            }
        }
    }
}   
```

or (yml)

```
environments:
    test:
        grails:
            config:
                locations:
                - ... 
```

`~/` references the users `$HOME` directory.
Notice, that using a system property you should use single quotes because otherwise it's interpreted as a Gstring.

The plugin will skip configuration files that are not found. 

For `.groovy` and `.yml` files the `environments` blocks in the config file are interpreted the same way, as in `application.yml` or `application.groovy`.

**Wildcard support**

It is possible to use `*` as wildcards. classpath wildcards work for yml:

```
grails:
    config:
        locations:
            - file:/etc/app/myconfig*.groovy
            - ~/.grails/myconfig*.groovy
            - classpath*:core/*.yml
```
or
```
grails.config.locations = [
        "file:/etc/app/myconfig*.groovy",
        "~/.grails/myconfig*.groovy",
]
```

__Note__: the wildcards are in the order they are found in the `locations` list, but the order of the expanded `locations` for each wildcard is not guaranteed, and is dependent on the OS used.

**Getting configuration from another folder than /conf on classpath without moving it with Gradle script**

If you wish to make your Grails application pull external configuration from classpath when running locally, but you do not wish to get it packed into the assembled war file (i.e. place the external configuration file in e.g. /external-config instead of /conf), then you can include the external configuration file to the classpath by adding the following line to build.gradle:dependencies
```
providedCompile files('external-config') // providedCompile to ensure that external config is not included in the war file
```
Alternatively, you can make a gradle script to move the external configuration file to your classpath (e.g. /build/classes)


