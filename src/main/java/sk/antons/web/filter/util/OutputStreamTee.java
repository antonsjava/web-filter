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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class for wrapping OutputStream instances. It enable 
 * to read previously written content.
 * @author antons
 */
public class OutputStreamTee extends OutputStream {
    private OutputStream os;
    private ByteArrayOutputStream bos;

    public OutputStreamTee(OutputStream os) { 
        this.os = os; 
        this.bos = new ByteArrayOutputStream();
    }

    public static OutputStreamTee instance(OutputStream os) { return new OutputStreamTee(os); }

    public static OutputStream nullOutputStream() {
        return OutputStream.nullOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
        bos.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        os.write(b);
        bos.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
        bos.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
        bos.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
        bos.close();
    }
    
    public byte[] toByteArray() {
        return bos.toByteArray();
    }
    
    public InputStream toInputStream() {
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
