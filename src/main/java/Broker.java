import java.io.FileInputStream;
import java.security.KeyStore;

import smart_city.Alpn;
import tech.kwik.core.log.SysOutLogger;
import tech.kwik.core.server.ServerConnectionConfig;
import tech.kwik.core.server.ServerConnector;

public class Broker {

	public static final int PORT = 4433;
	private static final String KEYSTORE_FILE = "cert.jks";
	private static final String KEYSTORE_PASSWORD="secret";
	private static final String CERT_ALIAS = "servercert";
	
	public static void main(String[] args) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		
		try(FileInputStream fis = new FileInputStream(KEYSTORE_FILE)){
			keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
		}
		
		
		// logger object for the server logs
		SysOutLogger logger = new SysOutLogger();
		logger.logInfo(false);
		logger.logWarning(false);
		
		ServerConnectionConfig config = ServerConnectionConfig.builder()
				.maxOpenPeerInitiatedBidirectionalStreams(50)
				.maxOpenPeerInitiatedUnidirectionalStreams(0)
				.build();
		
		// build the QUIC server connector
		ServerConnector connector = ServerConnector.builder()
				.withPort(PORT)
				.withKeyStore(keyStore, CERT_ALIAS, KEYSTORE_PASSWORD.toCharArray())
				.withConfiguration(config)
				.withLogger(logger)
				.build();
		
		// register the application protocol and connection factory
		connector.registerApplicationProtocol(Alpn.PROTOCOL, new QuicProtocolFactory());
		
		connector.start();
		
		System.out.println("server is running now on port "+ PORT);
		
		
	}
	
}
