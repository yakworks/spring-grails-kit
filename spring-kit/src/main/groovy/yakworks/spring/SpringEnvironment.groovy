/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import groovy.transform.CompileStatic

import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment

/**
 * Trait that implements the GrailsConfigurationAware and gives access to the config object
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@CompileStatic
trait SpringEnvironment implements EnvironmentAware {

    Environment environment

    @Override
    void setEnvironment(Environment env) {
        this.environment = env
    }

    /** alias to environment to keep backwards compat with old grails way */
    Environment getConfig() {
        return environment
    }
}
