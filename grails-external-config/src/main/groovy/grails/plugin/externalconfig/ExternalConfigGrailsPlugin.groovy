/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package grails.plugin.externalconfig

import grails.plugins.Plugin

class ExternalConfigGrailsPlugin extends Plugin {

    def grailsVersion = "3.3.0 > *"
    def pluginExcludes = []

    def title = "External Config" // Headline display name of the plugin
    def author = "Søren Berg Glasius"
    def authorEmail = "soeren@glasius.dk"
    def description = '''\
Load configs with grails.config.locations like in Grails 2.x
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/external-config"
    def license = "APACHE"

    // Any additional developers beyond the author specified above.
    def developers = [
        [ name: "Sudhir Nimavat", url: "https://github.com/snimavat" ],
        [ name: "Dennie de Lange", url: "https://github.com/tkvw" ],
    ]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "https://github.com/sbglasius/external-config/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/sbglasius/external-config" ]
}
