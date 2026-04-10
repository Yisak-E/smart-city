import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smart_city.Alpn;
import tech.kwik.core.log.SysOutLogger;
import tech.kwik.core.server.ServerConnectionConfig;
import tech.kwik.core.server.ServerConnector;

public class Broker {

	public static final int PORT = 8443;
	private static final String KEYSTORE_FILE = "cert.jks";
	private static final String KEYSTORE_PASSWORD = Optional.ofNullable(System.getenv("KEYSTORE_PASSWORD"))
			.orElseThrow(() -> new IllegalStateException("Environment variable 'KEYSTORE_PASSWORD' is not set"));
	private static final String CERT_ALIAS = "servercert";
	private static final Logger logger = LoggerFactory.getLogger(Broker.class);

	public static void main(String[] args) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		
		try (FileInputStream fis = new FileInputStream(KEYSTORE_FILE)) {
			keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
		}
		
		
		// logger object for the server logs
		SysOutLogger sysLogger = new SysOutLogger();
		sysLogger.logInfo(false);
		sysLogger.logWarning(false);

		ServerConnectionConfig config = ServerConnectionConfig.builder()
				.maxOpenPeerInitiatedBidirectionalStreams(50)
				.maxOpenPeerInitiatedUnidirectionalStreams(0)
				.build();
		
		// build the QUIC server connector
		try (ServerConnector connector = ServerConnector.builder()
				.withPort(PORT)
				.withKeyStore(keyStore, CERT_ALIAS, KEYSTORE_PASSWORD.toCharArray())
				.withConfiguration(config)
				.withLogger(sysLogger)
				.build()) {

			// register the application protocol and connection factory
			connector.registerApplicationProtocol(Alpn.PROTOCOL, new QuicProtocolFactory());

			connector.start();

			logger.info("Server is running now on port {}", PORT);
		}
	}
}
