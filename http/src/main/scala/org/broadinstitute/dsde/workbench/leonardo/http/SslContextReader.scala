package org.broadinstitute.dsde.workbench.leonardo
package http

import akka.actor.ActorSystem
import cats.effect.Sync
import com.typesafe.sslconfig.akka.util.AkkaLoggerFactory
import com.typesafe.sslconfig.ssl.{
  ConfigSSLContextBuilder,
  DefaultKeyManagerFactoryWrapper,
  DefaultTrustManagerFactoryWrapper,
  SSLConfigFactory
}
import org.broadinstitute.dsde.workbench.leonardo.dns.ProxyHostnameVerifier

import javax.net.ssl.SSLContext

object SslContextReader {
  def getSSLContext[F[_]: Sync]()(implicit as: ActorSystem): F[SSLContext] = Sync[F].delay {
    val akkaOverrides = as.settings.config.getConfig("akka.ssl-config")

    val defaults = as.settings.config.getConfig("ssl-config")
    val sslConfigSettings = SSLConfigFactory
      .parse(akkaOverrides.withFallback(defaults))
      // Note: this doesn't seem to have any effect. See akka-http issue: <tbd>
      .withHostnameVerifierClass(classOf[ProxyHostnameVerifier])
    val keyManagerAlgorithm = new DefaultKeyManagerFactoryWrapper(sslConfigSettings.keyManagerConfig.algorithm)
    val trustManagerAlgorithm = new DefaultTrustManagerFactoryWrapper(sslConfigSettings.trustManagerConfig.algorithm)

    new ConfigSSLContextBuilder(new AkkaLoggerFactory(as),
                                sslConfigSettings,
                                keyManagerAlgorithm,
                                trustManagerAlgorithm).build()
  }
}
