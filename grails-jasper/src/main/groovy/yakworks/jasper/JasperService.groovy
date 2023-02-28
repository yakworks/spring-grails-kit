/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import yakworks.jasper.spring.JasperViewService

/**
 * WIP for jasper report resources.
 * TODO start building out the default dir to load reports from as well as subreports dir.
 */
@CompileStatic
class JasperService {

    //the MVC view related service.
    @Autowired JasperViewService jasperViewService

}
