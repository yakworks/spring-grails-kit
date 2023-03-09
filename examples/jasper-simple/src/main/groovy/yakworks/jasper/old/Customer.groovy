package yakworks.jasper.old

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * Created by basejump on 10/15/16.
 */
@Entity
@GrailsCompileStatic
class Customer {
    String num
    String name

    static constraints = {
        num nullable:true
    }
}
