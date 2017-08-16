package intellij.haskell.runconfig.test;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HaskellTestConfigurationForm extends SettingsEditor<HaskellTestConfiguration> {
    private JPanel myPanel;
    private RawCommandLineEditor stackArgsEditor;
    private JComboBox myTestsuiteComboBox;
    private RawCommandLineEditor myTestFilterTextField;

    public HaskellTestConfigurationForm(@NotNull Project project) {
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
}
