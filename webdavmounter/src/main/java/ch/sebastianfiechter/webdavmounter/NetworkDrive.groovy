package ch.sebastianfiechter.webdavmounter

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.media.Log;

import groovy.util.logging.*

@Slf4j
@Component
class NetworkDrive {

	@Autowired
	MounterPreferences preferences;
	
	def driveLetter;
	def alreadyConnected = false;
		
	public boolean hasConnection() {
		log.info("check connection to: ${preferences.host}");
		
		try {
			String html = preferences.host.toURL().text
		} catch (java.net.UnknownHostException ex) {
			log.info("not connected: host not connected.", ex);
		} catch (java.io.FileNotFoundException ex) {
			log.info("not connected: path incorrect.", ex);
		} catch (java.io.IOException ex) {
			log.info(ex.getMessage());
			if (ex.getMessage().contains("401")) {
				log.info("ok, connection established to ${preferences.host}");
				return true;
			}
		}

	    return false;
	}
	
	void connect() {
		
		gatherDriveLetter();
	
		log.info("Drive Letter gathered: " + driveLetter);
		
		def processExec = "net use ${driveLetter}: \"${preferences.host}\" \"${preferences.password}\" /User:\"${preferences.username}\" /persistent:no";
		
		log.info("net use command: net use ${driveLetter}: \"${preferences.host}\" \"***\" /User:\"${preferences.username}\" /persistent:no");
		
		// Run a java app in a separate system process
		Process proc = Runtime.getRuntime().exec(processExec);

		// Then retrieve the process output
		ProcessInputStreamReader.copy(proc.getInputStream(), System.out);
		ProcessInputStreamReader.copy(proc.getErrorStream(), System.err);

				
		proc.waitFor();
		
	}
	
	private void gatherDriveLetter() {
		
		//default
		driveLetter = preferences.driveLetter.toUpperCase();
		
		def processExec = "net use";
		
		// Run a java app in a separate system process
		Process proc = Runtime.getRuntime().exec(processExec);
		// Then retrieve the process output
		ProcessInputStreamReader.copy(proc.getInputStream(), new DriveLetterParser());
	
		proc.waitFor();

	}
	
	private boolean checkConnected() {
		
		alreadyConnected = false;
		
		//prepare host
		def host = preferences.host.replace("https://", "").replace("http://", "")
		def hostTokens = host.tokenize("/");
		
		log.info("checkAlreadyConnected with hostTokens: ${hostTokens}");
		
		def processExec = "net use";
		
		// Run a java app in a separate system process
		Process proc = Runtime.getRuntime().exec(processExec);
		// Then retrieve the process output
		ProcessInputStreamReader.copy(proc.getInputStream(), new AlreadyConnectedParser(hostTokens));
	
		proc.waitFor();
		
		log.info("checkAlreadyConnected ${alreadyConnected}")
		
		return alreadyConnected;

	}
	
	class AlreadyConnectedParser extends ProcessInputStreamReader.LineReceiver {

		def hostTokens
		
		public AlreadyConnectedParser(def hostTokens) {
			this.hostTokens = hostTokens;
		}
		
		@Override
		public void println(String line) {
			log.info ("ACP>" + line );
			if (!alreadyConnected) {
				def ok = true;
				hostTokens.each {
					if (!line.contains(it)) {
						ok = false;
					}
				}
				if (ok) alreadyConnected = true;
			}
		}
	}	
	
	class DriveLetterParser extends ProcessInputStreamReader.LineReceiver {

		@Override
		public void println(String line) {
			log.info ("DLP>" + line );
			if (line.contains(preferences.driveLetter.toUpperCase()+":")) {
				driveLetter = "*";
			}
			
		}
	}
	
}
