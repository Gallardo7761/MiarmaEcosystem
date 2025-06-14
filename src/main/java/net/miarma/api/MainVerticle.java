package net.miarma.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.Promise;
import net.miarma.api.common.ConfigManager;
import net.miarma.api.common.Constants;
import net.miarma.api.common.security.SecretManager;
import net.miarma.api.common.vertx.VertxJacksonConfig;
import net.miarma.api.microservices.core.verticles.CoreMainVerticle;
import net.miarma.api.microservices.huertos.verticles.HuertosMainVerticle;
import net.miarma.api.microservices.huertosdecine.verticles.CineMainVerticle;
import net.miarma.api.microservices.miarmacraft.verticles.MMCMainVerticle;
import net.miarma.api.util.DeploymentUtil;
import net.miarma.api.util.MessageUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MainVerticle extends AbstractVerticle {
	private ConfigManager configManager;
	
	public static void main(String[] args) {
		Launcher.executeCommand("run", MainVerticle.class.getName());
	}
	
	private void init() {	
		this.configManager = ConfigManager.getInstance();
	    initializeDirectories();
	    copyDefaultConfig();
	    this.configManager.loadConfig();
	    SecretManager.getOrCreateSecret();
        VertxJacksonConfig.configure();
    }
	
	@Override
	public void start(Promise<Void> startPromise) {
		try {
			init();
			deploy(startPromise);
			startPromise.complete();
		} catch (Exception e) {
			startPromise.fail(e);
		}
	}

	@Override
	public void stop(Promise<Void> stopPromise) {
		vertx.deploymentIDs().forEach(id -> vertx.undeploy(id));
		stopPromise.complete();
	}
	
	private void deploy(Promise<Void> startPromise) {
		vertx.deployVerticle(new CoreMainVerticle(), result -> {
            if (result.succeeded()) {
            	Constants.LOGGER.info(
            			DeploymentUtil.successMessage(CoreMainVerticle.class));
            } else {
            	Constants.LOGGER.error(
            			DeploymentUtil.failMessage(CoreMainVerticle.class, result.cause()));
            }
        });
		
		vertx.deployVerticle(new HuertosMainVerticle(), result -> {
			if (result.succeeded()) {
				Constants.LOGGER.info(
						DeploymentUtil.successMessage(HuertosMainVerticle.class));
			} else {
				Constants.LOGGER.error(
						DeploymentUtil.failMessage(HuertosMainVerticle.class, result.cause()));
			}
		});
		
		vertx.deployVerticle(new MMCMainVerticle(), result -> {
			if (result.succeeded()) {
				Constants.LOGGER.info(
						DeploymentUtil.successMessage(MMCMainVerticle.class));
			} else {
				Constants.LOGGER.error(
						DeploymentUtil.failMessage(MMCMainVerticle.class, result.cause()));
			}
		});

		vertx.deployVerticle(new CineMainVerticle(), result -> {
			if (result.succeeded()) {
				Constants.LOGGER.info(
						DeploymentUtil.successMessage(CineMainVerticle.class));
			} else {
				Constants.LOGGER.error(
						DeploymentUtil.failMessage(CineMainVerticle.class, result.cause()));
			}
		});
	}
	
	private void initializeDirectories() {        
        File baseDir = new File(this.configManager.getBaseDir());
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }
    
    private void copyDefaultConfig() {
        File configFile = new File(configManager.getConfigFile().getAbsolutePath());
        if (!configFile.exists()) {
            try (InputStream in = MainVerticle.class.getClassLoader().getResourceAsStream("default.properties")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                	Constants.LOGGER.error(
                			MessageUtil.notFound("Default config", "resources"));
                }
            } catch (IOException e) {
                Constants.LOGGER.error(
                		MessageUtil.failedTo("copy", "default config", e));
            }
        }
    }
    
}
