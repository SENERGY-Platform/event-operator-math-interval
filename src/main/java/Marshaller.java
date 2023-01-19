/*
 * Copyright 2020 InfAI (CC SES)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.infai.ses.senergy.operators.FlexInput;
import org.infai.ses.senergy.operators.Helper;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Marshaller implements ConverterInterface {
    private String url;
    private String functionId;
    private String aspectId;
    private String path;
    private String userToken;
    private String characteristicId;
    private Map<String, String> topicToServiceId;
    private boolean debug;

    public Marshaller(String url, String userToken, String functionId, String aspectId, String path, String characteristicId, String topicToServiceId) {
        this.debug = Boolean.parseBoolean(Helper.getEnv("DEBUG", "false"));
        if(this.debug){
            System.out.println("DEBUG: start with debug log");
        }
        this.url = url;
        this.userToken = userToken;
        this.functionId = functionId;
        this.aspectId = aspectId;
        this.path = path;
        this.characteristicId = characteristicId;
        this.topicToServiceId = parseTopicToServiceId(topicToServiceId);
    }

    private Map<String,String> parseTopicToServiceId(String topicToServiceId){
        topicToServiceId = topicToServiceId.trim();
        if (topicToServiceId.equals("")) {
            return new HashMap<String,String>();
        }
        Gson g = new Gson();
        Type mapType = new TypeToken<Map<String,String>>() {}.getType();
        return g.fromJson(topicToServiceId, mapType);
    }

    public Object convert(FlexInput input, Object value) throws Exception{
        if(this.debug){
            System.out.println("DEBUG: convert " + value.toString());
        }
        String topic = input.getCurrentInputTopic();
        String serviceId = this.topicToServiceId.getOrDefault(topic, topic);

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("characteristic_id", this.characteristicId);
        payload.put("path", this.path);
        payload.put("function_id", this.functionId);
        payload.put("aspect_node_id", this.aspectId);
        payload.put("serialized_output", value);

        String payloadString = this.objToJsonStr(payload);
        StringEntity entity = new StringEntity(payloadString);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String endpoint = this.url + "/v2/unmarshal/" + URLEncoder.encode(serviceId, StandardCharsets.UTF_8.toString());
        HttpPost request = new HttpPost(endpoint);

        request.addHeader("content-type", "application/json");
        if (!this.userToken.equals("")) {
            request.addHeader("Authorization", this.userToken);
        }
        request.setEntity(entity);
        request.addHeader("content-type", "application/json");

        if(this.debug){System.out.println();
            System.out.println("DEBUG: marshal request = " + endpoint + "\n " + payloadString );
        }

        CloseableHttpResponse resp = httpClient.execute(request);
        String respStr = new BasicResponseHandler().handleResponse(resp);
        if(this.debug){
            System.out.println("DEBUG: marshal resp = " + resp.getStatusLine().getStatusCode() + " " + respStr );
        }

        return this.jsonStrToObject(respStr);
    }

    private String objToJsonStr(Object in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, in);
        return writer.toString();
    }

    private Object jsonStrToObject(String in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(in, Object.class);
    }
}