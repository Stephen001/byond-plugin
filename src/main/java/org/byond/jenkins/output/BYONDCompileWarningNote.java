package org.byond.jenkins.output;

import java.util.regex.Pattern;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

import org.byond.jenkins.BYONDBuild;

public class BYONDCompileWarningNote extends ConsoleNote<BYONDBuild> {
	private static final long serialVersionUID = -67843344788704147L;

	@Override
	public ConsoleAnnotator<?> annotate(BYONDBuild context, MarkupText text, int charPos) {
		text.addMarkup(0, text.length(), "<span class='byond-warning'>", "</span>");
		return null;
	}

	@Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "BYOND Compile Warnings";
        }
    }
	
	public static final Pattern PATTERN = Pattern.compile("^.+?:[0-9]+:warning:.*$");
}
