package com.allstontrading.disco.worker.task;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import com.allstontrading.disco.worker.protocol.DiscoIOChannel;
import com.allstontrading.disco.worker.protocol.decode.types.DiscoInput;
import com.allstontrading.disco.worker.protocol.decode.types.DiscoInputReplica;

/**
 * @author Luke Hoersten <lhoersten@allstontrading.com>
 * 
 */
public abstract class DiscoTask {

	private static final String WORKING_DIR_NAME_FORMAT = "{0}_{1}_{2}";

	private final int taskId;
	private final File workingDir;
	private final DiscoTaskInputFetcher inputFetcher;

	public DiscoTask(final DiscoIOChannel discoIOChannel, final int taskId, final int discoPort) {
		this.taskId = taskId;
		this.inputFetcher = new DiscoTaskInputFetcher(discoIOChannel, discoPort);

		this.workingDir = new File(getWorkingDirName());
		this.workingDir.mkdirs();
	}

	public File getWorkingDir() {
		return workingDir;
	}

	private String getWorkingDirName() {
		return MessageFormat.format(WORKING_DIR_NAME_FORMAT, taskId, getTaskTypeName(), System.currentTimeMillis());
	}

	protected abstract String getTaskTypeName();

	protected DiscoTaskInputFetcher getInputFetcher() {
		return inputFetcher;
	}

	public void ok() {}

	public void input(final boolean isDone, final List<DiscoInput> inputs) {
		inputFetcher.input(isDone, inputs);
	}

	public void fail(final int inputId, final List<Integer> replicaIds) {
		inputFetcher.fail(inputId, replicaIds);
	}

	public void retry(final List<DiscoInputReplica> replicas) {
		inputFetcher.retry(replicas);
	}

	public void pause(final int seconds) {
		inputFetcher.pause(seconds);
	}

}