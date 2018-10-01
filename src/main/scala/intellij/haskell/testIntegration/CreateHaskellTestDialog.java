package intellij.haskell.testIntegration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Binding of the "Create test" dialog. Responsible for fields declaration, validation and exiting the dialog (either by clicking OK, Cancel or closing the window).
 *
 * @implNote This is a Java file because Intellij integration of dialogs / forms only supports Java, not Scala.
 */
public class CreateHaskellTestDialog extends DialogWrapper {
    private JPanel mainPanel;
    private JTextField moduleName;

    public CreateHaskellTestDialog(@Nullable Project project) {
        // Basic configuration required by DialogWrapper
        super(project);
        init();
        setTitle("Create New Test Module");

        // Fields configuration
        moduleName.getDocument().addDocumentListener(new MandatoryTextFieldListener());
    }

    /**
     * @return A unique key so that dialog window resizing is remembered for future tests dialog creation
     */
    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "#haskell.test.module.creation.dialog";
    }
    /**
     * Needed by Intellij to bind this class to the form.
     */
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    private boolean isValid() {
        return !StringUtil.isEmptyOrSpaces(moduleName.getText());
    }
    public String getModuleName() {
        return moduleName.getText().trim();
    }
    public void setModuleName(String text) {
        moduleName.setText(text);
    }

    private class MandatoryTextFieldListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            getOKAction().setEnabled(isValid());
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            getOKAction().setEnabled(isValid());
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            getOKAction().setEnabled(isValid());
        }
    }
}
