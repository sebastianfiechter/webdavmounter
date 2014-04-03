package ch.sebastianfiechter.webdavmounter;

import groovy.util.logging.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.springframework.stereotype.Component;

class ProcessInputStreamReader {

	private static final int BUFFER_SIZE = 8192;
	
	static void copy(InputStream is, def lr) {
		BufferedReader inStream = new BufferedReader(new InputStreamReader(is));
		String line = null

		while (null != (line = inStream.readLine())) {
			lr.println(line);
		}

	}
	
	interface LineReceiver {

		void println(String line);
		
	}

}
