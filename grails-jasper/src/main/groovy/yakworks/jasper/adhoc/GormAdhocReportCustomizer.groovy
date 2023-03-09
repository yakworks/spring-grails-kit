/*
* Copyright 2010-2018 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.adhoc

import groovy.transform.CompileStatic

import net.sf.dynamicreports.adhoc.configuration.AdhocColumn
import net.sf.dynamicreports.adhoc.configuration.AdhocComponent
import net.sf.dynamicreports.adhoc.configuration.AdhocGroup
import net.sf.dynamicreports.adhoc.configuration.AdhocReport
import net.sf.dynamicreports.adhoc.configuration.AdhocSort
import net.sf.dynamicreports.adhoc.configuration.AdhocSubtotal
import net.sf.dynamicreports.adhoc.report.DefaultAdhocReportCustomizer
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.ReportBuilder
import net.sf.dynamicreports.report.builder.SortBuilder
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.datatype.BooleanType
import net.sf.dynamicreports.report.builder.datatype.DataTypes
import net.sf.dynamicreports.report.builder.expression.Expressions
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.group.GroupBuilder
import net.sf.dynamicreports.report.builder.subtotal.SubtotalBuilder
import net.sf.dynamicreports.report.definition.datatype.DRIDataType
import net.sf.dynamicreports.report.definition.expression.DRIExpression
import net.sf.dynamicreports.report.exception.DRException
import yakworks.meta.MetaEntity
import yakworks.meta.MetaProp

/**
 * This is the new WIP iteration on DynamicReports. Build on the adhoc but doesnt have all the features yet.
 * The adhoc just provides the VO so that its easy to bind json as the setups, still uses the same underlying JasperReportBuilder
 * the is core to all the "dynamic" reports.
 * Still need to look at the metadata for the metaEntity to pick up formating for things like the boolean
 * also, the group bands dont show the "{Field} Totals" on left of band like the Dynmaic reports enables.
 *
 */
@SuppressWarnings(['AssignCollectionSort', 'EmptyCatchBlock'])
@CompileStatic
class GormAdhocReportCustomizer extends DefaultAdhocReportCustomizer {

    MetaEntity metaEntity
    Map<String, MetaProp> metaEntityFlat

    GormAdhocReportCustomizer metaEntity(MetaEntity v){
        this.metaEntity = v
        this.metaEntityFlat = metaEntity.flatten()
        return this
    }
    /**
     * If you want to add some fixed content to a report that is not needed to store in the xml file.
     * For example you can add default page header, footer, default fonts,...
     * Override IN ORDER TO show header
     */
    @Override
    void customize(ReportBuilder<?> report, AdhocReport adhocReport) throws DRException {
        this.report = report;
        this.adhocReport = adhocReport;
        report.setTextStyle(style(adhocReport.getTextStyle()));
        report.setColumnStyle(style(adhocReport.getColumnStyle()));
        report.setColumnTitleStyle(style(adhocReport.getColumnTitleStyle()));
        report.setGroupStyle(style(adhocReport.getGroupStyle()));
        report.setGroupTitleStyle(style(adhocReport.getGroupTitleStyle()));
        report.setSubtotalStyle(style(adhocReport.getSubtotalStyle()));

        //TODO these add the CUSTOM_VALUES that make it hard to edit in the jasper designer.
        // report.setDetailOddRowStyle(simpleStyle(adhocReport.getDetailOddRowStyle()));
        // report.setHighlightDetailOddRows(adhocReport.getHighlightDetailOddRows());
        // report.setDetailEvenRowStyle(simpleStyle(adhocReport.getDetailEvenRowStyle()));
        // report.setHighlightDetailEvenRows(adhocReport.getHighlightDetailEvenRows());
        //put above back in and remove the false settings below to get alt row colors back on.
        report.setHighlightDetailOddRows(false)
        report.setHighlightDetailEvenRows(false)

        report.setIgnorePagination(adhocReport.getIgnorePagination());
        report.setTableOfContents(adhocReport.getTableOfContents());
        page(report, adhocReport.getPage());
        if (adhocReport.getPage() != null) {
            report.setIgnorePageWidth(adhocReport.getPage().getIgnorePageWidth());
        }
        for (AdhocColumn adhocColumn : adhocReport.getColumns()) {
            ColumnBuilder<?, ?> column = column(adhocColumn)
            if (column != null) {
                report.addColumn(column)
                columns.put(adhocColumn.getName(), column)
            }
        }
        int groupnum = 1
        for (AdhocGroup adhocGroup : adhocReport.getGroups()) {
            GroupBuilder<?> group = group(adhocGroup)
            //shows header on group. commented out for now
            //if(groupnum == 1) group.showColumnHeaderAndFooter()
            report.addGroup(group)
            groups.put(adhocGroup.getName(), group)
            groupnum++
        }

        for (AdhocSort adhocSort : adhocReport.getSorts()) {
            SortBuilder sort = this.sort(adhocSort)
            report.addSort(sort)
        }
        for (AdhocComponent adhocComponent : adhocReport.getComponents()) {
            ComponentBuilder<?, ?> component = component(adhocComponent)
            components.put(adhocComponent.getKey(), component)
        }
        addSubtotals()
        addComponents()
        //adds the Grand Total label at end on the left instead of unbold above.
        //report.setSummaryBackgroundComponent(Components.text("Grand Total").setStyle(TemplateStyles.grandTotal))
    }

    @Override
    protected ColumnBuilder<?, ?> column(AdhocColumn adhocColumn) {
        DRIDataType<?, ?> ftype = getFieldType(adhocColumn.name);
        def expr = getFieldExpression(adhocColumn.name)
        TextColumnBuilder<?> column = Columns.column(expr)
        //do boolean types to be checkmark

        if(ftype instanceof BooleanType){
            //add field as we changed the name to an expression in getFieldExpression
            report.addField(adhocColumn.name, Boolean)
            column.setDataType(DataTypes.booleanType() as DRIDataType)
            //column.setWidth(adhocColumn.width == null ? 1 : adhocColumn.width)
            column.width = 2
        } else {
            column.width = 4
        }

        if (adhocColumn.title != null) {
            column.title = Expressions.jasperSyntaxText(adhocColumn.title)
        } else {
            String columnTitle = getFieldLabel(adhocColumn.getName())
            if (columnTitle != null) {
                column.setTitle(Expressions.jasperSyntaxText(adhocColumn.title) )
            }
        }
        if (adhocColumn.getWidth() != null) {
            column.setFixedWidth(adhocColumn.getWidth())
        }

        column.setStyle(style(adhocColumn.getStyle()))
        column.setTitleStyle(style(adhocColumn.getTitleStyle()))
        return column
    }

    //Override to detect type
    @Override
    protected DRIDataType<?, ?> getFieldType(String name) {
        Class clazz= metaEntityFlat[name].classType
        DRIDataType dt
        try {
            dt = DataTypes.detectType(clazz)
        } catch(e){
            // dt = DataTypes.detectType(Object)
            // do nothing and return null for now.
        }
        return dt
    }

    //@Override so we can do booleans
    @Override
    protected DRIExpression<?> getFieldExpression(String name) {
        DRIDataType<?, ?> type = getFieldType(name);
        if(type instanceof BooleanType){
            //add field as we are changing the name
            //report.addField(name, Boolean)
            JasperExpression boolExpr = jrExp('$F{' + name + '} ? (char)0x2713 : ""')
            return boolExpr
        }
        if (type != null) {
            return DynamicReports.field(name, type).build();
        }
        return DynamicReports.field(name, Object.class).build();
    }

    static JasperExpression jrExp(String expression) {
        return Expressions.jasperSyntax(expression)
    }

    //@Override so we can set jasper expressions and it doesnt do the CUSTOM_VALUES stuff
    @Override
    protected SubtotalBuilder<?, ?> subtotal(AdhocSubtotal adhocSubtotal) {
        SubtotalBuilder sb = super.subtotal(adhocSubtotal)
        if (adhocSubtotal.label != null) {
            sb.setLabel("");
            sb.label = Expressions.jasperSyntaxText(adhocSubtotal.label)
        }
        return sb
    }
}
