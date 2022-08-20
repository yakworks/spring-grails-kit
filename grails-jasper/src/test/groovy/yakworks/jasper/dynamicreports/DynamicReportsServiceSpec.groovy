package yakworks.jasper.dynamicreports

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.gorm.transactions.Transactional
import spock.lang.IgnoreRest
import yakworks.gorm.testing.model.KitchenSeedData
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing
import yakworks.jasper.dynamic.DynamicConfig
import yakworks.jasper.dynamic.DynamicReportsService
import yakworks.jasper.dynamic.ReportSaveUtils

class DynamicReportsServiceSpec extends GormToolsHibernateSpec  { //implements GrailsWebUnitTest {
    static Path folder = Paths.get("build/jasper-tests/DynamicReportsServiceSpec/")

    List<Class> getDomainClasses() { [KitchenSink, Thing, SinkItem] }

    @Transactional
    void setupSpec() {
        KitchenSeedData.createKitchenSinks(20)
    }

    @Transactional
    void cleanupSpec() {
        KitchenSink.deleteAll()
    }
    // @Shared
    private DynamicReportsService dynamicReportsService //= new DynamicReportsService()

    void setup() {
        dynamicReportsService = new DynamicReportsService()
        dynamicReportsService.resourceLoader = grailsApplication.mainContext
        dynamicReportsService.configuration = grailsApplication.config
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

    @IgnoreRest
    void "No Group"() {
        when:
        Map cfg = [
            title: "No Group",
            entityName              : 'KitchenSink',
            fields                  : ['name', 'num', 'kind', 'amount'],
            groups                  : ['kind'],
            subtotals               : [amount: "sum"], //put these on all the group summaries
            subtotalsHeader         : [amount: "sum"], //put these on all the group summaries
            columnHeaderInFirstGroup: false, //for each new primary group value the column header will be reprinted, if false they occur once per page
            groupTotalLabels        : true, //puts a group total label on the subtotal footers
            //highlightDetailOddRows:true,
            showGridLines           : true,
            //            tableOfContents:true,
            //            landscape:true //short cut for pageFormat:[size:'letter', landscape:true]
            //            pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
        ]
        def rptCfg = new DynamicConfig(cfg)
        def dr = dynamicReportsService.buildDynamicReport(rptCfg)
        def list = getData(rptCfg.groups)
        dr.setDataSource(list)

        then:
        assert dr

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        ReportSaveUtils.saveToFiles(dr, folder, 'dynamic_group')

        //dr.show()
        //sleep(5000)
    }

}
