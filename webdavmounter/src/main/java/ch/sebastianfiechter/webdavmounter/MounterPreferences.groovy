package ch.sebastianfiechter.webdavmounter

import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired;

import groovy.util.logging.*
import javax.annotation.PostConstruct;

import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.prefs.Preferences;

import javax.swing.*;

@Slf4j
@Component
class MounterPreferences {

	private Preferences prefs;
	
	def host
	def username
	def password
	def driveLetter;
	
	
	
	@PostConstruct
	void loadPreferences() {
		
		prefs = Preferences.userRoot().node("ch.sebastianfiechter.webdav");
		
		this.host = prefs.get("host", "");
		this.username = prefs.get("username", "");
		this.password = prefs.get("password", "");
		this.driveLetter = prefs.get("driveLetter", "F");
		
	}
	
	boolean arePreferencesSet() {
		if (this.host.equals("")) {
			return false;
		}
		
		return true;
	}
	
	void fetchAndStorePrerences() {
		
		 JTextField hostField = new JTextField(this.host, 30);
	     JTextField usernameField = new JTextField(this.username, 30);
	     JPasswordField passwordField = new JPasswordField(this.password, 30);
	     JTextField driveLetterField = new JTextField(this.driveLetter, 30);

	     JPanel myPanel = new JPanel(new BorderLayout(5,5));
	     
	     JPanel labels = new JPanel(new GridLayout(4,1,2,2));
	     labels.add(new JLabel("URL:", SwingConstants.RIGHT));
	     labels.add(new JLabel("E-Mail:", SwingConstants.RIGHT));
	     labels.add(new JLabel("Passwort:", SwingConstants.RIGHT));
	     labels.add(new JLabel("Laufwerk-Buchstabe:", SwingConstants.RIGHT));
	     
	     myPanel.add(labels, BorderLayout.WEST);
	     
	     JPanel controls = new JPanel(new GridLayout(4,1,2,2));
	     controls.add(hostField);
	     controls.add(usernameField);
	     controls.add(passwordField);
	     controls.add(driveLetterField);
	      
	      myPanel.add(controls, BorderLayout.CENTER);

	      int result = JOptionPane.showConfirmDialog(null, myPanel, 
	               "Bitte gib die URL, deine E-Mail-Adresse und das Passwort für den Zugang zur Ablage ein.", JOptionPane.OK_CANCEL_OPTION);
	      if (result == JOptionPane.OK_OPTION) {
	      	this.host = hostField.getText();
	      	this.username = usernameField.getText();
	      	this.password = passwordField.getPassword().toString();
	      	this.driveLetter = driveLetterField.getText();
	      	
			prefs.put("host", this.host);		
			prefs.put("username", this.username);		
			prefs.put("password", this.password);		
			prefs.put("driveLetter", this.driveLetter);		      	
	      }

	     		
	}
	
}
