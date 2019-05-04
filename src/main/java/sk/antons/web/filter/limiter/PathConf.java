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

import java.util.ArrayList;
import java.util.List;
import sk.antons.web.path.PathMatcher;

/**
 * Helper class for configuring path matchers in limiter.
 * @author antons
 */
public class PathConf<T> {

    private T parent = null;
    private int max = 64;

    protected List<Combo> includes = new ArrayList<Combo>();
    protected List<Combo> excludes = new ArrayList<Combo>();
    
    protected PathConf(T parent) { this.parent = parent; }
    
    /**
     * Back to limiter instance.
     * @return 
     */
    public T and() { return parent; }
//    public ComboConf withIp(String ip) { return ComboConf.instance(Combo.instance(null, null), this, max).withIp(ip); }
//    public ComboConf withHost(String host) { return ComboConf.instance(Combo.instance(null, null), this, max).withHost(host); }
//    public ComboConf withContentType(String contenttype) { return ComboConf.instance(Combo.instance(null, null), this, max).withContentType(contenttype); }
//    public ComboConf withResponseStatus(ResponseStatusCheck check) { return ComboConf.instance(Combo.instance(null, null), this, max).withResponseStatus(check); }
    
    /**
     * Clears all setting for path configuration.
     * @param max maximal number of path element which may occure.
     * @return this path configuration instance
     */
    public PathConf reset(int max) {
        this.max = max;
        includes = new ArrayList<Combo>();
        excludes = new ArrayList<Combo>();
        return this;
    }

    /**
     * Add include path matcher 
     * @param pattern pattern for path matcher (mandatory)
     * @return this path configuration instance
     */
    public PathConf include(String pattern) {
        return include(pattern, null);
    }

    /**
     * Add include path matcher 
     * @param pattern pattern for path matcher (mandatory)
     * @param method method applied together with matcher if is is not null
     * @return this path configuration instance
     */
    public PathConf include(String pattern, String method) { 
        includes.add(Combo.instance(PathMatcher.instance(pattern, max), method)); 
        return this;
    }

    /**
     * Add include path matcher 
     * @param pattern pattern for path matcher (mandatory)
     * @param method method applied together with matcher if is is not null
     * @param ip ip address applied together with matcher if is is not null
     * @param host host name applied together with matcher if is is not null
     * @param contenttype content type applied together with matcher if is is not null
     * @param responseStatusCheck method applied together with matcher if is is not null
     * @return this path configuration instance
     */
    public PathConf include(String pattern, String method, String ip, String host, String contenttype, ResponseStatusCheck responseStatusCheck) { 
        includes.add(Combo.instance(PathMatcher.instance(pattern, max), method, ip, host, contenttype, responseStatusCheck)); 
        return this;
    }

    /**
     * Add exclude path matcher 
     * @param pattern pattern for path matcher (mandatory)
     * @return this path configuration instance
     */
    public PathConf exclude(String pattern) {
        return exclude(pattern, null);
    }

    /**
     * Add exclude path matcher 
     * @param pattern pattern for path matcher (mandatory)
     * @param method method applied together with matcher if is is not null
     * @return this path configuration instance
     */
    public PathConf exclude(String pattern, String method) { 
        excludes.add(Combo.instance(PathMatcher.instance(pattern, max), method)); 
        return this;
    }
    
    /**
     * Add exclude path matcher 
     * @param pattern pattern for path matcher (mandatory)
     * @param method method applied together with matcher if is is not null
     * @param ip ip address applied together with matcher if is is not null
     * @param host host name applied together with matcher if is is not null
     * @param contenttype content type applied together with matcher if is is not null
     * @param responseStatusCheck method applied together with matcher if is is not null
     * @return this path configuration instance
     */
    public PathConf exclude(String pattern, String method, String ip, String host, String contenttype, ResponseStatusCheck responseStatusCheck) { 
        excludes.add(Combo.instance(PathMatcher.instance(pattern, max), method, ip, host, contenttype, responseStatusCheck)); 
        return this;
    }

    public static class ComboConf {
        private int max;
        private Combo combo;
        private PathConf pathConf;
        
        public ComboConf(Combo combo, PathConf pathConf, int max) {
            this.max = max;
            this.combo = combo;
            this.pathConf = pathConf;
        }

        public static ComboConf instance(Combo combo, PathConf pathConf, int max) {
            return new ComboConf(combo, pathConf, max);
        }

        public ComboConf withIp(String ip) {this.combo.ip = ip; return this;}
        public ComboConf withHost(String host) {this.combo.host = host; return this;}
        public ComboConf withContentType(String contenttype) {this.combo.contenttype = contenttype; return this;}
        public ComboConf withResponseStatus(ResponseStatusCheck check) {this.combo.responseStatusCheck = check; return this;}
        public PathConf exclude(String pattern) {
            return exclude(pattern, null);
        }
        public PathConf exclude(String pattern, String method) { 
            this.combo.matcher = PathMatcher.instance(pattern, max);
            this.combo.method = method; 
            this.pathConf.excludes.add(this.combo);
            return this.pathConf;
        }
        public PathConf include(String pattern) {
            return include(pattern, null);
        }
        public PathConf include(String pattern, String method) { 
            this.combo.matcher = PathMatcher.instance(pattern, max);
            this.combo.method = method; 
            this.pathConf.includes.add(this.combo);
            return this.pathConf;
        }

        
    }
    
    protected static class Combo {
        private PathMatcher matcher;
        private String method;
        private String ip;
        private String host;
        private String contenttype;
        private ResponseStatusCheck responseStatusCheck = null;
        
        public Combo(PathMatcher matcher, String method) {
            this.matcher = matcher;
            this.method = method;
        }
        
        public Combo(PathMatcher matcher, String method, String ip, String host, String contenttype, ResponseStatusCheck responseStatusCheck) {
            this.matcher = matcher;
            this.method = method;
            this.ip = ip;
            this.host = host;
            this.contenttype = contenttype;
            this.responseStatusCheck = responseStatusCheck;
        }

        public static Combo instance(PathMatcher matcher, String method) {
            return new Combo(matcher, method);
        }

        public static Combo instance(PathMatcher matcher, String method, String ip, String host, String contenttype, ResponseStatusCheck responseStatusCheck) {
            return new Combo(matcher, method, ip, host, contenttype, responseStatusCheck);
        }
        
        public boolean match(int responseStatus) {
            if(responseStatus <=0) return true;
            if(this.responseStatusCheck != null) {
                if(!responseStatusCheck.allow(responseStatus)) return false;
            }
            return true;
        }

        public boolean match(String path, String method, String ip, String host, String contenttype) {
            if(this.matcher != null) {
                if(!matcher.match(path)) return false;
            }
            if(this.method != null) {
                if(!this.method.equals(method)) return false;
            }
            if(this.ip != null) {
                if(!this.ip.equals(ip)) return false;
            }
            if(this.host != null) {
                if(!this.host.equals(host)) return false;
            }
            if(this.contenttype != null) {
                if(!this.contenttype.equals(contenttype)) return false;
            }
            return true;
        }
    }
}
