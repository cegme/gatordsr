package fileProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class FileProcessorOld {

	public static void downloadFile(URL from, File to, boolean overwrite) throws Exception {
	    if (to.exists()) {
	        if (!overwrite)
	            throw new Exception("File " + to.getAbsolutePath() + " exists already.");
	        if (!to.delete())
	            throw new Exception("Cannot delete the file " + to.getAbsolutePath() + ".");
	    }

	    int lengthTotal = 0;
	    try {
	        HttpURLConnection content = (HttpURLConnection) from.openConnection();
	        lengthTotal = content.getContentLength();
	    } catch (Exception e) {
	        lengthTotal = -1;
	    }

	    int lengthSoFar = 0;
	    InputStream is = from.openStream();
	    
	    String[] gpgCommands = new String[] {
	            "gpg",
	            "--passphrase",
	            "password",
	            "--decrypt",
	            "test-files/accounts.txt.gpg"
	    };

	    Process gpgProcess = Runtime.getRuntime().exec(gpgCommands);
	    BufferedReader gpgOutput = new BufferedReader(new InputStreamReader(gpgProcess.getInputStream()));
	    BufferedReader gpgError = new BufferedReader(new InputStreamReader(gpgProcess.getErrorStream()));
	    byte[] bytes = IOUtils.toByteArray(is);
	    
	    
	    is.close();
	    
	}
}
