/*
* Copyright 2010-2018 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.adhoc.config

import groovy.transform.CompileStatic

import net.sf.dynamicreports.adhoc.configuration.AdhocReport

/**
 * Extends AdhocReport to add extras like Title
 */
@CompileStatic
class AdhocReportConfig extends AdhocReport {

    String title
    List<String> fields = [] as List<String>


}
