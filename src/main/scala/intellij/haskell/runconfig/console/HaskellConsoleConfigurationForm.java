package intellij.haskell.runconfig.console;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.RawCommandLineEditor;
import intellij.haskell.module.HaskellModuleType;
import intellij.haskell.runconfig.HaskellStackConfigurationBase;
import intellij.haskell.util.HaskellUIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HaskellConsoleConfigurationForm extends SettingsEditor<HaskellStackConfigurationBase> {
    private JPanel myPanel;
    private TextFieldWithBrowseButton myWorkingDirPathField;
    private JComboBox myModuleComboBox;
    private RawCommandLineEditor myConsoleArgsEditor;

    public HaskellConsoleConfigurationForm(@NotNull Project project) {
        myModuleComboBox.setEnabled(true);
        HaskellUIUtil.installWorkingDirectoryChooser(myWorkingDirPathField, project);
    }

    @Override
    protected void resetEditorFrom(@NotNull HaskellStackConfigurationBase config) {
        myModuleComboBox.removeAllItems();
        for (Module module : config.getValidModules()) {
            if (ModuleType.get(module) == HaskellModuleType.getInstance()) {
                //noinspection unchecked
                myModuleComboBox.addItem(module);
            }
        }
        //noinspection unchecked
        myModuleComboBox.setRenderer(getListCellRendererWrapper());
        myModuleComboBox.setSelectedItem(config.getConfigurationModule().getModule());

        myWorkingDirPathField.setText(config.getWorkingDirPath());
        myConsoleArgsEditor.setText(config.getConsoleArgs());
    }

    @Override
    protected void applyEditorTo(@NotNull HaskellStackConfigurationBase config) throws ConfigurationException {
        config.setModule((Module) myModuleComboBox.getSelectedItem());
        config.setWorkingDirPath(myWorkingDirPathField.getText());
        config.setConsoleArgs(myConsoleArgsEditor.getText());
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

    @NotNull
    private static ListCellRendererWrapper<Module> getListCellRendererWrapper() {
        return new ListCellRendererWrapper<Module>() {
            @Override
            public void customize(JList list, @Nullable Module module, int index, boolean selected, boolean hasFocus) {
                if (module != null) {
                    setText(module.getName());
                }
            }
        };
    }
}
