package intellij.haskell.testIntegration;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
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
    private TextFieldWithBrowseButton targetDir;
    private JTextField fileName;

    public CreateHaskellTestDialog(@Nullable Project project) {
        // Basic configuration required by DialogWrapper
        super(project);
        init();
        setTitle("Create New Test");

        // Fields initialization
        targetDir.addBrowseFolderListener("Select Target Directory", null, project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor());
        targetDir.setEditable(false);
        targetDir.addActionListener(actionEvent -> getOKAction().setEnabled(isValid()));

        fileName.getDocument().addDocumentListener(new MandatoryTextFieldListener());
    }

    /**
     * Needed by Intellij to bind this class to the form.
     */
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    private boolean isValid() {
        return !StringUtil.isEmptyOrSpaces(getTargetDir());
    }
    public String getTargetDir() {
        return targetDir.getText().trim();
    }
    public void setTargetDir(String text) {
        targetDir.setText(text);
    }
    public String getFileName() {
        return fileName.getText().trim();
    }
    public void setFileName(String text) {
        fileName.setText(text);
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
