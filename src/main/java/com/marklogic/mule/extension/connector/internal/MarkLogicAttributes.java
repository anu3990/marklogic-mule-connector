package com.marklogic.mule.extension.connector.internal;

import java.io.Serializable;

public class MarkLogicAttributes implements Serializable {

    private final String mimetype;
    private String hello = "world";
    private int myNumber = 12;

    public MarkLogicAttributes(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getHello() {
        return hello;
    }

    public int getMyNumber() {
        return myNumber;
    }
}
