package org.byond.jenkins.output;

import java.util.regex.Pattern;

import org.byond.jenkins.BYONDBuild;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

public class BYONDCompileErrorNote extends ConsoleNote<BYONDBuild> {
	private static final long serialVersionUID = 4733201200264129840L;
	
	@Override
	public ConsoleAnnotator<?> annotate(BYONDBuild context, MarkupText text, int charPos) {
		text.addMarkup(0, text.length(), "<span class='byond-error'>", "</span>");
		return null;
	}

	@Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "BYOND Compile Errors";
        }
    }
	
	public static final Pattern PATTERN = Pattern.compile("^.+?:[0-9]+:error:.*$");
}
