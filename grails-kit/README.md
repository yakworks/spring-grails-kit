General Helpers for Grails

### GrailsWebEnvironment

GrailsWebEnvironment.bindRequestIfNull() methods are the ones of interest.
based on the RenderEnvironment in grails-rendering and private class in grails-mail
All this does is bind a mock request and mock response if one doesn't exist
deals with setting the WrappedResponseHolder.wrappedResponse as well
You will need the spring test lib on your main compile.

```compile "org.springframework:spring-test"```
