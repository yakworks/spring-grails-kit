package yakworks.reports.dynamic

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.IgnoreRest
import yakworks.jasper.dynamic.DynamicConfig
import yakworks.jasper.dynamic.ReportSaveUtils
import yakworks.jasperapp.model.Bills
import yakworks.jasperapp.model.Customer
import yakworks.jasperapp.model.Product
import yakworks.jasperapp.model.ProductGroup
import yakworks.testing.gorm.GormToolsHibernateSpec
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
    static Path folder = Paths.get("build/jasper-tests/DynamicReportsServiceSpec/")

    void setup() {
        ReportSaveUtils.OPEN_REPORTS_ON_SAVE = false //SET TO TRUE TO OPEN THE REPORTS IN BROWSER FOR TESTING

        dynamicReportsService = new DynamicReportsService()
        dynamicReportsService.resourceLoader = grailsApplication.mainContext
        dynamicReportsService.environment = grailsApplication.mainContext.environment

        if (!folder.exists()) folder.mkdirs()
    }

    List getData(List<String> groupFields) {
        def list = Bills.query{
            groupFields.each { fld ->
                order(fld)
            }
        }.list()
        return list
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
            groupTotalLabelsOnLast  : false, //whether to show total label and main group
            //highlightDetailOddRows:true,
            showGridLines           : true,
            //            tableOfContents:true,
            //            landscape:true //short cut for pageFormat:[size:'letter', landscape:true]
            //            pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
        ]
        def rptCfg = new DynamicConfig(cfg)
        def jrb = dynamicReportsService.buildDynamicReport(rptCfg)
        new SeedData().seed()
        def list = getData(rptCfg.groups)
        jrb.setDataSource(list)

        then:
        assert jrb

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        ReportSaveUtils.saveToFiles(jrb, folder, '3levels')

        //dr.show()
        //sleep(5000)
    }

    void "Customer Group"() {
        when:
        Map cfg = [
            title: "By Customer",
            entityName              : 'Bills',
            fields                  : ['customer.name', 'color', 'product.name', 'isPaid', 'tranDate', 'qty', 'amount'],
            groups                  : ['customer.name', 'color'],
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

        ReportSaveUtils.saveToFiles(dr, folder, 'singleGroup')

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
        new SeedData().seed()
        def list = getData(rptCfg.groups)
        dr.setDataSource(list)

        then:
        assert dr

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        ReportSaveUtils.saveToFiles(dr, folder, 'noGroup')
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

}
