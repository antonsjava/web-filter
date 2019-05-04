
# web-filter

 Set of classes usefull for Servlet filter implementation.
  - RequestLimiter. API for filter usage configuration 
  - LogFilter. Implementation of request response logging filter. 
    Implemented as example of RequestLimiter usage.

## RequestLimiter

 Implementation of various configuations and check for ServletRequests
 and its state. 
 
 It is quit easy to setup Filter for your API. But is is complicated to 
 implement configuration of filter in a way where you want to limit filter
 functionality on som request condition.

 RequestLimiter can provide such configuration for your filter implementation.
 
 As example of usage you add limiter as atttribute of filter 
```java
  public class MyFilter implements Filter {
    private RequestLimiter limiter = new RequestLimiter();
    public RequestLimiter limit() { return limiter; }
```

 Now you can configure the filter
 
```java
  MyFilter filter = MyFilter.instance();
  filter.limit()
    .path()
      .include("/foo/**") 
      .exclude("/foo.bar", "POST");
  ;
```

 And in MyFilter implementation you can use something like 

```java
  if (limiter.allow(request)) { ... do something }
  chain.doFilter(request, response);
  if (limiter.allow(request)) { ... do something }
```

 See LogFilter doc for examples of usage.

## LogFilter

 Implements logging filter for ServletRequests. It can show usage of 
 RequestLimiter usage.

 LogFilter logs content of http requests and responses. You can use them 
 to debug your web API. 
 
 It logs following events 
 - start or the request with method and path (and 'vvv' sequence indicating 
   that relevant log is bellow)
 - possible unhandled exception (ussualy not present)
 - request path headers and payload
 - response status headers and payload
 
 It enable variety of configure 'where', 'what' and 'how' to log messages. 
 And also there is possible to configure when to log and when not. This 
 is usefull as possibility to configure filter presence itself is ussually 
 pretty complicated.
 
### Filter configuration - common
 
 There is only API way to configure the filter. So if you have no possibility 
 to use API directy you must inherit from LogFilter configure and use your 
 inherited (and configured) instance. 
 
### Filter configuration - where
 
 Consumer is abstraction target for log message. By default fileter log 
 messages using jdk14 logging API. It uses sk.antons.web.filter.log.LogFilter
 logger and produce messages to FINEST level.
 
 If you want to use different API for logging you simply implement Consumer 
 and ConsumerStatus (check if logging is enabled) interfaces and configure 
 filter
 Example for slf4j and debug level.

```java
   filter.consumer(
               (message) -> { log.debug(message); } 
               , () -> { return log.isDebugEnabled();} );
```
 
### Filter configuration - when 
 
 LogFilter was implemented as example of usage RequestLimiter class/lib. By 
 default is LogFilter configured with empty RequestLimiter so it is applied 
 for all requests where is configured.
 
 But it is possible to limit LogFilter usage by configuration of RequestLimiter.
 See this class for configuration possibilities.
 Example for limit LogFilter functionality using path and method

```java
   filter.limit()
               .path()
                  .include("/foo/**")
                  .exclude("/foo/bar", "POST");
```
 
### Filter configuration - what
 - filter.requestBeforePrefix("REQ") If it is set to null no start request 
   message will be displayed
 - filter.requestPrefix("REQ") If it is set to null no request message 
   will be displayed
 - filter.responsePrefix("RES") If it is set to null no response message 
   will be displayed
 
### Filter configuration - how
 - filter.logHeaders(true) If it is set to false no header information is 
   included in request message and response message
 - filter.logPayload(true) If it is set to false no payload information is 
   included in request message and response message. There are also 
   filter.printable() which define if payload can be displayed depending 
   on contentType.
 - filter.truncateTo(0) If it is set to value >0 printable payload will be 
   truncated to this value. (usefull for variety of base64 values....)
 - filter.truncateLineTo(0) If it is set to value >0 each line of printable 
   payload will be truncated to this value. (usefull for variety of 
   base64 values followed by another usefful information where truncateTo 
   skip that information)
 - filter.truncateJsonelementTo(0) If it is set to value >0 and content is
   filter.jsonable() each string literal will be truncated to this value.
   (usefull for variety of base64 attributes in json)
 - filter.forceOneLine(true) If it is set to false content is not formated
   otherwise itis formated to one line. New line characters are escaped 
   with \\n. Json content is formated to one line in inative form.
 
### Filter configuration - example
 
 This is simple example for springboot and jdk14 default logging

```java
   @Bean
   public Filter requestResponseDumpFilter() {
       return LogFilter.instance();
   }
```
 
 This is simple example for springboot and slf4j and some configuration
 
```java
   @Bean
   public Filter requestResponseDumpFilter() {
     LogFilter filter = LogFilter.instance();
     filter
       .consumer(
          (String string) -> { log.debug(string); } 
          , () -> { return log.isDebugEnabled();} )
       .truncateTo(10000)
       .truncateLineTo(100)
       .truncateJsonLiteral(100)
       //.oneLine(false)
       .limit()
         .path()
           .include("/foo/**", "POST") // - allow path pattern together with POST method
           .include("/dummy/**" //path - allow only this pattern
             , null //method - allow all methos
             , null //ip - allow all incoming ips
             , null //host - allow all incoming host names
             , MediaType.APPLICATION_JSON_VALUE //contentType - allow only json
             , (status) -> { return status >= 400;}) //respons status check - allow bad statuses 
           .exclude("/foo/bar/**" //path - disallow only this pattern
             , "PUT" //method - disallow POT 
             , "127.0.0.1" //ip - localhost request
             , null //host - disallow all incoming host names
             , null //contentType - disallow all
             , null) //respons status check - disallow all
           //.exclude("/dummy/**", "GET")
     ;
     return filter;
   }
```

## Dependencies
 
 Implementation depends on Servlet API (j2ee 7) and 
  - com.github.antonsjava:web-path-matcher small library for path patterns
  - com.github.antonsjava:json small library for parsing json

## Maven usage

```
   <dependency>
      <groupId>com.github.antonsjava</groupId>
      <artifactId>web-filter</artifactId>
      <version>1.0</version>
   </dependency>
```
