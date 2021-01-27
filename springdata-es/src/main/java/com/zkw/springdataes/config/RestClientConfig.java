package com.zkw.springdataes.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

/**
 * @author zkw
 * @date 2020-12-25
 **/
@Configuration
public class RestClientConfig extends AbstractElasticsearchConfiguration {
//    @Override
//    public RestHighLevelClient elasticsearchClient() {
//        final ClientConfiguration clientConfiguration = ClientConfiguration
//                .builder()
//                .connectedTo("localhost:9200","localhost:9201")
//                .build();
//
//        return RestClients.create(clientConfiguration).rest();
//    }

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("120.25.220.74:9200")
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}
