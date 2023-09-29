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
class CronJobProps {

    /**
     * CRON expression for the job
     * default is "-" which is springs way of disabling the job.
     */
    String cron = Scheduled.CRON_DISABLED

    /**
     * Timezone for the cron expressions. UTC is default.
     * Should use the region. so for CST set the "America/Chicago" and EST "America/New_York", etc..
     * A time zone for which the cron expression will be resolved.
     * By default, this attribute is the empty String (i.e. the server's local time zone will be used).
     */
    String zone = ''

    boolean isEnabled(){
        return cron && cron != Scheduled.CRON_DISABLED
    }
}
