package com.epam.es.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AverageAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.epam.es.dto.Employee;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Qualifier("javaApiClient")
public class JavaApiClientEmployeeService implements EmployeeService {
    private final ObjectMapper objectMapper;
    private final ElasticsearchClient client;

    @Autowired
    public JavaApiClientEmployeeService(ObjectMapper objectMapper) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "BwX2hfX7pNhHaOr6qbVp"));

        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    //httpClientBuilder.setSSLContext(sslContext);
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpClientBuilder; }).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        this.client = new ElasticsearchClient(transport);
        this.objectMapper = objectMapper;


    }

    @Override
    public List<Employee> findAll() throws IOException {
        Query matchAllQuery = new MatchAllQuery.Builder().build()._toQuery();
        SearchResponse<Employee> response = client.search(s -> s
                        .index(ServiceConstant.INDEX)
                        .query(matchAllQuery),
                Employee.class
        );
        return getSearchResult(response);
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) throws IOException {
        GetResponse<Employee> response = client.get(g -> g.index(ServiceConstant.INDEX).id(id), Employee.class);
        return Optional.ofNullable(response.source());
    }

    @Override
    public String deleteEmployee(String id) throws IOException {
        DeleteResponse response = client.delete(g -> g.index(ServiceConstant.INDEX).id(id));
        return response.result().toString();
    }

    @Override
    public String createEmployee(Employee employee, String id) throws IOException {


        IndexResponse response = client.index(i -> i
                .index(ServiceConstant.INDEX)
                .id(id)
                .document(employee));
        return response.result().toString();
    }

    @Override
    public List<Employee> searchByField(String fieldName, String fieldValue) throws IOException {
        SearchResponse<Employee> response = client.search(s -> s
                        .index(ServiceConstant.INDEX)
                        .query(q -> q
                                .match(t -> t
                                        .field(fieldName)
                                        .query(fieldValue))),
                Employee.class);
        return getSearchResult(response);
    }

    @Override
    public JsonNode aggregateEmployees(String aggrField, String metricType, String metricField) throws IOException {
        Map<String, Aggregation> map = new HashMap<>();

        Aggregation subAggregation = new Aggregation.Builder()
                .avg(new AverageAggregation.Builder().field(metricField).build())
                .build();

        Aggregation aggregation = new Aggregation.Builder()
                .terms(new TermsAggregation.Builder().field(aggrField + ".keyword").build())
                .aggregations(new HashMap<>() {{
                    put("avg_rating", subAggregation);
                }}).build();
        String key = "agg_" + aggrField;
        map.put(key, aggregation);

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(ServiceConstant.INDEX)
                .size(0)
                .aggregations(map)
                .build();

        SearchResponse<Void> response = client.search(searchRequest, Void.class);

        return objectMapper.readTree(response.aggregations().get(key).toString().replace("Aggregate: ", ""));
    }

    private List<Employee> getSearchResult(SearchResponse<Employee> response) {
        List<Hit<Employee>> hits = response.hits().hits();
        List<Employee> employees = new ArrayList<>();
        for (Hit<Employee> hit : hits) {
            employees.add(hit.source());
        }
        return employees;
    }
}
