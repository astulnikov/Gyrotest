package com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.dagger;

import com.stulnikov.arduino.arduinoBt.gyrotest.gyrotest.MainActivity;

import dagger.Component;

/**
 * @author alexeystulnikov 2/28/17.
 */
@Component(modules = {PresenterModule.class}, dependencies = ApplicationComponent.class)
@PerActivity
public interface PresenterComponent {

    void inject(MainActivity mainActivity);
}
