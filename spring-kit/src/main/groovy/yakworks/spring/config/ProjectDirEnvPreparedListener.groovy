/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

/**
 * add default or `.` used for testing and dev for project.rootProjectDir.
 * in gradle you would set for test and bootRun which works fine but when running Application from intellij
 * it does not pick up the settings so this just makes the default `.`
 *
 * ```
 * tasks.withType(Test) { // and bootRun {
 *   ...
 *   systemProperty "project.rootProjectDir", rootProject.projectDir.absolutePath
 * }
 * //then in the application.props or yaml
 * app.resources.rootLocation: "${project.rootProjectDir}/build/rootLocation"
 * ```
 *
 * see https://stackoverflow.com/a/29133241/6500859
 * could also look into using the EnvironmentPostProcessor
 * aslo example here
 * https://github.com/spring-cloud/spring-cloud-sleuth/blob/2.2.x/spring-cloud-sleuth-core/src/main/java/org/springframework/cloud/sleuth/
 *   autoconfig/TraceEnvironmentPostProcessor.java
 */
@CompileStatic
class ProjectDirEnvPreparedListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.environment
        String rootProjectDir = System.getProperty("project.rootProjectDir") ?: "."
        String projectDir = System.getProperty("project.projectDir") ?: "."
        // String rootProjectDir = environment.getProperty("app.resources.rootProjectDir",)
        var cfgMap = [
            "project.rootProjectDir": rootProjectDir,
            "project.projectDir": projectDir,
        ] as  Map<String, Object>
        environment.getPropertySources().addFirst(new MapPropertySource("rootProjectDirListener", cfgMap))
    }

}
