package yakworks.jasperapp

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.jasper.dynamic.DynamicConfig
import yakworks.jasper.dynamic.DynamicReportsService
import yakworks.jasper.dynamic.ReportSaveUtils
import yakworks.jasperapp.model.Bills
import yakworks.rally.orgs.model.Org
import yakworks.reports.SeedData

import static net.sf.dynamicreports.report.builder.DynamicReports.export

@Integration
@Rollback
class DynamicOrgReportSpec extends Specification {

    @Autowired DynamicReportsService dynamicReportsService

    static Path folder = Paths.get("build/jasper-tests/DynamicOrgReportSpec/")

    void setupSpec() {
        ReportSaveUtils.OPEN_REPORTS_ON_SAVE = false //SET TO TRUE TO OPEN THE REPORTS IN BROWSER FOR TESTING
    }

    def "sanity check seed data worked and we have services working"() {
        when:
        def orgCount = Org.count()

        then:
        orgCount == 100
        dynamicReportsService
    }

    List getData(List<String> groupFields) {
        def list = Org.query{
            groupFields.each { fld ->
                order(fld)
            }
        }.list()
        return list
    }

    void "Org"() {
        when:
        Map cfg = [
            title: "Org By Type",
            entityName              : 'Org',
            fields                  : ['type', 'num', 'name', 'calc.totalDue'],
            columnTitles            : ['calc.totalDue': 'Total Due'],
            groups                  : ['type'],
            subtotals               : ['calc.totalDue': 'sum'], //put these on all the group summaries
            subtotalsHeader         : ['calc.totalDue': 'sum'], //put these on all the group summaries
            columnHeaderInFirstGroup: true, //for each new primary group value the column header will be reprinted, if false they occur once per page
            groupTotalLabels        : true, //puts a group total label on the subtotal footers
            // highlightDetailOddRows:true,
            showGridLines           : true,
            // tableOfContents:true,
            // landscape:true //short cut for pageFormat:[size:'letter', landscape:true]
            //            pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
        ]
        def rptCfg = new DynamicConfig(cfg)
        def dr = dynamicReportsService.buildDynamicReport(rptCfg)
        // new SeedData().seed()
        def list = getData(rptCfg.groups)
        dr.setDataSource(list)

        then:
        assert dr

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        ReportSaveUtils.saveToFiles(dr, folder, 'Orgs')

        //dr.show()
        //sleep(5000)
    }

}
