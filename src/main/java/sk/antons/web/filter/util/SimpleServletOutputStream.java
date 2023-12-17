/*
 * Copyright 2019 Anton Straka
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
package sk.antons.web.filter.util;

import java.io.IOException;
import java.io.OutputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

/**
 * Simple ServletOutputStream implementation. 
 * Listener mechanism is not implemented.
 * @author antons
 */
public class SimpleServletOutputStream extends ServletOutputStream {
    private OutputStream os;

    public SimpleServletOutputStream(OutputStream os) { this.os = os; }

    public static SimpleServletOutputStream instance(OutputStream os) { return new SimpleServletOutputStream(os); }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

    

}
