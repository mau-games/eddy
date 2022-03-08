module EvolutionaryDungeonDesigner
{
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;

    requires org.apache.commons.io;
    requires com.google.common;
    requires gson;
    requires java.sql;
    requires slf4j.api;

    opens gui;
    opens gui.interactive;
    opens gui.controls;
    opens gui.utils;
    opens gui.views;
    opens gui.ML;
    opens game.narrative;

    opens graphics;
    opens graphics.dungeonbrushes;
    opens graphics.examples;
    opens graphics.tiles;
    opens graphics.mesopatterns;

    opens config;
    opens config.profiles;
    opens config.ranges;
    opens config.test_batches;

    opens generator.algorithm.MAPElites.Dimensions;
    opens generator.algorithm.MAPElites.grammarDimensions;

    opens runners to javafx.fxml;
    exports runners;
}