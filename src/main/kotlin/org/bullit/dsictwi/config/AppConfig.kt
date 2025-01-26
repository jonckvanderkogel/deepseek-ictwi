package org.bullit.dsictwi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppConfig (
    val ngramSize: Int
)