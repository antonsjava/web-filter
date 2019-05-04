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
import java.util.Collection;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper class for wrapping HttpServletResponse instances. It enable 
 * to read previously written content.
 * @author antons
 */
public class HttpServletResponseWrapper extends ServletResponseWrapper implements HttpServletResponse {
    private HttpServletResponse response;

    public HttpServletResponseWrapper(HttpServletResponse response) { 
        super(response);
        this.response = response; 
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String string) {
        return response.containsHeader(string);
    }

    @Override
    public String encodeURL(String string) {
        return response.encodeURL(string);
    }

    @Override
    public String encodeRedirectURL(String string) {
        return response.encodeRedirectURL(string);
    }

    @Override
    public String encodeUrl(String string) {
        return response.encodeUrl(string);
    }

    @Override
    public String encodeRedirectUrl(String string) {
        return response.encodeRedirectUrl(string);
    }

    @Override
    public void sendError(int i, String string) throws IOException {
        response.sendError(i, string);
    }

    @Override
    public void sendError(int i) throws IOException {
        response.sendError(i);
    }

    @Override
    public void sendRedirect(String string) throws IOException {
        response.sendRedirect(string);
    }

    @Override
    public void setDateHeader(String string, long l) {
        //setHeaderImpl(string, l);
        response.setDateHeader(string, l);
    }

    @Override
    public void addDateHeader(String string, long l) {
        //addHeaderImpl(string, l);
        response.addDateHeader(string, l);
    }

    @Override
    public void setHeader(String string, String string1) {
        //setHeaderImpl(string, string1);
        response.setHeader(string, string1);
    }

    @Override
    public void addHeader(String string, String string1) {
        //addHeaderImpl(string, string1);
        response.addHeader(string, string1);
    }

    @Override
    public void setIntHeader(String string, int i) {
        //setHeaderImpl(string, i);
        response.setIntHeader(string, i);
    }

    @Override
    public void addIntHeader(String string, int i) {
        //addHeaderImpl(string, i);
        response.addIntHeader(string, i);
    }

    @Override
    public void setStatus(int i) {
        response.setStatus(i);
    }

    public int getStatus() { return response.getStatus(); }

    @Override
    public void setStatus(int i, String string) {
        response.setStatus(i, string);
    }


//    private Map<String, List<String>> headers = new HashMap<String, List<String>>();
//    private void addHeaderImpl(String name, Object value) {
//        if(name == null) return ;
//        if(value == null) return ;
//        List<String> list = headers.get(name);
//        if(list == null) {
//            list = new ArrayList<String>();
//            headers.put(name, list);
//        }
//        list.add(value.toString());
//    }
//    private void setHeaderImpl(String name, Object value) {
//        if(name == null) return ;
//        List<String> list = new ArrayList<String>();
//        headers.put(name, list);
//        if(value != null) list.add(value.toString());
//    }
//
//    public Enumeration<String> getHeaderNames() {
//        return new Vector<String>(headers.keySet()).elements();
//    }
//    public Enumeration<String> getHeaders(String name) {
//        List<String> list = headers.get(name);
//        if(list == null) return new Vector<String>().elements();
//        else return new Vector<String>(list).elements();
//    }

    @Override
    public String getHeader(String name) {
        return response.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }
}
