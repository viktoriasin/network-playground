package ru.sinvic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.sinvic.server.RequestTimeInMemoryStorage;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class);
        System.out.printf("Чтобы перейти на страницу сайта открывай: %n%s%n",
            "http://localhost:8080/ping");

        RequestTimeInMemoryStorage bean = run.getBean(RequestTimeInMemoryStorage.class);
        Thread.sleep(10500);
        System.out.println(bean.calculateRequestStatistic("/ping"));
    }
}