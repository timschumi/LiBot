package com.mz.libot.core.music.entities.exceptions;

public class QueueFullException extends Exception {

	public QueueFullException() {
		super("You can not add more elements into that track queue!");
	}

}
