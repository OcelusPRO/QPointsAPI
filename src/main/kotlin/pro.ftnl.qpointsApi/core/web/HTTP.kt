package pro.ftnl.qpointsApi.core.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.hsts.*
import org.slf4j.event.Level
import java.time.ZonedDateTime

/**
 * Configure base HTTP server
 */
fun Application.configureHTTP() {
    install(ForwardedHeaders)
    install(HSTS) { includeSubDomains = true }
    install(AutoHeadResponse)

    install(DefaultHeaders) {
        header("Made-By", "OcelusPRO")
        header("Content-Security-Policy", "script-src 'self'")
        header("X-Frame-Options", "SAMEORIGIN")
        header("X-Content-Type-Options", "nosniff")
    }

    install(CallLogging) { level = Level.INFO }

    install(Compression) {
        gzip {
            matchContentType(ContentType.Text.Any)
            priority = 0.9
            minimumSize(1024)
        }
        deflate {
            matchContentType(ContentType.Text.Any)
            priority = 1.0
            minimumSize(1024)
        }
    }
    install(CachingHeaders) {
        options { _, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.JavaScript -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = (86400)),
                    ZonedDateTime.now().plusDays(1)
                )
                ContentType.Text.CSS -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = (86400)),
                    ZonedDateTime.now().plusDays(15)
                )
                ContentType.Text.Html -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = (86400)),
                    ZonedDateTime.now().plusDays(15)
                )
                ContentType.Image.Any -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = (3600)),
                    ZonedDateTime.now().plusDays(15)
                )
                ContentType.Video.Any -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = (7200)),
                    ZonedDateTime.now().plusDays(15)
                )
                else -> null
            }
        }
    }

}
