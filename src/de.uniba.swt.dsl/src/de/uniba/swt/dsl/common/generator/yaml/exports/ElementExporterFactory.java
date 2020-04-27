package de.uniba.swt.dsl.common.generator.yaml.exports;

import de.uniba.swt.dsl.bahn.*;
import de.uniba.swt.dsl.common.util.ExtraBlockElement;
import de.uniba.swt.dsl.common.util.PointAspect;
import de.uniba.swt.dsl.common.util.YamlExporter;

public class ElementExporterFactory {
    public static <T> void build(YamlExporter exporter, T obj) {
        if (obj instanceof SegmentElement) {
            new SegmentElementYamlExporter().build(exporter, (SegmentElement) obj);
            return;
        }

        if (obj instanceof RegularSignalElement) {
            new RegularSignalElementYamlExporter().build(exporter, (RegularSignalElement) obj);
            return;
        }

        if (obj instanceof CompositionSignalElement) {
            new CompositionSignalElementYamlExporter().build(exporter, (CompositionSignalElement) obj);
            return;
        }

        if (obj instanceof AspectElement) {
            new AspectElementYamlExporter().build(exporter, (AspectElement) obj);
            return;
        }

        if (obj instanceof PointElement) {
            new PointElementYamlExporter().build(exporter, (PointElement) obj);
            return;
        }

        if (obj instanceof PointAspect) {
            new PointAspectYamlExporter().build(exporter, (PointAspect) obj);
            return;
        }

        if (obj instanceof BoardElement) {
            new BoardElementYamlExporter().build(exporter, (BoardElement) obj);
            return;
        }

        if (obj instanceof BoardFeatureElement) {
            new BoardFeatureElementYamlExporter().build(exporter, (BoardFeatureElement) obj);
            return;
        }

        if (obj instanceof TrainElement) {
            new TrainElementYamlExporter().build(exporter, (TrainElement) obj);
            return;
        }

        if (obj instanceof TrainPeripheral) {
            new TrainPeripheralYamlExporter().build(exporter, (TrainPeripheral) obj);
            return;
        }

        if (obj instanceof ExtraBlockElement) {
            new ExtraBlockElementYamlExporter().build(exporter, (ExtraBlockElement) obj);
            return;
        }

        if (obj instanceof CrossingElement) {
            new CrossingElementYamlExporter().build(exporter, (CrossingElement) obj);
            return;
        }

        if (obj instanceof SignalType) {
            new SignalTypeYamlExporter().build(exporter, (SignalType) obj);
            return;
        }
    }
}
