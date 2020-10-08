package com.mz.libot.utils.entities;

public class EvalResult {

	private Object result;
	private String output;
	private String errOutput;

	public EvalResult(Object result, String output, String errOutput) {
		this.result = result;
		this.output = output;
		this.errOutput = errOutput;
	}

	public Object getResult() {
		return this.result;
	}

	public String getOutput() {
		return this.output;
	}

	public String getErrorOutput() {
		return this.errOutput;
	}

}
