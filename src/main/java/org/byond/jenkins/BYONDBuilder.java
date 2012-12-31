package org.byond.jenkins;

import hudson.AbortException;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.EnvironmentSpecific;
import hudson.model.Messages;
import hudson.model.Result;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;

import org.byond.jenkins.output.BYONDCompilerStream;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class BYONDBuilder extends Builder {
	private final String tool;
	private final String dme;
	
	@DataBoundConstructor
	public BYONDBuilder(String tool, String dme) {
		super();
		this.tool = tool;
		this.dme  = dme;
	}
	
	public String getTool() {
		return tool;
	}

	public String getDme() {
		return dme;
	}

	/* (non-Javadoc)
	 * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
	 */
	@Override
	public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		Result result = Result.SUCCESS;
		BYONDBuilderDescriptorImpl impl = Jenkins.getInstance().getDescriptorByType(BYONDBuilderDescriptorImpl.class);
		BYONDTool t = null;
		for (BYONDTool storedTool : impl.getTools()) {
			if (storedTool.getName().equals(tool)) {
				t = storedTool;
				break;
			}
		}
		if (t == null) {
			return false;
		}
		Map<String, String> environment = new HashMap<String, String>();
		File libPath = t.getLibraryLocation();
		if (libPath != null) {
			environment.put("LD_LIBRARY_PATH", libPath.getAbsolutePath());
		}
		BYONDCompilerStream stream = new BYONDCompilerStream(listener, build);
		int i = launcher.launch().cmds(t.getCompiler(), dme).envs(environment).stdout(stream).pwd(build.getWorkspace()).join();
		if (i != 0) {
			throw new AbortException("Could not successfully run the BYOND Compiler.");
		}
		if (stream.hasFoundWarnings()) {
			result = Result.UNSTABLE;
		}
		if (stream.hasFoundErrors()) {
			result = Result.FAILURE;
		}
		listener.finished(result);
 		return Result.FAILURE != result;
	}
	
	@Extension
	public static class BYONDBuilderDescriptorImpl extends BuildStepDescriptor<Builder> {
		@CopyOnWrite
		private volatile BYONDTool[] tools = new BYONDTool[0];
		
		public BYONDBuilderDescriptorImpl() {
			super();
			load();
		}
		
		@Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			return BYONDProject.class.isAssignableFrom(jobType);
		}

		@Override
		public String getDisplayName() {
			return "BYOND Builder";
		}
		
		public BYONDTool[] getTools() {
			return tools;
		}
		
		public void setTools(BYONDTool[] tools) {
			if (tools != null) {
				this.tools = Arrays.copyOf(tools, tools.length);
			} else {
				this.tools = new BYONDTool[0];
			}
			save();
		}
		
		public FormValidation doCheckTool(String tool) {
			for (BYONDTool t : getTools()) {
				if (t.getName().equals(tool)) {
					return FormValidation.ok();
				}
			}
			return FormValidation.error("Could not find a valid BYOND installation to use.");
		}
		
		public FormValidation doCheckDME(String dme) {
			return dme.endsWith(".dme") ? FormValidation.ok() : FormValidation.error("The DME filename must end in .dme");
		}
	}
	
	public static class BYONDTool extends ToolInstallation implements NodeSpecific<BYONDTool>, EnvironmentSpecific<BYONDTool> {
		private static final long serialVersionUID = -6046857647482689362L;

		public BYONDTool(String name, String home) {
			super(name, home, Collections.<ToolProperty<?>>emptyList());
		}
		
		@DataBoundConstructor
		public BYONDTool(String name, String home, List<? extends ToolProperty<?>> properties) {
			super(name, home, properties);
		}
		
		public File getCompiler() throws IllegalStateException {
			File windowsExe = new File(getHome(), "bin/dm.exe");
	        File unixExe = new File(getHome(), "bin/DreamMaker");
	        if (windowsExe.exists() && windowsExe.canExecute())
	        	return windowsExe;
	        if (unixExe.exists() && unixExe.canExecute())
	        	return unixExe;
	        throw new IllegalStateException("Cannot find executable BYOND compiler.");
		}
		
		public File getLibraryLocation() throws IllegalStateException {
			File windowsExe = new File(getHome(), "bin/dm.exe");
	        File unixExe = new File(getHome(), "bin/DreamMaker");
	        if (windowsExe.exists() && windowsExe.canExecute())
	        	return null;
	        if (unixExe.exists() && unixExe.canExecute())
	        	return new File(getHome(), "lib");
			throw new IllegalStateException("Cannot find executable BYOND compiler.");
		}

		/* (non-Javadoc)
		 * @see hudson.slaves.NodeSpecific#forNode(hudson.model.Node, hudson.model.TaskListener)
		 */
		public BYONDTool forNode(Node arg0, TaskListener arg1) throws IOException, InterruptedException {
			return new BYONDTool(getName(), translateFor(arg0, arg1));
		}

		/* (non-Javadoc)
		 * @see hudson.model.EnvironmentSpecific#forEnvironment(hudson.EnvVars)
		 */
		public BYONDTool forEnvironment(EnvVars arg0) {
			return new BYONDTool(getName(), arg0.expand(getHome()));
		}
		
		@Extension
		public static class BYONDToolDescriptorImpl extends ToolDescriptor<BYONDTool> {
			
			/* (non-Javadoc)
			 * @see hudson.model.Descriptor#getDisplayName()
			 */
			@Override
			public String getDisplayName() {
				return "BYOND Installation";
			}
			
			/* (non-Javadoc)
			 * @see hudson.tools.ToolDescriptor#getInstallations()
			 */
			@Override
			public BYONDTool[] getInstallations() {
				return Jenkins.getInstance().getDescriptorByType(BYONDBuilderDescriptorImpl.class).getTools();
			}
			
			/* (non-Javadoc)
			 * @see hudson.tools.ToolDescriptor#setInstallations(T[])
			 */
			@Override
			public void setInstallations(BYONDTool ... installations) {
				Jenkins.getInstance().getDescriptorByType(BYONDBuilderDescriptorImpl.class).setTools(installations);
			}
			
			public FormValidation doCheckName(@QueryParameter String value) {
	            return FormValidation.validateRequired(value);
	        }
			
			
			public FormValidation doCheckHome(@QueryParameter File value) {
				Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

	            if(value.getPath().equals(""))
	                return FormValidation.ok();

	            if(!value.isDirectory())
	                return FormValidation.error(Messages.Hudson_NotADirectory(value));
	            
	            File windowsExe = new File(value, "bin/dm.exe");
	            File unixExe = new File(value, "bin/DreamMaker");
	            if (!(windowsExe.exists() && windowsExe.canExecute()) && !(unixExe.exists() && unixExe.canExecute()))
	            	return FormValidation.error("BYOND installation does not appear to be valid. Cannot find bin\\dm.exe or bin/DreamMaker.");
	            
	            return FormValidation.ok();
			}
		}
	}
}
