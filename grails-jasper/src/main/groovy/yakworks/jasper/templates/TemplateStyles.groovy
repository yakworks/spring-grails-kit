/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.templates

import java.awt.*

import groovy.transform.CompileStatic

import org.springframework.core.io.ResourceLoader

import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder
import net.sf.dynamicreports.report.builder.style.SimpleStyleBuilder
import net.sf.dynamicreports.report.builder.style.Styles
import net.sf.dynamicreports.report.builder.style.TemplateStylesBuilder
import net.sf.dynamicreports.report.constant.Evaluation
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment
import net.sf.dynamicreports.report.constant.Language

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.exp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl

/**
 * A bunch of helpers, mostly for examples and tests. should be using TemplateStyles so its configurable
 */
@SuppressWarnings(['NoWildcardImports'])
@CompileStatic
public class TemplateStyles {
    public static ReportStyleBuilder root = Styles.templateStyle("root")
    public static ReportStyleBuilder bold = Styles.templateStyle("bold")
    public static ReportStyleBuilder italic = Styles.templateStyle("italic")
    public static ReportStyleBuilder boldCentered = Styles.templateStyle("boldCentered")
    public static ReportStyleBuilder bold12Centered = Styles.templateStyle("bold12Centered")
    public static ReportStyleBuilder bold18Centered = Styles.templateStyle("bold18Centered")
    public static ReportStyleBuilder bold22Centered = Styles.templateStyle("bold22Centered")

    public static ReportStyleBuilder column = Styles.templateStyle("column")
    public static ReportStyleBuilder columnTitle = Styles.templateStyle("columnTitle")
    public static ReportStyleBuilder columnWithGridLines = Styles.templateStyle("columnWithGridLines")
    public static ReportStyleBuilder columnsBoolean = Styles.templateStyle("columnsBoolean")

    public static ReportStyleBuilder group = Styles.templateStyle("group")
    public static ReportStyleBuilder groupTitle = Styles.templateStyle("groupTitle")
    public static ReportStyleBuilder groupFooter = Styles.templateStyle("groupFooter")
    public static ReportStyleBuilder groupHeader = Styles.templateStyle("groupHeader")
    public static ReportStyleBuilder groupL2 = Styles.templateStyle("groupL2")
    public static ReportStyleBuilder groupL3 = Styles.templateStyle("groupL3")
    public static ReportStyleBuilder groupFooterL2 = Styles.templateStyle("groupFooterL2")
    public static ReportStyleBuilder groupHeaderL2 = Styles.templateStyle("groupHeaderL2")
    public static ReportStyleBuilder groupFooterL3 = Styles.templateStyle("groupFooterL3")
    public static ReportStyleBuilder groupHeaderL3 = Styles.templateStyle("groupHeaderL3")
    public static ReportStyleBuilder subtotal = Styles.templateStyle("subtotal")
    public static ReportStyleBuilder grandTotal = Styles.templateStyle("grandTotal")

    // public static CurrencyType currencyType = new CurrencyType()

    //setDetailOddRowStyle requires a simpleStyleBuilder, cant use a ReportStyleBuilder for some reason.
    public static SimpleStyleBuilder oddRowStyle = stl.simpleStyle().setBackgroundColor(Color.decode("#f1f5f6"))
            // .setBottomBorder(stl.penThin().setLineColor(Color.decode("#dddddd")))
            // .setTopBorder(stl.penThin().setLineColor(Color.decode("#dddddd")))

    // public static PenBuilder lineStyle = stl.penThin().setLineColor(Color.decode("#bbbbbb"))
    // public static PenBuilder lineStyleLight = stl.penThin().setLineColor(Color.decode("#dddddd"))
    // public static ReportTemplateBuilder reportTemplate
    // public static ComponentBuilder<?, ?> dynamicReportsComponent
    // public static ComponentBuilder<?, ?> footerComponent


    public static ReportTemplateBuilder getReportTemplate() {
        DynamicReports.template()
            .setLanguage(Language.GROOVY)
            .setLocale(Locale.ENGLISH)
            .setColumnStyle(column)
            .setColumnTitleStyle(columnTitle)
            .setGroupStyle(group)
            //.setGroupFooterStyle(groupFooterStyleL2)
            .setGroupTitleStyle(groupTitle)
            .setSubtotalStyle(subtotal)
            .highlightDetailOddRows()
            .setDetailOddRowStyle(oddRowStyle)
    }

    /**
     * Creates custom component which is possible to add to any report band component
     */
    public static ComponentBuilder<?, ?> createTitleComponent(String title , String rightTitle = "") {
        // HyperLinkBuilder link = hyperLink(exp.jasperSyntaxText("https://dynamicreports.readthedocs.io/en/latest/"))
        //TODO add sub title
        def dynamicReportsComponent =
                cmp.horizontalList(
                        // cmp.image(new URL("https://www.9ci.com/www/assets/images/9ci-logo-orange.png")).setFixedDimension(60, 60),
                        cmp.verticalList(
                                cmp.text(exp.jasperSyntaxText(title))
                                        .setStyle(bold22Centered)
                                        .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT),
                                // cmp.text(exp.jasperSyntaxText("Docs"))
                                //         .setStyle(italic).setHyperLink(link)
                        )
                ).setFixedWidth(400)

        return cmp.horizontalList()
            .add(
                dynamicReportsComponent,
                cmp.text(exp.jasperSyntaxText(rightTitle))
                    .setStyle(bold18Centered)
                    .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
            )
            .newRow()
            .add(cmp.line())
            .newRow()
            .add(cmp.verticalGap(3))
    }

    public static ComponentBuilder<?, ?> createFooter() {
        def pgOf = cmp.text(exp.jasperSyntax(' "Page "+$V{PAGE_NUMBER}+" of" '))
            .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)

        def pgTotal = cmp.text(exp.jasperSyntax(' " " + $V{PAGE_NUMBER}'))
            .setEvaluationTime(Evaluation.REPORT)
            .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
            .setWidth(10)

        //def page = cmp.horizontalList().add(pgOf,pgTotal)
        def date = cmp.text(exp.jasperSyntax('new java.util.Date()')).setPattern('EEEEE dd MMMMM yyyy')

        return cmp.horizontalList()
                .add(cmp.verticalGap(3))
                .newRow()
                .add(date, pgOf, pgTotal)

    }

    //TODO finish so a custom jrtx can be passed into this
    static TemplateStylesBuilder loadStyles(ResourceLoader loader, String jrtxFileName = null) {
        if (!jrtxFileName) jrtxFileName = "yakworks/jasper/TemplateStyles.jrtx"

        def resTemplateStyles = loader.getResource("classpath:$jrtxFileName")
        assert resTemplateStyles.exists()
        return Styles.loadStyles(resTemplateStyles.inputStream)
    }

    // @CompileStatic
    // public static class CurrencyType extends BigDecimalType {
    //     private static final long serialVersionUID = 1L
    //
    //     @Override
    //     public String getPattern() {
    //         return "\$ #,###.00"
    //     }
    // }

    // @CompileStatic
    // private static class CurrencyValueFormatter extends AbstractValueFormatter<String, Number> {
    //     private static final long serialVersionUID = 1L
    //
    //     private String label
    //
    //     public CurrencyValueFormatter(String label) {
    //         this.label = label
    //     }
    //
    //     @Override
    //     public String format(Number value, ReportParameters reportParameters) {
    //         return label + currencyType.valueToString(value, reportParameters.getLocale())
    //     }
    // }
    //
    // public static CurrencyValueFormatter createCurrencyValueFormatter(String label) {
    //     return new CurrencyValueFormatter(label)
    // }

}
