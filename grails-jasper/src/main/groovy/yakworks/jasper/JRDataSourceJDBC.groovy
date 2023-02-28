/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper

import javax.sql.DataSource

import groovy.transform.CompileStatic

import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField

/**
 * Simple container class so we can pass the jdbc DataSource around as a JRDataSource
 * Used with the views so only 1 type needs to be checked
 */
@CompileStatic
class JRDataSourceJDBC implements JRDataSource {

    DataSource dataSource

    public JRDataSourceJDBC(DataSource dataSource) {
        this.dataSource = dataSource
    }

    @Override
    boolean next() throws JRException {
        throw new JRException("Not implemented")
    }

    @Override
    Object getFieldValue(JRField jrField) throws JRException {
        throw new JRException("Not implemented")
    }

}
