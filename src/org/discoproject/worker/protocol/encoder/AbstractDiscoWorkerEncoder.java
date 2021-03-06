package org.discoproject.worker.protocol.encoder;

import org.discoproject.worker.protocol.encoder.types.RequestMessageName;

/**
 * @author Luke Hoersten <lhoersten@allstontrading.com>
 * 
 */
public abstract class AbstractDiscoWorkerEncoder {

	protected static final String QUOTE = "\"";
	private static final char SPACE = ' ';
	private static final char NEWLINE = '\n';

	private final RequestMessageName name;
	private String payload = "";
	private int payloadLength = 0;

	public AbstractDiscoWorkerEncoder(final RequestMessageName name) {
		this.name = name;
	}

	protected RequestMessageName getName() {
		return name;
	}

	protected long payloadLength() {
		return payloadLength;
	}

	protected void setPayload(final String payload) {
		this.payloadLength = payload.length();
		this.payload = payload;
	}

	@Override
	public String toString() {
		return name.toString() + SPACE + payloadLength + SPACE + payload + NEWLINE;
	}

}
