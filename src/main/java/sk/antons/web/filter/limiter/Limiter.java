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

import javax.servlet.ServletRequest;

/**
 * Checks if request is allowed by this limiter.
 * @author antons
 */
public interface Limiter {
    
    /**
     * Returns true if limiter allow filter processing
     * @param request request to check
     * @return true is filter should be processed.
     */
    boolean allow(ServletRequest request);

    /**
     * Returns true if limiter allow filter processing after cain execution.
     * This variant can be used after chanin prpocessing when status code is known.
     * @param request request to check
     * @param status response status to check
     * @return true is filter should be processed.
     */
    boolean allowResponseStatus(ServletRequest request, int status);
    
}
