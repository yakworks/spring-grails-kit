/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring.config

import groovy.transform.CompileStatic

import org.springframework.scheduling.annotation.Scheduled

/**
 * Base class for jobs, extends on the enabler class
 */
@CompileStatic
class CronConfig {
    /**
     * CRON expression for the job
     * default is "-" which is springs way of disabling the job.
     */
    String cron = Scheduled.CRON_DISABLED

    boolean isEnabled(){
        return cron && cron != Scheduled.CRON_DISABLED
    }
}
