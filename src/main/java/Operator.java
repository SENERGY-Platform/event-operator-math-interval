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



import org.infai.ses.senergy.operators.Config;
import org.infai.ses.senergy.operators.Stream;

import java.text.ParseException;

public class Operator {
    public static void main(String[] args) throws ParseException {
        Stream stream  = new Stream();
        Config config = new Config();
        String deprecatedValue = config.getConfigValue("interval", "(*,*)");
        String value = config.getConfigValue("value", deprecatedValue);
        String triggerUrl = config.getConfigValue("url", "");
        String eventId = config.getConfigValue("eventId", "");
        String converterUrl = config.getConfigValue("converterUrl", "");
        String extendedConverterUrl = config.getConfigValue("extendedConverterUrl", "");
        String convertFrom = config.getConfigValue("convertFrom", "");
        String convertTo = config.getConfigValue("convertTo", "");
        String topicToPathAndCharacteristic = config.getConfigValue("topicToPathAndCharacteristic", "");
        String castExtension = config.getConfigValue("castExtensions", "");
        String userToken = config.getConfigValue("userToken", "");

        Converter converter = new Converter(extendedConverterUrl, converterUrl, convertFrom, convertTo, topicToPathAndCharacteristic, castExtension);
        EventMathInterval filter = new EventMathInterval(
                userToken,
                value,
                triggerUrl,
                eventId,
                converter
        );
        stream.start(filter);
    }
}
