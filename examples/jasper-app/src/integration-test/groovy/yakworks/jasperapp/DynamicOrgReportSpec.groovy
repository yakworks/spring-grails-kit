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
        if (Files.notExists(folder)) Files.createDirectories(folder)
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

    boolean saveToFiles(JasperReportBuilder dr, String fname) {
        def jrxmlos = new FileOutputStream( folder.resolve("${fname}.jrxml").toFile() )
        dr.toJrXml(jrxmlos)
        //dr.toJrXml(new FileOutputStream( new File(folder,"${fname}.jrxml")))
        long start = System.currentTimeMillis();
        def os = new FileOutputStream( folder.resolve("${fname}.pdf").toFile() )
        dr.toPdf(os)
        System.err.println("PDF time : " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        dr.ignorePagination()//.ignorePageWidth() //.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
            .rebuild()
        //.toHtml(new FileOutputStream( new File(folder,"basic.html")))
        def htmlos = new FileOutputStream( folder.resolve("${fname}.html").toFile() )
        dr.toHtml(
            export.htmlExporter(htmlos)
                .setHtmlHeader(getHTMLHeader())
                .setHtmlFooter(HTMLFooter) //.setFramesAsNestedTables(true).setZoomRatio(200)
        )
        System.err.println("HTML time : " + (System.currentTimeMillis() - start));

        //if running on a mac will open it.
        if(System.getProperty("os.name").equals("Mac OS X")) {
            def fpointer = folder.resolve("${fname}.html").toString()
            "open ${fpointer}".execute()
        }
        return true
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
        saveToFiles(dr, 'Orgs')

        //dr.show()
        //sleep(5000)
    }

    String HTMLHeader = ('''
<html>
    <head>
        <title></title>
        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>
        <style type=\"text/css\">
            a {text-decoration: none}
        </style>
    </head>
    <body text=\"#000000\" link=\"#000000\" alink=\"#000000\" vlink=\"#000000\">
        <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">
            <tr>
                <td width="10%">&nbsp;</td>
                <td align=\"center\">
 ''').toString()

    String HTMLFooter = '''
                </td>
                <td width="10%">&nbsp;</td>
            </tr>
        </table>
    </body>
</html>
'''
}
