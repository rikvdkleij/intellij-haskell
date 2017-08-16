package intellij.haskell.runconfig.console;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HaskellConsoleConfigurationForm extends SettingsEditor<HaskellConsoleConfiguration> {
    private JPanel myPanel;
    private JComboBox targetcomboBox;

    public HaskellConsoleConfigurationForm(@NotNull Project project) {
        targetcomboBox.setEnabled(true);
    }

    @Override
    protected void resetEditorFrom(@NotNull HaskellConsoleConfiguration config) {
        targetcomboBox.removeAllItems();
        for (String name : config.getStackTargetNames()) {
            //noinspection unchecked
            targetcomboBox.addItem(name);
        }
        //noinspection unchecked
        targetcomboBox.setSelectedItem(config.getStackTarget());
    }

    @Override
    protected void applyEditorTo(@NotNull HaskellConsoleConfiguration config) throws ConfigurationException {
        config.setStackTarget((String) targetcomboBox.getSelectedItem());
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
