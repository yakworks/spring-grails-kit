package yakworks.jasper

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class JasperViewServiceTests extends Specification {

    JasperViewService jasperViewService
    def jasperViewResourceLocator

    void testGetView() {
        def res = jasperViewResourceLocator.locate('/test/testme.jrxml')
        assert res

        assert res.getURI().toString().endsWith("/test/testme.jrxml")

        def view = jasperViewService.getView("/test/testme.jrxml")
        assert view //.getTemplate(Locale.US)
    }
}
