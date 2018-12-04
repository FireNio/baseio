/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.codec.http11;

/**
 * @author wangkai
 *
 */
public enum HttpVersion {

    HTTP1_0(1, "HTTP/1.0"), HTTP1_1(2, "HTTP/1.1"), OTHER(0, "OTHER");

    private final String value;

    private final int    id;

    private final byte[] bytes;

    private HttpVersion(int id, String value) {
        this.id = id;
        this.value = value;
        this.bytes = value.getBytes();
    }

    public String getValue() {
        return value;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getId() {
        return id;
    }

    private static final HttpVersion[] enums;

    static {
        enums = new HttpVersion[values().length];
        for (HttpVersion m : values()) {
            enums[m.id] = m;
        }
    }

    public static HttpVersion getMethod(int id) {
        return enums[id];
    }

    public static HttpVersion get(String version) {
        if (HTTP1_1.value.equals(version)) {
            return HTTP1_1;
        } else if (HTTP1_0.value.equals(version)) {
            return HTTP1_0;
        } else {
            return HttpVersion.OTHER;
        }
    }

}
