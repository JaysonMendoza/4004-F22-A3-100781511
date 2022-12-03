package ca.jkmconsulting.crazyEightsCountdown;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class CrazyEightsCountdownDedicatedServer {
    private final Logger LOG = LoggerFactory.getLogger(CrazyEightsCountdownDedicatedServer.class);
    public static void main(String[] args) {
        SpringApplication.run(CrazyEightsCountdownDedicatedServer.class, args);
    }

    @Bean(name="asyncExecutor")
    public Executor asyncExecutor()
    {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("AsyncThreadQuestGame-");
        executor.initialize();
        return executor;
    }
}
