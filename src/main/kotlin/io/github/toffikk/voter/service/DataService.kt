package io.github.toffikk.voter.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import java.io.File

@Service
class DataService {
    private final val config = File("config")
    private final val file = File(this.config, "frontend.yml")
    private final val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    lateinit var frontendAddress: FrontendAddress

    data class FrontendAddress(val allowedOrigin: String = "https://sejmik-nkw.vercel.app")

    @PostConstruct
    fun init() {
        this.config.mkdirs()
        if (!this.file.exists()) {
            this.createDefaultFile()
        }
        frontendAddress = mapper.readValue(file, FrontendAddress::class.java)
    }

    private final fun createDefaultFile() {
        val default = FrontendAddress()
        this.mapper.writeValue(this.file, default)
    }
}