package ch.sebastianfiechter.webdavmounter;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired;

import groovy.util.logging.*

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

import ch.sebastianfiechter.webdavmounter.ProcessInputStreamReader;
import com.jcabi.manifests.Manifests

import java.util.timer.* 


@Slf4j
@Component
class WebdavMounter {
	
	static final int AUTOCONNECT_PERIOD_MS = 60*1000;
	
	enum LAST_STATUS {STARTUP, NO_CONNECTION, CONNECTED, ERROR}
	
	@Autowired
	MounterPreferences preferences;
	
	@Autowired
	NetworkDrive networkDrive;
	
	@Autowired
	MounterSystemTray systemTray;
	
	TimerTask timerTaskAutoConnect;
	def lastStatus;
	
	static main(args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");

		WebdavMounter mounter = applicationContext.getBean(WebdavMounter.class)
		
		mounter.init();
	}
	
	public init() {
		
		lastStatus = LAST_STATUS.STARTUP
				
		systemTray.showGui();
		
		//wait for GUI
		while (!systemTray.guiLoaded) Thread.sleep(500);
		
		if (!preferences.arePreferencesSet()) {
			settings();
		} else {
			connect();
		}
	}


	
	class TimerTaskAutoConnect extends TimerTask {  
        public void run() {  
        	connect();   
        }  
	} 
	

	//callbacks from GUI
	void about() {
		JOptionPane.showMessageDialog(null,
                "WebdavMounter von Sebastian Fiechter. Version "+getVersion());
	}
	
	
	void manualConnect() {
		lastStatus = LAST_STATUS.STARTUP
		connect();
	}
	
	
	void connect() {
		systemTray.disableAll()
		timerTaskAutoConnect?.cancel()
		
		try {
			if (networkDrive.checkConnected()) {
				log.info("already connected");
				if (lastStatus != LAST_STATUS.CONNECTED) {
					systemTray.showInfo("Verbindung bereits vorhanden.");
					systemTray.showTrayIconGreen();
				}
				lastStatus = LAST_STATUS.CONNECTED
			} else {
				if (!networkDrive.hasConnection()) {
					if (lastStatus != LAST_STATUS.NO_CONNECTION) {
						log.info("no connection at all")
						systemTray.showInfo("Keine Verbindung zur Ablage.");
						systemTray.showTrayIcon();	
					}
					this.lastStatus = LAST_STATUS.NO_CONNECTION
				} else {
					networkDrive.connect();
					if (networkDrive.checkConnected()) {
						log.info("successfully connected")
						if (lastStatus != LAST_STATUS.CONNECTED) {
							systemTray.showInfo("Verbindung hergestellt.");
							systemTray.showTrayIconGreen();
						}
						lastStatus = LAST_STATUS.CONNECTED
					} else {
						log.error ("username and/or password incorrect");
						if (lastStatus != LAST_STATUS.ERROR) {
							systemTray.showError("Uups, da stimmen wohl deine Angaben nicht (E-Mail und/oder Passwort).");
							systemTray.showTrayIconRed();		
						}
						this.lastStatus = LAST_STATUS.ERROR
					}
				}
			}
		} catch (Exception exp) {
			log.error("cannot connect due unexcpected exceptions. stop autoconnect.", exp);
			if (lastStatus != LAST_STATUS.ERROR) {
				systemTray.showError("Uups, es ist ein Fehler aufgetreten. Bitte kontaktiere deinen IT-Support.");
				systemTray.showTrayIconRed();
			}
			this.lastStatus = LAST_STATUS.ERROR
		} 

		//start timerTaskAutoConnect
		Timer timer = new Timer()  
		timerTaskAutoConnect = new TimerTaskAutoConnect();		
		timer.schedule(timerTaskAutoConnect, AUTOCONNECT_PERIOD_MS);
		
		systemTray.enableAll();
	}
	
	void settings() {
		timerTaskAutoConnect?.cancel();
		systemTray.disableAll()
		preferences.fetchAndStorePrerences();
		connect();
	}
	
	String getVersion() {
		return Manifests.exists("App-Version")==true ? Manifests.read("App-Version"): "";
	}
	
	void exit() {
		timerTaskAutoConnect?.cancel();
		systemTray.hide();
        System.exit(0);
	}
}
