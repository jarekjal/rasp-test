/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jarekjal;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.platform.Platforms;
import com.pi4j.util.Console;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author luca
 */
public class Main {

    private static final int PIN_LED = 21; // PIN ?? = BCM 21 (pin 15 = bcm 22)

    private static final int PIN_BUTTON = 24; // PIN 18 = BCM 24

    private static int pressCount = 0;


    private static final Console console = new Console();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        console.box("Hello Rasbian world !");
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
        Platforms platforms = pi4j.platforms();

        console.box("Pi4J PLATFORMS");
        console.println();
        platforms.describe().print(System.out);
        console.println();

        var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("LED Flasher")
                .address(PIN_LED)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
        var led = pi4j.create(ledConfig);


        var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("button")
                .name("Press button")
                .address(PIN_BUTTON)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3000L)
                .provider("pigpio-digital-input");
        var button = pi4j.create(buttonConfig);

        button.addListener(e -> {
            if (e.state() == DigitalState.LOW) {
                pressCount++;
                console.println("Button " + e.source() + " was pressed for the " + pressCount + "th time");
            }
        });

        while (true) {


            while (pressCount < 3) {

            }


            int counter = 0;
            while (counter < 5) {
                if (led.equals(DigitalState.HIGH)) {
                    led.low();
                    System.out.println("counter: " + counter + ", low");
                } else {
                    led.high();
                    System.out.println("counter: " + counter + ", high");
                }
                Thread.sleep(500);
                counter++;
            }
            pressCount = 0;
        }
    }
}
