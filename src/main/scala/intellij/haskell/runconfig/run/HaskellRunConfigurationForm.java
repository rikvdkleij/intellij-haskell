package intellij.haskell.runconfig.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HaskellRunConfigurationForm extends SettingsEditor<HaskellRunConfiguration> {
    private JPanel myPanel;
    private RawCommandLineEditor stackArgsEditor;
    private JComboBox myExecutableComboBox;
    private RawCommandLineEditor programArgsEditor;

    public HaskellRunConfigurationForm() {
    }

    @Override
    protected void resetEditorFrom(@NotNull HaskellRunConfiguration config) {
        myExecutableComboBox.removeAllItems();
        for (String executable : config.getExecutableNames()) {
            //noinspection unchecked
            myExecutableComboBox.addItem(executable);
        }
        myExecutableComboBox.setSelectedItem(config.getExecutableName());

        stackArgsEditor.setText(config.getStackArgs());
        programArgsEditor.setText(config.getProgramArgs());
    }

    @Override
    protected void applyEditorTo(@NotNull HaskellRunConfiguration config) throws ConfigurationException {
        config.setStackArgs(stackArgsEditor.getText());
        config.setExecutableName((String) myExecutableComboBox.getSelectedItem());
        config.setProgramArgs(programArgsEditor.getText());
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
