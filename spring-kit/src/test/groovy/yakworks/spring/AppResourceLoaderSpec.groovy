package yakworks.spring


import spock.lang.Specification

class AppResourceLoaderSpec extends Specification {

    var arl = new AppResourceLoader()

    void "wtf"() {

        expect:
        //("foo" ==~ /^\\.?\\/?\\.?[a-zA-Z0-9]/)
        ("..foobar" ==~ /^\.?\/?\.?[a-zA-Z0-9].*/)
    }

    void "test validate path"() {

        expect:
        !validatePath("[:]/foo")
        !validatePath("null/foo")
        !validatePath('${foo}/foo')
        validatePath('./foo')
        validatePath('.foo')
        validatePath('/.foo')
        validatePath('/foo')
    }

    boolean validatePath(String dir){
        try{
            arl.validatePath(dir, 'x')
            return true
        } catch (e){
            return false
        }
    }

}
