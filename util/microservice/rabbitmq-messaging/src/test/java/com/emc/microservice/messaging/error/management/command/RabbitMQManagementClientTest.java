// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2015 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.messaging.error.management.command;

/**
 * @author nivenb
 */
public class RabbitMQManagementClientTest {
    //todo:messagegrouping
/*
    private static final Logger log = LoggerFactory.getLogger(RabbitMQManagementClientTest.class);

    private static LocalTestServer server;

    private RabbitMQManagementClient rabbitMQManagementClient;

    public RabbitMQManagementClientTest() {
        InetSocketAddress serverAddress = server.getServiceAddress();
        rabbitMQManagementClient = new RabbitMQManagementClient(serverAddress.getHostName(), serverAddress.getPort(), "", "", false);
    }

    */
    /**
     *
     * @throws Exception
     *//*

    @BeforeClass
    public static void setUp() throws Exception {
        server = new LocalTestServer(null, null);
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testGetBindings() {
        registerSuccessResponseWithBody("/api/bindings", "/management/bindings_all.json");
        List<Binding> bindings = rabbitMQManagementClient.getBindings();
        Assert.assertEquals(4, bindings.size());
        Binding binding1 = bindings.get(0);
        Assert.assertEquals("ListenerExchange", binding1.getSource());
        Assert.assertEquals("/", binding1.getVirtualHost());
        Assert.assertEquals("ListenerQ1", binding1.getDestination());
        Assert.assertEquals("queue", binding1.getDestinationType());
        Assert.assertEquals("100", binding1.getRoutingKey());
        Assert.assertEquals("100", binding1.getPropertiesKey());

        Assert.assertEquals("ListenerQ2", bindings.get(1).getDestination());
        Assert.assertEquals("ListenerQ3", bindings.get(2).getDestination());
        Assert.assertEquals("SomeOtherQ", bindings.get(3).getDestination());
    }

    @Test
    public void testGetBindingsOfExchangeAsSource() {
        registerSuccessResponseWithBody("/api/exchanges/%2F/ListenerExchange/bindings/source", "/management/bindings_of_exchange.json");
        List<Binding> bindings = rabbitMQManagementClient.getBindingsOfExchangeAsSource("/", "ListenerExchange");
        Assert.assertEquals(3, bindings.size());
        Binding binding1 = bindings.get(0);
        Assert.assertEquals("ListenerExchange", binding1.getSource());
        Assert.assertEquals("/", binding1.getVirtualHost());
        Assert.assertEquals("ListenerQ1", binding1.getDestination());
        Assert.assertEquals("queue", binding1.getDestinationType());
        Assert.assertEquals("100", binding1.getRoutingKey());
        Assert.assertEquals("100", binding1.getPropertiesKey());

        Assert.assertEquals("ListenerQ2", bindings.get(1).getDestination());
        Assert.assertEquals("ListenerQ3", bindings.get(2).getDestination());
    }

    @Test
    public void testGetQueues() {
        registerSuccessResponseWithBody("/api/queues", "/management/queues_all.json");
        List<Queue> queues = rabbitMQManagementClient.getQueues();
        Assert.assertEquals(4, queues.size());
        Queue queue1 = queues.get(0);
        Assert.assertEquals("ListenerQ1", queue1.getName());
        Assert.assertEquals("/", queue1.getVirtualHost());
        Assert.assertEquals(true, queue1.isDurable());
        Assert.assertEquals(false, queue1.isAutoDelete());
        Assert.assertEquals(1, queue1.getConsumers());

        Assert.assertEquals("ListenerQ2", queues.get(1).getName());
        Assert.assertEquals("ListenerQ3", queues.get(2).getName());
        Assert.assertEquals("SomeOtherQ", queues.get(3).getName());
    }

    @Test
    public void testGetQueuesOfVirtualHost() {
        registerSuccessResponseWithBody("/api/queues/%2F", "/management/queues_of_vhost.json");
        List<Queue> queues = rabbitMQManagementClient.getQueuesOfVirtualHost("/");
        Assert.assertEquals(4, queues.size());
        Queue queue1 = queues.get(0);
        Assert.assertEquals("ListenerQ1", queue1.getName());
        Assert.assertEquals("/", queue1.getVirtualHost());
        Assert.assertEquals(true, queue1.isDurable());
        Assert.assertEquals(false, queue1.isAutoDelete());
        Assert.assertEquals(1, queue1.getConsumers());

        Assert.assertEquals("ListenerQ2", queues.get(1).getName());
        Assert.assertEquals("ListenerQ3", queues.get(2).getName());
        Assert.assertEquals("SomeOtherQ", queues.get(3).getName());
    }

    @Test
    public void testGetQueuesWithNoConsumers() {
        registerSuccessResponseWithBody("/api/exchanges/%2F/ListenerExchange/bindings/source", "/management/bindings_of_exchange.json");
        registerSuccessResponseWithBody("/api/queues/%2F", "/management/queues_of_vhost.json");

        List<String> queuesWithNoConsumers = rabbitMQManagementClient.getQueuesWithNoConsumers("ListenerExchange", "/");
        Assert.assertEquals(2, queuesWithNoConsumers.size());
        Assert.assertEquals("ListenerQ2", queuesWithNoConsumers.get(0));
        Assert.assertEquals("ListenerQ3", queuesWithNoConsumers.get(1));
    }

    */
    /**
     *
     * @param url
     * @param responseFilePath
     *//*

    private void registerSuccessResponseWithBody(String url, String responseFilePath) {

        URL fileURL = this.getClass().getResource(responseFilePath);

        String responseBody;
        try {
            responseBody = new Scanner(new File(URLDecoder.decode(fileURL.getFile(), "utf-8")), "UTF8").useDelimiter("\\Z").next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HttpRequestHandler requestHandlerForURL = getRequestHandler(200, responseBody);

        log.debug("registering url {}", url);
        server.register(url, requestHandlerForURL);
    }

    */
    /**
     *
     * @param url
     * @param statusCode
     *//*

    protected void registerStatusCodeResponseWithNoBody(String url, int statusCode) {

        HttpRequestHandler requestHandlerForURL = getRequestHandler(statusCode, null);

        log.debug("registering url {}", url);
        server.register(url, requestHandlerForURL);
    }

    */
    /**
     *
     * @param statusCode
     * @param responseBody
     * @return
     *//*

    private HttpRequestHandler getRequestHandler(final int statusCode, final String responseBody) {

        HttpRequestHandler handler = new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                log.debug("Received request: {}", request);
                response.setStatusCode(statusCode);
                if (responseBody != null) {
                    response.setEntity(new StringEntity(responseBody));
                }
            }
        };

        return handler;
    }
*/
}
