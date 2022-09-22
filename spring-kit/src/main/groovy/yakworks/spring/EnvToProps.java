/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

/**
 * Helper to lookup includes for map or list based api, usually a json and rest based api.
 * look on the static includes field of the Class first and look for config overrides
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
public class EnvToProps {

    public static Properties toProperties(Environment environment){
        Properties props = new Properties();
        MutablePropertySources propSrcs = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
            .filter(ps -> ps instanceof EnumerablePropertySource)
            .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
            .flatMap(Arrays::<String>stream)
            .forEach(propName -> {
                try {
                    props.setProperty(propName, environment.getProperty(propName));
                } catch (Exception e){

                }
            });

        return props;
    }

}
