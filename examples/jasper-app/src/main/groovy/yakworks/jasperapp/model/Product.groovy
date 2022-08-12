package yakworks.jasperapp.model

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * Created by basejump on 10/15/16.
 */
@Entity
@GrailsCompileStatic
class Product {
    ProductGroup group
    String num
    String name

    static constraints = {
        num nullable:true
    }
}
