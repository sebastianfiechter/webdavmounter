package ch.sebastianfiechter.webdavmounter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.media.Log;

import groovy.util.logging.*

@Slf4j
@Component
class MounterSystemTray {
	
	@Autowired
	WebdavMounter webdavMounter;
	
	private TrayIcon trayIcon;
	private SystemTray tray;
	
	private MenuItem connectItem;
	private MenuItem settingsItem;
	private MenuItem aboutItem;
	private MenuItem exitItem;
	
	boolean guiLoaded;
	
	void showGui() {
		guiLoaded = false;
		
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });		
	}
    
    private void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported");
            return;
        }
       
        trayIcon = new TrayIcon(createImage("images/drive_network.png", "tray icon"));
        tray = SystemTray.getSystemTray();
        
        // Create a popup menu components
        
        connectItem = new MenuItem("Verbinden");
        settingsItem = new MenuItem("Einstellungen");
        aboutItem = new MenuItem("Info");
        exitItem = new MenuItem("Beenden");
        
        //Add components to popup menu
        final PopupMenu popup = new PopupMenu();  
        popup.add(connectItem);
        popup.addSeparator();
        popup.add(settingsItem);
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);
 
        trayIcon.setPopupMenu(popup);
        
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
        	log.error("TrayIcon could not be added.", e);
            return;
        }
        
        connectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                webdavMounter.manualConnect();
            }
        });
        
        settingsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                webdavMounter.settings();
            }
        });        
        
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                webdavMounter.about();
            }
        });
        
        
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                webdavMounter.exit();
            }
        });
        
        guiLoaded = true;
    }
    
    void showTrayIcon() {
        trayIcon.setImage(createImage("images/drive_network.png", "tray icon"));
    }
 
    void showTrayIconGreen() {
    	trayIcon.setImage(createImage("images/drive_network_green.png", "tray icon green"));
    }   
    
    void showTrayIconRed() {
    	trayIcon.setImage(createImage("images/drive_network_red.png", "tray icon red"));
    }   
    
    //Obtain the image URL
    protected static Image createImage(String path, String description) {
    	
    	BufferedImage img;
		try {
			img = ImageIO.read(MounterSystemTray.class.getClassLoader().getResource(path));
		} catch (IOException e) {
			log.error("Resource not found: " + path, e);       
			Log.error(e);
			return null;
		}
    	
        
        if (img == null) {
            log.error("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(img, description)).getImage();
        }
    }
    
    public void disableAll() {
        connectItem?.setEnabled(false);
        settingsItem?.setEnabled(false);
        aboutItem?.setEnabled(false);
        exitItem?.setEnabled(false);	
    }
    
    public void enableAll() {
        connectItem?.setEnabled(true);
        settingsItem?.setEnabled(true);
        aboutItem?.setEnabled(true);
        exitItem?.setEnabled(true);	    	
    }
    
    public void showInfo(def message) {
    	trayIcon.displayMessage("WebdavMounter",message, TrayIcon.MessageType.INFO);
    }
    
    public void showError(def message) {
    	trayIcon.displayMessage("WebdavMounter",message, TrayIcon.MessageType.ERROR);
    }
    
    public void hide() {
    	if (tray != null) {
    		tray.remove(tray.getTrayIcons()[0]);
    	}
    	
    }
}