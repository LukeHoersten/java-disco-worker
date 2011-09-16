package com.allstontrading.disco.worker.protocol.encode;

import com.allstontrading.disco.worker.protocol.encode.types.RequestMessageName;

/**
 * @author Luke Hoersten <lhoersten@allstontrading.com>
 * 
 */
public class FatalEncoder extends AbstractDiscoWorkerEncoder {

	public FatalEncoder() {
		super(RequestMessageName.FATAL);
	}

	public FatalEncoder set(final String message) {
		setPayload('"' + message + '"');
		return this;
	}

}