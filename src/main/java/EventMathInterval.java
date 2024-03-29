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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.infai.ses.senergy.exceptions.NoValueException;
import org.infai.ses.senergy.operators.BaseOperator;
import org.infai.ses.senergy.operators.FlexInput;
import org.infai.ses.senergy.operators.Input;
import org.infai.ses.senergy.operators.Message;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;


public class EventMathInterval extends BaseOperator {
    private Interval interval;
    private String url;
    private String eventId;
    private ConverterInterface converter;
    private String userToken;

    public EventMathInterval(String userToken,String interval, String url, String eventId, ConverterInterface converter) throws ParseException {
        this.interval = new Interval(interval);
        this.url = url;
        this.eventId = eventId;
        this.converter = converter;
        this.userToken = userToken;
    }

    @Override
    public void run(Message message) {
        try{
            FlexInput input = message.getFlexInput("value");
            Object value = this.getValueOfInput(input);
            if(this.operator(value)){
               this.trigger(value);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Object getValueOfInput(FlexInput input) throws Exception {
        if(this.converter != null){
            return this.converter.convert(input, input.getValue());
        } else {
            return input.getValue();
        }
    }

    private boolean operator(Object value){
        if (value instanceof Double) {
            return this.interval.check((double)value);
        }
        if (value instanceof Integer) {
            return this.interval.check(Double.valueOf((int)value));
        }
        return false;
    }


    private void trigger(Object value){
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        try {
            JSONObject json = new JSONObject()
                    .put("messageName", this.eventId)
                    .put("all", true)
                    .put("resultEnabled", false)
                    .put("processVariablesLocal", new JSONObject()
                            .put("event", new JSONObject()
                                    .put("value", value)
                            )
                    );
            HttpPost request = new HttpPost(this.url);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            if (!this.userToken.equals("")) {
                request.addHeader("Authorization", userToken);
            }
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
    public Message configMessage(Message message) {
        message.addFlexInput("value");
        return message;
    }
}
