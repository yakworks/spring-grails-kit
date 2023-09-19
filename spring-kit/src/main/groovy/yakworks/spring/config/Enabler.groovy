/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring.config

import groovy.transform.CompileStatic

/**
 * A base concrete class for all the `someConfig.enabled: true` config formats.
 */
@CompileStatic
class Enabler {
    /** whether this config item is enabled */
    boolean enabled = false

    Enabler(){}

    Enabler(boolean v){
        this.enabled = v
    }
}
