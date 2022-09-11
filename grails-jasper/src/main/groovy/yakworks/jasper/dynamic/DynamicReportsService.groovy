/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.dynamic

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader

import gorm.tools.utils.GormMetaUtils
import grails.util.GrailsUtil
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.HyperLinkBuilder
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.ValueColumnBuilder
import net.sf.dynamicreports.report.builder.component.Components
import net.sf.dynamicreports.report.builder.datatype.DataTypes
import net.sf.dynamicreports.report.builder.expression.Expressions
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.builder.group.GroupBuilder
import net.sf.dynamicreports.report.builder.group.Groups
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder
import net.sf.dynamicreports.report.builder.subtotal.SubtotalBuilder
import net.sf.dynamicreports.report.builder.subtotal.Subtotals
import net.sf.dynamicreports.report.constant.GroupHeaderLayout
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.definition.datatype.DRIDataType
import yakworks.jasper.templates.TemplateStyles
import yakworks.reports.DomainMetaUtils
import yakworks.reports.FieldMetadata
import yakworks.spring.SpringEnvironment

import static net.sf.dynamicreports.report.builder.DynamicReports.hyperLink

@Slf4j
@CompileStatic
class DynamicReportsService implements SpringEnvironment{

    @Autowired ResourceLoader resourceLoader

    JasperReportBuilder buildDynamicReport(DynamicConfig reportCfg) {
        PersistentEntity entityClass = GormMetaUtils.findPersistentEntity(reportCfg.entityName as String)
        assert entityClass
        buildDynamicReport(entityClass, reportCfg)
    }

    // @CompileDynamic
    JasperReportBuilder buildDynamicReport(PersistentEntity domainClass, DynamicConfig reportCfg, Map params = null) {
        log.debug "doReport with $params"

        //TODO do some basic validation on reportCfg. maybe even setup domains for them
        List fields = reportCfg.fields
        //StyleStatics.init()
        String rightTitle = reportCfg.rightTitle
        String title = reportCfg.title
        //?: getPropertyValue(domainClass.clazz, 'reportColumns') ?: domainClass.properties.name - ['id', 'version']
        JasperReportBuilder jrb = new JasperReportBuilder()
                .title(TemplateStyles.createTitleComponent(reportCfg.title))
                .setTemplate(TemplateStyles.reportTemplate)
                .templateStyles(TemplateStyles.loadStyles(resourceLoader))

//        def res = grailsApplication.mainContext.getResource("classpath:yakworks/jasper/DefaultTemplate.jrxml")
//        jrb.setTemplateDesign(res.inputStream)

        if (reportCfg.highlightDetailOddRows) {
            jrb.highlightDetailOddRows().setDetailOddRowStyle(TemplateStyles.oddRowStyle)
        }
        if (reportCfg.showGridLines) {
            jrb.setColumnStyle(TemplateStyles.columnWithGridLines)//StyleTemplates.columnStyleWithGridLines)//
        }
        if (reportCfg.tableOfContents) {
            jrb.tableOfContents()
        }
        if (reportCfg.ignorePagination) {
            jrb.ignorePagination()
        } else {
            jrb.pageFooter(TemplateStyles.createFooter())
        }

        if (reportCfg.landscape) {
            jrb.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        }

        //TODO should we do this just in case?
        //.sortBy(dateColumn, invoiceColumn)

        //Column setups
        Map<String, FieldMetadata> fieldMetaMap = DomainMetaUtils.getFieldMetadata(domainClass, fields, reportCfg.columnTitles as Map)
        populateColumnBuilders(fieldMetaMap, jrb)
        ColumnBuilder[] columnBuilderList = fieldMetaMap.values()*.builder as ColumnBuilder[]
        jrb.columns(columnBuilderList)

        //Groups
        Map groupBuilders = buildGroupBands(fieldMetaMap, reportCfg) as Map<String, Map>
        groupBuilders.eachWithIndex { String k, Map v, i ->
            GroupBuilder groupBuilder = v.builder as GroupBuilder
            AggregationSubtotalBuilder[] subtotalBuilders = v.subtotalBuilders as AggregationSubtotalBuilder[]
            jrb.groupBy(groupBuilder.headerWithSubtotal()).subtotalsAtGroupFooter(groupBuilder, subtotalBuilders)
        }

        List summaryBuilders = buildSummaryBands(fieldMetaMap, reportCfg)
        jrb.subtotalsAtSummary(summaryBuilders as SubtotalBuilder[])
        jrb.setSummaryBackgroundComponent(Components.text("Grand Total").setStyle(TemplateStyles.grandTotal))

        if (reportCfg.columnHeaderInFirstGroup) jrb.setShowColumnTitle(false)
        // if (reportCfg.showTableOfContents) jrb.tableOfContents()

        return jrb
    }

    @CompileDynamic
    Collection<?> createDataSource(PersistentEntity domainClass, List groupFields) {
        List results
        if (groupFields) {
            def c = domainClass.javaClass.createCriteria()
            results = c.list {
                orderNested(groupFields).call()
            }
            //recs = domainClass.clazz.findAll("from $domainClass.clazz.name as s order by ${groupFields.join(',')}")
        } else {
            results = domainClass.javaClass.list()
        }
        return results

    }

    /**
     * add a reference to the column builder into the FieldMetadata
     * @param fieldMap FieldMetadata
     * @return the same map ref populated
     */
    Map<String, FieldMetadata> populateColumnBuilders(Map<String, FieldMetadata> fieldMap, JasperReportBuilder jrb) {
        //Map<String,Map> drCols = [:]

        fieldMap.each { /*key*/ String field, /*value*/ FieldMetadata fld ->
            ColumnBuilder colb
            if(fld.typeClass == Object){
                colb = Columns.column(field, new ObjectType()).setAnchorName(field)
            } else {
                colb = Columns.column(field, DataTypes.detectType(fld.typeClass)).setAnchorName(field)
            }

            colb.setWidth(fld.width == null ? 4 : fld.width)
            //link symbols , 221e,260d, 2709 is email, 270e is pencil
            HyperLinkBuilder link = hyperLink(jrExp('"https://www.google.com/search?q=" + $F{' + field + '}'))
            colb.setHyperLink(link)

            if (fld.isBooleanType()) {
                jrb.addField(field, Boolean) //drb.field(field,Boolean.class)

                //? (char)0x2611 : (char)0x2610") //<- see http://dejavu.sourceforge.net/samples/DejaVuSans.pdf for more options
                JasperExpression bool = jrExp('$F{' + field + '} ? (char)0x2713 : ""')
                colb = Columns.column(bool).setDataType(DataTypes.booleanType() as DRIDataType)
                //do style
                //def sb = new StyleBuilder()
                //sb.object.parentStyle = jrb.object.columnStyle
                //colb.style = sb.bold()//.setFontSize(18)
                colb.setWidth(fld.width == null ? 1 : fld.width)
            }

            colb.setTitle(jrText(fld.title))
            fld.builder = colb
        }
        return fieldMap
    }

    // @CompileDynamic
    Map<String, Map> buildGroupBands(Map<String, FieldMetadata> fieldMetaMap, DynamicConfig reportCfg) {

        Map<String, Map> groups = [:]
        int grpSize = reportCfg.groups.size()
        reportCfg.groups.eachWithIndex { String field, Integer index ->
            ColumnGroupBuilder group = Groups.group("Group_$field", fieldMetaMap[field].builder as ValueColumnBuilder)
            boolean isLastOrSingleGroup = (grpSize == index + 1)
            group.setPadding(3)

            if (index == 0) {
                group.setHeaderLayout(GroupHeaderLayout.VALUE)
                if (reportCfg?.columnHeaderInFirstGroup) group.showColumnHeaderAndFooter()
                group.setStyle(TemplateStyles.group)
                group.setFooterStyle(TemplateStyles.group)

            } else if (index == 1) {
                group.setHeaderLayout(GroupHeaderLayout.VALUE)
                group.setStyle(TemplateStyles.groupL2)
                group.setHeaderStyle(TemplateStyles.groupHeaderL2)
                group.setFooterStyle(TemplateStyles.groupFooterL2)
                //group.setPadding(3)
                //group.showColumnHeaderAndFooter
            } else {
                group.setStyle(TemplateStyles.groupL3)
                group.setHeaderStyle(TemplateStyles.groupHeaderL3)
                group.setFooterStyle(TemplateStyles.groupFooterL3)
            }

            List<AggregationSubtotalBuilder> sbtList = []

            reportCfg.subtotals?.each { grpField, calc ->
                AggregationSubtotalBuilder subtot = createSubTotal(calc, fieldMetaMap[grpField].builder as ValueColumnBuilder)
                sbtList.add subtot
            }
//            def comp = drb.cmp.horizontalList()
//                .setFixedDimension(557, 20)
//                //.setBackgroundComponent(...)
//                .add(drb.cmp.gap(557,2))
//                .newRow()
//                .add(
//                    //2. a gap of width 70
//                    drb.cmp.gap(70,13),
//                    //3. the text field
//                    drb.cmp.text("Hello World")//.setStyle(...)
//                );
//
//            group.addFooterComponent(comp)

            //don't add it to the last group by default or if there is only 1 group
            if (reportCfg.groupTotalLabels && sbtList) {
                //just add it to the first one
                //sbtList[0].setLabel("${fieldMetaMap[field].title} Totals").setLabelPosition(Position.LEFT);

                boolean shoudlDoLabel = true
                //if its the only group or top level one then only do totals if reportCfg.groupTotalLabelsOnLast
                if(isLastOrSingleGroup && !reportCfg.groupTotalLabelsOnLast){
                    shoudlDoLabel = false
                }
                if (shoudlDoLabel){
                    JasperExpression<String> label = jrExp("\"\" + \$F{" + field + "} + \" Total\"", String)
                    //sbtList.add drb.sbt.first(label,fieldMetaMap[config.groupTotalLabels].builder)
                    group.setFooterBackgroundComponent(
                        Components.text(label).setStyle(TemplateStyles.subtotal)
                    )
                }
            }

            //add the subtotals
            groups[field] = [:]
            groups[field].subtotalBuilders = sbtList
            groups[field].builder = group
        }
        return groups
    }

    // @CompileDynamic
    List<AggregationSubtotalBuilder> buildSummaryBands(Map<String, FieldMetadata> fieldMetaMap, DynamicConfig reportCfg) {

        Map<String, Map> subtotals = [:]
        int subsSize = reportCfg.subtotals.size()
        List<AggregationSubtotalBuilder> sbtList = []
        reportCfg.subtotals?.each { grpField, calc ->
            AggregationSubtotalBuilder subtot = createSubTotal(calc, fieldMetaMap[grpField].builder as ValueColumnBuilder)
            subtot.setStyle(TemplateStyles.grandTotal)
            sbtList.add subtot
        }
        return sbtList
    }

    @CompileDynamic
    static AggregationSubtotalBuilder createSubTotal(String calc, ValueColumnBuilder colBuilder){
        AggregationSubtotalBuilder subtot = Subtotals."${calc}"(colBuilder)
        return subtot
    }

    // @SuppressWarnings(['UnusedPrivateMethod'])
    // @CompileDynamic
    // private Map loadConfig() {
    //     GroovyClassLoader classLoader = new GroovyClassLoader(getClass().classLoader)
    //     config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('DefaultDynamicJasperConfig')))
    //     try {
    //         config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('DynamicJasperConfig')))
    //     } catch (Exception ignored) {
    //         // ignore, just use the defaults
    //     }
    //     return new ConfigSlurper(GrailsUtil.environment).parse(new Properties()).merge(config.dynamicJasper)
    // }

    def getPropertyValue(Class clazz, String propertyName) {
        clazz.metaClass.hasProperty(clazz, propertyName)?.getProperty(clazz)
    }

    // def setPropertyIfNotNull(Object target, String propertyName, Object value) {
    //     if (value != null && (!(value instanceof ConfigObject) || !(value.isEmpty()))) {
    //         target[propertyName] = value
    //     }
    // }

    //jasper
    /**
     * Creates a new jasper string expression, useful only for showing a static text.<br/>
     * This method escapes the characters in a {@code String} using Java String rules.
     *
     * @param text text to be shown
     * @return the expression
     */
    public JasperExpression<String> jrText(String text) {
        return Expressions.jasperSyntaxText(text)
    }

    /**
     * Creates a new jasper expression.<br/>
     * This expression allows declaring an expression in a Jasper native syntax. Knowledge of the jasper syntax is also required for proper use.
     *
     * @param expression the jasper expression
     * @param valueClass the expression class
     * @return the expression
     */
    public <T> JasperExpression<T> jrExp(String expression, Class<? super T> valueClass) {
        return Expressions.jasperSyntax(expression, valueClass)
    }

    /**
     * Creates a new jasper expression.<br/>
     * This expression allows declaring an expression in a Jasper native syntax. Knowledge of the jasper syntax is also required for proper use.
     *
     * @param expression the jasper expression
     * @return the expression
     */
    @SuppressWarnings("rawtypes")
    public JasperExpression jrExp(String expression) {
        return Expressions.jasperSyntax(expression)
    }

}
