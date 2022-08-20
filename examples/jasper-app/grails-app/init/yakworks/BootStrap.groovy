package yakworks

import gorm.tools.transaction.TrxService
import yakworks.rally.testing.RallySeedData
import yakworks.reports.SeedData

class BootStrap {
    static Random rand = new Random()

    def init = { servletContext ->
        TrxService.bean().withTrx {
            new SeedData().seed()
        }
        RallySeedData.init()
        RallySeedData.fullMonty()
    }

    def destroy = {
    }

}
