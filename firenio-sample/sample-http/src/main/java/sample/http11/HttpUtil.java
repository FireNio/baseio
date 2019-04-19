/*
 * Copyright 2015 The FireNio Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.http11;

import com.firenio.codec.http11.WebSocketFrame;
import com.firenio.component.Frame;
import com.firenio.component.Channel;

public class HttpUtil {

    public static final String HTML_BOTTOM;

    public static final String HTML_HEADER;

    public static final String HTML_POWER_BY;

    static {

        StringBuilder builder = new StringBuilder();

        builder.append("<!DOCTYPE html>\n");
        builder.append("<html lang=\"en\">\n");
        builder.append("	<head>\n");
        builder.append("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
        builder.append("		<meta name=\"viewport\" content=\"initial-scale=1, maximum-scale=3, minimum-scale=1, user-scalable=no\">\n");
        builder.append("		<title>firenio</title>\n");
        builder.append("		<style type=\"text/css\"> \n");
        builder.append("			p {margin:15px;}\n");
        builder.append("			a:link { color:#F94F4F;  }\n");
        builder.append("			a:visited { color:#F94F4F; }\n");
        builder.append("			a:hover { color:#000000; }\n");
        builder.append("		</style>\n");
        builder.append("	</head>\n");
        builder.append("	<body style=\"font-family:Georgia;\">\n");
        HTML_HEADER = builder.toString();

        builder = new StringBuilder();
        builder.append("\t\t<hr>\n");
        builder.append("\t\t<p style=\"color: #FDA58C\">\n");
        builder.append("\t\t	Powered by firenio@\n");
        builder.append("\t\t	<a style=\"color:#F94F4F;\" href=\"https://github.com/firenio/firenio#readme\">\n");
        builder.append("\t\t		https://github.com/firenio/firenio\n");
        builder.append("\t\t	</a>\n");
        builder.append("\t\t</p>\n");
        HTML_POWER_BY = builder.toString();

        builder = new StringBuilder();
        builder.append("	</body>\n");
        builder.append("</html>");

        HTML_BOTTOM = builder.toString();
    }

    public static String getFrameName(Channel ch, Frame f) {
        if (f instanceof WebSocketFrame) {
            return ((WebSocketFrame) f).getFrameName(ch);
        }
        return f.getFrameName();
    }

}
