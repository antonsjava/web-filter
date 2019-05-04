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
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/**
 * Helper class for wrapping ServletResponse instances. It enable 
 * to read previously written content.
 * @author antons
 */
public class ServletResponseWrapper implements ServletResponse {
    private ServletResponse response;

    public ServletResponseWrapper(ServletResponse response) { this.response = response; }

    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    private OutputStreamTee ostee = null;
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if(ostee == null) ostee = new OutputStreamTee(response.getOutputStream());
        return new SimpleServletOutputStream(ostee);
    }

    private WriterTee wtee = null;
    @Override
    public PrintWriter getWriter() throws IOException {
        if(wtee == null) wtee = new WriterTee(response.getWriter());
        return new PrintWriter(wtee);
    }

    public InputStream getContentInputStream() {
        if(ostee != null) {
            return ostee.toInputStream();
        }
        if(wtee != null) {
            return wtee.toInputStream();
        }
        return null;
    }

    @Override
    public void setCharacterEncoding(String string) {
        response.setCharacterEncoding(string);
    }

    @Override
    public void setContentLength(int i) {
        response.setContentLength(i);
    }

    @Override
    public void setContentType(String string) {
        response.setContentType(string);
    }

    @Override
    public void setBufferSize(int i) {
        response.setBufferSize(i);
    }

    @Override
    public int getBufferSize() {
        return response.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        response.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        response.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

    @Override
    public void reset() {
        response.reset();
    }

    @Override
    public void setLocale(Locale locale) {
        response.setLocale(locale);
    }

    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    @Override
    public void setContentLengthLong(long len) {
        response.setContentLengthLong(len);
    }


    
}
