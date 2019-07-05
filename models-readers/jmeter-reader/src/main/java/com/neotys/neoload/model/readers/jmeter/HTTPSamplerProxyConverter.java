package com.neotys.neoload.model.readers.jmeter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.neotys.neoload.model.listener.EventListener;
import com.neotys.neoload.model.v3.project.userpath.Request;
import com.neotys.neoload.model.v3.project.userpath.Step;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;


public class HTTPSamplerProxyConverter implements BiFunction<HTTPSamplerProxy, HashTree, List<Step>> {

    private final EventListener eventListener;
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPSamplerProxyConverter.class);

    HTTPSamplerProxyConverter(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public List<Step> apply(HTTPSamplerProxy httpSamplerProxy, HashTree hashTree) {
        eventListener.readSupportedAction("HTTPSampler");
        Optional<String> domain = Optional.ofNullable(Strings.emptyToNull(httpSamplerProxy.getDomain()));
        Optional<String> path = Optional.ofNullable(Strings.emptyToNull(httpSamplerProxy.getPath()));
        Optional<String> protocol = Optional.ofNullable(Strings.emptyToNull(httpSamplerProxy.getProtocol()));
        int port = httpSamplerProxy.getPort();
        final Request.Builder req = Request.builder()
                .method(httpSamplerProxy.getMethod())
                .description(httpSamplerProxy.getComment());
        createParameters(httpSamplerProxy, req);

        if(hashTree.get(httpSamplerProxy)!=null) {
            HTTPHeaderConverter.createHeader(httpSamplerProxy, req, hashTree);
            eventListener.readSupportedAction("HTTPHeaderManager");
        } else{
            LOGGER.warn("There is not HeaderManager so HTTPRequest do not have Header");
            eventListener.readSupportedFunctionWithWarn("", "HttpRequest", null,"Don't have Header Manager");
        }
        createServer(httpSamplerProxy, domain, path, protocol, port, req);
        return ImmutableList.of(req.build());
    }

    private void createParameters(HTTPSamplerProxy httpSamplerProxy, Request.Builder req) {
        StringBuilder parameter = new StringBuilder();
        CollectionProperty collectionParameter = httpSamplerProxy.getArguments().getArguments();

        for (JMeterProperty ValueParameter : collectionParameter) {
            if (ValueParameter instanceof TestElementProperty) {
                HTTPArgument httpArgument = (HTTPArgument) ValueParameter.getObjectValue();
                parameter.append(httpArgument.getEncodedName());
                parameter.append(httpArgument.getMetaData());
                parameter.append((httpArgument.getEncodedValue()));
                parameter.append("&");
            }
        }
        if(!parameter.toString().isEmpty()){
            parameter.deleteCharAt(parameter.length()-1);
        }
        req.body(parameter.toString());
        LOGGER.info("Convert Parameters is a success");
        LOGGER.warn("If the Parameter in Neoload are strange, Please check that you have encoded the parameters in JMeter");
        eventListener.readSupportedAction("Put parameters into HttpRequest");
    }

    private void createServer(HTTPSamplerProxy httpSamplerProxy, Optional<String> domain, Optional<String> path, Optional<String> protocol, int port, Request.Builder req) {
        Servers.addServer(domain.orElse("host"), httpSamplerProxy.getPort(), httpSamplerProxy.getProtocol());
        String url = protocol.orElse("http") + "://" + domain.orElse("host") + ":" + port + path.orElse("/");
        req.url(url);
        req.server(domain.orElse("host"));
        path.ifPresent(req::name);
        eventListener.readSupportedAction("HTTPSampler");
    }
}