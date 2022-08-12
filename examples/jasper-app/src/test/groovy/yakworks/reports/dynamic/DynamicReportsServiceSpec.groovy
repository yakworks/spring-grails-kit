package yakworks.reports.dynamic

import yakworks.jasper.dynamic.DynamicConfig
import yakworks.jasperapp.model.Bills
import yakworks.jasperapp.model.Customer
import yakworks.jasperapp.model.Product
import yakworks.jasperapp.model.ProductGroup
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import yakworks.jasper.dynamic.DynamicReportsService
import yakworks.reports.SeedData

import static net.sf.dynamicreports.report.builder.DynamicReports.export

class DynamicReportsServiceSpec extends GormToolsHibernateSpec{

    List<Class> getDomainClasses() { [ProductGroup, Customer, Bills, Product] }

    // @Shared
    private DynamicReportsService dynamicReportsService //= new DynamicReportsService()
    /**
     * TODO
     * - links to pages
     * - subtotals on top
     * - incorporate patterns from ??
     * - better grouping for id and name concepts
     * - embed fonts into pdf
     * - how to use font awesome icons
     *
     */
    static folder = new File("build/jasper-tests/DynamicReportsServiceSpec/");

    void setup() {
        dynamicReportsService = new DynamicReportsService()
        dynamicReportsService.resourceLoader = grailsApplication.mainContext
        dynamicReportsService.configuration = grailsApplication.config
        if (!folder.exists()) folder.mkdirs();
    }

    List getData(List<String> groupFields) {
        def list = Bills.query{
            groupFields.each { fld ->
                order(fld)
            }
        }.list()
        return list
    }

    boolean saveToFiles(JasperReportBuilder dr, String fname) {
        //dr.toJrXml(new FileOutputStream( new File(folder,"${fname}.jrxml")))
        long start = System.currentTimeMillis();
        dr.toPdf(new FileOutputStream(new File(folder, "${fname}.pdf")))
        System.err.println("PDF time : " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        dr.ignorePagination()//.ignorePageWidth() //.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
                .rebuild()
        //.toHtml(new FileOutputStream( new File(folder,"basic.html")))
        def htmlRpt = new File(folder, "${fname}.html")
        dr.toHtml(
                export.htmlExporter(new FileOutputStream(htmlRpt))
                        .setHtmlHeader(getHTMLHeader())
                        .setHtmlFooter(HTMLFooter) //.setFramesAsNestedTables(true).setZoomRatio(200)
        )
        System.err.println("HTML time : " + (System.currentTimeMillis() - start));

        dr.toJrXml(new FileOutputStream(new File(folder, "${fname}.jrxml")))

        //if running on a mac will open it.
        if(System.getProperty("os.name").equals("Mac OS X")) {
            "open build/jasper-tests/DynamicReportsServiceSpec/${fname}.html".execute()
        }
        return true
    }

    void "grouped 3 levels"() {
        when:
        Map cfg = [
            title: "By Customer/group/color",
            entityName              : 'Bills',
            fields                  : ['customer.name', 'product.group.name', 'color', 'product.name', 'isPaid', 'tranDate', 'qty', 'amount'],
            // columns                 : ['tranProp': 'From Getter'],
            groups                  : ['customer.name', 'product.group.name', 'color'],
            subtotals               : [qty: "sum", amount: "sum"], //put these on all the group summaries
            subtotalsHeader         : [amount: "sum"], //put these on all the group summaries
            columnHeaderInFirstGroup: true, //for each new primary group value the column header will be reprinted, if false they occur once per page
            groupTotalLabels        : true, //puts a group total label on the subtotal footers
            //highlightDetailOddRows:true,
            showGridLines           : true,
            //            tableOfContents:true,
            //            landscape:true //short cut for pageFormat:[size:'letter', landscape:true]
            //            pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
        ]
        def rptCfg = new DynamicConfig(cfg)
        def dr = dynamicReportsService.buildDynamicReport(rptCfg)
        new SeedData().seed()
        def list = getData(rptCfg.groups)
        dr.setDataSource(list)

        then:
        assert dr

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        saveToFiles(dr, '3levels')

        //dr.show()
        //sleep(5000)
    }

    void "Customer Group"() {
        when:
        Map cfg = [
            title: "By Customer",
            entityName              : 'Bills',
            fields                  : ['customer.name', 'color', 'product.name', 'isPaid', 'tranDate', 'qty', 'amount'],
            groups                  : ['customer.name'],
            subtotals               : [qty: "sum", amount: "sum"], //put these on all the group summaries
            subtotalsHeader         : [amount: "sum"], //put these on all the group summaries
            columnHeaderInFirstGroup: true, //for each new primary group value the column header will be reprinted, if false they occur once per page
            groupTotalLabels        : true, //puts a group total label on the subtotal footers
            //highlightDetailOddRows:true,
            showGridLines           : true,
            //            tableOfContents:true,
            //            landscape:true //short cut for pageFormat:[size:'letter', landscape:true]
            //            pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
        ]
        def rptCfg = new DynamicConfig(cfg)
        def dr = dynamicReportsService.buildDynamicReport(rptCfg)
        new SeedData().seed()
        def list = getData(rptCfg.groups)
        dr.setDataSource(list)

        then:
        assert dr

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        saveToFiles(dr, 'singleGroup')

        //dr.show()
        //sleep(5000)
    }

    void "No Group"() {
        when:
        Map cfg = [
            title: "No Group",
            entityName              : 'Bills',
            fields                  : ['customer.name', 'product.name', 'isPaid', 'tranDate', 'qty', 'amount'],
            groups                  : [],
            subtotals               : [qty: "sum", amount: "sum"], //put these on all the group summaries
            subtotalsHeader         : [amount: "sum"], //put these on all the group summaries
            columnHeaderInFirstGroup: true, //for each new primary group value the column header will be reprinted, if false they occur once per page
            groupTotalLabels        : true, //puts a group total label on the subtotal footers
            //highlightDetailOddRows:true,
            showGridLines           : true,
            //            tableOfContents:true,
            //            landscape:true //short cut for pageFormat:[size:'letter', landscape:true]
            //            pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
        ]
        def rptCfg = new DynamicConfig(cfg)
        def dr = dynamicReportsService.buildDynamicReport(rptCfg)
        new SeedData().seed()
        def list = getData(rptCfg.groups)
        dr.setDataSource(list)

        then:
        assert dr

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        saveToFiles(dr, 'noGroup')

        //dr.show()
        //sleep(5000)
    }


//    void "complex"() {
//        when:
//        Map rptCfg = [
//            domain: 'Bills',
//            fields: ['customer.name', 'color', 'product.group.name', 'product.name', 'isPaid', 'tranProp', 'tranDate', 'qty', 'amount'],
//            columns: [
//                'customer.name': [
//                    title:'Cust',
//                    width:10,
//                    pattern:
//                ],
////            groups: [
////                'customer.num':[
////                    show:''
////                ], 'color', 'product.group.name'],
//            subtotals: [qty: "sum", amount: "sum"], //put these on all the group summaries
//            subtotalsHeader         : [amount: "sum"], //put these on all the group summaries
//            columnHeaderInFirstGroup: true, //for each new primary group value the column header will be reprinted, if false they occur once per page
//            groupTotalLabels        : true, //puts a group name total label on the subtotal footers
//            highlightDetailOddRows:true,
//            showGridLines:true,
//            tableOfContents         : true,
//            landscape               : true //short cut for pageFormat:[size:'letter', landscape:true]
//            //pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
//        ]
//        def dr = dynamicReportsService.buildDynamicReport(rptCfg)
//        if(!Bills.count())
//            SeedData.seed()
//
//        def list = getData(rptCfg.groups)
//        dr.setDataSource(list)
//
//        then:
//        assert dr
//
//        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
//        saveToFiles(dr,'complex')
//
//        //dr.show()
//        //sleep(5000)
//    }

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
