package yakworks.spring

import grails.testing.spring.AutowiredTest
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class AppCtxSpec extends Specification implements AutowiredTest, GrailsUnitTest {

    Closure doWithSpring() {{ ->
        appCtx(AppCtx)
        someBean(SomeBean)
    }}

    void "test AppCtx"() {
        expect:
        AppCtx.ctx
        AppCtx.get("someBean").class == SomeBean
        AppCtx.get("someBean",SomeBean)
        AppCtx.get(SomeBean)
    }

}
