/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import groovy.transform.CompileStatic

import grails.util.Holders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * A static that uses the Holder to get the spring ApplicationContext it beans and the GrailsApplication
 * when in those cases where its not practical or possible to inject them (such as Traits for a persitenceEntity)
 * Obviously its highly recommended to not use this and use injection whenever possible.
 *
 * @author Joshua Burnett (@basejump)
 * @since 5.x
 */
@Component
@CompileStatic
class AppCtx {

    private static ApplicationContext SPRING_CTX

    @Autowired
    AppCtx(ApplicationContext applicationContext) {
        SPRING_CTX = applicationContext
    }

    /**
     * Used in tests to assign the spring applicationContext
     */
    static void setApplicationContext(ApplicationContext context) {
        SPRING_CTX = context
    }

    /**
     * @return the spring ApplicationContext
     */
    static ApplicationContext getCtx() {
        // if (!SPRING_CTX) {
        //     SPRING_CTX = Holders.findApplicationContext()
        // }
        return SPRING_CTX
    }

    /**
     * call the ApplicationContext.getBean
     * @param name the bean name
     * @return the bean in the context
     */
    static Object get(String name){
        getCtx().getBean(name)
    }

    /**
     * Preferred method as typed checked, call the ApplicationContext.getBean
     *
     * @param name the name of the bean to retrieve
     * @param requiredType type the bean must match. Can be an interface or superclass
     * of the actual class, or {@code null} for any match. For example, if the value
     * is {@code Object.class}, this method will succeed whatever the class of the
     * returned instance.
     * @return the instance of the bean in the context
     */
    static <T> T get(String name, Class<T> requiredType){
        getCtx().getBean(name, requiredType)
    }

    static <T> T get(Class<T> requiredType){
        getCtx().getBean(requiredType)
    }

    /**
     * Publish events using spring appCtx.
     * we do this because grails 4.x uses MicronautApplicationEventPublisher - which internally forwards all published events to micronaut
     * which expects listeners to implement  io.micronaut.context.event.ApplicationEventListener, we dont want this.
     */
    static void publishEvent(Object event){
        ((ApplicationEventPublisher)getCtx()).publishEvent(event)
    }

    /**
     * Autowires bean properties for object relying on @autowired annotations
     */
    static Object autowire(Object obj) {
        //autowires using AUTOWIRE_NO
        getCtx().autowireCapableBeanFactory.autowireBeanProperties(obj, AutowireCapableBeanFactory.AUTOWIRE_NO, false)
        obj
    }
}
