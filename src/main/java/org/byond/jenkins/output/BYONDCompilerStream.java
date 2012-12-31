package org.byond.jenkins.output;

import hudson.console.LineTransformationOutputStream;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;

public class BYONDCompilerStream extends LineTransformationOutputStream {
	private final BuildListener listener;
	private final Charset charset;
	private boolean foundErrors = false;
	private boolean foundWarnings = false;
	
	public BYONDCompilerStream(BuildListener listener, AbstractBuild<?,?> build) {
		this.listener = listener;
		charset = build.getCharset();
	}
	
	/* (non-Javadoc)
	 * @see hudson.console.LineTransformationOutputStream#eol(byte[], int)
	 */
	@Override
	protected void eol(byte[] b, int len) throws IOException {
		String line = charset.decode(ByteBuffer.wrap(b, 0, len)).toString();
        // trim off CR/LF from the end
        line = trimEOL(line);
        
        Matcher matcher = BYONDCompileErrorNote.PATTERN.matcher(line);
        if (matcher.matches()) {
        	new BYONDCompileErrorNote().encodeTo(listener.getLogger());
        	foundErrors = true;
        }
        matcher = BYONDCompileWarningNote.PATTERN.matcher(line);
        if (matcher.matches()) {
        	new BYONDCompileWarningNote().encodeTo(listener.getLogger());
        	setFoundWarnings(true);
        }
        
        listener.getLogger().write(b,0,len);
	}

	public boolean hasFoundErrors() {
		return foundErrors;
	}

	public void setFoundErrors(boolean foundErrors) {
		this.foundErrors = foundErrors;
	}

	public boolean hasFoundWarnings() {
		return foundWarnings;
	}

	public void setFoundWarnings(boolean foundWarnings) {
		this.foundWarnings = foundWarnings;
	}
}
