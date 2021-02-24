package com.enliple.pudding.commons.chat.socket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

class WebsocketTransport extends WebSocketClient implements IOTransport {
    private final static Pattern PATTERN_HTTP = Pattern.compile("^http");
    public static final String TRANSPORT_NAME = "websocket";
    private IOConnection connection;

    public static IOTransport create(URL url, IOConnection connection) {
        URI uri = URI.create(
                PATTERN_HTTP.matcher(url.toString()).replaceFirst("ws")
                        + IOConnection.SOCKET_IO_1 + TRANSPORT_NAME
                        + "/" + connection.getSessionId());

        return new WebsocketTransport(uri, connection);
    }

    public WebsocketTransport(URI uri, IOConnection connection) {
        super(uri);

        this.setConnectionLostTimeout(0); // 매우 중요.. https://github.com/TooTallNate/Java-WebSocket/issues/560

        this.connection = connection;
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, null, null);
            if ("wss".equals(uri.getScheme()) && ctx != null) {
                //this.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(ctx));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see IOTransport#disconnect()
     */
    @Override
    public void disconnect() {
        try {
            this.close();
        } catch (Exception e) {
            connection.transportError(e);
        }
    }

    /* (non-Javadoc)
     * @see IOTransport#canSendBulk()
     */
    @Override
    public boolean canSendBulk() {
        return false;
    }

    /* (non-Javadoc)
     * @see IOTransport#sendBulk(java.lang.String[])
     */
    @Override
    public void sendBulk(String[] texts) throws IOException {
        throw new RuntimeException("Cannot send Bulk!");
    }

    /* (non-Javadoc)
     * @see IOTransport#invalidate()
     */
    @Override
    public void invalidate() {
        connection = null;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
//        IOConnection.logger.warning("WebsocketTransport--onClose");
        if (connection != null)
            connection.transportDisconnected();
    }

    @Override
    public void onMessage(String text) {
//        IOConnection.logger.warning("WebsocketTransport--onMessage: " + text);
        if (connection != null)
            connection.transportMessage(text);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
//        IOConnection.logger.warning("WebsocketTransport--onOpen: " + handshakedata.getHttpStatus() + ", " + handshakedata.getHttpStatusMessage());
        if (connection != null)
            connection.transportConnected();
    }

    @Override
    public String getName() {
        return TRANSPORT_NAME;
    }

    @Override
    public void onError(Exception ex) {
//        IOConnection.logger.warning("WebsocketTransport--onError: " + ex.getMessage());
    }
}