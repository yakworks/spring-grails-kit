package foo

import gorm.tools.hibernate.criteria.CreateCriteriaSupport
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class Bills implements RepoEntity<Bills> {

    Customer customer
    Product product
    String color
    BigDecimal amount
    Long qty
    Date tranDate
    Boolean isPaid = false

    Map ext

    static transients = ['tranProp']

    String getTranProp(){
        "tp"
    }
}
