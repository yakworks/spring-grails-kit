/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import groovy.transform.CompileStatic

import org.springframework.beans.factory.config.YamlMapFactoryBean

/**
 * Static helpers for Yaml using Springs utility classes to load, bind etc..
 */
@CompileStatic
class YamlSpringUtils {

    /**
     * Load and merge yaml for a list of resources.
     * Example: loadYaml(['classpath*:/foo.yml', 'classpath*:/bar/*.yml'])
     * will load in order. foo.yml and then all yml files under bar, merging where appropriate
     * @return the yaml as map or empty map if no files found.
     */
    static Map<String, Object> loadYaml(List<String> apiResources){
        if(!apiResources) return [:]
        YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean()
        yamlMapFactoryBean.setResources(ResourceUtils.getResources(apiResources))
        return yamlMapFactoryBean.getObject()
    }
}
