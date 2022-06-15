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

import com.sun.net.httpserver.HttpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


public class ConverterTest {


    private void test(Object in) throws IOException, java.text.ParseException, InterruptedException {
        HttpServer server = ConverterServerMock.create("/inCharacteristic/outCharacteristic");
        String mockUrl = "http://localhost:"+server.getAddress().getPort();
        Converter converter = new Converter(mockUrl, mockUrl, "inCharacteristic", "outCharacteristic");
        Object out = converter.convert(in);
        Assert.assertEquals(in, out);
        server.stop(0);
    }

    private void testExtended(Object in) throws IOException, java.text.ParseException, InterruptedException {
        HttpServer server = ConverterServerMock.create("/inCharacteristic/outCharacteristic");
        String mockUrl = "http://localhost:"+server.getAddress().getPort();
        String castExtension = "[{\"from\":\"foo\",\"to\":\"bar\",\"distance\":0,\"formula\":\"42\",\"placeholder_name\":\"\"}]";
        Converter converter = new Converter(mockUrl, mockUrl, "inCharacteristic", "outCharacteristic", "", castExtension);
        Object out = converter.convert(in);
        ArrayList<Object> extensions = new ArrayList();
        LinkedHashMap<String, Object> extension = new LinkedHashMap<String, Object>();
        extension.put("from", "foo");
        extension.put("to",  "bar");
        extension.put("distance", 0);
        extension.put("formula", "42");
        extension.put("placeholder_name", "");
        extensions.add(extension);
        LinkedHashMap<String, Object> expected = new LinkedHashMap<String, Object>();
        expected.put("input", in);
        expected.put("extensions", extensions);
        Assert.assertEquals(expected, out);
        server.stop(0);
    }

    @Test
    public void string() throws IOException, ParseException, InterruptedException {
        this.test("str");
    }

    @Test
    public void number() throws IOException, ParseException, InterruptedException {
        this.test(42);
    }


    @Test
    public void stringWithExtension() throws IOException, ParseException, InterruptedException {
        this.testExtended("str");
    }

    @Test
    public void numberWithExtension() throws IOException, ParseException, InterruptedException {
        this.testExtended(42);
    }
}