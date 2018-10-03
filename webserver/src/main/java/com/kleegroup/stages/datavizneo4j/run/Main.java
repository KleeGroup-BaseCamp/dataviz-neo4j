package com.kleegroup.stages.datavizneo4j.run;

import java.io.IOException;

import com.kleegroup.stages.datavizneo4j.config.ConfigBuilder;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.core.component.di.injector.DIInjector;

public class Main {

	public static void main(final String[] args) throws IOException {

		try (final AutoCloseableApp app = new AutoCloseableApp(ConfigBuilder.createAppConfigBuilder())) {
			final Main sample = new Main();
			DIInjector.injectMembers(sample, app.getComponentSpace());
			//------
			System.in.read();
		}

	}

}
