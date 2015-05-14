package org.elasticsearch.action;

import com.bazaarvoice.elasticsearch.client.core.spi.RestExecutor;
import com.bazaarvoice.elasticsearch.client.core.spi.RestResponse;
import org.elasticsearch.common.base.Function;
import org.elasticsearch.common.util.concurrent.ListenableFuture;

/**
 * Abstracts the function of taking some kind of ES request, sending it
 * and returning a future of the response.
 *
 * @param <Request>  the request type to send
 * @param <Response> the response type to return
 */
public abstract class AbstractRestClientAction<Request, Response> {
    protected final String protocol;
    protected final String host;
    protected final int port;
    protected final RestExecutor executor;
    protected final Function<RestResponse, Response> responseTransform;

    public AbstractRestClientAction(final String protocol, final String host, final int port, final RestExecutor executor, final Function<RestResponse, Response> responseTransform) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;

        this.executor = executor;
        this.responseTransform = responseTransform;
    }

    /**
     * Asynchronously execute the request
     *
     * @param request the request to send
     * @return a future of the response
     */
    public abstract ListenableFuture<Response> act(Request request);
}
