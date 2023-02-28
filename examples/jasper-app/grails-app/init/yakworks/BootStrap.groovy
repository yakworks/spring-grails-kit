package yakworks

import gorm.tools.transaction.TrxService
import yakworks.rally.seed.RallySeed
import yakworks.reports.SeedData

class BootStrap {
    static Random rand = new Random()

    def init = { servletContext ->
        TrxService.bean().withTrx {
            new SeedData().seed()
        }
        //RallySeed.init()
        RallySeed.fullMonty()
    }

    def destroy = {
    }

}
