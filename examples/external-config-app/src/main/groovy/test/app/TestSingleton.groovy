package test.app

import javax.inject.Singleton

import io.micronaut.context.annotation.Value

@Singleton
class TestSingleton {
    @Value('${test.config.value:not read}')
    String configTest

    @Value('${test.micronaut.only:not read}')
    String micronautOnly

    String getConfigValue() {
        configTest
    }

    String getMicronautOnlyValue() {
        micronautOnly
    }
}
