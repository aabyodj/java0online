package m6t3.client;

import static m6t3.client.LoginDialog.RED;
import static m6t3.client.LoginDialog.defBgrdColor;
import static m6t3.client.StudentEditDialog.SELECT_ALL_TEXT;
import static m6t3.client.StudentEditDialog.TRAVERSE_OR_EXIT;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import m6t3.common.User;

public class UserEditDialog extends Dialog {

	private final User user;
//	private final ClientMain client;
	private final Connection connection;
	private final UsersWindow usersWindow;

	private Object result;
	private Shell shell;
	private Text txtLogin;
	private Text txtPass;
	private Text txtPassAgain;

	private Button btnAdmin;
	private boolean loginIsValid;
	private boolean passHasChanged;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public UserEditDialog(Shell parent, int style) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
//		setText("Новый пользователь");
		user = null;
		usersWindow = null;
		connection = null;
	}
	
	public UserEditDialog(Shell parent, UsersWindow usersWindow, Connection connection) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		setText("Новый пользователь");
		user = new User();
		this.usersWindow = usersWindow;
		this.connection = connection;
		loginIsValid = false;
		passHasChanged = true;
	}

	public UserEditDialog(Shell parent, UsersWindow usersWindow, Connection connection, User user) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		setText("Изменить пользователя");
		this.user = user;
		this.usersWindow = usersWindow;
		this.connection = connection;
		loginIsValid = true;
		passHasChanged = false;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(384, 207);
		shell.setText(getText());
		shell.setLayout(new FormLayout());
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		FormData fd_composite = new FormData();
		fd_composite.left = new FormAttachment(0, 10);
		fd_composite.right = new FormAttachment(100, -10);
		composite.setLayoutData(fd_composite);
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				submit();				
			}
		});
		fd_composite.bottom = new FormAttachment(100, -42);
		
		Label lblLogin = new Label(composite, SWT.NONE);
		lblLogin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLogin.setText("Логин");
		
		txtLogin = new Text(composite, SWT.BORDER);
		txtLogin.addFocusListener(SELECT_ALL_TEXT);
		txtLogin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				loginIsValid = useLogin();
				if (loginIsValid) {
					txtLogin.setBackground(defBgrdColor);
				} else {
					txtLogin.setBackground(RED);
				}
//				((Text) e.widget).clearSelection();
			}
		});
		txtLogin.addKeyListener(TRAVERSE_OR_EXIT);
		txtLogin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtLogin.setText(user.getLogin());
		
		Label lblPass = new Label(composite, SWT.NONE);
		lblPass.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPass.setText("Пароль");
		
		txtPass = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		txtPass.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {	
				if (!passHasChanged) return;
				if (txtPass.getCharCount() < 1) {
					txtPass.setBackground(RED);
					return;
				}
				if (Arrays.equals(txtPass.getTextChars(), txtPassAgain.getTextChars())) {
					txtPass.setBackground(defBgrdColor);
					txtPassAgain.setBackground(defBgrdColor);
//					passHasChanged = false;
				}
			}
		});
		txtPass.addKeyListener(TRAVERSE_OR_EXIT);
		txtPass.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				passHasChanged = true;
			}
		});
		txtPass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPassAgain = new Label(composite, SWT.NONE);
		lblPassAgain.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassAgain.setText("Подтверждение пароля");
		
		txtPassAgain = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		txtPassAgain.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {	
				if (!passHasChanged) return;
				if (txtPassAgain.getCharCount() < 1) {
					txtPassAgain.setBackground(RED);
					return;
				}
				if (Arrays.equals(txtPass.getTextChars(), txtPassAgain.getTextChars())) {
					txtPass.setBackground(defBgrdColor);
					txtPassAgain.setBackground(defBgrdColor);
//					passHasChanged = false;
				} else {
					txtPassAgain.setBackground(RED);
				}
				
			}
		});
		txtPassAgain.addKeyListener(TRAVERSE_OR_EXIT);
		txtPassAgain.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				passHasChanged = true;
			}
		});
		txtPassAgain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		defBgrdColor = txtPassAgain.getBackground();
		
		Label lblAdmin = new Label(composite, SWT.NONE);
		lblAdmin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAdmin.setText("Администратор");
		
		btnAdmin = new Button(composite, SWT.CHECK);
		btnAdmin.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!btnAdmin.getSelection()) {
					if (usersWindow.noMoreAdmins(user.id)) {
						btnAdmin.setBackground(RED);
						return;
					}
					user.setAdmin(false);
				} else {
					user.setAdmin(true);
				}
				btnAdmin.setBackground(defBgrdColor);
			}
		});
		btnAdmin.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (SWT.CR == e.character) {
					submit();
//				} else if (e.character == SWT.ESC) {
//					((Control) e.widget).getShell().close();
				}
			}
		});
		btnAdmin.setSelection(user.isAdmin());
		
		FormData fd_btnOk = new FormData();
		fd_btnOk.top = new FormAttachment(composite, 6);
		btnOk.setLayoutData(fd_btnOk);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		fd_btnOk.right = new FormAttachment(btnCancel, -37);
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.left = new FormAttachment(0, 202);
		fd_btnCancel.top = new FormAttachment(composite, 6);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Отмена");

	}

	private boolean useLogin() {
		String login = txtLogin.getText();
		if (usersWindow.loginIsBusy(login, user.id)) return false;
		return user.setLogin(login);
	}

	private void submit() {
		if (!loginIsValid) return;
		if (passHasChanged) {
			if (txtPass.getCharCount() < 1) return;
			try {
				user.setPassword(txtPass.getTextChars());
				//			} catch (Exception e) {
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				//				System.err.println("Password: " + txtPass.getText());
				e.printStackTrace();
			}
		}
		if (!btnAdmin.getSelection()) {
			if (usersWindow.noMoreAdmins(user.id)) {
				btnAdmin.setBackground(RED);
				return;
			}
		} 
//		btnAdmin.setBackground(defBgrdColor);
		connection.outQueue.add(user);
		shell.close();
	}
}
