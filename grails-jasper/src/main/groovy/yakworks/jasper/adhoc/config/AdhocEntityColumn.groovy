/*
* Copyright 2010-2018 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.adhoc.config

import groovy.transform.CompileStatic

import net.sf.dynamicreports.adhoc.configuration.AdhocColumn
import yakworks.meta.MetaProp

/**
 * extends AdhocColumn
 */
@CompileStatic
class AdhocEntityColumn extends AdhocColumn {

    MetaProp metaProp

    /**
     * the java long qualified class name
     * ex: java.lang.String, java.math.BigDecimal etc...
     */
    Class typeClass //= Object

}
