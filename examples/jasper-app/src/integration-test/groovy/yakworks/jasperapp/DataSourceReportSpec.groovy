package yakworks.jasperapp

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource

import org.grails.io.support.FileSystemResource
import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperReport
import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.jasper.JRDataSourceJDBC
import yakworks.jasper.JasperUtils
import yakworks.jasper.dynamic.DynamicConfig
import yakworks.jasper.dynamic.DynamicReportsService
import yakworks.jasper.dynamic.ReportSaveUtils
import yakworks.rally.orgs.model.Org

/**
 * tests Reports that are fed by the standard JDBC DataSource for app and go straight to DB
 * reports can have the SQL embedded in them.
 */
@Integration
@Rollback
class DataSourceReportSpec extends Specification {
    static Path exportFolder = Paths.get("build/jasper-tests/")

    @Autowired DynamicReportsService dynamicReportsService
    @Autowired DataSource dataSource

    // void setupSpec() {
    //     ReportSaveUtils.OPEN_REPORTS_ON_SAVE = false //SET TO TRUE TO OPEN THE REPORTS IN BROWSER FOR TESTING
    //     if (Files.notExists(folder)) Files.createDirectories(folder)
    // }

    def "sanity check seed data worked and we have services working"() {
        expect:
        Org.count() == 100
        dataSource
    }

    def "test fill report pdf with datasource"() {
        when:
        Path rpt = BuildSupport.rootProjectPath.resolve("examples/jasperstudio-project/Orgs-list.jrxml")
        def report = JasperUtils.loadReport(rpt)
        def jprint = JasperUtils.fillReport(report, ["ReportTitle":"Orgs Test"], dataSource)
        Path pdf = exportFolder.resolve("org-list.pdf")
        JasperUtils.exportPDF(jprint, pdf)

        then:
        jprint
        Files.exists(pdf)
        //output.length > 0
        //new String(os.toByteArray(), "US-ASCII").startsWith("%PDF")
    }

}
