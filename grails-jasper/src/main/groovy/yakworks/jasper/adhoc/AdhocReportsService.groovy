/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.adhoc

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import net.sf.dynamicreports.adhoc.configuration.AdhocReport
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.DynamicReports
import yakworks.commons.lang.LabelUtils
import yakworks.jasper.templates.TemplateStyles
import yakworks.jasper.templates.Templates
import yakworks.json.jackson.JacksonJson
import yakworks.meta.MetaEntity
import yakworks.spring.SpringEnvironment

@Slf4j
@CompileStatic
class AdhocReportsService implements SpringEnvironment{

    @Autowired ResourceLoader resourceLoader

    // public static AdhocReportDefaults = [
    //     highlightDetailOddRows: true,
    // ]
    // JasperReportBuilder createReportBuilder(String entityName, Map cfg) {
    //
    // }

    JasperReportBuilder createReportBuilder(MetaEntity metaEntity, Map cfg) {
        def flatMeta = metaEntity.flatten()
        // preProcessAdhocConfig
        if(cfg['includes']){
            List incs = cfg.remove('includes')
            List columns = [] as List<Map>
            incs.each{ name ->
                columns << [name: name]
            }
            cfg['columns'] = columns
        }
        //if no columms then pull from whats speced in metaEntity
        if(!cfg['columns']) {
            cfg['columns'] = flatMeta.keySet() as List
        }
        if(cfg['columns']){
            List curList = cfg['columns'] as List
            cfg['columns'] = curList.collect {
                Map col = (it instanceof String ? [name: it] : it) as Map
                if(col.title == null) col.title = LabelUtils.getNaturalTitle(col.name as String)
                return col
            }
        }
        if(cfg['groups']){
            List curList = cfg['groups'] as List
            cfg['groups'] = curList.collect {
                return (it instanceof String) ? [name: it] : it
            }
        }
        AdhocReport adhocReport = objectMapper.convertValue(cfg, AdhocReport)
        return createReportBuilder(metaEntity, adhocReport)
    }

    JasperReportBuilder createReportBuilder(MetaEntity metaEntity, AdhocReport adhocReport, String title = null) {
        JasperReportBuilder jrb = DynamicReports.report()
        def adhocReportCustomizer = new GormAdhocReportCustomizer().metaEntity(metaEntity)
        adhocReportCustomizer.customize(jrb, adhocReport)

        styleCustom(jrb, title ?: metaEntity.title)
        //or example from net.sf.dynamicreports.examples that has alternating row colors
        //styleStock(jrb, title ?: metaEntity.title)

        // jrb.setSummaryBackgroundComponent(Components.text("Grand Total").setStyle(TemplateStyles.grandTotal))
        return jrb
    }

    ObjectMapper getObjectMapper(){
        def _objectMapper = JacksonJson.objectMapper
        //TODO just for testing, we probably dont want to do this so it fails if we have a bad prop.
        _objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return _objectMapper
    }

    //example from net.sf.dynamicreports.examples
    void styleStock(JasperReportBuilder jrb, String title){
        jrb.setTemplate(Templates.reportTemplate)
        if(title) jrb.title(Templates.createTitleComponent(title))
    }

    //example from net.sf.dynamicreports.examples
    void styleCustom(JasperReportBuilder jrb, String title){
        jrb.setTemplate(TemplateStyles.reportTemplate)
            .templateStyles(TemplateStyles.loadStyles(resourceLoader))
            // .tableOfContents()

        if(title) jrb.title(TemplateStyles.createTitleComponent(title))
    }

}
