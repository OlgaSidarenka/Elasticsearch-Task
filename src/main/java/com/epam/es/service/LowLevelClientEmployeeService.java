package com.epam.es.service;

import com.epam.es.dto.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Qualifier("lowLevelClient")
public class LowLevelClientEmployeeService implements EmployeeService {
    private final ObjectMapper objectMapper;
    private final RestClient client;

    @Autowired
    public LowLevelClientEmployeeService(ObjectMapper objectMapper) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "BwX2hfX7pNhHaOr6qbVp"));
        RestClient client = RestClient.builder(
                        new HttpHost("localhost", 9200, "http"))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpClientBuilder;
                }).build();


        this.objectMapper = objectMapper;
        this.client = client;
    }

    @Override
    public List<Employee> findAll() throws IOException {
        Request request = new Request(ServiceConstant.GET_METHOD, ServiceConstant.SEARCH_ENDPOINT);
        request.setJsonEntity("""
                {
                    "query": {
                        "match_all": {}
                    }
                }"""
        );
        String responseBody = EntityUtils.toString(client.performRequest(request).getEntity());
        return getSearchResult(objectMapper.readTree(responseBody));
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) throws IOException {
        Request request = new Request(ServiceConstant.GET_METHOD, ServiceConstant.CRUD_ENDPOINT + id);
        String responseBody = EntityUtils.toString(client.performRequest(request).getEntity());
        JsonNode employeeNode = objectMapper.readTree(responseBody).findPath("_source");
        return getEmployee(employeeNode);
    }

    @Override
    public String deleteEmployee(String id) throws IOException {
        Request request = new Request(ServiceConstant.DELETE_METHOD, ServiceConstant.CRUD_ENDPOINT + id);
        String responseBody = EntityUtils.toString(client.performRequest(request).getEntity());
        return objectMapper.readTree(responseBody).get("result").asText();
    }

    @Override
    public String createEmployee(Employee employee, String id) throws IOException {
        Request request = new Request(ServiceConstant.POST_METHOD, ServiceConstant.CRUD_ENDPOINT + id);
        request.setJsonEntity(objectMapper.convertValue(employee, JsonNode.class).toString());
        String responseBody = EntityUtils.toString(client.performRequest(request).getEntity());
        return objectMapper.readTree(responseBody).get("result").asText();
    }

    @Override
    public List<Employee> searchByField(String fieldName, String fieldValue) throws IOException {
        Request request = new Request(ServiceConstant.POST_METHOD, ServiceConstant.SEARCH_ENDPOINT);
        request.setJsonEntity(String.format("""
                        {
                            "query": {
                                "match": {
                                    "%s": "%s"
                                }
                            }
                        }""",
                fieldName, fieldValue));
        String responseBody = EntityUtils.toString(client.performRequest(request).getEntity());
        return getSearchResult(objectMapper.readTree(responseBody));
    }

    @Override
    public JsonNode aggregateEmployees(String aggrField, String metricType, String metricField) throws IOException {
        Request request = new Request(ServiceConstant.POST_METHOD, ServiceConstant.SEARCH_ENDPOINT);

        request.setJsonEntity(String.format("""
                        {
                            "size": 0,
                            "aggs": {
                                "skills-agg": {
                                    "terms": {
                                        "field": "%s.keyword"
                                    },
                                    "aggs": {
                                        "%s": {
                                            "%s": {
                                                "field": "%s"
                                            }
                                        }
                                    }
                                }
                            }
                        }""",
                aggrField, metricType, metricType, metricField));
        String responseBody = EntityUtils.toString(client.performRequest(request).getEntity());
        return objectMapper.readTree(responseBody).get("aggregations");
    }

    private List<Employee> getSearchResult(JsonNode jsonNode) throws IOException {
        List<JsonNode> employeesNodes = jsonNode.findValue("hits").findPath("hits").findValues("_source");
        List<Employee> employees = new ArrayList<>();
        for (JsonNode employeeNode : employeesNodes) {
            Optional<Employee> employee = getEmployee(employeeNode);
            employee.ifPresent(employees::add);
        }
        return employees;
    }

    private Optional<Employee> getEmployee(JsonNode jsonNode) throws IOException {
        ObjectReader reader = objectMapper.readerFor(new TypeReference<Employee>() {
        });
        Employee employee = reader.readValue(jsonNode);
        return Optional.ofNullable(employee);
    }
}
