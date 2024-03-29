//GORM DEFAULTS TO ALLOW NULLS AND FAIL ON ERROR
grails {
    //gorm.flushMode = 'AUTO'
    gorm.failOnError = true
    gorm.default.mapping = {
        // id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
        '*'(cascadeValidate: 'dirty')
    }
    gorm.default.constraints = {
        '*'(nullable: true)
    }
}
