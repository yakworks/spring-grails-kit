package grails522

import yakworks.reports.SeedData

class BootStrap {

    def init = { servletContext ->
        new SeedData().seed()
    }
    def destroy = {
    }
}
