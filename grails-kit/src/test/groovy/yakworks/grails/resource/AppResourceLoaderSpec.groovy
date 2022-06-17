package yakworks.grails.resource

import grails.testing.services.ServiceUnitTest
import grails.testing.spring.AutowiredTest
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class AppResourceLoaderSpec extends Specification implements AutowiredTest, GrailsUnitTest {

    AppResourceLoader appResourceLoader

    Closure doWithSpring() {{ ->
        appResourceLoader(AppResourceLoader)
    }}

    // def setup() {
    //     kitchenSinkCsv = new File("src/test/resources/KitchenSink.short.csv")
    //     sinkItemCsv = new File("src/test/resources/Sink.Item.short.csv")
    // }

    void rootlocation() {
        expect:
        appResourceLoader.rootLocation
    }

    void rootPath() {
        expect:
        appResourceLoader.rootPath
    }

    void "scripts"() {
        expect:
        appResourceLoader.getScripts().toList()
    }

}
