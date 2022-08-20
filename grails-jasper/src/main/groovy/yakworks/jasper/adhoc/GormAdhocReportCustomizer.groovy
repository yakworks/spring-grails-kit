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
import net.sf.dynamicreports.adhoc.report.AdhocReportCustomizer
import net.sf.dynamicreports.adhoc.report.DefaultAdhocReportCustomizer
import net.sf.dynamicreports.report.builder.ReportBuilder
import net.sf.dynamicreports.report.builder.SortBuilder
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.datatype.DataTypes
import net.sf.dynamicreports.report.builder.group.GroupBuilder
import net.sf.dynamicreports.report.definition.datatype.DRIDataType
import net.sf.dynamicreports.report.exception.DRException
import yakworks.meta.MetaEntity
import yakworks.meta.MetaProp

/**
 * <p>DefaultAdhocReportCustomizer class.</p>
 * Provides basic implementation for the {@link AdhocReportCustomizer#customize(ReportBuilder, AdhocReport)} method. The public methods can
 * be extended to provide further customization at runtime as shown;
 * <pre>
 *     {@code
 *      class ReportCustomizer extends DefaultAdhocReportCustomizer {
 *
 *         //@Override
 *         public void customize(ReportBuilder<?> report, AdhocReport adhocReport) throws DRException {
 *            super.customize(report, adhocReport);
 *            // default report values
 *            report.setTemplate(Templates.reportTemplate);
 *            report.title(Templates.createTitleComponent("AdhocCustomizer"));
 *            // a fixed page footer that user cannot change, this customization is not stored in the xml file
 *            report.pageFooter(Templates.footerComponent);
 *         }
 *
 *         //@Override
 *         protected DRIDataType<?, ?> getFieldType(String name) {
 *           if (name.equals("item")) {
 *             return type.stringType();
 *            }
 *           if (name.equals("orderdate")) {
 *             return type.dateType();
 *            }
 *            if (name.equals("quantity")) {
 *              return type.integerType();
 *            }
 *            if (name.equals("unitprice")) {
 *              return type.bigDecimalType();
 *            }
 *           return super.getFieldType(name);
 *          }
 *
 *         //@Override
 *         protected String getFieldLabel(String name) {
 *           if (name.equals("item")) {
 *              return "Item";
 *            }
 *            if (name.equals("orderdate")) {
 *              return "Order date";
 *            }
 *            if (name.equals("quantity")) {
 *               return "Quantity";
 *            }
 *            if (name.equals("unitprice")) {
 *               return "Unit price";
 *            }
 *            return name;
 *          }
 *       }
 *    }
 * </pre>
 *
 * @author Ricardo Mariaca
 *
 */
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
        report.setDetailOddRowStyle(simpleStyle(adhocReport.getDetailOddRowStyle()));
        report.setHighlightDetailOddRows(adhocReport.getHighlightDetailOddRows());
        report.setDetailEvenRowStyle(simpleStyle(adhocReport.getDetailEvenRowStyle()));
        report.setHighlightDetailEvenRows(adhocReport.getHighlightDetailEvenRows());
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
            // if(groupnum == 1) group.showColumnHeaderAndFooter()
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
    }

    @Override
    protected ColumnBuilder<?, ?> column(AdhocColumn adhocColumn) {

        TextColumnBuilder<?> column = Columns.column(getFieldExpression(adhocColumn.name))

        if (adhocColumn.title != null) {
            column.title = adhocColumn.title
        } else {
            String columnTitle = getFieldLabel(adhocColumn.getName())
            if (columnTitle != null) {
                column.setTitle(columnTitle)
            }
        }
        if (adhocColumn.getWidth() != null) {
            column.setFixedWidth(adhocColumn.getWidth())
        }

        column.setStyle(style(adhocColumn.getStyle()))
        column.setTitleStyle(style(adhocColumn.getTitleStyle()))
        return column
    }

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

}
