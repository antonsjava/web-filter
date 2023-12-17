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
package sk.antons.web.filter.limiter;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Servlet request limiter implementation. If you implement servlet filter 
 * and you want to limit some functionality in this filter. You can add 
 * this instance to your implementation and use them in filter implementation 
 * to limit a functionality.
 * 
 * As example you can see in messagge logger 
 * <pre>
 *   public class LogFilter implements Filter {
 *      private RequestLimiter limiter = new RequestLimiter();
 *      public RequestLimiter limit() { return limiter; }
 * </pre>
 * By this you can configure your instances
 * <pre>
 *      LogFilter filter = LogFilter.instance();
 *       filter
 *           .limit()
 *               .path()
 *                   .include("/foo/**", "POST") // - allow path pattern together with POST method
 *                   .include("/dummy/**" //path - allow only this pattern
 *                       , null //method - allow all methos
 *                       , null //ip - allow all incoming ips
 *                       , null //host - allow all incoming host names
 *                       , MediaType.APPLICATION_JSON_VALUE //contentType - allow only json
 *                       , (status) -> { return status >= 400;}) //respons status check - allow bad statuses 
 *                   .exclude("/bar/**" //path - disallow only this pattern
 *                       , "PUT" //method - disallow POT 
 *                       , "127.0.0.1" //ip - disallow all incoming ips
 *                       , null //host - disallow all incoming host names
 *                       , null //contentType - disallow all
 *                       , null) //respons status check - disallow all
 *                   
 *                   //.exclude("/dummy/**", "GET")
 * ;
 * </pre>
 * And in LogFilter implementation you can use something like 
 * <pre>
 *       if (limiter.allow(request)) {
 *           doFilterInternal(wrapRequest(request), wrapResponse(response), chain);
 *       } else {
 *           chain.doFilter(request, response);
 *       }
 * </pre>
 * @author antons
 */
public class RequestLimiter<T> implements Limiter {
    
	private T parent = null;
    
    public RequestLimiter(T parent) { this.parent = parent; }
    
    public T filter() { return parent; }


    private Limiter custom = null;
    private PathConf<RequestLimiter<T>> pathconf = new PathConf<RequestLimiter<T>>(this);
    private StringConf<RequestLimiter<T>> ipconf = new StringConf<RequestLimiter<T>>(this);
    private StringConf<RequestLimiter<T>> hostconf = new StringConf<RequestLimiter<T>>(this);
    private StringConf<RequestLimiter<T>> contenttypeconf = new StringConf<RequestLimiter<T>>(this);
    private ResponseStatusCheck responseStatusCheck = null;
    
    /**
     * Path mather configuration for limiter.
     * @return 
     */
    public PathConf<RequestLimiter<T>> path() { return pathconf; }
    
    /**
     * Host name configuration for limiter.
     * This will apply for all requests. You can also limit this only for 
     * concrete path in path() configuration.
     * @return 
     */
    public StringConf<RequestLimiter<T>> host() { return hostconf; }
    
    /**
     * Content type configuration for limiter.
     * This will apply for all requests. You can also limit this only for 
     * concrete path in path() configuration.
     * @return 
     */
    public StringConf<RequestLimiter<T>> contentType() { return contenttypeconf; }
    
    /**
     * You can add your custom limiter implementation.
     * @param limiter
     * @return 
     */
    public RequestLimiter<T> custom(Limiter limiter) { this.custom = limiter; return this; }
    
    /**
     * Response status configuration for limiter.
     * This will apply for all requests. You can also limit this only for 
     * concrete path in path() configuration.
     * @return 
     */
    public RequestLimiter<T> responseStatus(ResponseStatusCheck check) { this.responseStatusCheck = check; return this; }
    
    /**
     * Clears all settings for this limiter.
     * @return 
     */
    public RequestLimiter<T> reset() { 
        pathconf = new PathConf(this);
        ipconf = new StringConf(this);
        hostconf = new StringConf(this);
        contenttypeconf = new StringConf(this);
        return this; 
    }

    @Override
    public boolean allow(ServletRequest request) {
        if(request == null) return false;
        if((custom != null) && (!custom.allow(request))) return false;
        if(request instanceof HttpServletRequest) {
            HttpServletRequest httprequest = (HttpServletRequest)request;
            String ip = httprequest.getRemoteAddr();
            String host = httprequest.getRemoteHost();
            String contenttype = httprequest.getContentType();
            String path = httprequest.getRequestURI();
            String method = httprequest.getMethod();
            if(!pathAllow(pathconf, path, method, ip, host, contenttype)) return false;
        }
        String value = request.getContentType();
        if(!stringAllow(contenttypeconf, value)) return false;
        value = request.getRemoteAddr();
        if(!stringAllow(ipconf, value)) return false;
        value = request.getRemoteHost();
        if(!stringAllow(ipconf, value)) return false;
        return true;
    }

    @Override
    public boolean allowResponseStatus(ServletRequest request, int status) {
        if(status <= 0) return true;
        if(request == null) return false;
        if((custom != null) && (!custom.allowResponseStatus(request, status))) return false;
        if(request instanceof HttpServletRequest) {
            HttpServletRequest httprequest = (HttpServletRequest)request;
            String ip = httprequest.getRemoteAddr();
            String host = httprequest.getRemoteHost();
            String contenttype = httprequest.getContentType();
            String path = httprequest.getRequestURI();
            String method = httprequest.getMethod();
            if(!pathAllowResponseStatus(pathconf, path, method, ip, host, contenttype, status)) return false;
        }
        if(responseStatusCheck != null) return responseStatusCheck.allow(status);
        return true;
    }
    
    private static boolean pathAllow(PathConf conf, String path, String method, String ip, String host, String contenttype) {
        if(conf.includes.isEmpty()) {
            if(conf.excludes.isEmpty()) {
                return true;
            } else {
                for(Object o : conf.excludes) {
                    PathConf.Combo matcher = (PathConf.Combo)o;
                    if(matcher.match(path, method, ip, host, contenttype)) return false;
                }
                return true;
            }
        } else {
            boolean rv = false;
            for(Object o : conf.includes) {
                PathConf.Combo matcher = (PathConf.Combo)o;
                if(matcher.match(path, method, ip, host, contenttype)) {
                    rv = true;
                    break;
                }
            }
            if(!rv) return false;
            if(conf.excludes.isEmpty()) {
                return true;
            } else {
                for(Object o : conf.excludes) {
                    PathConf.Combo matcher = (PathConf.Combo)o;
                    if(matcher.match(path, method, ip, host, contenttype)) return false;
                }
                return true;
            }
        }
    }
    
    private static boolean pathAllowResponseStatus(PathConf conf, String path, String method, String ip, String host, String contenttype, int status) {
        if(conf.includes.isEmpty()) {
            if(conf.excludes.isEmpty()) {
                return true;
            } else {
                for(Object o : conf.excludes) {
                    PathConf.Combo matcher = (PathConf.Combo)o;
                    if(matcher.match(path, method, ip, host, contenttype) && matcher.match(status)) return false;
                }
                return true;
            }
        } else {
            boolean rv = false;
            for(Object o : conf.includes) {
                PathConf.Combo matcher = (PathConf.Combo)o;
                if(matcher.match(path, method, ip, host, contenttype) && matcher.match(status)) {
                    rv = true;
                    break;
                }
            }
            if(!rv) return false;
            if(conf.excludes.isEmpty()) {
                return true;
            } else {
                for(Object o : conf.excludes) {
                    PathConf.Combo matcher = (PathConf.Combo)o;
                    if(matcher.match(path, method, ip, host, contenttype) && matcher.match(status)) return false;
                }
                return true;
            }
        }
    }
    
    private static boolean stringAllow(StringConf conf, String value) {
        return StringConfReader.allow(conf, value);
    }
}
