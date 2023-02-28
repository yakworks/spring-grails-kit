package yakworks.jasper.adhoc

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import gorm.tools.metamap.MetaGormEntityBuilder
import grails.gorm.transactions.Transactional
import net.sf.dynamicreports.adhoc.configuration.AdhocCalculation
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.jasper.dynamic.ReportSaveUtils
import yakworks.meta.MetaEntity

import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.unit.GormHibernateTest

class AdhocReportsServiceSpec extends Specification implements GormHibernateTest   { //implements GrailsWebUnitTest {

    static Path folder = Paths.get("build/jasper-tests/DynamicReportsServiceSpec/")

    static entityClasses = [KitchenSink, Thing, SinkItem]

    void setupSpec() {
        ReportSaveUtils.OPEN_REPORTS_ON_SAVE = false //SET TO TRUE TO OPEN THE REPORTS IN BROWSER FOR TESTING
        if (Files.notExists(folder)) Files.createDirectories(folder)

        KitchenSink.repo.createKitchenSinks(50)
    }

    void cleanupSpec() {
        KitchenSink.deleteAll()
    }
    // @Shared
    private AdhocReportsService adhocReportsService //= new DynamicReportsService()

    void setup() {
        adhocReportsService = new AdhocReportsService()
        adhocReportsService.resourceLoader = grailsApplication.mainContext
        adhocReportsService.environment = grailsApplication.mainContext.environment
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
            columns: ['num', 'name', 'thing.name', 'inactive', 'amount'],
            subtotals:[
                [name: "amount", label: "Totals", calculation: AdhocCalculation.SUM],
            ]
        ]

        def mentity = MetaGormEntityBuilder.build(KitchenSink, ['num', 'name', 'thing.name', 'amount', 'inactive'])
        JasperReportBuilder jrb = buildReport(mentity, cfg)

        then:
        ReportSaveUtils.saveToFiles(jrb, folder, 'adhoc_simple')
    }

    // @IgnoreRest
    void "adhoc_group single group"() {
        when:
        Map cfg = [
            //columns: If cols not specifed will use the keys from flatten in metaEntity
            groups:[ "kind" ],
            subtotals: [
                //the group sum
                [name: "amount", label: "Kind Totals" , groupName:"kind", calculation: "SUM", position: "GROUP_FOOTER" ],
                //if groupName not speced then main summary (Grand Total)
                [name: "amount", label: "Grand Total" , calculation: "SUM", position: "SUMMARY"],
            ]
        ]
        def mentity = MetaGormEntityBuilder.build(KitchenSink, ["kind", "num", "name", "inactive", "amount"])
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
                [name: "amount", label: "Kind TOTALS", groupName:"kind", calculation: "SUM", position: "GROUP_FOOTER" ],
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
