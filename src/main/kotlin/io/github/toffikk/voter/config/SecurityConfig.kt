package io.github.toffikk.voter.config

import io.github.toffikk.voter.service.DataService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val dataService: DataService) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // would be good if we auth'd for the admin panel but its not needed rn
        http
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests { auth ->
                auth
                    .anyRequest().permitAll()
            }
            .httpBasic(Customizer.withDefaults())

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            addAllowedOrigin(dataService.frontendAddress.allowedOrigin)
            allowedMethods = listOf("GET", "POST", "OPTIONS")
            allowedHeaders = listOf("X-Voter-Id", "Content-Type")
            allowCredentials = false
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
        return source
    }
}