package org.byond.jenkins;

import hudson.model.Build;

import java.io.File;
import java.io.IOException;

public class BYONDBuild extends Build<BYONDProject, BYONDBuild> {

	public BYONDBuild(BYONDProject project) throws IOException {
		super(project);
	}

	public BYONDBuild(BYONDProject project, File directory) throws IOException {
		super(project, directory);
	}
}
