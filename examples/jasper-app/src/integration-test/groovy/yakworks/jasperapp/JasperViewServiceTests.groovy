package yakworks.jasperapp

import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.grails.web.GrailsWebEnvironment
import yakworks.jasper.spring.JasperViewService

@Integration
class JasperViewServiceTests extends Specification {

    JasperViewService jasperViewService
    def jasperViewResourceLocator

    def setup() {
        GrailsWebEnvironment.bindRequestIfNull()
    }

    void testGetView() {
        when:
        def res = jasperViewResourceLocator.locate('/reports/testme.jrxml')

        then:
        assert res

        assert res.getURI().toString().endsWith("/reports/testme.jrxml")

        def view = jasperViewService.getView("/reports/testme.jrxml")
        assert view //.getTemplate(Locale.US)
    }
}
