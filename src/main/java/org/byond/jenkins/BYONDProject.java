package org.byond.jenkins;
import hudson.Extension;
import hudson.Launcher;
import hudson.console.ExpandableDetailsNote.DescriptorImpl;
import hudson.model.BuildListener;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.Project;
import hudson.tasks.Builder;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */

public class BYONDProject extends Project<BYONDProject, BYONDBuild> implements TopLevelItem {
	private static final BYONDProjectDescriptorImpl descriptor = new BYONDProjectDescriptorImpl();
	
	@DataBoundConstructor
	public BYONDProject(ItemGroup<?> parent, String name) {
		super(parent, name);
	}

	@Override
	protected Class<BYONDBuild> getBuildClass() {
		return BYONDBuild.class;
	}
	
	/* (non-Javadoc)
	 * @see hudson.model.AbstractItem#getParent()
	 */
	@Override
	public ItemGroup<?> getParent() {
		return super.getParent();
	}

	@Extension
	public static class BYONDProjectDescriptorImpl extends AbstractProjectDescriptor {
		public BYONDProjectDescriptorImpl() {
			super();
			load();
		}
		
		@Override
		public String getDisplayName() {
			return "BYOND Project";
		}

		@Override
		public TopLevelItem newInstance(@SuppressWarnings("rawtypes") ItemGroup parent, String name) {
			return new BYONDProject(parent, name);
		}
	}

	/* (non-Javadoc)
	 * @see hudson.model.TopLevelItem#getDescriptor()
	 */
	public TopLevelItemDescriptor getDescriptor() {
		return descriptor;
	}
}

