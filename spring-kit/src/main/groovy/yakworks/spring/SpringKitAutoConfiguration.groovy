/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@ConditionalOnProperty(prefix = "handlebars", value = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfiguration
//@EnableConfigurationProperties(HandlebarsProperties.class)
class SpringKitAutoConfiguration {

    // ApplicationContext applicationContext

    @Bean
    AppCtx appCtx(ApplicationContext applicationContext) {
        new AppCtx(applicationContext)
    }

    @Bean
    AppResourceLoader appResourceLoader() {
        new AppResourceLoader()
    }
}
