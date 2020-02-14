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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.infai.seits.sepl.operators.Input;
import org.infai.seits.sepl.operators.Message;
import org.infai.seits.sepl.operators.OperatorInterface;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;


public class EventMathInterval implements OperatorInterface {
    private Interval interval;
    private String url;
    private String eventId;
    private Converter converter;

    public EventMathInterval(String interval, String url, String eventId, Converter converter) throws ParseException {
        this.interval = new Interval(interval);
        this.url = url;
        this.eventId = eventId;
        this.converter = converter;
    }

    @Override
    public void run(Message message) {
        try{
            Input input = message.getInput("value");
            if(this.operator(input)){
               this.trigger(input);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean operator(Input input){
        return this.interval.check(input.getValue());
    }


    private void trigger(Input input){
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            Object value = input.getValue();
            if(this.converter != null){
                value = this.converter.convert(value);
            }
            JSONObject json = new JSONObject()
                    .put("messageName", this.eventId)
                    .put("all", true)
                    .put("resultEnabled", true)
                    .put("localVariables", new JSONObject()
                            .put("event", new JSONObject()
                                    .put("value", value)
                            )
                    );
            HttpPost request = new HttpPost(this.url);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            CloseableHttpResponse resp = httpClient.execute(request);
            resp.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void config(Message message) {
        message.addInput("value");
    }
}
