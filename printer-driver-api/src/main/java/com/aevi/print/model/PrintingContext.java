package com.aevi.print.model;

public interface PrintingContext {
    /**
     * Send a message to the client
     *
     * @param message The message to send
     * @return True if the message was successfully sent
     */
    boolean send(String message);

    /**
     * Send end of stream message back to the client and close the stream
     *
     * @return True if the end message was sent successfully
     */
    boolean sendEndStream();

    /**
     * Send an error condition to the client
     *
     * @param code The error code to send
     * @param message The error message to send
     * @return True if the exception was successfully sent
     */
    boolean sendError(String code, String message);
}
