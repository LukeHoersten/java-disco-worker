package com.allstontrading.disco.lib.worker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.List;

import com.allstontrading.disco.lib.worker.protocol.DiscoIOChannel;
import com.allstontrading.disco.lib.worker.protocol.decode.DiscoWorkerDecoder;
import com.allstontrading.disco.lib.worker.protocol.decode.DiscoWorkerListener;
import com.allstontrading.disco.lib.worker.protocol.decode.types.DiscoInput;
import com.allstontrading.disco.lib.worker.protocol.decode.types.DiscoInputReplica;
import com.allstontrading.disco.lib.worker.protocol.decode.types.DiscoTaskMode;
import com.allstontrading.disco.lib.worker.protocol.encode.DoneEncoder;
import com.allstontrading.disco.lib.worker.protocol.encode.ErrorEncoder;
import com.allstontrading.disco.lib.worker.protocol.encode.FatalEncoder;
import com.allstontrading.disco.lib.worker.protocol.encode.OutputEncoder;
import com.allstontrading.disco.lib.worker.protocol.encode.RequestTaskEncoder;
import com.allstontrading.disco.lib.worker.protocol.encode.WorkerAnnounceEncoder;
import com.allstontrading.disco.lib.worker.protocol.encode.types.OutputType;
import com.allstontrading.disco.lib.worker.task.DiscoMapTask;
import com.allstontrading.disco.lib.worker.task.DiscoReduceTask;
import com.allstontrading.disco.lib.worker.task.DiscoTask;

/**
 * @author Luke Hoersten <lhoersten@allstontrading.com>
 * 
 */
public class DiscoWorker implements DiscoWorkerListener {

	private static final String WORKER_PROTOCOL_VERSION = "1.0";

	private final WorkerAnnounceEncoder workerAnnounceEncoder;
	private final RequestTaskEncoder requestTaskEncoder;
	private final OutputEncoder outputEncoder;
	private final DoneEncoder doneEncoder;

	private final DiscoIOChannel discoIOChannel;
	private DiscoMapTask map;
	private DiscoReduceTask reduce;

	private final ErrorEncoder errorEncoder;
	private final FatalEncoder fatalEncoder;

	public DiscoWorker(final InputStream inputStream, final OutputStream outputStream) {
		this.discoIOChannel = new DiscoIOChannel(inputStream, outputStream, new DiscoWorkerDecoder().setListener(this));
		this.map = null;
		this.reduce = null;

		this.workerAnnounceEncoder = new WorkerAnnounceEncoder();
		this.requestTaskEncoder = new RequestTaskEncoder();
		this.outputEncoder = new OutputEncoder();
		this.doneEncoder = new DoneEncoder();
		this.errorEncoder = new ErrorEncoder();
		this.fatalEncoder = new FatalEncoder();
	}

	public void requestTask() throws IOException {
		if (!hasTask()) {
			discoIOChannel.send(workerAnnounceEncoder.set(WORKER_PROTOCOL_VERSION, getPid()));
			discoIOChannel.send(requestTaskEncoder);
		}
	}

	public InputStream getMapInput() {
		return map.getMapInput();
	}

	public List<InputStream> getReduceInputs() {
		return reduce.getReduceInputs();
	}

	public void reportOutputs(final List<File> outputs) throws IOException {
		for (final File output : outputs) {
			reportOutput(output, OutputType.disco);
		}
	}

	public void reportOutput(final File outputLocation, final OutputType outputType) throws IOException {
		discoIOChannel.send(outputEncoder.set(getTask().getWorkingDir(), outputLocation, outputType, ""));
	}

	public void doneReportingOutput() throws IOException {
		discoIOChannel.send(doneEncoder);
	}

	public void reportError(final String msg) throws IOException {
		discoIOChannel.send(errorEncoder.set(msg));
	}

	public void reportFatalError(final String msg) throws IOException {
		discoIOChannel.send(fatalEncoder.set(msg));
	}

	public boolean hasMapTask() {
		return map != null;
	}

	public boolean hasReduceTask() {
		return reduce != null;
	}

	@Override
	public void task(final String taskHost, final String masterHost, final String jobName, final int taskId, final DiscoTaskMode taskMode,
	        final int discoPort, final int putPort, final File discoData, final File ddfsData, final File jobFile) {

		// TODO use all these other task arguments to optimize input fetching.
		switch (taskMode) {
			case map:
				map = new DiscoMapTask(discoIOChannel, taskId, discoPort);
				break;
			case reduce:
				reduce = new DiscoReduceTask(discoIOChannel, taskId, discoPort);
				break;
		}
	}

	@Override
	public void ok() {
		if (hasTask()) {
			getTask().ok();
		}
	}

	@Override
	public void input(final boolean isDone, final List<DiscoInput> inputs) {
		getTask().input(isDone, inputs);
	}

	@Override
	public void fail(final int inputId, final List<Integer> replicaIds) {
		getTask().fail(inputId, replicaIds);
	}

	@Override
	public void retry(final List<DiscoInputReplica> replicas) {
		getTask().retry(replicas);
	}

	@Override
	public void pause(final int seconds) {
		getTask().pause(seconds);
	}

	private int getPid() {
		return Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
	}

	private DiscoTask getTask() {
		return hasMapTask() ? map : reduce;
	}

	private boolean hasTask() {
		return hasMapTask() || hasReduceTask();
	}

}