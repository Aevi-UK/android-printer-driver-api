package com.aevi.print.model;

import com.aevi.android.rxmessenger.ChannelServer;
import com.aevi.android.rxmessenger.MessageException;

public class ChannelPrintingContext implements PrintingContext {
    private ChannelServer channelServer;

    public ChannelPrintingContext(ChannelServer channelServer) {
        this.channelServer = channelServer;
    }

    @Override
    public boolean send(String message) {
        return channelServer.send(message);
    }

    @Override
    public boolean sendEndStream() {
        return channelServer.sendEndStream();
    }

    @Override
    public boolean sendError(String code, String message) {
        return channelServer.send(new MessageException(code, message));
    }
}
