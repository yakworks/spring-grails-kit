/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yakworks.jasper.app

import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import yakworks.jasper.JasperUtils
import yakworks.security.user.UserInfo

/**
 * Controller for "/spring".
 *
 * @author Joe Grandja
 */
@Controller
@CompileStatic
@RequestMapping(value = "/reports")
class ReportsController {

    @Autowired ApplicationContext applicationContext
    @Autowired DataSource dataSource

    @GetMapping("/pdf1")
    void pdf1(HttpServletResponse response) {
        assert applicationContext
        assert dataSource
        String rptName = "Orgs-list"
        //String format = "pdf"
        Resource rpt = applicationContext.getResource("classpath:reports/${rptName}.jrxml")
        assert rpt.exists()
        JasperReport report = JasperUtils.loadReport(rpt)
        JasperPrint jprint = JasperUtils.fillReport(report, ["ReportTitle":"Orgs Test"] as Map<String, Object>, dataSource)

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=${rptName}.pdf")
        final OutputStream outStream = response.getOutputStream()
        JasperUtils.exportPDF(jprint, outStream)
    }

    @GetMapping("/pdf2")
    void pdf2(HttpServletResponse response,
              @RequestParam(required=false) Long orgTypeId,
              @RequestParam Map<String,String> params) {
        String rptName = "Orgs-list-filter"
        Resource rpt = applicationContext.getResource("classpath:reports/${rptName}.jrxml")
        assert rpt.exists()
        JasperReport report = JasperUtils.loadReport(rpt)
        Map jasParams = ["ReportTitle":"Orgs Test", orgTypeId: orgTypeId] as Map<String, Object>

        JasperPrint jprint = JasperUtils.fillReport(report, jasParams, dataSource)
        writeReport(response, jprint, rptName)
    }

    @GetMapping()
    String home() {
        "index"
    }

    @GetMapping("/about")
    @ResponseBody String about(@RequestParam Map<String,String> params) {
        "just a string with params ${params}"
    }

    void writeReport(HttpServletResponse response, JasperPrint jprint, String name){
        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=${name}.pdf")
        final OutputStream outStream = response.getOutputStream()
        JasperUtils.exportPDF(jprint, outStream)
    }
}
