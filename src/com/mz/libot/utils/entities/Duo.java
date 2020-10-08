package com.mz.libot.utils.entities;

public class Duo<L, R> {

	private L left;
	private R right;

	public Duo() {}

	public Duo(L key, R value) {
		this.left = key;
		this.right = value;
	}

	public L getLeft() {
		return this.left;
	}

	public R getRight() {
		return this.right;
	}

	public R setRight(R value) {
		R old = this.right;
		this.right = value;
		return old;
	}

	public L setLeft(L value) {
		L old = this.left;
		this.left = value;
		return old;
	}
}
