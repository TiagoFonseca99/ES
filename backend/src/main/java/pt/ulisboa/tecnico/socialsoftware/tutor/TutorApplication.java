package pt.ulisboa.tecnico.socialsoftware.tutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@PropertySource({ "classpath:application.properties" })
@EnableJpaRepositories
@EnableTransactionManagement
@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TutorApplication extends SpringBootServletInitializer implements InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(TutorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TutorApplication.class, args);
    }

    @Bean
    public TaskExecutor notifyExecutor() {
        logger.info("Notifying observers");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("NotifyThread");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();

        return executor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Run on startup
    }
}
