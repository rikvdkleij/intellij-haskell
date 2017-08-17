package intellij.haskell.runconfig.test;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HaskellTestConfigurationForm extends SettingsEditor<HaskellTestConfiguration> {
    private JPanel myPanel;
    private RawCommandLineEditor stackArgsEditor;
    private JComboBox myTestsuiteComboBox;
    private RawCommandLineEditor myTestFilterTextField;
    private JButton button;

    public HaskellTestConfigurationForm(@NotNull Project project) {
        try {
            final URI uri = new URI("http://hspec.github.io/options.html");
            class OpenUrlAction implements ActionListener {
                @Override
                public void actionPerformed(ActionEvent e) {
                    open(uri);
                }
            }
            button.addActionListener(new OpenUrlAction());
            button.setText("http://hspec.github.io/options.html");
        } catch (URISyntaxException e) {
            Messages.showErrorDialog("Error while creating URI action to hspec site", "Can not create URI action");
        }
    }

    @Override
    protected void resetEditorFrom(@NotNull HaskellTestConfiguration config) {
        myTestsuiteComboBox.removeAllItems();
        for (String executable : config.getTestSuiteTargetNames()) {
            //noinspection unchecked
            myTestsuiteComboBox.addItem(executable);
        }
        myTestsuiteComboBox.setSelectedItem(config.getTestSuiteTargetName());

        stackArgsEditor.setText(config.getStackArgs());
        myTestFilterTextField.setText(config.getTestArguments());
    }

    @Override
    protected void applyEditorTo(@NotNull HaskellTestConfiguration config) throws ConfigurationException {
        config.setStackArgs(stackArgsEditor.getText());
        config.setTestSuiteTargetName((String) myTestsuiteComboBox.getSelectedItem());
        config.setTestArguments(myTestFilterTextField.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    @Override
    protected void disposeEditor() {
        myPanel.setVisible(false);
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                Messages.showErrorDialog("Error while opening link", "Can not Open Link");
            }
        } else {
            Messages.showErrorDialog("Can not open link because Desktop is not supported", "Can not Open Link");
        }
    }
}
