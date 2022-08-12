/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.dynamic

class DynamicConfig {

    /**
     * domain name. can be short name or fully qualified.
     */
    String entityName

    List<String> fields // ['customer.name', 'product.group.name', 'color', 'product.name', 'isPaid', 'tranDate', 'qty', 'amount'],
    // Map columns // ['tranProp': 'From Getter'],
    List<String> groups // ['customer.name', 'product.group.name', 'color'],
    Map<String, String> subtotals // [qty: "sum", amount: "sum"], //put these on all the group summaries
    Map subtotalsHeader // [amount: "sum"], //put these on all the group summaries
    boolean columnHeaderInFirstGroup = true //for each new primary group value the column header will be reprinted, if false they occur once per page
    boolean groupTotalLabels = true //puts a group total label on the subtotal footers
    boolean highlightDetailOddRows = false
    boolean showGridLines = true
    boolean tableOfContents = false
    boolean ignorePagination = false
    Map columnTitles // ?? what this do?

    /**
     * short cut for pageFormat:[size:'letter', landscape:true]
     */
    boolean landscape = false

    /**
     * [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10,
     * basically any static in net.sf.dynamicreports.report.constant.PageType
     */
    Map<String, Object> pageFormat = [size:'letter', landscape:true]

    String rightTitle
    String title
    //TODO impl
    String subtitle
}
