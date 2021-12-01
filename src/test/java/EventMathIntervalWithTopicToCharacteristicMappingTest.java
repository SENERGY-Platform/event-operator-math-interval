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

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.infai.ses.senergy.models.DeviceMessageModel;
import org.infai.ses.senergy.models.MessageModel;
import org.infai.ses.senergy.operators.Config;
import org.infai.ses.senergy.operators.Helper;
import org.infai.ses.senergy.operators.Message;
import org.infai.ses.senergy.testing.utils.JSONHelper;
import org.infai.ses.senergy.utils.ConfigProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class EventMathIntervalWithTopicToCharacteristicMappingTest {
    public static boolean called = false;
    private static Object processVariable = null;
    private static String topicToPathAndCharacteristic = "{\"test\":[{\"json_path\":\"\",\"characteristic_id\":\"inCharacteristic\"}]}";

    private Object jsonNormalize(Object in) throws ParseException {
        Map<String, Object> wrapper = new HashMap<String, Object>();
        wrapper.put("value", in);
        JSONObject temp = new JSONObject(wrapper);
        Object candidate = ((JSONObject)(new JSONParser().parse(temp.toJSONString()))).get("value");
        if(candidate instanceof Long){
            candidate = Double.valueOf((Long)candidate);
        }
        if(candidate instanceof String){
            candidate = Double.valueOf((String)candidate);
        }
        return candidate;
    }

    private void test(String interval, Object messageValue, boolean expectedToTrigger) throws IOException, java.text.ParseException {
        EventMathIntervalWithTopicToCharacteristicMappingTest.called = false;
        HttpServer triggerServer = TriggerServerMock.create(inputStream -> {
            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
                if(
                        jsonObject.containsKey("processVariablesLocal")
                        && ((JSONObject)jsonObject.get("processVariablesLocal")).containsKey("event")
                        && ((JSONObject)((JSONObject)jsonObject.get("processVariablesLocal")).get("event")).containsKey("value")
                ){
                    EventMathIntervalWithTopicToCharacteristicMappingTest.called = true;
                    EventMathIntervalWithTopicToCharacteristicMappingTest.processVariable = ((JSONObject)((JSONObject)jsonObject.get("processVariablesLocal")).get("event")).get("value");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        HttpServer converterServer = ConverterServerMock.createWithResponse("/inCharacteristic/outCharacteristic", new Gson().toJson(messageValue));
        Converter converter = new Converter("http://localhost:"+converterServer.getAddress().getPort(), "", "outCharacteristic", topicToPathAndCharacteristic);
        EventMathInterval events = new EventMathInterval("", interval, "http://localhost:"+triggerServer.getAddress().getPort()+"/endpoint", "test", converter);
        Config config = new Config(new JSONHelper().parseFile("config.json").toString());
        ConfigProvider.setConfig(config);
        MessageModel model = new MessageModel();
        Message message = new Message();
        events.configMessage(message);
        JSONObject m = new JSONHelper().parseFile("message.json");
        ((JSONObject)((JSONObject) m.get("value")).get("reading")).put("value", "999999");
        DeviceMessageModel deviceMessageModel = JSONHelper.getObjectFromJSONString(m.toString(), DeviceMessageModel.class);
        assert deviceMessageModel != null;
        String topicName = config.getInputTopicsConfigs().get(0).getName();
        model.putMessage(topicName, Helper.deviceToInputMessageModel(deviceMessageModel, topicName));
        message.setMessage(model);
        events.run(message);
        triggerServer.stop(0);
        converterServer.stop(0);
        Assert.assertEquals(EventMathIntervalWithTopicToCharacteristicMappingTest.called, expectedToTrigger);
        if(expectedToTrigger){
            try {
                Object a = jsonNormalize(EventMathIntervalWithTopicToCharacteristicMappingTest.processVariable);
                Object b = jsonNormalize(messageValue);
                Assert.assertEquals(a, b);
            } catch (ParseException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void testSimple() throws IOException, java.text.ParseException {
        test("(0, 8)", 1, true);
    }

    @Test
    public void testOpenInt() throws IOException, java.text.ParseException {
        test("(0, 8)", -1, false);
        test("(0, 8)", 0, false);
        test("(0, 8)", 1, true);
        test("(0, 8)", 2, true);
        test("(0, 8)", 3, true);
        test("(0, 8)", 4, true);
        test("(0, 8)", 5, true);
        test("(0, 8)", 6, true);
        test("(0, 8)", 7, true);
        test("(0, 8)", 8, false);
        test("(0, 8)", 9, false);
    }

    @Test
    public void testOpen2Int() throws IOException, java.text.ParseException {
        test("]0, 8[", -1, false);
        test("]0, 8[", 0, false);
        test("]0, 8[", 1, true);
        test("]0, 8[", 2, true);
        test("]0, 8[", 3, true);
        test("]0, 8[", 4, true);
        test("]0, 8[", 5, true);
        test("]0, 8[", 6, true);
        test("]0, 8[", 7, true);
        test("]0, 8[", 8, false);
        test("]0, 8[", 9, false);
    }

    @Test
    public void testClosedInt() throws IOException, java.text.ParseException {
        test("[0, 8]", -1, false);
        test("[0, 8]", 0, true);
        test("[0, 8]", 1, true);
        test("[0, 8]", 2, true);
        test("[0, 8]", 3, true);
        test("[0, 8]", 4, true);
        test("[0, 8]", 5, true);
        test("[0, 8]", 6, true);
        test("[0, 8]", 7, true);
        test("[0, 8]", 8, true);
        test("[0, 8]", 9, false);
    }

    @Test
    public void testClosedFloat() throws IOException, java.text.ParseException {
        test("[1.3, 2.4]", 0, false);
        test("[1.3, 2.4]", 1, false);
        test("[1.3, 2.4]", 1.2, false);
        test("[1.3, 2.4]", 1.3, true);
        test("[1.3, 2.4]", 1.4, true);
        test("[1.3, 2.4]", 2, true);
        test("[1.3, 2.4]", 2.4, true);
        test("[1.3, 2.4]", 2.5, false);
        test("[1.3, 2.4]", 3, false);
        test("[1.3, 2.4]", 4, false);
        test("[1.3, 2.4]", 5, false);
    }

    @Test
    public void testOpenFloat() throws IOException, java.text.ParseException {
        test("(1.3, 2.4)", 0, false);
        test("(1.3, 2.4)", 1, false);
        test("(1.3, 2.4)", 1.2, false);
        test("(1.3, 2.4)", 1.3, false);
        test("(1.3, 2.4)", 1.4, true);
        test("(1.3, 2.4)", 2, true);
        test("(1.3, 2.4)", 2.4, false);
        test("(1.3, 2.4)", 2.5, false);
        test("(1.3, 2.4)", 3, false);
        test("(1.3, 2.4)", 4, false);
        test("(1.3, 2.4)", 5, false);
    }

    @Test
    public void testOpen2Float() throws IOException, java.text.ParseException {
        test("]1.3, 2.4[", 0, false);
        test("]1.3, 2.4[", 1, false);
        test("]1.3, 2.4[", 1.2, false);
        test("]1.3, 2.4[", 1.3, false);
        test("]1.3, 2.4[", 1.4, true);
        test("]1.3, 2.4[", 2, true);
        test("]1.3, 2.4[", 2.4, false);
        test("]1.3, 2.4[", 2.5, false);
        test("]1.3, 2.4[", 3, false);
        test("]1.3, 2.4[", 4, false);
        test("]1.3, 2.4[", 5, false);
    }

    @Test
    public void testOpenInfinity() throws IOException, java.text.ParseException {
        test("(*, *)", 0, true);
        test("(*, *)", 1, true);
        test("(*, *)", 1.2, true);
        test("(*, *)", 1.3, true);
        test("(*, *)", 1.4, true);
        test("(*, *)", 2, true);
        test("(*, *)", 2.4, true);
        test("(*, *)", 2.5, true);
        test("(*, *)", 3, true);
        test("(*, *)", 4, true);
        test("(*, *)", 5, true);
    }

    @Test
    public void testOpen2Infinity() throws IOException, java.text.ParseException {
        test("]*, *[", 0, true);
        test("]*, *[", 1, true);
        test("]*, *[", 1.2, true);
        test("]*, *[", 1.3, true);
        test("]*, *[", 1.4, true);
        test("]*, *[", 2, true);
        test("]*, *[", 2.4, true);
        test("]*, *[", 2.5, true);
        test("]*, *[", 3, true);
        test("]*, *[", 4, true);
        test("]*, *[", 5, true);
    }

    @Test
    public void testClosedInfinity() throws IOException, java.text.ParseException {
        test("[*, *]", 0, true);
        test("[*, *]", 1, true);
        test("[*, *]", 1.2, true);
        test("[*, *]", 1.3, true);
        test("[*, *]", 1.4, true);
        test("[*, *]", 2, true);
        test("[*, *]", 2.4, true);
        test("[*, *]", 2.5, true);
        test("[*, *]", 3, true);
        test("[*, *]", 4, true);
        test("[*, *]", 5, true);
    }

}
