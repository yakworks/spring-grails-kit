package yakworks.jasper.adhoc

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import net.sf.dynamicreports.adhoc.configuration.AdhocCalculation
import net.sf.dynamicreports.adhoc.configuration.AdhocReport
import spock.lang.Specification
import yakworks.json.jackson.JacksonJson
import yakworks.json.jackson.ObjectMapperWrapper

class AdhocBindSpec extends Specification {

    static ObjectMapper objectMapper

    void setupSpec(){
        objectMapper = JacksonJson.objectMapper
        //TODO just for testing, we probably dont want to do this so it fails if we have a bad prop.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    void "sanity check"(){
        expect:
        def arpt = new AdhocReport()
        !arpt.highlightDetailOddRows
    }

    void "test using ObjectMapper to setup and bind to an AdhocReport"(){
        when:
        def dta = [
            someNonExistingProp: 'x',
            highlightDetailOddRows: true,
            columns: [
                [name: "foo"],
                [name: "bar"]
            ],
            subtotals:[
                [name: "foo", label: "TOTALS", calculation: "NOTHING"],
                [name: "amount", calculation: "SUM"],
            ]
        ]
        AdhocReport arpt = objectMapper.convertValue(dta, AdhocReport.class)

        then:
        arpt.highlightDetailOddRows
        arpt.columns.size() == 2
        arpt.columns[0].name == 'foo'
        arpt.columns[1].name == 'bar'

        arpt.subtotals[0].name == 'foo'
        arpt.subtotals[0].calculation == AdhocCalculation.NOTHING
        arpt.subtotals[1].calculation == AdhocCalculation.SUM
    }

}
