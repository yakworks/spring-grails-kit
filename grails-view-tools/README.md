
____   ____.__                                           _.-````'-,_
\   \ /   /|__|  ____  __  _  __                     ,-'`           `'-.,_
 \   Y   / |  |_/ __ \ \ \/ \/ /             /)     (\       9ci's       '``-.
  \     /  |  |\  ___/  \     /             ( ( .,-') )    Yak Works         ```
   \___/   |__| \___  >  \/\_/               \ '   (_/                         !!
                    \/                        |       /)           '           !!!
  ___________           .__                   ^\    ~'            '     !    !!!!
  \__    ___/___   ____ |  |   ______           !      _/! , !   !  ! !  !   !!!
    |    | /  _ \ /  _ \|  |  /  ___/            \Y,   |!!!  !  ! !!  !! !!!!!!!
    |    |(  <_> |  <_> )  |__\___ \               `!!! !!!! !!  )!!!!!!!!!!!!!
    |____| \____/ \____/|____/____  >               !!  ! ! \( \(  !!!|/!  |/!
                                  \/               /_(      /_(/_(    /_(  /_(   
         Version: 1.2.1
         
</pre>

[RELEASE NOTES](docs/release-notes.md)

| Guide | API | 
|------|--------|
|[Released Docs](https://yakworks.github.io/view-tools/) | [Released Api](https://yakworks.github.io/view-tools/api)
|[snapshot](https://yakworks.github.io/view-tools/snapshot) | [snapshot](https://yakworks.github.io/view-tools/snapshot/api)


```
compile "org.grails.plugins:view-tools:1.2.1"

``` 
## Description

Used in the Jasper and Freemarker plugins, 

these docs are old and out of date, will not be accurate and needs updating.

API Documentation can be found here [https://yakworks.github.io/view-tools/api](https://yakworks.github.io/view-tools/api)

utility helpers to locate views in the spring mvc context
- **ViewResourceLocator** for locating views in grails-app/views, plugins, and custom external paths.
- **GrailsWebEnvironment** for binding a mock request if one doesn't exist so that services can operate without a controller.

Used to locate View resources whether in development or WAR deployed mode from static
resources, custom resource loaders and binary plugins.
Loads from a local grails-app folder for dev and from WEB-INF in
development mode.

## Install
**Grails 3**
```
compile org.grails.plugins:view-tools:1.2.0
```

**Grails 2**
```
compile :view-tools:0.3-grails2
```

## Configuration
To make plugin look into grails3 folders add next line as a first line for you `BuildConfig.groovy`
```
grails.useGrails3FolderLayout = true
```

### ViewResourceLocator 
**Example Bean**
```groovy
viewResourceLocator(yakworks.grails.mvc.ViewResourceLocator) { bean ->
    //initial searchLocations
    searchLocations = [
        "classpath:templates/", // consistent with spring-boot defaults
        "file:/someLoc/my-templates/"
    ] 

    // in dev mode there will be a groovyPageResourceLoader 
    // with base dir set to the running project
    //if(Environment.isDevelopmentEnvironmentAvailable()) <- better for Grails 3
    if(!application.warDeployed){ // <- grails2
        resourceLoader = ref('groovyPageResourceLoader') //adds to list, does not replace
    }

}
```

- **Resource locate(String uri)** : is the primary method and is used to find a view resource for a path. For example /foo/bar.xyz will search for /WEB-INF/grails-app/views/foo/bar.xyz in production and grails-app/views/foo/bar.xyz at development time. It also uses the the controller if called from a plugin to figure out where its located and finally does a brute force locate. Most of the logic is based on and uses what Grail's DefaultGroovyPageLocator does.
- **Resource getResource(String uri)** : also implements Springs [ResourceLoader](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/ResourceLoader.html) interface. This method works like a normal ResoruceLoader and **uri** can start with the standard _file:, classpath:, etc_


### Example App

see https://github.com/yakworks/view-tools/tree/master/test-projects/app
It contains a number of examples as well as a simple spring based viewResolver that uses ViewResourceLocator to find the template files it needs.



