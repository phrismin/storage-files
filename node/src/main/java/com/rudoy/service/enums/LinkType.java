package com.rudoy.service.enums;

public enum LinkType {
    GET_PHOTO("file/getPhoto"),
    GET_DOC("file/getDoc");

    private final String link;

    LinkType(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return link;
    }
}
