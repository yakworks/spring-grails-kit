package yakworks

import java.nio.file.Files
import java.nio.file.Path

import org.grails.testing.GrailsUnitTest

import grails.testing.spring.AutowiredTest
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.spring.AppResourceLoader

@Ignore
class AppResourceLoaderSpec extends Specification implements AutowiredTest, GrailsUnitTest {

    AppResourceLoader appResourceLoader

    Closure doWithSpring() {{ ->
        appResourceLoader(AppResourceLoader)
    }}

    // def setup() {
    //     kitchenSinkCsv = new File("src/test/resources/KitchenSink.short.csv")
    //     sinkItemCsv = new File("src/test/resources/Sink.Item.short.csv")
    // }

    void rootPath() {
        expect:
        appResourceLoader.rootPath.toString() == "./build/rootLocation"
    }

    def "test getTempDirectory"() {
        when:
        def dir = appResourceLoader.getTempDirectory()

        then:
        dir
        Files.exists(dir)
        //defaults to java.io.tmpdir
        dir.toString().startsWith(System.getProperty("java.io.tmpdir"))
        dir.toFile().canWrite()
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
        13 == file.size()
        'hello, world!' == file.getText()
    }

}
