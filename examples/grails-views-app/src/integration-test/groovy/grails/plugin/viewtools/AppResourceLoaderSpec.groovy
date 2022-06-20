package grails.plugin.viewtools

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import grails.testing.mixin.integration.Integration
import org.apache.commons.io.FileUtils
import org.springframework.core.io.Resource
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.grails.resource.AppResourceLoader

@Integration
class AppResourceLoaderSpec extends Specification {

    @Shared AppResourceLoader appResourceLoader

    void cleanupSpec() {
        appResourceLoader.deleteDirectory("attachments.location")
        appResourceLoader.deleteDirectory("tempDir")
    }

    def testCreateTempFile_empty() {
        when:
        Path file = appResourceLoader.createTempFile("hello.txt", null)

        then:
        file
        file.fileName.toString().startsWith('hello')
        file.fileName.toString().endsWith('txt')
        0 == file.size()
    }

    def testCreateTempFile_string() {
        when:
        def file = appResourceLoader.createTempFile("hello.txt", 'hello, world!')

        then:
        file
        'hello, world!' == file.getText()
    }

    def testCreateTempFile_bytes() {
        when:
        def bytes = 'hello, world!'.getBytes()
        def file = appResourceLoader.createTempFile("hello.txt", bytes)

        then:
        file
        'hello, world!' == file.getText()
    }

    def testDeleteTempUploadedFiles() {
        when:
        def file1 = appResourceLoader.createTempFile('file1.txt', 'hello, world!')
        def file2 = appResourceLoader.createTempFile('file2.txt', 'goodbye cruel world.')

        then:
        Files.exists(file1)
        Files.exists(file2)

        when:
        appResourceLoader.deleteIfExists(file1, file2)

        then:
        !Files.exists(file1)
        !Files.exists(file2)
    }

    def testGetRootPath() {
        when:
        Path rootPath = appResourceLoader.rootPath

        then:
        rootPath != null
        Files.exists(rootPath)
        Files.isDirectory(rootPath)

        rootPath.endsWith('root-location')
    }

    def "test getTempDirectory"() {
        when:
        def dir = appResourceLoader.getTempDirectory()

        then:
        dir
        Files.exists(dir)
        //relative
        dir.toString().endsWith("temp")
        dir.toFile().canWrite()
    }

    def testGetLocation_absolute_scripts() {
        when:
        Path scripts = appResourceLoader.scripts
        Files.createDirectories(scripts)

        then:
        Files.exists(scripts)
    }

    def testGetLocation_relative_checkImages() {
        when:
        Path checkImages = appResourceLoader.getPath('checkImage.location', true)

        then:
        Files.exists(checkImages)
        Files.isDirectory(checkImages)
    }

    def "test get resource"() {
        setup:
        byte[] data = Files.readAllBytes(Paths.get('src/integration-test/resources/grails_logo.jpg'))

        Path viewsDirectory = appResourceLoader.getPath('views.location')

        assert Files.exists(viewsDirectory)

        Path viewFile = viewsDirectory.resolve("test.view")

        Files.write(viewFile, data)

        expect:
        Files.exists(viewFile)

        when:
        Resource resource = appResourceLoader.getResource("views/test.view")

        then:
        resource.exists()

        when:
        resource = appResourceLoader.getResource("views.location", "test.view")

        then:
        resource.exists()

        cleanup:
        Files.delete(viewFile)
    }
}
