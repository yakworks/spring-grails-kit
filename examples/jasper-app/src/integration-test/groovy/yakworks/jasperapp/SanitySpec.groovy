package yakworks.jasperapp

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.jasper.dynamic.DynamicReportsService
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class SanitySpec extends Specification {

    @Autowired DynamicReportsService dynamicReportsService

    def "sanity check seed data worked and we have services working"() {
        when:
        def orgCount = Org.count()

        then:
        orgCount == 100
        dynamicReportsService
    }

}
