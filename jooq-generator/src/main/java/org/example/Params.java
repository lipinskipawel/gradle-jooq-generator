package org.example;

import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildServiceParameters;

public interface Params extends BuildServiceParameters {

    Property<String> getUser();

    Property<String> getPassword();
}
