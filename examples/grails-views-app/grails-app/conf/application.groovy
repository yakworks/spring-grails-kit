app {
    resources {

        currentTenant = { return [id: 1, num: "testTenant"] }
        views.location = "views"

        rootLocation = "root-location"
        tempDir = "temp"

        attachments.location = 'attachments'
        checkImage.location = "checkImages"
        scripts.location = "scripts"
    }
}
