/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.externalconfig

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.config.PropertySourcesConfig
import org.springframework.beans.factory.config.YamlMapFactoryBean
import org.springframework.boot.ConfigurableBootstrapContext
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.boot.env.PropertiesPropertySourceLoader
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.util.AntPathMatcher

import grails.util.Environment

@SuppressWarnings(['ReturnsNullInsteadOfEmptyCollection', 'ReturnNullFromCatchBlock',
    'ReturnsNullInsteadOfEmptyCollection', 'EmptyMethod'])
@CompileStatic
@Slf4j(category = 'grails.plugin.externalconfig.ExternalConfig')
class ExternalConfigRunListener implements SpringApplicationRunListener {

    ResourceLoader defaultResourceLoader = new DefaultResourceLoader()
    // private YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader()
    private PropertiesPropertySourceLoader propertiesPropertySourceLoader = new PropertiesPropertySourceLoader()

    private String userHome = System.properties.getProperty('user.home')
    private String separator = System.properties.getProperty('file.separator')

    final SpringApplication application

    ExternalConfigRunListener(SpringApplication application, String[] args) {
        this.application = application
    }

    @Override
    void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        List<Object> locations = getLocations(environment)

        String encoding = environment.getProperty('grails.config.encoding', String, 'UTF-8')

        for (location in locations) {
            List<PropertySource<?>> propertySources = []
            Map currentProperties = getCurrentConfig(environment)
            if (location instanceof Class) {
                propertySources = loadClassConfig(location as Class, currentProperties)
            } else {
                Resource resource
                if( location instanceof Resource ){
                    resource = location
                } else {
                    // Replace placeholders from known locations
                    String finalLocation = environment.resolvePlaceholders(location as String)
                    resource = defaultResourceLoader.getResource(finalLocation)
                }

                if (resource.exists()) {
                    String fname = resource.filename
                    if (fname.endsWith('.groovy')) {
                        propertySources = loadGroovyConfig(resource, encoding, currentProperties)
                    } else if (fname.endsWith('.yml') || fname.endsWith('.yaml')) {
                        environment.activeProfiles
                        propertySources = loadYamlConfig(resource)
                    } else {
                        // Attempt to load the config as plain old properties file (POPF)
                        propertySources = loadPropertiesConfig(resource)
                    }
                } else {
                    log.debug("Config file {} not found", [resource] as Object[])
                }
            }
            propertySources.each {
                environment.propertySources.addFirst(it)
            }
        }

        // setMicronautConfigLocations(locations)
    }

    // Resolve final locations, taking into account user home prefix and file wildcards
    private List<Object> getLocations(ConfigurableEnvironment environment) {
        List<Object> locations = environment.getProperty('grails.config.locations', List, []) as List<Object>
        // See if grails.config.locations is defined in an environments block like 'development' or 'test'
        String environmentString = "environments.${Environment.current.name}.grails.config.locations"
        locations = environment.getProperty(environmentString, List, locations)
        locations.collectMany { Object location ->
            if (location instanceof CharSequence) {
                location = replaceUserHomePrefix(location as String)
                List<Object> expandedLocations = handleWildcardLocation(location as String)
                if (expandedLocations) {
                    return expandedLocations
                }
            }
            return [location]
        }
    }

    // Expands wildcards if any
    private List<Object> handleWildcardLocation(String location) {
        if (location.startsWith('file:')) {
            String locationFileName = location.tokenize(separator)[-1]
            if (locationFileName.contains('*')) {
                String parentLocation = location - locationFileName
                try {
                    Resource resource = defaultResourceLoader.getResource(parentLocation)
                    if (resource.file.exists() && resource.file.isDirectory()) {
                        Path dir = resource.file.toPath()
                        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, locationFileName)

                        return stream.collect { Path p ->
                            "file:${p.toAbsolutePath()}"
                        } as List<Object>
                    }
                } catch (FileNotFoundException ignore) {
                    return null
                }

            }
        } else if (location.startsWith('classpath') && (location.tokenize(separator)[-1]).contains('*') ) {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(defaultResourceLoader)
            (resolver.pathMatcher as AntPathMatcher).setCaseSensitive(false)
            List<Resource> resourceList = resolver.getResources(location).toList()
            return resourceList as List<Object>
        }
        return null
    }

    // Resource[] findResources(Resource baseResource , List<String> locationPatterns) {
    //     Resource[] resources
    //     ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(defaultResourceLoader)
    //     (resolver.pathMatcher as AntPathMatcher).setCaseSensitive(false)
    //     try {
    //         // first load all ymls
    //         List<Resource> resourceList = []
    //         locationPatterns.each{
    //             resourceList.addAll(resolver.getResources(it))
    //         }
    //
    //         resources = resourceList as Resource[]
    //         //filter them down
    //         resources = resources.length > 0 ? filterResources(resources, suffix, locale) : resources
    //
    //     } catch (IOException e) {
    //         log.error("IOException loading i18n yaml messages", e)
    //     }
    //
    //     return resources
    //
    // }

    // Replace ~ with value from system property 'user.home' if set
    private String replaceUserHomePrefix(String location) {
        if (userHome && location.startsWith('~/')) {
            location = "file:${userHome}${location[1..-1]}"
        }
        return location
    }

    // Load groovy config from classpath
    private static List<PropertySource<?>> loadClassConfig(Class location, Map currentConfig) {
        log.info("Loading config class {}", location.name)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        WriteFilteringMap filterMap = new WriteFilteringMap(currentConfig)
        slurper.binding = filterMap
        Map properties = slurper.parse((Class) location)?.flatten()
        Map writtenValues = filterMap.getWrittenValues()
        properties.putAll(writtenValues)
        return [new MapPropertySource(location.toString(), properties)] as List<PropertySource<?>>
    }

    // Load groovy config from resource
    private static List<PropertySource<?>> loadGroovyConfig(Resource resource, String encoding, Map currentConfig) {
        log.info("Loading groovy config file {}", resource.URI)
        String configText = resource.inputStream.getText(encoding)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        WriteFilteringMap filterMap = new WriteFilteringMap(currentConfig)
        slurper.binding = filterMap
        ConfigObject configObject = slurper.parse(configText)
        Map<String, Object> properties = configText ? configObject?.flatten() as Map<String, Object> : [:]
        Map writtenValues = filterMap.getWrittenValues()
        properties.putAll(writtenValues)
        return [new MapPropertySource(resource.filename, properties)] as List<PropertySource<?>>
    }

    private List<PropertySource<?>> loadYamlConfig(Resource resource) {
        // log.info("Loading YAML config file {}", resource.URI)
        // return yamlPropertySourceLoader.load(resource.filename, resource)
        //Use the one in grails so it doesnt wreak havoc on the lists and maps
        org.grails.config.yaml.YamlPropertySourceLoader propertySourceLoader = new org.grails.config.yaml.YamlPropertySourceLoader();
        return propertySourceLoader.load(resource.filename, resource)
    }

    // private List<PropertySource<?>> loadYamlMapConfig(Resource resource) {
    //     log.info("MAP based, Loading YAML config file {}", resource.URI)
    //     YamlMapFactoryBean yamlFactory = new YamlMapFactoryBean();
    //     yamlFactory.setResources(resource)
    //     return [new MapPropertySource(resource.filename, yamlFactory.getObject())] as List<PropertySource<?>>
    // }

    private List<PropertySource<?>> loadPropertiesConfig(Resource resource) {
        log.info("Loading properties config file {}", resource.URI)
        return propertiesPropertySourceLoader.load(resource.filename, resource)
    }

    // private void setMicronautConfigLocations(List<Object> newSources) {
    //     List<String> sources = System.getProperty('micronaut.config.files', System.getenv('MICRONAUT_CONFIG_FILES') ?: '').tokenize(',')
    //     sources.addAll(newSources.collect { it.toString() })
    //     sources = filterMissingMicronautLocations(sources)
    //     log.debug("---> Setting 'micronaut.config.files' to ${sources.join(',')}")
    //     System.setProperty('micronaut.config.files', sources.join(',') )
    // }

    private List<String> filterMissingMicronautLocations(List<String> sources) {
        sources.findAll { String location ->
            try {
                def resource = defaultResourceLoader.getResource(location)
                if (!resource.exists()) {
                    log.debug("Configuration file ${location} not found, ignoring.")
                    return false
                }
            } catch (FileNotFoundException ignore) {
                log.debug("Configuration file ${location} not found, ignoring.")
                return false
            }
            true
        }
    }

    static Map getCurrentConfig(ConfigurableEnvironment environment) {
        return new PropertySourcesConfig(environment.propertySources)
    }

}
