package yakworks.jasper.adhoc

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import gorm.tools.metamap.MetaGormEntityBuilder
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.gorm.transactions.Transactional
import net.sf.dynamicreports.adhoc.configuration.AdhocCalculation
import net.sf.dynamicreports.adhoc.configuration.AdhocReport
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import spock.lang.Ignore
import spock.lang.IgnoreRest
import yakworks.gorm.testing.model.KitchenSeedData
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing
import yakworks.jasper.dynamic.DynamicConfig
import yakworks.jasper.dynamic.ReportSaveUtils
import yakworks.meta.MetaEntity

class AdhocReportsServiceSpec extends GormToolsHibernateSpec  { //implements GrailsWebUnitTest {

    static Path folder = Paths.get("build/jasper-tests/DynamicReportsServiceSpec/")

    List<Class> getDomainClasses() { [KitchenSink, Thing, SinkItem] }

    @Transactional
    void setupSpec() {
        KitchenSeedData.createKitchenSinks(50)
    }

    @Transactional
    void cleanupSpec() {
        KitchenSink.deleteAll()
    }
    // @Shared
    private AdhocReportsService adhocReportsService //= new DynamicReportsService()

    void setup() {
        adhocReportsService = new AdhocReportsService()
        adhocReportsService.resourceLoader = grailsApplication.mainContext
        adhocReportsService.configuration = grailsApplication.config
        if (Files.notExists(folder)) Files.createDirectories(folder)
    }

    List getData(List<String> groupFields) {
        def list = KitchenSink.query{
            groupFields.each { fld ->
                order(fld)
            }
        }.list()
        return list
    }

    JasperReportBuilder buildReport(MetaEntity mentity, cfg){
        JasperReportBuilder jrb = adhocReportsService.createReportBuilder(mentity, cfg)
        def list = getData(['kind', 'status'])
        jrb.setDataSource(list)
        return jrb
    }

    // @IgnoreRest
    void "adhoc_simple No Group"() {
        when:
        Map cfg = [
            columns: ['num', 'name', 'thing.name', 'amount'],
            subtotals:[
                [name: "amount", calculation: AdhocCalculation.SUM],
            ]
        ]

        def mentity = MetaGormEntityBuilder.build(KitchenSink, ['num', 'name', 'thing.name', 'amount'])
        JasperReportBuilder jrb = buildReport(mentity, cfg)

        then:
        ReportSaveUtils.saveToFiles(jrb, folder, 'adhoc_simple')
    }

    // @IgnoreRest
    void "adhoc_group single group"() {
        when:
        Map cfg = [
            //columns: If cols not specifed will use the keys from flatten in metaEntity
            groups:["kind"],
            subtotals:[
                //the group sum
                [name: "amount", groupName:"kind", calculation: "SUM", position: "GROUP_FOOTER" ],
                //if groupName not speced then main summary (Grand Total)
                [name: "amount", calculation: "SUM", position: "SUMMARY"],
            ]
        ]
        def mentity = MetaGormEntityBuilder.build(KitchenSink, ["id", "kind", "num", "name", "amount"])
        JasperReportBuilder jrb = buildReport(mentity, cfg)

        then:
        ReportSaveUtils.saveToFiles(jrb, folder, 'adhoc_group')
    }

    // @IgnoreRest
    void "adhoc_multiple_groups"() {
        when:
        Map cfg = [
            //columns: If cols not specifed will use the keys from flatten in metaEntity
            groups:["kind", "status"],
            subtotals:[
                //the group sum
                [name: "amount", groupName:"kind", calculation: "SUM", position: "GROUP_FOOTER" ],
                //if groupName not speced then main summary (Grand Total)
                [name: "amount", calculation: "SUM", position: "SUMMARY"],
            ]
        ]
        def mentity = MetaGormEntityBuilder.build(KitchenSink, ["id", "kind", "status", "num", "name", "amount"])
        JasperReportBuilder jrb = buildReport(mentity, cfg)

        then:
        ReportSaveUtils.saveToFiles(jrb, folder, 'adhoc_multiple_groups')
    }

    @Ignore
    void "Includes"() {
        when:
        Map cfg = [
            includes: ["id", "num", "name", "amount"],
            subtotals:[
                [name: "amount", calculation: "SUM"],
            ]
        ]

        JasperReportBuilder jrb = adhocReportsService.createReportBuilder("KitchenSink",cfg)
        def list = getData([])
        jrb.setDataSource(list)

        then:
        assert jrb
        ReportSaveUtils.saveToFiles(jrb, folder, 'adhoc_includes')
    }

}
