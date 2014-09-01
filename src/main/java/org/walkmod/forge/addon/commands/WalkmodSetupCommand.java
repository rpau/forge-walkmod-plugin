package org.walkmod.forge.addon.commands;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginImpl;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

public class WalkmodSetupCommand extends AbstractProjectCommand {

	@Inject
	private ProjectFactory projectFactory;

	@Inject
	@WithAttributes(label = "embedded", type = InputType.CHECKBOX, defaultValue = "true", description = "Adds the maven-walkmod-plugin into the pom.xml to run walkmod in an embedded mode")
	private UIInput<Boolean> embedded;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(WalkmodSetupCommand.class).name(
				"walkmod-setup");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		builder.add(embedded);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		Project project = getSelectedProject(context);
		ResourcesFacet resourcesFacet = project.getFacet(ResourcesFacet.class);
		FileResource<?> fileResource = resourcesFacet
				.getResource("walkmod.xml");

		if (!fileResource.exists()) {
			URL template = this.getClass().getClassLoader()
					.getResource("/template"+File.separator+"walkmod.xml");

			fileResource.createFrom(new File(template.toURI()));

			if (project.hasFacet(MavenPluginFacet.class) && embedded.getValue()) {

				MavenPluginFacet facet = project
						.getFacet(MavenPluginFacet.class);
				MavenPluginImpl mp = new MavenPluginImpl();

				CoordinateBuilder coordinate = CoordinateBuilder.create()
						.setGroupId("org.walkmod")
						.setArtifactId("maven-walkmod-plugin")
						.setVersion("1.0");
				mp.setCoordinate(coordinate);
				mp.addExecution(ExecutionBuilder.create()
						.setPhase("generate-sources").addGoal("apply"));
				facet.addPlugin(mp);
			}
		}
		return Results
				.success("Command 'walkmod-setup' successfully executed!");
	}

	@Override
	protected ProjectFactory getProjectFactory() {
		return projectFactory;
	}

	@Override
	protected boolean isProjectRequired() {
		return true;
	}
}