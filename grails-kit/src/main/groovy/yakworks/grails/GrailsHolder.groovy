/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails

import groovy.transform.CompileStatic

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher

import grails.config.Config
import grails.core.GrailsApplication
import grails.util.Holders

/**
 * A static that uses the Holder to get the spring ApplicationContext it beans and the GrailsApplication
 * when in those cases where its not practical or possible to inject them (such as Traits for a persitenceEntity)
 * Obviously its highly recommended to not use this and use injection whenever possible.
 *
 * @author Joshua Burnett (@basejump)
 * @since 5.x
 */
@CompileStatic
class GrailsHolder {

    private static GrailsApplication cachedGrailsApplication

    /**
     * @return the GrailsApplication
     */
    static GrailsApplication getGrailsApplication() {
        if (!cachedGrailsApplication) {
            cachedGrailsApplication = Holders.grailsApplication
        }
        return cachedGrailsApplication
    }

    /**
     * Used in tests to assign the right GrailsApplication
     */
    static void setGrailsApplication(GrailsApplication gapp) {
        cachedGrailsApplication = gapp
    }

    /**
     * @return the merged configs from application.yml, application.groovy, etc...
     */
    static Config getConfig() {
        Holders.config
    }

}
