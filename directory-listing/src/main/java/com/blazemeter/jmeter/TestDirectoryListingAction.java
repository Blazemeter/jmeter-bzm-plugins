package com.blazemeter.jmeter;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JMeterStopThreadException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class TestDirectoryListingAction implements ActionListener {

    private final DirectoryListingConfigGui directoryListingConfigGui;

    public TestDirectoryListingAction(DirectoryListingConfigGui fileListingGui) {
        this.directoryListingConfigGui = fileListingGui;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final DirectoryListingConfig config = (DirectoryListingConfig) directoryListingConfigGui.createTestElement();

        config.setRewindOnTheEnd(false);
        config.setIndependentListPerThread(false);

        JTextArea checkArea = directoryListingConfigGui.getCheckArea();

        try {
            final CompoundVariable compVarSrcDir = new CompoundVariable();
            compVarSrcDir.setParameters(config.getSourceDirectory());
            config.setSourceDirectory(compVarSrcDir.execute());

            final CompoundVariable compVarDestVar = new CompoundVariable();
            compVarDestVar.setParameters(config.getDestinationVariableName());
            config.setDestinationVariableName(compVarDestVar.execute());

            String variableName = DirectoryListingConfig.getStringOrDefault(
                    config.getDestinationVariableName(),
                    DirectoryListingConfig.DEFAULT_DESTINATION_VARIABLE_NAME
            );

            JMeterVariables variables = new JMeterVariables();
            JMeterContextService.getContext().setVariables(variables);

            final List<String> filePaths = new ArrayList<>();

            config.testStarted();

            try {
                while (true) {
                    config.iterationStart(null);
                    filePaths.add(variables.get(variableName));
                }
            } catch (JMeterStopThreadException ex) {
                // OK
            }

            config.testEnded();

            final StringBuilder builder = new StringBuilder();

            builder.append("Listing of directory successfully finished, ").append(filePaths.size()).append(" files found:\r\n");
            for (String filePath : filePaths) {
                builder.append("${").append(variableName).append("} = ");
                builder.append(filePath);
                builder.append("\r\n");
            }

            checkArea.setText(builder.toString());
            // move scroll to top
            checkArea.setCaretPosition(0);
        } catch (RuntimeException | InvalidVariableException e) {
            checkArea.setText(e.getMessage());
        }
    }

}
