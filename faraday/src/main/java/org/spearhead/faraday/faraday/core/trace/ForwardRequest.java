package org.spearhead.faraday.faraday.core.trace;

import static org.spearhead.faraday.faraday.core.utils.BodyConverter.convertBodyToString;

public class ForwardRequest extends IncomingRequest {

    protected String mappingName;
    protected byte[] body;

    public String getMappingName() { return mappingName; }

    protected void setMappingName(String mappingName) { this.mappingName = mappingName; }

    public String getBodyAsString() { return convertBodyToString(body); }

    public byte[] getBody() { return body; }

    protected void setBody(byte[] body) { this.body = body; }
}
