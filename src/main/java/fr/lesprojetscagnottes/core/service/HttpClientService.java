package fr.lesprojetscagnottes.core.service;

import fr.lesprojetscagnottes.core.common.StringsCommon;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

@Service
public class HttpClientService {

    private static final String HTTP_PROXY = System.getenv("HTTP_PROXY");

    public HttpClient getHttpClient() {

        HttpClient httpClient;

        if(HTTP_PROXY != null) {
            String[] proxy = HTTP_PROXY.replace("http://", StringsCommon.EMPTY_STRING).replace("https://", StringsCommon.EMPTY_STRING).split(":");
            httpClient = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress(proxy[0], Integer.parseInt(proxy[1]))))
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        } else {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        }

        return httpClient;
    }

}
