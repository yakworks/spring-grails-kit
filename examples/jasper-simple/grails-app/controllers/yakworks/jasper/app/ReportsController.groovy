package yakworks.jasper.app

import java.nio.file.Path
import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

import net.sf.jasperreports.export.Exporter
import yakworks.commons.util.BuildSupport
import yakworks.jasper.JasperUtils
import yakworks.reports.ReportFormat

class ReportsController {

    @Autowired ApplicationContext applicationContext
    @Autowired DataSource dataSource

    List<Map> dataList = [
            [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
            [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]

    //defaults to the index.gsp
    def index() {}

    def check() {
        render "passed ${params.id} format ${params.format}"
    }

    //datasource pdf
    def pdf1() {
        assert applicationContext
        assert dataSource
        String rptName = "Orgs-list"
        String format = "pdf"
        Resource rpt = applicationContext.getResource("classpath:reports/${rptName}.jrxml")
        assert rpt.exists()
        def report = JasperUtils.loadReport(rpt)
        def jprint = JasperUtils.fillReport(report, ["ReportTitle":"Orgs Test"], dataSource)

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=${rptName}.pdf")
        final OutputStream outStream = response.getOutputStream()
        JasperUtils.exportPDF(jprint, outStream)
        //return null
        //render "passed report name ${rptName} format ${format}"
    }

}
