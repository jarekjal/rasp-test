/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jarekjal;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.platform.Platforms;
import com.pi4j.util.Console;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author luca
 */
public class Main {

    private static final int PIN_LED = 21; // PIN ?? = BCM 21 (pin 15 = bcm 22)
    private static final int PIN_BUTTON = 24; // PIN 18 = BCM 24

    private static final Console console = new Console();

    private static int turns = 10;
    private static int pressCount = 0;
    private static long startTime;
    private static long elapsedTime;
    private static final List<Long> times = new ArrayList<>();


    public static void main(String[] args) {
        console.box("Gra czas reakcji...");
        Context pi4j = null;
        try {
            pi4j = Pi4J.newAutoContext();
            new Main().run(pi4j);
        } catch (InvocationTargetException e) {
            console.println("Error: " + e.getTargetException().getMessage());
        } catch (Exception e) {
            console.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (pi4j != null) {
                pi4j.shutdown();
            }
        }
    }

    private void run(Context pi4j) throws Exception {
        describePlatform(pi4j);

        DigitalOutput led = createLED(pi4j);
        DigitalInput button = createButton(pi4j);


        while (nextTurn()) {
            console.println("* * *   " + pressCount + 1 + "   * * *");
            CountDownLatch countDownLatch = new CountDownLatch(1);
            DigitalStateChangeListener digitalStateChangeListener = e -> {
                if (e.state() == DigitalState.LOW && led.state().equals(DigitalState.HIGH)) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    pressCount++;
                    console.println("Guzik " + e.source() + " nacisniety " + pressCount + " razy");
                    console.println("Czas reakcji: " + elapsedTime + " ms");
                    times.add(elapsedTime);
                    led.low();
                    console.println("Dioda zgaszona");
                    countDownLatch.countDown();
                }
            };
            button.addListener(digitalStateChangeListener);
            Thread.sleep(5000);
            led.high();
            console.println("Dioda zapalona");
            startTime = System.currentTimeMillis();
            countDownLatch.await();
            button.removeListener(digitalStateChangeListener);
        }
        console.println("Koniec!");
        console.println("Czasy: " + times);
    }

    private boolean nextTurn() {
        turns--;
        return turns >= 0;
    }

    private DigitalInput createButton(Context pi4j) {
        var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("button")
                .name("Press button")
                .address(PIN_BUTTON)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L)
                .provider("pigpio-digital-input");
        return pi4j.create(buttonConfig);
    }

    private DigitalOutput createLED(Context pi4j) {
        var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("LED Flasher")
                .address(PIN_LED)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
        return pi4j.create(ledConfig);
    }

    private void describePlatform(Context pi4j) {
        Platforms platforms = pi4j.platforms();
        console.box("Pi4J PLATFORMS");
        console.println();
        platforms.describe().print(System.out);
        console.println();
    }
}
