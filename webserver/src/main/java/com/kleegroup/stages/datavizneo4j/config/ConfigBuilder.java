package com.kleegroup.stages.datavizneo4j.config;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.discovery.ModuleDiscoveryFeatures;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.param.xml.XmlParamPlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.plugins.resource.local.LocalResourceResolverPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.impl.definitions.OrchestraDefinitionManagerImpl;
import io.vertigo.orchestra.impl.services.OrchestraServicesImpl;
import io.vertigo.orchestra.plugins.definitions.memory.MemoryProcessDefinitionStorePlugin;
import io.vertigo.orchestra.plugins.services.execution.memory.MemoryProcessExecutorPlugin;
import io.vertigo.orchestra.plugins.services.schedule.memory.MemoryProcessSchedulerPlugin;
import io.vertigo.orchestra.services.OrchestraServices;
import io.vertigo.vega.VegaFeatures;

public class ConfigBuilder {

	public static AppConfig createAppConfigBuilder() {
		final Features myModule = new ModuleDiscoveryFeatures("myModule") {
			@Override
			protected String getPackageRoot() {
				return "com.kleegroup.stages.datavizneo4j";
			}
		};

		final AppConfigBuilder appConfigBuilder = AppConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.addPlugin(LocalResourceResolverPlugin.class)
				.addPlugin(XmlParamPlugin.class, Param.of("url", "conf/application-config.xml"))
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache(MemoryCachePlugin.class)
						.withScript()
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.build())
				.addModule(new VegaFeatures()
						.withEmbeddedServer(8080)
						.build())
				.addModule(ModuleConfig.builder("orchestra")
						.addPlugin(MemoryProcessDefinitionStorePlugin.class)
						.addPlugin(MemoryProcessSchedulerPlugin.class)
						.addPlugin(MemoryProcessExecutorPlugin.class,
								Param.of("workersCount", String.valueOf(10)))
						.addComponent(OrchestraDefinitionManager.class, OrchestraDefinitionManagerImpl.class)
						.addComponent(OrchestraServices.class, OrchestraServicesImpl.class)
						.build())
				.addModule(myModule.build());

		return appConfigBuilder
				.addInitializer(OrchestraDefinitionInitializer.class)
				.build();
	}

}
