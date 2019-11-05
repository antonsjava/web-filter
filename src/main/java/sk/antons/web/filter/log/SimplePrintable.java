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
package sk.antons.web.filter.log;

/**
 * Siple implementation for printable 
 * <li> empty content type
 * <li> starting with "text/"
 * <li> "application/json"
 * <li> "application/*+json"
 * <li> any content type containg "json"
 * <li> "application/xml"
 * <li> "application/xml"
 * <li> "application/*+xml"
 * <li> starts with "application/x-www-form-urlencoded"))
 * @author antons
 */
public class SimplePrintable implements Printable {

    @Override
    public boolean isPrintable(String contentType) {
        if(contentType == null) return true;
        else if("".equals(contentType)) return true;
        else if(contentType.startsWith("text/")) return true;
        else if("application/json".equals(contentType)) return true;
        else if(contentType.startsWith("application/") && contentType.endsWith("+json")) return true;
        else if(contentType.contains("json")) return true;
        else if("application/xml".equals(contentType)) return true;
        else if(contentType.startsWith("application/") && contentType.endsWith("+xml")) return true;
        else if(contentType.startsWith("application/x-www-form-urlencoded")) return true;
        return false;
    }
    
}
