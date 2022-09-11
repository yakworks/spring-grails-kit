package yakworks.jasper

import gorm.tools.utils.GormMetaUtils
import grails.gorm.transactions.Transactional
import org.grails.datastore.mapping.model.PersistentEntity
import spock.lang.Ignore
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.Thing
import yakworks.reports.DomainMetaUtils
import yakworks.testing.gorm.GormToolsHibernateSpec

class DomainMetaUtilsSpec extends GormToolsHibernateSpec  { //implements GrailsWebUnitTest {

    List<Class> getDomainClasses() { [KitchenSink, Thing, SinkItem] }

    @Transactional
    void setupSpec() {
        KitchenSink.repo.createKitchenSinks(10)
    }

    @Transactional
    void cleanupSpec() {
        KitchenSink.deleteAll()
    }

    void "buildColumnMap works for KitchenSink"() {
        expect:
        KitchenSink.get(1)
        def pe = GormMetaUtils.getPersistentEntity(KitchenSink.name)
        pe
        DomainMetaUtils.getFieldMetadata(pe, ['name'])

    }

    @Ignore('https://github.com/yakworks/grails-jasper-reports/issues/11')
    void "buildColumnMap works with config on Bills and nested properties"() {
        given:
        PersistentEntity domainClass = grailsApplication.mappingContext.getPersistentEntity(KitchenSink.name)
        def fields = ['customer.name','product.group.name', 'color', 'product.name', 'qty', 'amount']

        Map cfg = ['product.name':'Flubber']
        String foo = ""

        expect:
        domainClass != null

        when:

        def colmap = DomainMetaUtils.getFieldMetadata(domainClass, fields,cfg)

        then:
        assert colmap.size() == 6
        assert colmap[field].title == title
        assert colmap[field].typeClassName == typeName

        where:
        field               | title           | typeName
        'customer.name'     | 'Customer'      | 'java.lang.String'
        'product.group.name'| 'Product Group' | 'java.lang.String'
        'product.name'      | 'Flubber'       | 'java.lang.String'
        'qty'               | 'Qty'           | 'java.lang.Long'
        'amount'            | 'Amount'        | 'java.math.BigDecimal'
        'color'            | 'Color'        | 'java.lang.String'


    }

    void "findPersistentEntity"() {
        expect:
        datastore.mappingContext.getPersistentEntity(KitchenSink.name)
        GormMetaUtils.findPersistentEntity("yakworks.testing.gorm.model.KitchenSink")
        GormMetaUtils.findPersistentEntity("KitchenSink")
        GormMetaUtils.findPersistentEntity("asfasfasdf") == null
    }

    void "getNaturalTitle"() {
        expect:
        DomainMetaUtils.getNaturalTitle("Thing") == 'Thing'
        DomainMetaUtils.getNaturalTitle("thing") == 'Thing'
        DomainMetaUtils.getNaturalTitle("yakworks.jasperapp.model.Bills") == 'Yakworks Jasperapp Model Bills'
        DomainMetaUtils.getNaturalTitle("customer.name") == 'Customer'
        DomainMetaUtils.getNaturalTitle("customer.org.name") == 'Customer Org'
        DomainMetaUtils.getNaturalTitle("customer.org.id") == 'Customer Org Id'
        DomainMetaUtils.getNaturalTitle("customerOrgNum") == 'Customer Org Num'
        DomainMetaUtils.getNaturalTitle("customerOrgName") == 'Customer Org Name'
        DomainMetaUtils.getNaturalTitle("xx99yy1URLlocX90") == 'Xx99yy1 URL loc X90'
    }
}
