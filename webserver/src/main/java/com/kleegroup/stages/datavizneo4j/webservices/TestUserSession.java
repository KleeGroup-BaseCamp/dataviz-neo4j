package com.kleegroup.stages.datavizneo4j.webservices;

import java.util.Locale;

import io.vertigo.persona.security.UserSession;

public final class TestUserSession extends UserSession {
	private static final long serialVersionUID = 1L;

	@Override
	public Locale getLocale() {
		return Locale.FRANCE;
	}
}
