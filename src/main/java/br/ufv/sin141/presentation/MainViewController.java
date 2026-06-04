package br.ufv.sin141.presentation;

import br.ufv.sin141.application.controller.TheoryAppController;

public class MainViewController {

    private static final TheoryAppController APP_CONTROLLER = new TheoryAppController();

    public static TheoryAppController appController() {
        return APP_CONTROLLER;
    }
}
