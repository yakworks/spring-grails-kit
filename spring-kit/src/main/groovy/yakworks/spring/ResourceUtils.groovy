/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import groovy.transform.CompileStatic

import org.apache.groovy.util.Arrays
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternUtils

/**
 * Static helpers for Spring's resources
 */
@CompileStatic
class ResourceUtils {

    /**
     * spins through a list of string resourses and returns full array.
     * Ex: getResources(['classpath*:/foo.yml', 'classpath*:/bar.yml'])
     */
    static Resource[] getResources(List<String> apiResources){
        // ResourceLoader defaultResourceLoader = new DefaultResourceLoader()
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(AppCtx.ctx)
        Resource[] resources = [] as Resource[]
        apiResources.each {
            resources = Arrays.concat(resources, resolver.getResources(it))
        }
        resources
    }
}
