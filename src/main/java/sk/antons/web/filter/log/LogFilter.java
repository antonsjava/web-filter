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

import sk.antons.web.filter.util.HttpServletRequestWrapper;
import sk.antons.web.filter.util.HttpServletResponseWrapper;
import sk.antons.web.filter.util.ServletResponseWrapper;
import sk.antons.web.filter.util.ServletRequestWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sk.antons.json.util.JsonFormat;
import sk.antons.web.filter.limiter.RequestLimiter;

/**
 * LogFilter logs content of http requests and responses. You can use them 
 * to debug your web API. 
 * 
 * It logs following events 
 * <li> start or the request with method and path (and 'vvv' sequence indicating that relevant log is bellow)
 * <li> possible unhandled exception (ussualy not present)
 * <li> request path headers and payload
 * <li> response status headers and payload
 * 
 * It enable variety of configure 'where', 'what' and 'how' to log messages. 
 * And also there is possible to configure when to log and when not. This 
 * is usefull as possibility to configure filter presence itself is ussually 
 * pretty complicated.
 * 
 * <b>Filter configuration</b> 
 * 
 * There is only API way to configure the filter. So if you have no possibility 
 * to use API directy you must inherit from LogFilter configure and use your 
 * inherited (and configured) instance. 
 * 
 * <b>Filter configuration - where</b> 
 * 
 * Consumer is abstraction target for log message. By default fileter log 
 * messages using jdk14 logging API. It uses sk.antons.web.filter.log.LogFilter
 * logger and produce messages to FINEST level.
 * 
 * If you want to use different API for logging you simply implement Consumer 
 * and ConsumerStatus (check if logging is enabled) interfaces and configure 
 * filter
 * Example for slf4j and debug level.
 * <pre>
 *   filter.consumer(
 *               (message) -> { log.debug(message); } 
 *               , () -> { return log.isDebugEnabled();} );
 * </pre>
 * 
 * <b>Filter configuration - when</b> 
 * 
 * LogFilter was implemented as example of usage RequestLimiter class/lib. By 
 * default is LogFilter configured with empty RequestLimiter so it is applied 
 * for all requests where is configured.
 * 
 * But it is possible to limit LogFilter usage by configuration of RequestLimiter.
 * See this class for configuration possibilities.
 * Example for limit LogFilter functionality using path and method
 * <pre>
 *   filter.limit()
 *               .path()
                    .include("/foo/**")
                    .exclude("/foo/bar", "POST");
 * </pre>
 * 
 * <b>Filter configuration - what</b>
 * <ul>
 *   <li> filter.requestBeforePrefix("REQ") If it is set to null no start request 
 *        message will be displayed
 *   <li> filter.requestPrefix("REQ") If it is set to null no request message 
 *        will be displayed
 *   <li> filter.responsePrefix("RES") If it is set to null no response message 
 *        will be displayed
 * </ul>
 * 
 * <b>Filter configuration - how</b>
 * <ul>
 *   <li> filter.logHeaders(true) If it is set to false no header information is 
 *        included in request message and response message
 *   <li> filter.logPayload(true) If it is set to false no payload information is 
 *        included in request message and response message. There are also 
 *        filter.printable() which define if payload can be displayed depending 
 *        on contentType.
 *   <li> filter.truncateTo(0) If it is set to value >0 printable payload will be 
 *        truncated to this value. (usefull for variety of base64 values....)
 *   <li> filter.truncateLineTo(0) If it is set to value >0 each line of printable 
 *        payload will be truncated to this value. (usefull for variety of 
 *        base64 values followed by another usefful information where truncateTo 
 *        skip that information)
 *   <li> filter.truncateJsonelementTo(0) If it is set to value >0 and content is
 *        filter.jsonable() each string literal will be truncated to this value.
 *        (usefull for variety of base64 attributes in json)
 *   <li> filter.forceOneLine(true) If it is set to false content is not formated
 *        otherwise itis formated to one line. New line characters are escaped 
 *        with \\n. Json content is formated to one line in inative form.
 * </ul>
 * 
 * <b>Filter configuration - example</b>
 * 
 * This is simple example for springboot and jdk14 default logging
 * <pre>
 *   @Bean
 *   public Filter requestResponseDumpFilter() {
 *       return LogFilter.instance();
 *   }
 * </pre>
 * 
 * This is simple example for springboot and slf4j and some configuration
 * <pre>
 *   @Bean
 *   public Filter requestResponseDumpFilter() {
 *     LogFilter filter = LogFilter.instance();
 *     filter
 *       .consumer(
 *          (String string) -> { log.debug(string); } 
 *          , () -> { return log.isDebugEnabled();} )
 *       .truncateTo(10000)
 *       .truncateLineTo(100)
 *       .truncateJsonLiteral(100)
 *       //.oneLine(false)
 *       .limit()
 *         .path()
 *           .include("/foo/**", "POST") // - allow path pattern together with POST method
 *           .include("/dummy/**" //path - allow only this pattern
 *             , null //method - allow all methos
 *             , null //ip - allow all incoming ips
 *             , null //host - allow all incoming host names
 *             , MediaType.APPLICATION_JSON_VALUE //contentType - allow only json
 *             , (status) -> { return status >= 400;}) //respons status check - allow bad statuses 
 *           .exclude("/bar/**" //path - disallow only this pattern
 *             , "PUT" //method - disallow POT 
 *             , "127.0.0.1" //ip - disallow all incoming ips
 *             , null //host - disallow all incoming host names
 *             , null //contentType - disallow all
 *             , null) //respons status check - disallow all
 *           //.exclude("/dummy/**", "GET")
 *     ;
 *     return filter;
 *   }
 * </pre>
 * @author antons
 */
public class LogFilter implements Filter {

    private final RequestLimiter<LogFilter> limiter = new RequestLimiter<LogFilter>(this);
    private Consumer consumer = new LogConsumer();
    private ConsumerStatus consumerStatus = new LogConsumerStatus();
    private Printable printable = new SimplePrintable();
    private Jsonable jsonable = new SimpleJsonable();
    private Xmlable xmlable = new SimpleXmlable();
    private final Set<String> requestHeaderFilter = new HashSet<String>();
    private final Set<String> responseHeaderFilter = new HashSet<String>();
    private boolean logRequestHeaders = true;
    private boolean logRequestPayload = true;
    private boolean logResponseHeaders = true;
    private boolean logResponsePayload = true;
    private boolean forceOneLine = true;
    private int truncateTo = 0;
    private int truncateLineTo = 0;
    private int truncateJsonelementTo = 0;
    private String requestBeforePrefix = "REQ";
    private String requestPrefix = "REQ";
    private String responsePrefix = "RES";
    private boolean logIdentity = false;

    public static LogFilter instance() { return new LogFilter(); }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (consumerStatus.isConsumerOn() && limiter.allow(request)) {
            doFilterInternal(wrapRequest(request), wrapResponse(response), chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {}

    @Override
    public void destroy() {}
    
    /**
     * Configure filter processing limitations.
     * @return 
     */
    public RequestLimiter<LogFilter> limit() { return limiter; }
    
    /**
     * Setup consumer of LogFilter messages
     * @param consumer message consumer (default is LogConsumer)
     * @param consumerStatus consumer on/off status provider (default ia LogConsumerStatus)
     * @return this
     */
    public LogFilter consumer(Consumer consumer, ConsumerStatus consumerStatus) { 
        if(consumer == null) throw new IllegalArgumentException("Consumer can't be null");
        if(consumerStatus == null) throw new IllegalArgumentException("ConsumerStatus can't be null");
        this.consumer = consumer;
        this.consumerStatus = consumerStatus;
        return this; 
    }

    /**
     * Setup printable decision provider.
     * @param printable printable decision provider (decision ia SiplePrintable)
     * @return this
     */
    public LogFilter printable(Printable printable) {
        if(printable == null) throw new IllegalArgumentException("Printable can't be null");
        this.printable = printable;
        return this; 
    }

    /**
     * Setup jsonable decision provider.
     * @param printable jsonable decision provider (decision ia SipleJsonablw)
     * @return this
     */
    public LogFilter jsonable(Jsonable jsonable) {
        if(jsonable == null) throw new IllegalArgumentException("Jsonable can't be null");
        this.jsonable = jsonable;
        return this; 
    }
    
    /**
     * Setup xmlable decision provider.
     * @param printable xmlable decision provider (decision ia SipleXmlablw)
     * @return this
     */
    public LogFilter xmlable(Xmlable xmlable) {
        if(xmlable == null) throw new IllegalArgumentException("Xmlable can't be null");
        this.xmlable = xmlable;
        return this; 
    }
    
    /**
     * Configure printing of header info in request and response.
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter headers(boolean value) { 
        this.logRequestHeaders = value; 
        this.logResponseHeaders = value; 
        return this; 
    }
    
    /**
     * Configure printing of payload info in request and response.
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter payloads(boolean value) { 
        this.logRequestPayload = value; 
        this.logResponsePayload = value; 
        return this; 
    }
    
    /**
     * Configure printing of header info in request.
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter requestHeaders(boolean value) { this.logRequestHeaders = value; return this; }
    
    /**
     * Configure printing of payload info in request.
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter requestPayloads(boolean value) { this.logRequestPayload = value; return this; }
    
    /**
     * Configure printing of header info in response.
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter responseHeaders(boolean value) { this.logResponseHeaders = value; return this; }
    
    /**
     * Configure printing of payload info in response.
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter responsePayloads(boolean value) { this.logResponsePayload = value; return this; }
    
    /**
     * Configure printing of payload as one line
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter oneLine(boolean value) { this.forceOneLine = value; return this; }
    
    /**
     * Configure max length of printable payload to print
     * @param value max value of payload (0 unlimited)
     * @return this
     */
    public LogFilter truncateTo(int value) { this.truncateTo = value; return this; }
    
    /**
     * Configure max line length of printable payload to print
     * @param value max value of payload line (0 unlimited)
     * @return this
     */
    public LogFilter truncateLineTo(int value) { this.truncateLineTo = value; return this; }
    
    /**
     * Configure max length of string literal of jsonable payload to print
     * @param value max value of string literal (0 unlimited)
     * @return this
     */
    public LogFilter truncateJsonLiteral(int value) { this.truncateJsonelementTo = value; return this; }
    
    /**
     * String to identify start request message 
     * if it is null message is not printed at all.
     * @param value start request prefix (default 'REQ')
     * @return this
     */
    public LogFilter requestBeforePrefix(String value) { this.requestBeforePrefix = value; return this; }
    
    /**
     * String to identify request info message 
     * if it is null message is not printed at all.
     * @param value start request prefix (default 'REQ')
     * @return this
     */
    public LogFilter requestPrefix(String value) { this.requestPrefix = value; return this; }
    
    /**
     * String to identify response info message 
     * if it is null message is not printed at all.
     * @param value start request prefix (default 'RES')
     * @return this
     */
    public LogFilter responsePrefix(String value) { this.responsePrefix = value; return this; }
    
    /**
     * Add filter for displayed header info.
     * @param value name of the header property to be displayed
     * @return this
     */
    public LogFilter requestHeaderFilter(String value) { 
        if(value == null) return this;
        requestHeaderFilter.add(value);
        return this; 
    }
    
    /**
     * Add filter for displayed header info.
     * @param value name of the header property to be displayed
     * @return this
     */
    public LogFilter responseHeaderFilter(String value) { 
        if(value == null) return this;
        responseHeaderFilter.add(value);
        return this; 
    }
    
    /**
     * Configure printing of request to print principal name.
     * @param value true if info should be logged
     * @return this
     */
    public LogFilter identity(boolean value) { 
        this.logIdentity = value; 
        return this; 
    }

    private static long requestId = 1;
    protected void doFilterInternal(ServletRequestWrapper request, ServletResponseWrapper response, FilterChain filterChain) throws ServletException, IOException {
        StringBuilder pathbuff = new StringBuilder();
        StringBuilder requestheaderbuff = new StringBuilder();
        StringBuilder requestpayloadbuff = new StringBuilder();
        StringBuilder responseheadersbuff = new StringBuilder();
        StringBuilder responsepayloadbuff = new StringBuilder();
        int status = -1;
        int exceprionStatus = -1;
        long id = requestId++;
        long starttime = System.currentTimeMillis();
        try {
            if (consumerStatus.isConsumerOn()) {
                requestData(request, pathbuff, requestheaderbuff, requestpayloadbuff);
                if(requestBeforePrefix != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(requestBeforePrefix)
                        .append('[').append(id).append(']')
                        .append(pathbuff)
                        .append(" vvv");
                    consumer.consume(sb.toString());
                }
            }
            filterChain.doFilter(request, response);
            if(response instanceof HttpServletResponseWrapper) status = ((HttpServletResponseWrapper)response).getStatus();
        } catch(Throwable t) {
            exceprionStatus = 500;
            if (consumerStatus.isConsumerOn()) {
                StringBuilder sb = new StringBuilder();
                sb.append(responsePrefix)
                    .append('[').append(id).append(']')
                    .append(" ServletException ")
                    .append(pathbuff).append(' ').append(t);
                consumer.consume(sb.toString());
            }
            if(t instanceof IOException) throw (IOException)t;
            else if(t instanceof ServletException) throw (ServletException)t;
            else throw new ServletException(t);
        } finally {
            if (consumerStatus.isConsumerOn()) {
                if(exceprionStatus > 0) status = exceprionStatus;
                if((status <= 0) || (limiter.allowResponseStatus(request, status))) {
                    responseData(response, responseheadersbuff, responsepayloadbuff);
                    long endtime = System.currentTimeMillis();
                    long time = (endtime - starttime);
                    if(requestPrefix != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(requestPrefix)
                            .append('[').append(id).append(']')
                            .append(pathbuff)
                            .append(requestheaderbuff)
                            .append(requestpayloadbuff);
                        consumer.consume(sb.toString());
                    }
                    if(responsePrefix != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(responsePrefix)
                            .append('[').append(id).append(']')
                            .append(pathbuff);
                        sb.append(" status: ").append(status);
                        sb.append(" time: ").append(time)
                            .append(responseheadersbuff)
                            .append(responsepayloadbuff);
                        consumer.consume(sb.toString());
                    }
                }
            }
        }
        
    }
    private static ServletRequestWrapper wrapRequest(ServletRequest request) {
        if (request instanceof ServletRequestWrapper) {
            return (ServletRequestWrapper) request;
        } else if (request instanceof HttpServletRequest) {
            return new HttpServletRequestWrapper((HttpServletRequest)request);
        } else {
            return new ServletRequestWrapper(request);
        }
    }

    private static ServletResponseWrapper wrapResponse(ServletResponse response) {
        if (response instanceof ServletResponseWrapper) {
            return (ServletResponseWrapper) response;
        } else if (response instanceof HttpServletResponse) {
            return new HttpServletResponseWrapper((HttpServletResponse)response);
        } else {
            return new ServletResponseWrapper(response);
        }
    }

    protected void requestData(ServletRequestWrapper request, StringBuilder pathbuff, StringBuilder requestheaderbuff, StringBuilder requestpayloadbuff) {
        HttpServletRequestWrapper httprequest = null;
        if(request instanceof HttpServletRequestWrapper) httprequest = (HttpServletRequestWrapper)request;
        if(httprequest != null) {
            String method = httprequest.getMethod();
            String pathString = httprequest.getRequestURI();
            String queryString = httprequest.getQueryString();
            pathbuff.append(' ').append(method).append(' ').append(pathString);
            if(queryString != null) pathbuff.append('?').append(queryString);
        }
        if(logIdentity && (httprequest != null)) {
            requestheaderbuff.append(" identity(");
            try {
                Principal user = httprequest.getUserPrincipal();
                if(user != null) requestheaderbuff.append(user.getName());
            } catch(Exception e) {
            }
            requestheaderbuff.append(")");
        }
        if(logRequestHeaders && (httprequest != null)) {
            if(forceOneLine) {
                requestheaderbuff.append(' ').append(request.getProtocol());
                requestheaderbuff.append(" headers(");
                boolean filter = !requestHeaderFilter.isEmpty();
                Enumeration<String> params = httprequest.getHeaderNames();
                boolean nofirst = false;
                while(params.hasMoreElements()) {
                    String param = params.nextElement();
                    if(filter && (!requestHeaderFilter.contains(param))) continue;
                    Enumeration<String> values = httprequest.getHeaders(param);
                    while(values.hasMoreElements()) {
                        String value = values.nextElement();
                        if(nofirst) requestheaderbuff.append(", ");
                        else nofirst = true;
                        requestheaderbuff.append(param).append(": ").append(value);
                    }
                }
                requestheaderbuff.append(")");
            } else {
                requestheaderbuff.append("\n-- header ------------------------ ");
                requestheaderbuff.append('\n').append(request.getProtocol());
                boolean filter = !requestHeaderFilter.isEmpty();
                Enumeration<String> params = httprequest.getHeaderNames();
                while(params.hasMoreElements()) {
                    String param = params.nextElement();
                    if(filter && (!requestHeaderFilter.contains(param))) continue;
                    Enumeration<String> values = httprequest.getHeaders(param);
                    while(values.hasMoreElements()) {
                        String value = values.nextElement();
                        requestheaderbuff.append('\n').append(param).append(": ").append(value);
                    }
                }
            }
        }

        if(logRequestPayload) {
            boolean printable = this.printable.isPrintable(request.getContentType());
            boolean jsonable = this.jsonable.isJsonable(request.getContentType());
            boolean xmlable = this.xmlable.isXmlable(request.getContentType());
            int length = 0;
            InputStream is = null;
            try {
                int num = 0;
                byte[] data = new byte[1024];
                is = request.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((num = is.read(data, 0, data.length)) != -1) {
                    length = length + num;
                    if(printable) buffer.write(data, 0, num);
                }   
                buffer.flush();
                byte[] bytes = buffer.toByteArray();
                String text = "";
                if(printable) {
                    text = new String(bytes, "utf-8");
                    if(jsonable && (length > 0)) {
                        if(forceOneLine) {
                            try {
                                if(truncateJsonelementTo > 0) text = JsonFormat.from(text).noindent().cutStringLiterals(truncateJsonelementTo).toText();
                                else text = JsonFormat.from(text).noindent().toText();
                            } catch(Exception e) {}
                        }
                    } else if(xmlable && (length > 0)) {
                        if(forceOneLine) {
                            try {
                                text = XmlFormat.instance(text, 0).forceoneline().format();
                            } catch(Exception e) {}
                        }
                    } else if((truncateLineTo > 0) || (truncateTo > 0) || forceOneLine) {
                        text = cut(text, truncateTo, truncateLineTo, forceOneLine);
                    }
                }
                if(forceOneLine) {
                    requestpayloadbuff.append(" payload[").append(text).append(']');
                    requestpayloadbuff.append(" size: ").append(length);
                } else {
                    requestpayloadbuff.append("\n-- payload ------------------------\n");
                    requestpayloadbuff.append(text);
                    requestpayloadbuff.append("\n size: ").append(length);
                }
            } catch(IOException e) {
                throw new IllegalArgumentException(e);
            } finally {
            }
        }

    }
    
    private static boolean isSpace(char c) {
        if(c == ' ') return true;
        if(c == '\n') return true;
        if(c == '\t') return true;
        if(c == '\r') return true;
        return false;
    }

    private static String cut(String value, int wholeLength, int onelineLength, boolean forceOneLine) {
        if(value == null) return value;
        if(value.length() == 0) return value;
        if((wholeLength <= 0) 
            && (onelineLength <= 0)
            && (!forceOneLine)) return value;
        int startpos = 0;
        int endpos = value.length() - 1;
        while((startpos <= endpos) && isSpace(value.charAt(startpos))) startpos++;
        while((endpos > startpos) && isSpace(value.charAt(endpos))) endpos--;
        if(startpos >= endpos) return "";
        
        int oldPos = startpos;
        int pos = value.indexOf('\n', oldPos);
        if((pos < 0) || (pos > endpos)) {
            if(value.length() < wholeLength) return value;
            value = value.substring(0, wholeLength-13) + "... truncated";
            return value;
        } else {
            StringBuilder sb = new StringBuilder();
            while(pos >= 0) {
                if(sb.length()>0) {
                    if(forceOneLine) sb.append("\\n");
                    else sb.append('\n');
                }
                if((onelineLength > 0) && ((pos - oldPos) > onelineLength)) {
                    sb.append(value, oldPos, oldPos+onelineLength-13);
                    sb.append("... truncated");
                } else {
                    sb.append(value, oldPos, pos);
                }
                oldPos = pos+1;
                pos = value.indexOf('\n', oldPos);
            }
            int len = value.length();
            if(oldPos < len) {
                if((onelineLength > 0) && ((pos - oldPos) > onelineLength)) {
                    sb.append(value, oldPos, oldPos+onelineLength-13);
                    sb.append("... truncated");
                } else {
                    sb.append(value, oldPos, len);
                }
            }
            if((wholeLength >0) && (sb.length() > wholeLength)) {
                sb.setLength(wholeLength-13);
                sb.append("... truncated");
            }
            return sb.toString();
        }
    }

    protected void responseData(ServletResponseWrapper response, StringBuilder responseheadersbuff, StringBuilder responsepayloadbuff) {
        HttpServletResponseWrapper httpresponse = null;
        if(response instanceof HttpServletResponseWrapper) httpresponse = (HttpServletResponseWrapper)response;
        if(logResponseHeaders && (httpresponse != null)) {
            if(forceOneLine) {
                responseheadersbuff.append(" headers(");
                boolean filter = !responseHeaderFilter.isEmpty();
                Collection<String> params = httpresponse.getHeaderNames();
                boolean nofirst = false;
                for(String param : params) {
                    if(filter && (!responseHeaderFilter.contains(param))) continue;
                    Collection<String> values = httpresponse.getHeaders(param);
                    for(String value : values) {
                        if(nofirst) responseheadersbuff.append(", ");
                        else nofirst = true;
                        responseheadersbuff.append(param).append(": ").append(value);
                    }
                }
                responseheadersbuff.append(")");
            } else {
                responseheadersbuff.append("\n-- header ------------------------ ");
                boolean filter = !responseHeaderFilter.isEmpty();
                Collection<String> params = httpresponse.getHeaderNames();
                for(String param : params) {
                    if(filter && (!responseHeaderFilter.contains(param))) continue;
                    Collection<String> values = httpresponse.getHeaders(param);
                    for(String value : values) {
                        responseheadersbuff.append('\n').append(param).append(": ").append(value);
                    }
                }
            }
        }
        
        if(logResponsePayload) {
            boolean printable = this.printable.isPrintable(response.getContentType());
            boolean jsonable = this.jsonable.isJsonable(response.getContentType());
            boolean xmlable = this.xmlable.isXmlable(response.getContentType());
            int length = 0;
            InputStream is = null;
            try {
                is = response.getContentInputStream();
                String text = "";
                byte[] bytes = null;
                if(is != null) {
                    int num = 0;
                    byte[] data = new byte[1024];
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    while ((num = is.read(data, 0, data.length)) != -1) {
                        length = length + num;
                        if(printable) buffer.write(data, 0, num);
                    }   
                    buffer.flush();
                    bytes = buffer.toByteArray();
                }    
                if(printable && (bytes != null)) {
                    text = new String(bytes, "utf-8");
                    if(jsonable && (length > 0)) {
                        if(forceOneLine) {
                            try {
                                if(truncateJsonelementTo > 0) text = JsonFormat.from(text).noindent().cutStringLiterals(truncateJsonelementTo).toText();
                                else text = JsonFormat.from(text).noindent().toText();
                            } catch(Exception e) {}
                        } else {
                            try {
                                if(truncateJsonelementTo > 0) text = JsonFormat.from(text).cutStringLiterals(truncateJsonelementTo).toText();
                            } catch(Exception e) {}
						}
                    } else if(xmlable && (length > 0)) {
                        if(forceOneLine) {
                            try {
                                text = XmlFormat.instance(text, 0).forceoneline().format();
                            } catch(Exception e) {}
						}
                    } else if((truncateLineTo > 0) || (truncateTo > 0) || forceOneLine) {
                        text = cut(text, truncateTo, truncateLineTo, forceOneLine);
                    }
                }
                if(forceOneLine) {
                    responsepayloadbuff.append(" payload[").append(text).append(']');
                    responsepayloadbuff.append(" size: ").append(length);
                } else {
                    responsepayloadbuff.append("\n-- payload ------------------------\n");
                    responsepayloadbuff.append(text);
                    responsepayloadbuff.append("\n size: ").append(length);
                }
            } catch(IOException e) {
                throw new IllegalArgumentException(e);
            } finally {
            }
        }

    }

}
