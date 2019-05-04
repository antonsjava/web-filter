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

import java.util.HashSet;
import java.util.Set;

/**
 * String value configuration 
 * @author antons
 */
public class StringConf<T> {

    private T parent = null;

    protected Set<String> includes = new HashSet<String>();
    protected Set<String> excludes = new HashSet<String>();
    
    public StringConf(T parent) { this.parent = parent; }
    
    /**
     * Back to limiter instance.
     * @return 
     */
    public T and() { return parent; }
    
    /**
     * Adds allow value for configuration attribute.
     * @param value value to be accepted.
     * @return this configuration instance
     */
    public StringConf include(String value) { 
        includes.add(value); 
        return this;
    }
    
    /**
     * Adds disallow value for configuration attribute.
     * @param value value to be rejected.
     * @return this configuration instance
     */
    public StringConf exclude(String value) { 
        excludes.add(value); 
        return this;
    }
    
    /**
     * Implements allow check for defined includes and excludes.
     * @param value value to be checked
     * @return true if value is allowed by defined includes and excludes;
     */
    protected boolean allow(String value) {
        if(includes.isEmpty()) {
            if(excludes.isEmpty()) {
                return true;
            } else {
                return !excludes.contains(value);
            }
        } else {
            boolean rv = includes.contains(value);
            if(!rv) return false;
            if(excludes.isEmpty()) {
                return true;
            } else {
                return !excludes.contains(value);
            }
        }
    }
}
