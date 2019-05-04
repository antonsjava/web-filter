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
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Helper class for wrapping Writer instances. It enable 
 * to read previously written content.
 * @author antons
 */
public class WriterTee extends Writer {
    private Writer writer;
    private ByteArrayOutputStream bos;
    private Writer bwriter;


    public WriterTee(Writer is) { 
        this.writer = writer; 
        bos = new ByteArrayOutputStream();
        try {
            bwriter = new OutputStreamWriter(bos, "utf-8");
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static WriterTee instance(Writer writer) { return new WriterTee(writer); }

    @Override
    public void write(int c) throws IOException {
        writer.write(c);
        bwriter.write(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        writer.write(cbuf);
        bwriter.write(cbuf);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
        bwriter.write(cbuf, off, len);
    }

    @Override
    public void write(String str) throws IOException {
        writer.write(str);
        bwriter.write(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        writer.write(str, off, len);
        bwriter.write(str, off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        writer.append(csq);
        bwriter.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        writer.append(csq, start, end);
        bwriter.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        writer.append(c);
        bwriter.append(c);
        return this;
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
        bwriter.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
        bwriter.close();
    }
    
    public byte[] toByteArray() {
        return bos.toByteArray();
    }
    
    public InputStream toInputStream() {
        return new ByteArrayInputStream(bos.toByteArray());
    }

}
