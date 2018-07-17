
package com.orange.util;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.csm.ConnectionForCSI;

public class ConnectionWindowForCSI extends CustomJFrame {

	
	private static final long serialVersionUID = 1L;
	JComboBox<String> conList;
	JLabel conListLabel;
	JLabel passwordLabel;
	JLabel portLabel;
	JLabel queryprefixLabel;
	JButton setnExit;
	JLabel sidLabel;
	JLabel urlLabel;
	JLabel userLabel;
	
	/*
	private JPasswordField password;
	private JTextField port;
	private JTextField queryprefix;
	private JTextField sid;
	private JTextField url;
	private JTextField user;
	*/
	
	CustomJCheckBox validateFromDataBaseForCSI;
	
	private Properties properties = CommonUtils.getCsiDBProperties();
	
	public ConnectionWindowForCSI() {
		setLayout(null);
		setIconImage(CommonUtils.setSizeImage(Icons.database, 35, 35));
		conListLabel = new JLabel("DataBase");
		conListLabel.setIcon(CommonUtils.getIcon(Icons.database));
		conList = new JComboBox<String>();
		
		
		
		if(null != properties)
		{
			for (Object o : properties.keySet()) 
			{
				conList.addItem((String) o);
		    }
		}
		//conList.setSelectedIndex(0);
		conListLabel.setBounds(5, 10, 100, 20);
		conList.setBounds(105, 10, 250, 25);
		
		urlLabel = new JLabel("URL");
		sidLabel = new JLabel("SID");
		userLabel = new JLabel("User");
		passwordLabel = new JLabel("Password");
		queryprefixLabel = new JLabel("Prefix");
		portLabel = new JLabel("Port");

		urlLabel.setBounds(5, 40, 100, 20);
		sidLabel.setBounds(5, 70, 100, 20);
		userLabel.setBounds(5, 100, 100, 20);
		passwordLabel.setBounds(5, 130, 100, 20);
		queryprefixLabel.setBounds(5, 160, 100, 20);
		portLabel.setBounds(5, 190, 100, 20);

		/*
		url = new JTextField(ConnectionBean.getUrl());
		sid = new JTextField(ConnectionBean.getSid());
		user = new JTextField(ConnectionBean.getUserName());
		password = new JPasswordField(ConnectionBean.getPassword());
		queryprefix = new JTextField(ConnectionBean.getDbPrefix());
		port = new JTextField(ConnectionBean.getPort());
		 */
		JLabel urlLabel1 = new JLabel("Auto Submit");
		JLabel sidLabel1 = new JLabel("Auto Submit");
		JLabel userLabel1 = new JLabel("Auto Submit");
		JLabel passwordLabel1 = new JLabel("Auto Submit");
		JLabel queryprefixLabel1 = new JLabel("Auto Submit");
		JLabel portLabel1 = new JLabel("Auto Submit");
		
		urlLabel1.setBounds(105, 40, 250, 25);
		sidLabel1.setBounds(105, 70, 250, 25);
		userLabel1.setBounds(105, 100, 250, 25);
		passwordLabel1.setBounds(105, 130, 250, 25);
		queryprefixLabel1.setBounds(105, 160, 250, 25);
		portLabel1.setBounds(105, 190, 250, 25);

		urlLabel1.setForeground(Color.blue);
		sidLabel1.setForeground(Color.blue);
		userLabel1.setForeground(Color.blue);
		urlLabel1.setForeground(Color.blue);
		queryprefixLabel1.setForeground(Color.blue);
		portLabel1.setForeground(Color.blue);
		passwordLabel1.setForeground(Color.blue);
		
		add(urlLabel1);
		add(sidLabel1);
		add(userLabel1);
		add(passwordLabel1);
		add(queryprefixLabel1);
		add(portLabel1);

				
		setnExit = new JButton("Connection");
		setnExit.setBounds(105, 230, 150, 30);

		add(urlLabel);
		add(sidLabel);
		add(userLabel);
		add(passwordLabel);
		add(queryprefixLabel);
		add(portLabel);

		add(setnExit);
		
		add(conListLabel);
		add(conList);
		
		setnExit.addActionListener(new ExitAction());
	}

	class ExitAction implements ActionListener {

		
		public void actionPerformed(ActionEvent e) {
			
			try {
				if(null == conList.getSelectedItem()){
					return;
				}
				
				String[] values = properties.get(conList.getSelectedItem().toString()).toString().split(PublicKeyGenerator.encrypt(PublicPrivateKeyGenerator.seperator));
				
				String userName = PublicKeyGenerator.decrypt(values[0]);
				String password = PublicKeyGenerator.decrypt(values[1]);
				String hostName = PublicKeyGenerator.decrypt(values[2]);
				String port 	= PublicKeyGenerator.decrypt(values[3]);
				String sid 		= PublicKeyGenerator.decrypt(values[4]);
				String prefix 	= PublicKeyGenerator.decrypt(values[5]);
				
				ConnectionBeanCSI.setDbName(conList.getSelectedItem().toString());
				ConnectionBeanCSI.setUserName(userName);
				ConnectionBeanCSI.setSid(sid);
				ConnectionBeanCSI.setUrl(hostName);
				ConnectionBeanCSI.setDbPrefix(prefix);
				ConnectionBeanCSI.setPort(port);
				ConnectionBeanCSI.setPassword(password);
				
			} catch (Exception E) {CommonUtils.showExceptionStack(E);}
			
			try {
				if (null == ConnectionForCSI.csiconnection) {
					Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
					String url = "jdbc:oracle:thin:@" + ConnectionBeanCSI.getUrl()+ ":"+ConnectionBeanCSI.getPort()+":" + ConnectionBeanCSI.getSid();
					ConnectionForCSI.csiconnection = DriverManager.getConnection(url, ConnectionBeanCSI.getUserName(),ConnectionBeanCSI.getPassword());

				}
				setVisible(false);

			} catch (Exception e1) {
				if(null != validateFromDataBaseForCSI){
				validateFromDataBaseForCSI.setSelected(false);
				}
				JOptionPane.showMessageDialog(new Frame(), e1.getMessage());
				setVisible(false);
			}
		}
	}

	public void setCheckBox(CustomJCheckBox validateFromDataBaseForCSI) {
		this.validateFromDataBaseForCSI = validateFromDataBaseForCSI;
	}

}