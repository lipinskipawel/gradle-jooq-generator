package com.github.lipinskipawel

import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

interface Params : BuildServiceParameters {

    val user: Property<String>

    val password: Property<String>
}
