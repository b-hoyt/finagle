package com.twitter.finagle.ssl

import javax.net.ssl.{SSLContext, SSLEngine}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SslConfigurationsTest extends FunSuite {

  private[this] def createTestEngine(): SSLEngine = {
    val sslContext = SSLContext.getInstance("TLSv1.2")
    sslContext.init(null, null, null)
    sslContext.createSSLEngine()
  }

  test("initializeSslContext succeeds for defaults") {
    val sslContext = SslConfigurations.initializeSslContext(
      KeyCredentials.Unspecified, TrustCredentials.Unspecified)
    assert(sslContext != null)
    assert(sslContext.getProtocol == "TLSv1.2")
  }

  test("configureCipherSuites succeeds with good suites") {
    val sslEngine = createTestEngine()
    val cipherSuites = CipherSuites.Enabled(Seq("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"))
    SslConfigurations.configureCipherSuites(sslEngine, cipherSuites)

    val enabled = sslEngine.getEnabledCipherSuites()
    assert(enabled.length == 1)
    assert(enabled(0) == "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384")
  }

  test("configureCipherSuites fails with bad suites") {
    val sslEngine = createTestEngine()
    val cipherSuites = CipherSuites.Enabled(Seq("TLS_ECDHE_ECDSA_WITH_AES_102_CBC_SHA496"))

    intercept[IllegalArgumentException] {
      SslConfigurations.configureCipherSuites(sslEngine, cipherSuites)
    }
  }

  test("configureProtocols succeeds with good protocols") {
    val sslEngine = createTestEngine()
    val protocols = Protocols.Enabled(Seq("TLSv1.2"))
    SslConfigurations.configureProtocols(sslEngine, protocols)

    val enabled = sslEngine.getEnabledProtocols()
    assert(enabled.length == 1)
    assert(enabled(0) == "TLSv1.2")
  }

  test("configureProtocols fails with bad protocols") {
    val sslEngine = createTestEngine()
    val protocols = Protocols.Enabled(Seq("TLSv2.0"))

    intercept[IllegalArgumentException] {
      SslConfigurations.configureProtocols(sslEngine, protocols)
    }
  }

  test("checkKeyCredentialsNotSupported does nothing for Unspecified") {
    val keyCredentials = KeyCredentials.Unspecified
    SslConfigurations.checkKeyCredentialsNotSupported("TestFactory", keyCredentials)
  }

  test("checkKeyCredentialsNotSupported throws for CertAndKey") {
    val keyCredentials = KeyCredentials.CertAndKey(null, null)
    val ex = intercept[SslConfigurationException] {
      SslConfigurations.checkKeyCredentialsNotSupported("TestFactory", keyCredentials)
    }
    assert("KeyCredentials.CertAndKey is not supported at this time for TestFactory" ==
      ex.getMessage)
  }

  test("checkKeyCredentialsNotSupported throws for CertKeyAndChain") {
    val keyCredentials = KeyCredentials.CertKeyAndChain(null, null, null)
    val ex = intercept[SslConfigurationException] {
      SslConfigurations.checkKeyCredentialsNotSupported("TestFactory", keyCredentials)
    }
    assert("KeyCredentials.CertKeyAndChain is not supported at this time for TestFactory" ==
      ex.getMessage)
  }

  test("checkTrustCredentialsNotSupported does nothing for Unspecified") {
    val trustCredentials = TrustCredentials.Unspecified
    SslConfigurations.checkTrustCredentialsNotSupported("TestFactory", trustCredentials)
  }

  test("checkTrustCredentialsNotSupported throws for Insecure") {
    val trustCredentials = TrustCredentials.Insecure
    val ex = intercept[SslConfigurationException] {
      SslConfigurations.checkTrustCredentialsNotSupported("TestFactory", trustCredentials)
    }
    assert("TrustCredentials.Insecure is not supported at this time for TestFactory" ==
      ex.getMessage)
  }

  test("checkTrustCredentialsNotSupported throws for CertCollection") {
    val trustCredentials = TrustCredentials.CertCollection(null)
    val ex = intercept[SslConfigurationException] {
      SslConfigurations.checkTrustCredentialsNotSupported("TestFactory", trustCredentials)
    }
    assert("TrustCredentials.CertCollection is not supported at this time for TestFactory" ==
      ex.getMessage)
  }

  test("checkApplicationProtocolsNotSupported does nothing for Unspecified") {
    val appProtocols = ApplicationProtocols.Unspecified
    SslConfigurations.checkApplicationProtocolsNotSupported("TestFactory", appProtocols)
  }

  test("checkApplicationProtocolsNotSupported throws for Supported") {
    val appProtocols = ApplicationProtocols.Supported(Seq("h2"))
    val ex = intercept[SslConfigurationException] {
      SslConfigurations.checkApplicationProtocolsNotSupported("TestFactory", appProtocols)
    }
    assert("ApplicationProtocols.Supported is not supported at this time for TestFactory" ==
      ex.getMessage)
  }

}
