package com.mz.libot.core.handlers;

import net.dv8tion.jda.api.JDA;

public abstract class HandlerParameter {

	protected JDA jda;

	public JDA getJDA() {
		return this.jda;
	}

	public void setJDA(JDA jda) {
		this.jda = jda;
	}
}
