/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.dynamic

import java.nio.file.Path

import groovy.transform.CompileStatic

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder

import static net.sf.dynamicreports.report.builder.DynamicReports.export

/**
 * Helpers for testing reports
 * Some of this should be moved to a common helper in the jasper plugin.
 */
@CompileStatic
class ReportSaveUtils {

    static boolean OPEN_REPORTS_ON_SAVE = true

    /**
     * Saves the files in jrxml, pdf and html
     */
    static boolean saveToFiles(JasperReportBuilder dr, Path folder, String fname) {
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
                .setHtmlHeader(HTMLHeader)
                .setHtmlFooter(HTMLFooter) //.setFramesAsNestedTables(true).setZoomRatio(200)
        )
        System.err.println("HTML time : " + (System.currentTimeMillis() - start));

        if(OPEN_REPORTS_ON_SAVE){
            openFile(folder, "${fname}.html")
        }
        return true
    }

    static openFile(Path folder, String fname){

        // TODO only works on the mac for now.
        // if running on a mac will open it.
        if(System.getProperty("os.name") == "Mac OS X") {
            def fpointer = folder.resolve(fname).toString()
            "open ${fpointer}".execute()
        }
    }

    static String HTMLHeader = ('''
<html>
    <head>
        <title></title>
        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>
        <style type=\"text/css\">
            a {text-decoration: none}
            html { zoom: 110%; }
        </style>
    </head>
    <body text=\"#000000\" link=\"#000000\" alink=\"#000000\" vlink=\"#000000\">
        <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">
            <tr>
                <td width="10%">&nbsp;</td>
                <td align=\"center\">
 ''')

    static String HTMLFooter = '''
                </td>
                <td width="10%">&nbsp;</td>
            </tr>
        </table>
    </body>
</html>
'''
}
