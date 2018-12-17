/*
 * Copyright 2015 The Baseio Project
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
package test.others;

import java.io.File;

import com.firenio.baseio.common.Encoding;
import com.firenio.baseio.common.FileUtil;
import com.firenio.baseio.common.KMPByteUtil;

import test.test.ITest;
import test.test.ITestHandle;

public class TestKMPHttpHeader {

    public static void main(String[] args) throws Exception {

        final KMPByteUtil KMP_HEADER = new KMPByteUtil("\r\n\r\n".getBytes());

        File file = new File("test.header");

        String content = FileUtil.readStringByFile(file, Encoding.UTF8);

        final byte[] array = content.getBytes();

        ITestHandle.doTest(new ITest() {

            @Override
            public void test(int i) throws Exception {

                KMP_HEADER.match(array);
            }
        }, 1000000, "kmp-http-header");
    }
}
