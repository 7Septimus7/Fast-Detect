package com.processdataquality.praeclarus.ui.component;

import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;

public class DetectionOutput {

    private final String imperfectionPattern;
    private final String detectionAlgorithm;
    private final String count;
    private ProgressBar progressBar;
    private final double expectedValue;

    public DetectionOutput(String imperfectionPattern, String detectionAlgorithm, String count, double expectedValue) {
        this.imperfectionPattern = imperfectionPattern;
        this.detectionAlgorithm = detectionAlgorithm;
        this.count = count;
        this.expectedValue = expectedValue;
        buildProgressBar();
    }

    private void buildProgressBar() {
        progressBar = new ProgressBar();
        progressBar.setValue(calculateProgressBar());
        if (calculateProgressBar() < 0.2) {
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
        } else if (calculateProgressBar() == 1) {
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        }
    }

    private double calculateProgressBar() {
        double n = Double.parseDouble(count.split(" ")[2]);
        double x = Double.parseDouble(count.split(" ")[0]);
        double z = Math.log(0.5)/Math.log(expectedValue);
        return 1 - Math.pow(x/n, z);
    }

    public String getImperfectionPattern() {
        return imperfectionPattern;
    }

    public String getDetectionAlgorithm() {
        return detectionAlgorithm;
    }

    public String getCount() {
        return count;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
